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
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(4) // Chạy sau DemoUserInitializer
public class DemoMedicalSupplyInitializer implements ApplicationRunner {

    private final MedicalSupplyRepository medicalSupplyRepository;
    private final UserRepository userRepository;
    private final SupplyTransactionRepository supplyTransactionRepository;

    // Record class to hold sample data
    private record SampleSupply(String name, String category, String unit, String description) {}

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Bắt đầu khởi tạo dữ liệu Vật tư Y tế...");

        // Lấy một người dùng quản lý để làm người tạo
        Optional<User> staffManagerOpt = userRepository.findByEmail("nursemanager1@example.com");
        if (staffManagerOpt.isEmpty()) {
            log.warn("Không tìm thấy người dùng 'nursemanager1@example.com'. Bỏ qua khởi tạo vật tư y tế.");
            return;
        }
        User creator = staffManagerOpt.get();

        // Danh sách các vật tư y tế mẫu
        List<SampleSupply> sampleSupplies = List.of(
            new SampleSupply("Băng gạc y tế tiệt trùng", "Băng vết thương", "Gói", "Băng gạc y tế tiệt trùng, kích thước 10x10cm."),
            new SampleSupply("Thuốc hạ sốt Paracetamol 500mg", "Thuốc uống", "Viên", "Thuốc hạ sốt, giảm đau Paracetamol 500mg."),
            new SampleSupply("Nước muối sinh lý NaCl 0.9%", "Dung dịch sát khuẩn", "Chai 500ml", "Dùng để rửa vết thương, súc miệng."),
            new SampleSupply("Bông y tế", "Vật tư tiêu hao", "Cuộn", "Bông gòn y tế thấm hút tốt."),
            new SampleSupply("Cồn 70 độ", "Dung dịch sát khuẩn", "Chai 100ml", "Dùng để sát khuẩn da và dụng cụ y tế."),
            new SampleSupply("Nhiệt kế điện tử", "Dụng cụ y tế", "Cái", "Nhiệt kế điện tử đo nhiệt độ cơ thể chính xác."),
            new SampleSupply("Oxy già 3%", "Dung dịch sát khuẩn", "Chai 60ml", "Dùng để rửa và sát trùng vết thương."),
            new SampleSupply("Băng keo cá nhân", "Băng vết thương", "Hộp 100 miếng", "Băng keo co giãn, không thấm nước."),
            new SampleSupply("Găng tay y tế", "Vật tư bảo hộ", "Hộp 100 chiếc", "Găng tay cao su y tế không bột."),
            new SampleSupply("Khẩu trang y tế 4 lớp", "Vật tư bảo hộ", "Hộp 50 cái", "Khẩu trang y tế kháng khuẩn, lọc bụi."),
            new SampleSupply("Dung dịch Povidone-Iodine 10%", "Dung dịch sát khuẩn", "Chai 100ml", "Dung dịch sát khuẩn vết thương phổ rộng."),
            new SampleSupply("Kéo y tế đầu tù", "Dụng cụ y tế", "Cái", "Kéo bằng thép không gỉ để cắt băng gạc."),
            new SampleSupply("Băng thun co giãn", "Băng vết thương", "Cuộn", "Băng thun dùng để cố định vết thương hoặc bong gân."),
            new SampleSupply("Túi chườm nóng/lạnh", "Vật tư khác", "Túi", "Túi gel tái sử dụng để chườm nóng hoặc lạnh."),
            new SampleSupply("Máy đo huyết áp điện tử", "Dụng cụ y tế", "Bộ", "Máy đo huyết áp bắp tay tự động."),
            new SampleSupply("Thuốc nhỏ mắt Natri Clorid 0.9%", "Thuốc nhỏ mắt", "Lọ 10ml", "Dùng để rửa mắt, giảm khô mắt."),
            new SampleSupply("Kem chống hăm Bepanthen", "Thuốc bôi ngoài da", "Tuýp 30g", "Kem bảo vệ và làm lành da khi bị hăm tã."),
            new SampleSupply("Viên ngậm giảm ho Strepsils", "Thuốc uống", "Vỉ 8 viên", "Viên ngậm kháng khuẩn, làm dịu cổ họng."),
            new SampleSupply("Dầu gió xanh", "Thuốc bôi ngoài da", "Chai 24ml", "Dầu gió dùng để giảm đau đầu, chóng mặt, côn trùng cắn."),
            new SampleSupply("Urgo-tup", "Băng vết thương", "Miếng", "Gạc vô trùng tẩm tulle gras để chống dính vào vết thương.")
        );

        Random random = new Random();
        int createdCount = 0;

        for (SampleSupply sample : sampleSupplies) {
            if (medicalSupplyRepository.findFirstByName(sample.name()).isEmpty()) {
                MedicalSupply supply = MedicalSupply.builder()
                        .name(sample.name())
                        .category(sample.category())
                        .unit(sample.unit())
                        .currentStock(0) // Sẽ được cập nhật bởi transaction
                        .description(sample.description())
                        .status(MedicalSupplyStatus.AVAILABLE)
                        .createdByUser(creator)
                        .updatedByUser(creator)
                        .build();
                medicalSupplyRepository.save(supply);

                // Tạo giao dịch nhập kho ban đầu với số lượng ngẫu nhiên
                int initialQuantity = 50 + random.nextInt(151); // Số lượng từ 50 đến 200
                createInitialStockTransaction(supply, initialQuantity, "Nhập kho ban đầu", creator);
                log.info("Đã tạo vật tư: '{}' với số lượng ban đầu {}.", supply.getName(), initialQuantity);
                createdCount++;
            } else {
                log.info("Vật tư y tế '{}' đã tồn tại. Bỏ qua.", sample.name());
            }
        }

        if (createdCount > 0) {
            log.info("Hoàn tất khởi tạo {} vật tư y tế mới.", createdCount);
        } else {
            log.info("Không có vật tư y tế mới nào được tạo.");
        }
    }

    private void createInitialStockTransaction(MedicalSupply medicalSupply, int quantity, String note, User performedBy) {
        medicalSupply.setCurrentStock(medicalSupply.getCurrentStock() + quantity);
        medicalSupply.setUpdatedByUser(performedBy);

        SupplyTransaction transaction = SupplyTransaction.builder()
                .medicalSupply(medicalSupply)
                .quantity(quantity)
                .supplyTransactionType(SupplyTransactionType.RECEIVED)
                .note(note)
                .performedByUser(performedBy)
                .transactionDateTime(LocalDateTime.now())
                .build();
        supplyTransactionRepository.save(transaction);

        medicalSupplyRepository.save(medicalSupply);
        log.debug("Đã tạo giao dịch nhập kho cho '{}' với số lượng {}.", medicalSupply.getName(), quantity);
    }
}
