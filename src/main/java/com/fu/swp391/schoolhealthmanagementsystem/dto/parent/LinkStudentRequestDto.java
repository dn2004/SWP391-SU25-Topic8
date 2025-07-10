package com.fu.swp391.schoolhealthmanagementsystem.dto.parent;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.RelationshipType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Yêu cầu liên kết phụ huynh với học sinh bằng mã mời")
public record LinkStudentRequestDto(
        @Schema(
                description = "Mã mời duy nhất của học sinh",
                example = "INVITE12345"
        )
        @NotBlank(message = "Mã mời không được để trống")
        @Size(
                max = 20,
                message = "Mã mời tối đa 20 ký tự"
        )
        String invitationCode,

        @Schema(
                description = "Loại mối quan hệ giữa phụ huynh và học sinh",
                example = "Bố"
        )
        @NotNull(message = "Loại mối quan hệ không được để trống")
        RelationshipType relationshipType
) {}
