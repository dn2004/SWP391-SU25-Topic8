package com.fu.swp391.schoolhealthmanagementsystem.mapper;


import com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication.ScheduledMedicationTaskResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.ScheduledMedicationTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ScheduledMedicationTaskMapper {

    @Mapping(source = "studentMedication.studentMedicationId", target = "studentMedicationId")
    @Mapping(source = "studentMedication.medicationName", target = "medicationName") // Lấy tên thuốc
    @Mapping(source = "administeredByStaff.userId", target = "administeredByStaffId")
    @Mapping(source = "administeredByStaff.fullName", target = "administeredByStaffName")
    ScheduledMedicationTaskResponseDto toDto(ScheduledMedicationTask entity);
    // Các mapping khác cho việc tạo/cập nhật task nếu có
}