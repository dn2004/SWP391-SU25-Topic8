package com.fu.swp391.schoolhealthmanagementsystem.dto.blog;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO phản hồi trạng thái")
public record StatusResponseDto(
    @Schema(description = "Giá trị trạng thái", example = "PUBLIC")
    String value,
    @Schema(description = "Tên hiển thị của trạng thái", example = "Công khai")
    String displayName,
    @Schema(description = "Màu chữ cho trạng thái", example = "#FFFFFF")
    String color,
    @Schema(description = "Màu nền cho trạng thái", example = "#00FF00")
    String backgroundColor
) {
}