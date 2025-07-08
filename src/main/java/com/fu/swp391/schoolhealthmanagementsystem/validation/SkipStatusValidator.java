package com.fu.swp391.schoolhealthmanagementsystem.validation;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.EnumSet;

public class SkipStatusValidator implements ConstraintValidator<ValidSkipStatus, ScheduledMedicationTaskStatus> {

    private static final EnumSet<ScheduledMedicationTaskStatus> VALID_STATUSES = EnumSet.of(
            ScheduledMedicationTaskStatus.SKIPPED_STUDENT_ABSENT,
            ScheduledMedicationTaskStatus.SKIPPED_STUDENT_REFUSED,
            ScheduledMedicationTaskStatus.SKIPPED_SUPPLY_ISSUE,
            ScheduledMedicationTaskStatus.NOT_ADMINISTERED_OTHER
    );

    @Override
    public void initialize(ValidSkipStatus constraintAnnotation) {
    }

    @Override
    public boolean isValid(ScheduledMedicationTaskStatus value, ConstraintValidatorContext context) {
        if (value == null) {
            return false; // @NotNull will handle this, but good practice to be safe
        }
        return VALID_STATUSES.contains(value);
    }
}
