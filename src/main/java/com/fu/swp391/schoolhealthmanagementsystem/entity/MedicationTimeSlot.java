package com.fu.swp391.schoolhealthmanagementsystem.entity;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolSession; // Nếu bạn dùng enum này
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp; // Nếu cần theo dõi thời gian tạo slot
import org.hibernate.annotations.UpdateTimestamp;   // Nếu cần theo dõi thời gian cập nhật slot

import java.time.LocalDateTime; // Cho created/updated at
// import java.time.LocalTime; // Có thể dùng LocalTime cho timeSlot nếu DB hỗ trợ tốt

@Entity
@Table(name = "MedicationTimeSlots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationTimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TimeSlotID")
    private Long timeSlotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentMedicationID", nullable = false)
    private StudentMedication studentMedication; // Mối quan hệ ngược lại với StudentMedication

    @Column(name = "TimeExpression", nullable = false, length = 50, columnDefinition = "NVARCHAR(50)")
    private String timeExpression; // Ví dụ: "08:00", "Sau bữa trưa", "14:30"
    // Vẫn giữ dạng text để linh hoạt, việc suy ra SchoolSession sẽ ở logic

    @Enumerated(EnumType.STRING)
    @Column(name = "SchoolSessionHint", length = 20) // Gợi ý buổi, có thể NVYT chọn hoặc hệ thống suy ra
    private SchoolSession schoolSessionHint;

    @Column(name = "SpecificNotes", length = 200, columnDefinition = "NVARCHAR(200)")
    private String specificNotes;
}