package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication.MedicationTimeSlotDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.MedicationTimeSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MedicationTimeSlotMapper {

    @Mapping(target = "timeSlotId", ignore = true) // ID sẽ được generate
    @Mapping(target = "studentMedication", ignore = true) // Sẽ được set từ parent entity
    @Mapping(source = "time", target = "timeExpression")
    // schoolSessionHint sẽ được tính toán
    @Mapping(target = "schoolSessionHint", expression = "java(com.fu.swp391.schoolhealthmanagementsystem.util.SchoolSessionUtil.getSessionFromTime(dto.time()))")
    MedicationTimeSlot toEntity(MedicationTimeSlotDto dto);

    // Dùng @AfterMapping để set StudentMedication cho từng slot sau khi list được tạo
    // Hoặc làm trong service khi gọi mapper
    // List<MedicationTimeSlot> toEntities(List<MedicationTimeSlotDto> dtos);

    @Mapping(source = "timeExpression", target = "time")
    MedicationTimeSlotDto toDto(MedicationTimeSlot entity);

    List<MedicationTimeSlotDto> toDtos(List<MedicationTimeSlot> entities);
}