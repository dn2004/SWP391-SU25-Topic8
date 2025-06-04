package com.fu.swp391.schoolhealthmanagementsystem.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// ConfirmFullNameRequestDto.java
@Data
public class ConfirmFullNameRequestDto {
    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ và tên phải từ 2 đến 100 ký tự")
    private String fullName;
}
