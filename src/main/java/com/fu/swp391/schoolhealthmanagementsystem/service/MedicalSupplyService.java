package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.supply.MedicalSupplyRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.supply.MedicalSupplyResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.supply.MedicalSupplyStockAdjustmentDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.supply.MedicalSupplyUpdateDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.supply.SupplyTransactionResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.HealthIncident; // Thêm import nếu chưa có
import com.fu.swp391.schoolhealthmanagementsystem.entity.MedicalSupply;
import com.fu.swp391.schoolhealthmanagementsystem.entity.SupplyTransaction;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SupplyTransactionType;
// import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole; // Không cần nữa nếu không check role ở đây
import com.fu.swp391.schoolhealthmanagementsystem.exception.ResourceNotFoundException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.MedicalSupplyMapper;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.SupplyTransactionMapper;
import com.fu.swp391.schoolhealthmanagementsystem.repository.MedicalSupplyRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.SupplyTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
// import org.springframework.security.access.AccessDeniedException; // Không cần nữa nếu không check role ở đây
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalSupplyService {

    private final MedicalSupplyRepository medicalSupplyRepository;
    private final SupplyTransactionRepository supplyTransactionRepository;
    private final MedicalSupplyMapper medicalSupplyMapper;
    private final SupplyTransactionMapper supplyTransactionMapper;
    private final AuthorizationService authorizationService; // Vẫn cần để lấy currentUser

    @Transactional
    public MedicalSupplyResponseDto createMedicalSupply(MedicalSupplyRequestDto requestDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        // @PreAuthorize("hasAnyRole('NURSE', 'NURSE_MANAGER')") sẽ ở Controller
        log.info("Người dùng {} đang tạo vật tư y tế mới: {}", currentUser.getEmail(), requestDto.name());

        MedicalSupply medicalSupply = medicalSupplyMapper.requestDtoToEntity(requestDto);
        medicalSupply.setCreatedByUser(currentUser);
        medicalSupply.setUpdatedByUser(currentUser);

        MedicalSupply savedSupply = medicalSupplyRepository.save(medicalSupply);

        if (requestDto.initialStock() != null && requestDto.initialStock() > 0) {
            createAndSaveTransaction(
                    savedSupply,
                    requestDto.initialStock(),
                    SupplyTransactionType.RECEIVED,
                    "Nhập kho ban đầu",
                    currentUser,
                    null
            );
        }

        log.info("Đã tạo vật tư y tế ID: {} với tên: {}", savedSupply.getSupplyId(), savedSupply.getName());
        return medicalSupplyMapper.entityToResponseDto(savedSupply);
    }

    @Transactional(readOnly = true)
    public Page<SupplyTransactionResponseDto> getTransactionsForSupply(Long supplyId, Pageable pageable) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("Người dùng {} đang lấy lịch sử giao dịch cho vật tư y tế ID: {}", currentUser.getEmail(), supplyId);

        MedicalSupply medicalSupply = medicalSupplyRepository.findById(supplyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật tư y tế với ID: " + supplyId));

        Page<SupplyTransaction> transactions = supplyTransactionRepository.findByMedicalSupply(medicalSupply, pageable);

        return transactions.map(supplyTransactionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public MedicalSupplyResponseDto getMedicalSupplyById(Long supplyId) {
        User currentUser = authorizationService.getCurrentUserAndValidate(); // Vẫn cần cho logging hoặc nếu có logic khác
        // @PreAuthorize("hasAnyRole('ADMIN', 'NURSE_MANAGER', 'NURSE')") sẽ ở Controller
        // Hoặc Parent có thể xem gián tiếp qua HealthIncident, không phải qua API này trực tiếp

        log.info("Người dùng {} đang lấy thông tin vật tư y tế ID: {}", currentUser.getEmail(), supplyId);
        MedicalSupply medicalSupply = medicalSupplyRepository.findById(supplyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật tư y tế với ID: " + supplyId));

        return medicalSupplyMapper.entityToResponseDto(medicalSupply);
    }

    @Transactional(readOnly = true)
    public Page<MedicalSupplyResponseDto> getAllMedicalSupplies(Pageable pageable, Boolean isActiveFilter) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        // @PreAuthorize("hasAnyRole('ADMIN', 'NURSE_MANAGER', 'NURSE')") sẽ ở Controller

        log.info("Người dùng {} đang lấy danh sách vật tư y tế. isActiveFilter: {}", currentUser.getEmail(), isActiveFilter);
        Page<MedicalSupply> medicalSuppliesPage;
        if (isActiveFilter != null) {
            medicalSuppliesPage = medicalSupplyRepository.findAllByActive(isActiveFilter, pageable);
        } else {
            medicalSuppliesPage = medicalSupplyRepository.findAll(pageable);
        }
        return medicalSuppliesPage.map(medicalSupplyMapper::entityToResponseDto);
    }

    @Transactional
    public MedicalSupplyResponseDto updateMedicalSupply(Long supplyId, MedicalSupplyUpdateDto updateDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        // @PreAuthorize("hasAnyRole('NURSE', 'NURSE_MANAGER')") sẽ ở Controller

        log.info("Người dùng {} đang cập nhật vật tư y tế ID: {}", currentUser.getEmail(), supplyId);
        MedicalSupply existingSupply = medicalSupplyRepository.findById(supplyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật tư y tế với ID: " + supplyId));

        if (!existingSupply.isActive()) {
            log.warn("Không thể cập nhật vật tư y tế ID: {} vì nó không hoạt động.", supplyId);
            throw new IllegalStateException("Không thể cập nhật vật tư y tế không hoạt động.");
        }

        medicalSupplyMapper.updateEntityFromUpdateDto(updateDto, existingSupply);
        existingSupply.setUpdatedByUser(currentUser);

        MedicalSupply updatedSupply = medicalSupplyRepository.save(existingSupply);
        log.info("Đã cập nhật vật tư y tế ID: {}", updatedSupply.getSupplyId());
        return medicalSupplyMapper.entityToResponseDto(updatedSupply);
    }

    @Transactional
    public MedicalSupplyResponseDto adjustMedicalSupplyStock(Long supplyId, MedicalSupplyStockAdjustmentDto adjustmentDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        // @PreAuthorize("hasAnyRole('NURSE', 'NURSE_MANAGER')") sẽ ở Controller

        log.info("Người dùng {} đang điều chỉnh tồn kho cho vật tư ID: {}. Chi tiết: {}",
                currentUser.getEmail(), supplyId, adjustmentDto);

        MedicalSupply medicalSupply = medicalSupplyRepository.findById(supplyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật tư y tế với ID: " + supplyId));

        if (!medicalSupply.isActive()) {
            log.warn("Không thể điều chỉnh tồn kho cho vật tư ID: {} vì nó không hoạt động.", supplyId);
            throw new IllegalStateException("Không thể điều chỉnh tồn kho cho vật tư y tế không hoạt động.");
        }

        int quantityChange = adjustmentDto.quantity();
        SupplyTransactionType transactionType = adjustmentDto.transactionType();
        int oldStock = medicalSupply.getCurrentStock();

        if (transactionType == SupplyTransactionType.ADJUSTMENT_OUT) { // Mở rộng cho cả EXPORT nếu có API riêng
            if (oldStock < quantityChange) {
                log.error("Không đủ tồn kho ({} < {}) cho vật tư ID {} để thực hiện giảm/xuất.",
                        oldStock, quantityChange, supplyId);
                throw new IllegalStateException("Không đủ tồn kho để thực hiện. Tồn kho hiện tại: " + oldStock);
            }
            medicalSupply.setCurrentStock(oldStock - quantityChange);
        } else if (transactionType == SupplyTransactionType.RECEIVED) {
            medicalSupply.setCurrentStock(oldStock + quantityChange);
        } else {
            log.error("Loại giao dịch không hợp lệ ({}) cho việc điều chỉnh tồn kho vật tư ID {}.", transactionType, supplyId);
            throw new IllegalArgumentException("Loại giao dịch không hợp lệ cho việc điều chỉnh tồn kho.");
        }

        medicalSupply.setUpdatedByUser(currentUser);
        MedicalSupply updatedSupply = medicalSupplyRepository.save(medicalSupply);

        createAndSaveTransaction(
                updatedSupply,
                quantityChange,
                transactionType,
                adjustmentDto.note(),
                currentUser,
                null // No incident for manual adjustment
        );

        log.info("Đã điều chỉnh tồn kho cho vật tư ID: {}. Tồn kho {} -> {}. Ghi chú: {}",
                supplyId, oldStock, updatedSupply.getCurrentStock(), adjustmentDto.note());
        return medicalSupplyMapper.entityToResponseDto(updatedSupply);
    }

    // Phương thức này được gọi từ HealthIncidentService khi xuất kho cho sự cố
    // Nó không cần @PreAuthorize ở đây vì HealthIncidentService sẽ có phân quyền riêng
    @Transactional
    public void recordSupplyUsageForIncident(MedicalSupply medicalSupply, int quantityUsed, HealthIncident healthIncident, User performedBy) {
        log.info("Ghi nhận sử dụng {} đơn vị vật tư '{}' (ID: {}) cho sự cố ID: {}",
                quantityUsed, medicalSupply.getName(), medicalSupply.getSupplyId(), healthIncident.getIncidentId());

        if (!medicalSupply.isActive()) {
            throw new IllegalStateException("Vật tư y tế '" + medicalSupply.getName() + "' không hoạt động.");
        }
        if (medicalSupply.getCurrentStock() < quantityUsed) {
            throw new IllegalStateException("Không đủ tồn kho cho vật tư '" + medicalSupply.getName() + "'. Yêu cầu: " + quantityUsed + ", Hiện có: " + medicalSupply.getCurrentStock());
        }

        int oldStock = medicalSupply.getCurrentStock();
        medicalSupply.setCurrentStock(oldStock - quantityUsed);
        medicalSupply.setUpdatedByUser(performedBy);
        medicalSupplyRepository.save(medicalSupply);

        createAndSaveTransaction(
                medicalSupply,
                quantityUsed,
                SupplyTransactionType.USED_FOR_INCIDENT,
                "Sử dụng cho sự cố y tế ID: " + healthIncident.getIncidentId(),
                performedBy,
                healthIncident // Liên kết với HealthIncident
        );
        log.info("Đã cập nhật tồn kho cho vật tư '{}' (ID: {}), tồn kho {} -> {}. Sử dụng cho sự cố ID: {}",
                medicalSupply.getName(), medicalSupply.getSupplyId(), oldStock, medicalSupply.getCurrentStock(), healthIncident.getIncidentId());
    }

    @Transactional
    public void deleteMedicalSupply(Long supplyId) { // Soft delete
        User currentUser = authorizationService.getCurrentUserAndValidate();
        // @PreAuthorize("hasRole('NURSE_MANAGER')") sẽ ở Controller

        log.info("Người dùng {} đang yêu cầu xóa (soft delete) vật tư y tế ID: {}", currentUser.getEmail(), supplyId);
        MedicalSupply medicalSupply = medicalSupplyRepository.findById(supplyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật tư y tế với ID: " + supplyId));

        if (!medicalSupply.isActive()) {
            log.warn("Vật tư y tế ID: {} đã ở trạng thái không hoạt động.", supplyId);
            throw new IllegalStateException("Vật tư y tế này đã ở trạng thái không hoạt động.");
        }

        medicalSupply.setActive(false);
        medicalSupply.setUpdatedByUser(currentUser);
        medicalSupplyRepository.save(medicalSupply);
        log.info("Đã xóa mềm (chuyển sang inactive) vật tư y tế ID: {}", supplyId);
    }

    // --- Helper Method for Transactions ---

    private SupplyTransaction createAndSaveTransaction(MedicalSupply medicalSupply, int quantity,
                                                       SupplyTransactionType type, String note, User performedBy, HealthIncident healthIncident) {
        SupplyTransaction transaction = SupplyTransaction.builder()
                .medicalSupply(medicalSupply)
                .quantity(quantity)
                .supplyTransactionType(type)
                .note(note)
                .performedByUser(performedBy)
                .healthIncident(healthIncident)
                .build();
        return supplyTransactionRepository.save(transaction);
    }
}