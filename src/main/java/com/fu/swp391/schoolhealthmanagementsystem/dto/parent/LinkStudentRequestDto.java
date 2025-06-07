package com.fu.swp391.schoolhealthmanagementsystem.dto.parent;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.RelationshipType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Yêu cầu liên kết phụ huynh với học sinh bằng mã mời")
public record LinkStudentRequestDto(

        @NotBlank(message = "Mã mời không được để trống")
        @Size(max = 20, message = "Mã mời tối đa 20 ký tự")
        @Schema(description = "Mã mời duy nhất của học sinh", example = "INVITE12345")
        String invitationCode,

        @NotNull(message = "Loại mối quan hệ không được để trống")
        @Schema(description = "Loại mối quan hệ với học sinh", example = "MOTHER",
                allowableValues = {"FATHER", "MOTHER", "GUARDIAN", "GRANDFATHER", "GRANDMOTHER", "BROTHER", "SISTER", "OTHER"})
        RelationshipType relationshipType

) {}
