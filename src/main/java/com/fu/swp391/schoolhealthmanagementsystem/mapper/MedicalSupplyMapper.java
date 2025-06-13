package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.supply.MedicalSupplyRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.supply.MedicalSupplyResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.supply.MedicalSupplyUpdateDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.MedicalSupply;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface MedicalSupplyMapper {

    @Mapping(target = "supplyId", ignore = true)
    @Mapping(target = "currentStock", source = "initialStock")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "supplyTransactions", ignore = true)
    MedicalSupply requestDtoToEntity(MedicalSupplyRequestDto dto);

    @Mapping(source = "createdByUser", target = "createdByUserEmail", qualifiedByName = "userToEmail")
    @Mapping(source = "updatedByUser", target = "updatedByUserEmail", qualifiedByName = "userToEmail")
    MedicalSupplyResponseDto entityToResponseDto(MedicalSupply entity);

    @Mapping(target = "supplyId", ignore = true)
    @Mapping(target = "currentStock", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "supplyTransactions", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromUpdateDto(MedicalSupplyUpdateDto dto, @MappingTarget MedicalSupply entity);

    @Named("userToEmail")
    default String userToEmail(User user) {
        return user != null ? user.getEmail() : null;
    }
}