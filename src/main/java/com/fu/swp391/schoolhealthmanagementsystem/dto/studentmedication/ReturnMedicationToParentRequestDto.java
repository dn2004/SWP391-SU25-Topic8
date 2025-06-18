package com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication;

import jakarta.validation.constraints.Size;

public record ReturnMedicationToParentRequestDto(
        @Size(max = 500, message = "Ghi chú về việc trả thuốc không quá 500 ký tự")
        String staffNotes // Người nhận, ngày trả, lý do trả (nếu có),...
) {}