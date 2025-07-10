package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.supply.MedicalSupplyRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.supply.MedicalSupplyResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.supply.MedicalSupplyStockAdjustmentDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.supply.MedicalSupplyUpdateDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.supply.SupplyTransactionResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.HealthIncident;
import com.fu.swp391.schoolhealthmanagementsystem.entity.MedicalSupply;
import com.fu.swp391.schoolhealthmanagementsystem.entity.SupplyTransaction;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicalSupplyStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SupplyTransactionType;
import com.fu.swp391.schoolhealthmanagementsystem.exception.ResourceNotFoundException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.MedicalSupplyMapper;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.SupplyTransactionMapper;
import com.fu.swp391.schoolhealthmanagementsystem.repository.MedicalSupplyRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.SupplyTransactionRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.specification.MedicalSupplySpecification;
import com.fu.swp391.schoolhealthmanagementsystem.repository.specification.SupplyTransactionSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalSupplyService {

    private final MedicalSupplyRepository medicalSupplyRepository;
    private final SupplyTransactionRepository supplyTransactionRepository;
    private final MedicalSupplyMapper medicalSupplyMapper;
    private final SupplyTransactionMapper supplyTransactionMapper;
    private final AuthorizationService authorizationService;
    private final MedicalSupplySpecification medicalSupplySpecification;
    private final SupplyTransactionSpecification supplyTransactionSpecification;

    @Transactional
    public MedicalSupplyResponseDto createMedicalSupply(MedicalSupplyRequestDto requestDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
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
    public Page<SupplyTransactionResponseDto> getTransactionsForSupply(Long supplyId, SupplyTransactionType transactionType, Pageable pageable) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("Người dùng {} đang lấy lịch sử giao dịch cho vật tư y tế ID: {}", currentUser.getEmail(), supplyId);

        if (!medicalSupplyRepository.existsById(supplyId)) {
            throw new ResourceNotFoundException("Không tìm thấy vật tư y tế với ID: " + supplyId);
        }

        Specification<SupplyTransaction> spec = Specification.allOf(supplyTransactionSpecification.forSupply(supplyId))
                .and(supplyTransactionSpecification.hasType(transactionType));

        Page<SupplyTransaction> transactions = supplyTransactionRepository.findAll(spec, pageable);

        return transactions.map(supplyTransactionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public MedicalSupplyResponseDto getMedicalSupplyById(Long supplyId) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("Người dùng {} đang lấy thông tin vật tư y tế ID: {}", currentUser.getEmail(), supplyId);

        MedicalSupply medicalSupply = medicalSupplyRepository.findById(supplyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật tư y tế với ID: " + supplyId));

        return medicalSupplyMapper.entityToResponseDto(medicalSupply);
    }

    @Transactional(readOnly = true)
    public Page<MedicalSupplyResponseDto> getAllMedicalSupplies(String name, String category, MedicalSupplyStatus status, Pageable pageable) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("Người dùng {} đang lấy danh sách vật tư y tế với các bộ lọc: name={}, category={}, status={}",
                 currentUser.getEmail(), name, category, status);

        Specification<MedicalSupply> spec = Specification.allOf(medicalSupplySpecification.hasNameContaining(name))
                .and(medicalSupplySpecification.hasCategory(category))
                .and(medicalSupplySpecification.hasStatus(status));

        Page<MedicalSupply> medicalSuppliesPage = medicalSupplyRepository.findAll(spec, pageable);

        return medicalSuppliesPage.map(medicalSupplyMapper::entityToResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<MedicalSupplyResponseDto> getAllMedicalSupplies(String name, String category,
                                                               MedicalSupplyStatus status,
                                                               LocalDate expiredDateFrom,
                                                               LocalDate expiredDateTo,
                                                               Pageable pageable) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("Người dùng {} đang lấy danh sách vật tư y tế với các bộ lọc: name={}, category={}, status={}, expiredDateFrom={}, expiredDateTo={}",
                 currentUser.getEmail(), name, category, status, expiredDateFrom, expiredDateTo);

        Specification<MedicalSupply> spec = Specification.allOf(medicalSupplySpecification.hasNameContaining(name))
                .and(medicalSupplySpecification.hasCategory(category))
                .and(medicalSupplySpecification.hasStatus(status))
                .and(medicalSupplySpecification.hasExpiredDateFrom(expiredDateFrom))
                .and(medicalSupplySpecification.hasExpiredDateTo(expiredDateTo));

        Page<MedicalSupply> medicalSuppliesPage = medicalSupplyRepository.findAll(spec, pageable);

        return medicalSuppliesPage.map(medicalSupplyMapper::entityToResponseDto);
    }

    @Transactional
    public MedicalSupplyResponseDto updateMedicalSupply(Long supplyId, MedicalSupplyUpdateDto updateDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("Người dùng {} đang cập nhật vật tư y tế ID: {}", currentUser.getEmail(), supplyId);

        MedicalSupply existingSupply = medicalSupplyRepository.findById(supplyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật tư y tế với ID: " + supplyId));

        if (existingSupply.getStatus() == MedicalSupplyStatus.DISPOSE) {
            log.warn("Không thể cập nhật vật tư y tế ID: {} vì nó đã được lưu trữ.", supplyId);
            throw new IllegalStateException("Không thể cập nhật vật tư y tế đã được lưu trữ.");
        }

        // Kiểm tra ngày hết hạn phải lớn hơn ngày tạo vật tư ít nhất 30 ngày
        if (updateDto.expiredDate() != null) {
            LocalDate creationDate = existingSupply.getCreatedAt().toLocalDate();
            LocalDate minValidDate = creationDate.plusDays(30);

            if (updateDto.expiredDate().isBefore(minValidDate)) {
                throw new IllegalArgumentException(
                    "Ngày hết hạn phải lớn hơn ngày tạo vật tư (" +
                    creationDate.toString() + ") ít nhất 30 ngày"
                );
            }
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
        log.info("Người dùng {} đang điều chỉnh tồn kho cho vật tư ID: {}. Chi tiết: {}",
                currentUser.getEmail(), supplyId, adjustmentDto);

        MedicalSupply medicalSupply = medicalSupplyRepository.findById(supplyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật tư y tế với ID: " + supplyId));

        if (medicalSupply.getStatus() == MedicalSupplyStatus.DISPOSE) {
            log.warn("Không thể điều chỉnh tồn kho cho vật tư ID: {} vì nó đã được lưu trữ.", supplyId);
            throw new IllegalStateException("Không thể điều chỉnh tồn kho cho vật tư y tế đã được lưu trữ.");
        }

        int quantityChange = adjustmentDto.quantity();
        SupplyTransactionType transactionType = adjustmentDto.transactionType();
        int oldStock = medicalSupply.getCurrentStock();

        if (transactionType == SupplyTransactionType.ADJUSTMENT_OUT) {
            if (oldStock < quantityChange) {
                log.error("Không đủ tồn kho ({} < {}) cho vật tư ID {} để thực hiện giảm/xuất.",
                        oldStock, quantityChange, supplyId);
                throw new IllegalStateException("Không đủ tồn kho để thực hiện. Tồn kho hiện tại: " + oldStock);
            }
            medicalSupply.setCurrentStock(oldStock - quantityChange);

            // Nếu stock giảm xuống 0, cập nhật trạng thái thành OUT_OF_STOCK
            if (medicalSupply.getCurrentStock() == 0) {
                medicalSupply.setStatus(MedicalSupplyStatus.OUT_OF_STOCK);
                log.info("Vật tư ID: {} đã hết hàng, cập nhật trạng thái thành OUT_OF_STOCK", supplyId);
            }
        } else if (transactionType == SupplyTransactionType.ADJUSTMENT_IN) {
            medicalSupply.setCurrentStock(oldStock + quantityChange);

            // Nếu vật tư trước đó hết hàng và giờ có stock, cập nhật trạng thái thành AVAILABLE
            if (oldStock == 0 && medicalSupply.getStatus() == MedicalSupplyStatus.OUT_OF_STOCK) {
                medicalSupply.setStatus(MedicalSupplyStatus.AVAILABLE);
                log.info("Vật tư ID: {} đã có hàng, cập nhật trạng thái thành AVAILABLE", supplyId);
            }
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

    @Transactional
    public void recordSupplyUsageForIncident(MedicalSupply medicalSupply, int quantityUsed, HealthIncident healthIncident, User performedBy) {
        log.info("Ghi nhận sử dụng {} đơn vị vật tư '{}' (ID: {}) cho sự cố ID: {}",
                quantityUsed, medicalSupply.getName(), medicalSupply.getSupplyId(), healthIncident.getIncidentId());

        if (medicalSupply.getStatus() == MedicalSupplyStatus.DISPOSE ||
            medicalSupply.getStatus() == MedicalSupplyStatus.EXPIRED) {
            throw new IllegalStateException("Vật tư y tế '" + medicalSupply.getName() +
                "' không khả dụng (trạng thái: " + medicalSupply.getStatus() + ").");
        }

        if (medicalSupply.getCurrentStock() < quantityUsed) {
            throw new IllegalStateException("Không đủ tồn kho cho vật tư '" + medicalSupply.getName() +
                "'. Yêu cầu: " + quantityUsed + ", Hiện có: " + medicalSupply.getCurrentStock());
        }

        int oldStock = medicalSupply.getCurrentStock();
        medicalSupply.setCurrentStock(oldStock - quantityUsed);

        // Cập nhật trạng thái nếu hết hàng
        if (medicalSupply.getCurrentStock() == 0) {
            medicalSupply.setStatus(MedicalSupplyStatus.OUT_OF_STOCK);
        }

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
    public void disposeMedicalSupply(Long supplyId) { // Soft delete -> chuyển trạng thái thành DISPOSE
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("Người dùng {} đang yêu cầu lưu trữ vật tư y tế ID: {}", currentUser.getEmail(), supplyId);

        MedicalSupply medicalSupply = medicalSupplyRepository.findById(supplyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật tư y tế với ID: " + supplyId));

        if (medicalSupply.getStatus() == MedicalSupplyStatus.DISPOSE) {
            log.warn("Vật tư y tế ID: {} đã ở trạng thái lưu trữ.", supplyId);
            throw new IllegalStateException("Vật tư y tế này đã ở trạng thái lưu trữ.");
        }

        medicalSupply.setStatus(MedicalSupplyStatus.DISPOSE);
        medicalSupply.setUpdatedByUser(currentUser);
        medicalSupplyRepository.save(medicalSupply);
        log.info("Đã lưu trữ vật tư y tế ID: {}", supplyId);
    }

    @Transactional
    public void deleteMedicalSupply(Long supplyId) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("Người dùng {} đang yêu cầu xóa cứng vật tư y tế ID: {}", currentUser.getEmail(), supplyId);

        MedicalSupply medicalSupply = medicalSupplyRepository.findById(supplyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật tư y tế với ID: " + supplyId));



        // Kiểm tra xem có giao dịch nào liên quan đến sự cố y tế không
        boolean hasIncidentRelation = supplyTransactionRepository.existsByMedicalSupplyAndHealthIncidentNotNull(medicalSupply);
        if (hasIncidentRelation) {
            log.warn("Không thể xóa cứng vật tư y tế ID: {} vì nó có giao dịch liên quan đến sự cố y tế.", supplyId);
            throw new IllegalStateException("Không thể xóa cứng vật tư y tế này vì có giao dịch liên quan.");
        }

        String deletedName = medicalSupply.getName();

        // Thực hiện xóa cứng
        medicalSupplyRepository.delete(medicalSupply);
        log.info("Đã xóa vật tư y tế ID: {}, tên: {}", supplyId, deletedName);
    }

    // --- Helper Method for Transactions ---

    private void createAndSaveTransaction(MedicalSupply medicalSupply, int quantity,
                                          SupplyTransactionType type, String note, User performedBy, HealthIncident healthIncident) {
        SupplyTransaction transaction = SupplyTransaction.builder()
                .medicalSupply(medicalSupply)
                .quantity(quantity)
                .supplyTransactionType(type)
                .note(note)
                .performedByUser(performedBy)
                .healthIncident(healthIncident)
                .build();
        supplyTransactionRepository.save(transaction);
    }
}
