package com.fu.swp391.schoolhealthmanagementsystem.dto.parent;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thông tin liên kết giữa phụ huynh và học sinh")
public record LinkStudentResponse(
        @Schema(
                description = "Id của user phụ huynh",
                example = "1"
        )
        Long parentId,

        @Schema(
                description = "Id của học sinh",
                example = "1"
        )
        Long studentId,

        @Schema(
                description = "Mối quan hệ của phụ huynh và học sinh",
                example = "Bố"
        )
        String relationship
) {}
