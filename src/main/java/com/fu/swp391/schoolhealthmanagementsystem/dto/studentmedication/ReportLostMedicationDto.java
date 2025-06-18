package com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportLostMedicationDto(
        @NotNull Integer dosesLost,
        @Size(max=500) String staffNotes
) {}