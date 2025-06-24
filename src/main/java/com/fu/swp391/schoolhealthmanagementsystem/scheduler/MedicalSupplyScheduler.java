package com.fu.swp391.schoolhealthmanagementsystem.scheduler;

import com.fu.swp391.schoolhealthmanagementsystem.entity.MedicalSupply;
import com.fu.swp391.schoolhealthmanagementsystem.entity.SupplyTransaction;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicalSupplyStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SupplyTransactionType;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.repository.MedicalSupplyRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.SupplyTransactionRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Lịch trình tự động kiểm tra và cập nhật trạng thái cho các vật tư y tế hết hạn
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MedicalSupplyScheduler {

    private final MedicalSupplyRepository medicalSupplyRepository;
    private final SupplyTransactionRepository supplyTransactionRepository;
    private final UserRepository userRepository;


    @Scheduled(cron = "0 0 0 * * ?") // Chạy lúc 00:00:00 mỗi ngày
    @Transactional
    public void checkAndUpdateExpiredSupplies() {
        log.info("Bắt đầu kiểm tra vật tư y tế hết hạn...");

        LocalDate today = LocalDate.now();

        // Lấy danh sách tất cả vật tư có ngày hết hạn <= ngày hiện tại và chưa được đánh dấu là hết hạn
        List<MedicalSupply> expiredSupplies = medicalSupplyRepository.findAllByExpiredDateLessThanEqualAndStatusNot(
            today, MedicalSupplyStatus.EXPIRED);

        if (expiredSupplies.isEmpty()) {
            log.info("Không có vật tư y tế nào hết hạn hôm nay.");
            return;
        }

        log.info("Tìm thấy {} vật tư y tế đã hết hạn cần cập nhật.", expiredSupplies.size());

        // Lấy tài khoản hệ thống để tạo giao dịch tự động
        User systemUser = userRepository.findByEmail("system@example.com")
            .orElseGet(() -> {
                log.warn("Không tìm thấy tài khoản hệ thống, sẽ sử dụng Admin đầu tiên");
                return userRepository.findFirstByRole(UserRole.System)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản Admin trong hệ thống"));
            });

        List<SupplyTransaction> transactionsToSave = new ArrayList<>();

        // Cập nhật trạng thái và tạo giao dịch cho từng vật tư hết hạn
        for (MedicalSupply supply : expiredSupplies) {
            log.info("Cập nhật vật tư hết hạn: {} (ID: {}). Ngày hết hạn: {}",
                supply.getName(), supply.getSupplyId(), supply.getExpiredDate());

            // Cập nhật trạng thái thành EXPIRED
            supply.setStatus(MedicalSupplyStatus.EXPIRED);
            supply.setCurrentStock(0); // Đặt số lượng tồn kho về 0
            supply.setUpdatedByUser(systemUser);

            // Tạo giao dịch ghi nhận việc vật tư hết hạn
            SupplyTransaction transaction = SupplyTransaction.builder()
                .medicalSupply(supply)
                .quantity(supply.getCurrentStock()) // Ghi nhận toàn bộ số lượng hiện tại
                .supplyTransactionType(SupplyTransactionType.EXPIRED)
                .note(String.format("Vật tư tự động đánh dấu hết hạn. Ngày hết hạn: %s", supply.getExpiredDate()))
                .performedByUser(systemUser)
                .build();

            transactionsToSave.add(transaction);
            log.debug("Đã tạo ghi nhận hết hạn cho vật tư {} (ID: {}). Số lượng: {}",
                supply.getName(), supply.getSupplyId(), supply.getCurrentStock());
        }

        // Lưu tất cả các vật tư đã cập nhật
        medicalSupplyRepository.saveAll(expiredSupplies);
        log.info("Đã cập nhật trạng thái cho {} vật tư y tế hết hạn.", expiredSupplies.size());

        // Lưu tất cả các giao dịch
        supplyTransactionRepository.saveAll(transactionsToSave);
        log.info("Đã tạo {} giao dịch ghi nhận vật tư hết hạn.", transactionsToSave.size());
    }
}
