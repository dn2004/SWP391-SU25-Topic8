package com.fu.swp391.schoolhealthmanagementsystem.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu đăng nhập bằng Firebase ID Token")
public class FirebaseLoginRequestDto {
    @NotBlank(message = "Firebase ID token không được để trống")
    @Schema(description = "ID Token nhận được từ Firebase Authentication phía client")
    private String idToken;
}