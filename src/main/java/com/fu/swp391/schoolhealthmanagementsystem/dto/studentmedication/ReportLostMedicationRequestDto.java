package com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication;

import jakarta.validation.constraints.Size;

public record ReportLostMedicationRequestDto(
        @Size(max = 500, message = "Ghi chú về việc thất lạc không quá 500 ký tự")
        String staffNotes // Lý do, hoàn cảnh thất lạc, ai phát hiện,...
) {}
