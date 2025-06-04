package com.fu.swp391.schoolhealthmanagementsystem.dto.parent;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu liên kết phụ huynh với học sinh bằng mã mời")
public class LinkStudentRequestDto {
    @NotBlank(message = "Mã mời không được để trống")
    @Size(max = 20, message = "Mã mời tối đa 20 ký tự")
    @Schema(description = "Mã mời duy nhất của học sinh", example = "INVITE12345")
    private String invitationCode;

    @NotBlank(message = "Mối quan hệ không được để trống")
    @Size(max = 50, message = "Mối quan hệ tối đa 50 ký tự")
    @Schema(description = "Mối quan hệ với học sinh (ví dụ: Bố, Mẹ, Người giám hộ)", example = "Mẹ")
    private String relationshipType;
}