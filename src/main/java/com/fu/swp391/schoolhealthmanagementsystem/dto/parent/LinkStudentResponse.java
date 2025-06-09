package com.fu.swp391.schoolhealthmanagementsystem.dto.parent;

import io.swagger.v3.oas.annotations.media.Schema;

public record LinkStudentResponse (
        @Schema(description = "Id của user phụ huynh", example = "1")
        Long parentId,
        @Schema(description = "Id của sinh viên", example = "1")
        Long studentId,
        @Schema(description = "Mối quan hệ của phụ huynh và sinh viên")
        String relationship
)
{}
