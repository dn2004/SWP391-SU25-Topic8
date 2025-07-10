package com.fu.swp391.schoolhealthmanagementsystem.validation;

import com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination.CreateVaccinationCampaignRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidAgeRangeValidator implements ConstraintValidator<ValidAgeRange, CreateVaccinationCampaignRequestDto> {
    @Override
    public boolean isValid(CreateVaccinationCampaignRequestDto dto, ConstraintValidatorContext context) {
        if (dto == null) return true;
        Integer min = dto.targetAgeMin();
        Integer max = dto.targetAgeMax();
        if (min == null || max == null) return true;
        return min <= max;
    }
}

