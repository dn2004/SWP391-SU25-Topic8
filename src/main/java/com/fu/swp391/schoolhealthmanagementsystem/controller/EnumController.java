package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.blog.CategoryResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.blog.StatusResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogCategory;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/enums")
@Tag(name = "Enums", description = "API để lấy danh sách các giá trị enum trong hệ thống")
public class EnumController {

    @Operation(summary = "Lấy danh sách tất cả các danh mục blog",
            description = "Endpoint công khai để lấy danh sách tất cả các danh mục blog có thể sử dụng khi tạo blog mới.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/blog-categories")
    public ResponseEntity<List<CategoryResponseDto>> getAllBlogCategories() {
        List<CategoryResponseDto> categories = Arrays.stream(BlogCategory.values())
                .map(category -> new CategoryResponseDto(category.name(), category.getDisplayName(), category.getColor()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Lấy danh sách tất cả các trạng thái blog",
            description = "Endpoint công khai để lấy danh sách tất cả các trạng thái blog.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/blog-statuses")
    public ResponseEntity<List<StatusResponseDto>> getAllBlogStatuses() {
        List<StatusResponseDto> statuses = Arrays.stream(BlogStatus.values())
                .map(status -> new StatusResponseDto(status.name(), status.getDisplayName(), status.getBackgroundColor()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(statuses);
    }
}
