package com.fu.swp391.schoolhealthmanagementsystem.init;

import com.fu.swp391.schoolhealthmanagementsystem.entity.MedicalSupply;
import com.fu.swp391.schoolhealthmanagementsystem.entity.SupplyTransaction;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicalSupplyStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SupplyTransactionType;
import com.fu.swp391.schoolhealthmanagementsystem.repository.MedicalSupplyRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.SupplyTransactionRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(3) // Chạy sau UserInitializer (1) và DemoUserInitializer (2)
public class DemoMedicalSupplyInitializer implements ApplicationRunner {

    private final MedicalSupplyRepository medicalSupplyRepository;
    private final UserRepository userRepository;
    private final SupplyTransactionRepository supplyTransactionRepository; // Cần để tạo giao dịch nhập kho ban đầu

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Bắt đầu khởi tạo dữ liệu Vật tư Y tế...");

        // Lấy một người dùng có vai trò quản lý kho (ví dụ: Nurse Manager hoặc School Admin) để làm người tạo
        // Giả sử Nurse Manager đã được tạo bởi DemoUserInitializer
        Optional<User> staffManagerOpt = userRepository.findByEmail("nursemanager@example.com");
        User creator;
        if (staffManagerOpt.isPresent()) {
            creator = staffManagerOpt.get();
        } else {
            log.warn("Không tìm thấy người dùng phù hợp (NurseManager hoặc SchoolAdmin) để khởi tạo vật tư y tế. Bỏ qua.");
            return;
        }

        // Tạo vật tư y tế 1
        String supplyName1 = "Băng gạc y tế tiệt trùng";
        if (medicalSupplyRepository.findFirstByName(supplyName1).isEmpty()) {
            MedicalSupply supply1 = MedicalSupply.builder()
                    .name(supplyName1)
                    .category("Băng vết thương")
                    .unit("Gói")
                    .currentStock(0) // Sẽ được cập nhật bởi transaction ban đầu
                    .description("Băng gạc y tế tiệt trùng, kích thước 10x10cm, dùng một lần.")
                    .status(MedicalSupplyStatus.AVAILABLE)
                    .createdByUser(creator)
                    .updatedByUser(creator)
                    // createdAt và lastUpdatedAt sẽ được tự động gán
                    .build();
            medicalSupplyRepository.save(supply1);
            createInitialStockTransaction(supply1, 100, "Nhập kho ban đầu", creator);
            log.info("Đã tạo vật tư: {} với số lượng ban đầu 100", supply1.getName());
        } else {
            log.info("Vật tư y tế '{}' đã tồn tại.", supplyName1);
        }

        // Tạo vật tư y tế 2
        String supplyName2 = "Thuốc hạ sốt Paracetamol 500mg";
        if (medicalSupplyRepository.findFirstByName(supplyName2).isEmpty()) {
            MedicalSupply supply2 = MedicalSupply.builder()
                    .name(supplyName2)
                    .category("Thuốc uống")
                    .unit("Viên")
                    .currentStock(0)
                    .description("Thuốc hạ sốt, giảm đau Paracetamol 500mg.")
                    .status(MedicalSupplyStatus.AVAILABLE)
                    .createdByUser(creator)
                    .updatedByUser(creator)
                    .build();
            medicalSupplyRepository.save(supply2);
            createInitialStockTransaction(supply2, 200, "Nhập kho ban đầu", creator);
            log.info("Đã tạo vật tư: {} với số lượng ban đầu 200", supply2.getName());
        } else {
            log.info("Vật tư y tế '{}' đã tồn tại.", supplyName2);
        }

        // Tạo vật tư y tế 3
        String supplyName3 = "Nước muối sinh lý NaCl 0.9%";
        if (medicalSupplyRepository.findFirstByName(supplyName3).isEmpty()) {
            MedicalSupply supply3 = MedicalSupply.builder()
                    .name(supplyName3)
                    .category("Dung dịch sát khuẩn")
                    .unit("Chai 500ml")
                    .currentStock(0)
                    .description("Nước muối sinh lý dùng để rửa vết thương, súc miệng.")
                    .status(MedicalSupplyStatus.AVAILABLE)
                    .createdByUser(creator)
                    .updatedByUser(creator)
                    .build();
            medicalSupplyRepository.save(supply3);
            createInitialStockTransaction(supply3, 50, "Nhập kho ban đầu", creator);
            log.info("Đã tạo vật tư: {} với số lượng ban đầu 50", supply3.getName());
        } else {
            log.info("Vật tư y tế '{}' đã tồn tại.", supplyName3);
        }

        log.info("Hoàn tất khởi tạo dữ liệu Vật tư Y tế.");
    }

    private void createInitialStockTransaction(MedicalSupply medicalSupply, int quantity, String note, User performedBy) {
        // Cập nhật tồn kho thực tế cho MedicalSupply
        medicalSupply.setCurrentStock(medicalSupply.getCurrentStock() + quantity);
        medicalSupply.setUpdatedByUser(performedBy); // Người thực hiện giao dịch cũng là người cập nhật vật tư
        // medicalSupplyRepository.save(medicalSupply); // Sẽ được save khi transaction được flush hoặc service gọi save

        SupplyTransaction transaction = SupplyTransaction.builder()
                .medicalSupply(medicalSupply)
                .quantity(quantity)
                .supplyTransactionType(SupplyTransactionType.RECEIVED) // Giao dịch nhập kho
                .note(note)
                .performedByUser(performedBy)
                .transactionDateTime(LocalDateTime.now()) // Nên được tự động bởi @CreationTimestamp trong SupplyTransaction
                .build();
        supplyTransactionRepository.save(transaction);

        // Lưu lại medicalSupply sau khi đã có transaction và cập nhật stock
        medicalSupplyRepository.save(medicalSupply);
        log.debug("Đã tạo giao dịch nhập kho ban đầu cho {} với số lượng {}", medicalSupply.getName(), quantity);
    }
}