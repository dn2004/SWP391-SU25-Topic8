package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.supply.SupplyTransactionResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.SupplyTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SupplyTransactionMapper {

    @Mapping(target = "supplyId", source = "medicalSupply.supplyId")
    @Mapping(target = "supplyName", source = "medicalSupply.name")
    @Mapping(target = "incidentId", source = "healthIncident.incidentId")
    @Mapping(target = "performedBy", source = "performedByUser.fullName")
    SupplyTransactionResponseDto toDto(SupplyTransaction transaction);
}

