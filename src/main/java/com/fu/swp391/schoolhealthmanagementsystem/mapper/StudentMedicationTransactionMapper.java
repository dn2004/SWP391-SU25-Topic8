package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication.StudentMedicationTransactionResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentMedicationTransaction;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface StudentMedicationTransactionMapper {

    @Mapping(target = "performedBy", source = "performedByUser", qualifiedByName = "userToFullName")
    @Mapping(target = "scheduledMedicationTaskId", source = "scheduledMedicationTask.scheduledTaskId")
    StudentMedicationTransactionResponseDto toDto(StudentMedicationTransaction transaction);

    @Named("userToFullName")
    default String userToFullName(User user) {
        if (user == null) {
            return "Hệ thống";
        }
        return user.getFullName();
    }
}

