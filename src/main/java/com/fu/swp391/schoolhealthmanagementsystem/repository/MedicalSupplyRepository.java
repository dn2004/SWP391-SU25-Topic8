package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.MedicalSupply;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicalSupplyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicalSupplyRepository extends JpaRepository<MedicalSupply, Long>, JpaSpecificationExecutor<MedicalSupply> {
    Optional<MedicalSupply> findBySupplyId(Long supplyId);

    Optional<MedicalSupply> findByNameAndUnit(String name, String unit);

    // Phương thức tìm theo status thay vì active
    Page<MedicalSupply> findAllByStatus(MedicalSupplyStatus status, Pageable pageable);

    // Phương thức tìm theo status khác ARCHIVED (thay thế cho active=true)
    Page<MedicalSupply> findAllByStatusNot(MedicalSupplyStatus status, Pageable pageable);

    // Phương thức tìm theo danh sách các status
    Page<MedicalSupply> findAllByStatusIn(List<MedicalSupplyStatus> statuses, Pageable pageable);

    Optional<MedicalSupply> findFirstByName(String supplyName);

    /**
     * Tìm tất cả vật tư có ngày hết hạn <= ngày được chỉ định và không ở trạng thái đã được đưa ra
     * @param date Ngày cần so sánh
     * @param status Trạng thái cần loại trừ (thường là EXPIRED để chỉ tìm các vật tư chưa được đánh dấu là hết hạn)
     * @return Danh sách các vật tư y tế thỏa mãn điều kiện
     */
    List<MedicalSupply> findAllByExpiredDateLessThanEqualAndStatusNot(LocalDate date, MedicalSupplyStatus status);

    /**
     * Tìm tất cả vật tư có ngày hết hạn sắp đến (trong khoảng ngày chỉ định)
     * @param startDate Ngày bắt đầu khoảng
     * @param endDate Ngày kết thúc khoảng
     * @return Danh sách các vật tư y tế sắp hết hạn
     */
    List<MedicalSupply> findAllByExpiredDateBetweenAndStatusNot(LocalDate startDate, LocalDate endDate, MedicalSupplyStatus status);

    /**
     * Kiểm tra xem vật tư y tế có bất kỳ giao dịch nào liên quan đến sự cố y tế không
     * @param supplyId ID của vật tư y tế cần ki���m tra
     * @return true nếu có liên quan đến sự cố y tế, false nếu không
     */
    @Query("SELECT CASE WHEN COUNT(st) > 0 THEN true ELSE false END FROM SupplyTransaction st WHERE st.medicalSupply.supplyId = :supplyId AND st.healthIncident IS NOT NULL")
    boolean hasTransactionsRelatedToHealthIncident(@Param("supplyId") Long supplyId);
}