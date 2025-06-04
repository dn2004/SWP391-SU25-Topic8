package com.fu.swp391.schoolhealthmanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "Cấu trúc phản hồi lỗi chuẩn")
public record ErrorResponseDto(
        @Schema(description = "Thời gian xảy ra lỗi", example = "2023-10-27T10:15:30")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,

        @Schema(description = "Mã trạng thái HTTP", example = "400")
        int status,

        @Schema(description = "Loại lỗi (thường là tên ngắn của mã trạng thái)", example = "Bad Request")
        String error,

        @Schema(description = "Thông điệp lỗi chi tiết cho người dùng", example = "Email đã được sử dụng!")
        String message,

        @Schema(description = "Đường dẫn API gây ra lỗi", example = "/api/auth/register/parent")
        String path,

        @Schema(description = "Chi tiết lỗi validation (nếu có)")
        Map<String, List<String>> validationErrors
) {
    public static ErrorResponseDto of(HttpStatus status, String message, String path) {
        return new ErrorResponseDto(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                null
        );
    }

    public static ErrorResponseDto of(HttpStatus status, String message, String path, Map<String, List<String>> validationErrors) {
        return new ErrorResponseDto(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                validationErrors
        );
    }
}
