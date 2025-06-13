// File: com/fu/swp391/schoolhealthmanagementsystem/mapper/HealthIncidentMapper.java
package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.incident.CreateHealthIncidentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.incident.HealthIncidentResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.incident.HealthIncidentSupplyUsageResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.incident.UpdateHealthIncidentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.HealthIncident;
import com.fu.swp391.schoolhealthmanagementsystem.entity.SupplyTransaction;
import org.mapstruct.*;

import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring") // Bỏ UserMapper nếu không dùng hoặc dùng cách khác
public interface HealthIncidentMapper {

    @Mapping(target = "incidentId", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "recordedByUser", ignore = true)
    @Mapping(target = "supplyUsages", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedByUser", ignore = true)
    HealthIncident toEntity(CreateHealthIncidentRequestDto dto);

    @Mapping(source = "student.studentId", target = "studentId")
    @Mapping(source = "student.fullName", target = "studentName")
    @Mapping(source = "student.className", target = "studentClass")
    @Mapping(source = "recordedByUser.userId", target = "recordedByUserId") // Đổi tên
    @Mapping(source = "recordedByUser.fullName", target = "recordedByUserName") // Đổi tên
    @Mapping(source = "updatedByUser.userId", target = "updatedByUserId") // Thêm
    @Mapping(source = "updatedByUser.fullName", target = "updatedByUserName") // Thêm
    @Mapping(source = "supplyUsages", target = "supplyUsages", qualifiedByName = "transactionsToResponseDtos")
    HealthIncidentResponseDto toDto(HealthIncident entity);

    List<HealthIncidentResponseDto> toDtos(List<HealthIncident> entities);

    @Named("transactionsToResponseDtos")
    default List<HealthIncidentSupplyUsageResponseDto> transactionsToResponseDtos(List<SupplyTransaction> transactions) {
        if (transactions == null) {
            return List.of();
        }
        return transactions.stream()
                .map(this::transactionToResponseDto)
                .collect(Collectors.toList());
    }

    @Mapping(source = "transactionId", target = "transactionId")
    @Mapping(source = "medicalSupply.supplyId", target = "supplyId")
    @Mapping(source = "medicalSupply.name", target = "supplyName")
    @Mapping(source = "medicalSupply.unit", target = "unit")
    @Mapping(source = "quantity", target = "quantityUsed")
    @Mapping(source = "note", target = "note")
    HealthIncidentSupplyUsageResponseDto transactionToResponseDto(SupplyTransaction transaction);


    @Mapping(target = "incidentId", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "recordedByUser", ignore = true)
    @Mapping(target = "supplyUsages", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedByUser", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateHealthIncidentRequestDto dto, @MappingTarget HealthIncident entity);
}