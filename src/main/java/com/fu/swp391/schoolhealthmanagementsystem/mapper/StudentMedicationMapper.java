package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication.CreateStudentMedicationByStaffRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication.StudentMedicationResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication.UpdateMedicationScheduleRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication.UpdateStudentMedicationInfoRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.MedicationTimeSlot;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentMedication;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context; // Không cần thiết nếu không dùng @Context
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List; // Import List

@Mapper(componentModel = "spring", uses = {MedicationTimeSlotMapper.class})
public interface StudentMedicationMapper {

    @Mapping(target = "studentMedicationId", ignore = true)
    @Mapping(target = "student", source = "student")
    @Mapping(target = "submittedByParent", source = "parent")
    // Các trường khác từ DTO sẽ được map tự động nếu tên giống:
    // medicationName, dosagePerAdministrationText, totalDosesProvided, expiryDate, notes, usageInstruction, scheduleStartDate
    @Mapping(target = "remainingDoses", source = "dto.totalDosesProvided") // Ban đầu remaining = total
    @Mapping(target = "dateReceived", expression = "java(java.time.LocalDate.now())") // Tự gán ngày nhận
    @Mapping(target = "receivedByMedicalStaff", source = "staff") // NVYT nhận thuốc
    @Mapping(target = "status", ignore = true) // Service sẽ set status (ví dụ: AVAILABLE)
    @Mapping(target = "createdAt", ignore = true) // Được quản lý bởi @CreationTimestamp
    @Mapping(target = "createdByUser", source = "staff") // NVYT tạo bản ghi
    @Mapping(target = "updatedAt", ignore = true) // Được quản lý bởi @UpdateTimestamp
    @Mapping(target = "updatedByUser", source = "staff") // Ban đầu, người cập nhật cũng là người tạo
    @Mapping(target = "scheduledTasks", ignore = true)
    @Mapping(target = "medicationTransactions", ignore = true)
    @Mapping(source = "dto.scheduleTimes", target = "medicationTimeSlots") // MapStruct dùng MedicationTimeSlotMapper
    @Mapping(target = "nextScheduledTaskGenerationDate", ignore = true) // Service quản lý
    StudentMedication createDtoToEntity(CreateStudentMedicationByStaffRequestDto dto, Student student, User parent, User staff);

    @AfterMapping
    default void afterCreateDtoToEntity(CreateStudentMedicationByStaffRequestDto dto, @MappingTarget StudentMedication entity) {
        if (entity.getMedicationTimeSlots() != null) {
            for (MedicationTimeSlot slot : entity.getMedicationTimeSlots()) {
                slot.setStudentMedication(entity);
            }
        }
    }

    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.fullName", target = "studentFullName")
    @Mapping(source = "submittedByParent.userId", target = "submittedByParentId")
    @Mapping(source = "submittedByParent.fullName", target = "parentFullName")
    @Mapping(source = "receivedByMedicalStaff.userId", target = "receivedByMedicalStaffId")
    @Mapping(source = "receivedByMedicalStaff.fullName", target = "medicalStaffFullName")
    @Mapping(source = "createdByUser.userId", target = "createdByUserId")
    @Mapping(source = "createdByUser.fullName", target = "createdByUserName")
    @Mapping(source = "updatedByUser.userId", target = "updatedByUserId")
    @Mapping(source = "updatedByUser.fullName", target = "updatedByUserName")
    @Mapping(source = "medicationTimeSlots", target = "scheduleTimes") // MapStruct dùng MedicationTimeSlotMapper
        // isScheduleActive đã bị bỏ
    StudentMedicationResponseDto entityToResponseDto(StudentMedication entity);

    // --- Mapping cho Update Info ---
    @Mapping(target = "studentMedicationId", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "submittedByParent", ignore = true)
    @Mapping(target = "totalDosesProvided", ignore = true)
    @Mapping(target = "remainingDoses", ignore = true)
    @Mapping(target = "dateReceived", ignore = true)
    @Mapping(target = "receivedByMedicalStaff", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true) // Sẽ được service set
    @Mapping(target = "scheduledTasks", ignore = true)
    @Mapping(target = "medicationTransactions", ignore = true)
    @Mapping(target = "scheduleStartDate", ignore = true)
    @Mapping(target = "medicationTimeSlots", ignore = true)
    @Mapping(target = "nextScheduledTaskGenerationDate", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateMedicationInfoFromDto(UpdateStudentMedicationInfoRequestDto dto, @MappingTarget StudentMedication entity);

    // --- Mapping cho Update Schedule ---
    @Mapping(target = "studentMedicationId", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "submittedByParent", ignore = true)
    @Mapping(target = "medicationName", ignore = true)
    @Mapping(target = "dosagePerAdministrationText", ignore = true)
    @Mapping(target = "totalDosesProvided", ignore = true)
    @Mapping(target = "remainingDoses", ignore = true)
    @Mapping(target = "expiryDate", ignore = true)
    @Mapping(target = "dateReceived", ignore = true)
    @Mapping(target = "receivedByMedicalStaff", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "usageInstruction", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true) // Sẽ được service set
    @Mapping(target = "scheduledTasks", ignore = true)
    @Mapping(target = "medicationTransactions", ignore = true)
    @Mapping(source = "dto.scheduleStartDate", target = "scheduleStartDate")
    @Mapping(source = "dto.scheduleTimes", target = "medicationTimeSlots")
    @Mapping(target = "nextScheduledTaskGenerationDate", ignore = true) // Service sẽ quản lý
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateScheduleFromDto(UpdateMedicationScheduleRequestDto dto, @MappingTarget StudentMedication entity);

    @AfterMapping
    default void afterUpdateScheduleFromDto(UpdateMedicationScheduleRequestDto dto, @MappingTarget StudentMedication entity) {
        entity.clearMedicationTimeSlots(); // Xóa slot cũ
        if (dto.scheduleTimes() != null && !dto.scheduleTimes().isEmpty()) {
            // MapStruct đã tự động map dto.scheduleTimes sang entity.medicationTimeSlots
            // Giờ chỉ cần set lại tham chiếu ngược
            for (MedicationTimeSlot slotEntity : entity.getMedicationTimeSlots()) {
                slotEntity.setStudentMedication(entity);
            }
        }
    }
}