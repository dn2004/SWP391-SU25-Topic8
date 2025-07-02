package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.blog.CategoryResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogCategory;
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
@RequestMapping("/api/blog-categories")
@Tag(name = "Blog Categories", description = "API để lấy danh sách danh mục blog")
public class BlogCategoryController {

    @Operation(summary = "Lấy danh sách tất cả các danh mục blog",
            description = "Endpoint công khai để lấy danh sách tất cả các danh mục blog có thể sử dụng khi tạo blog mới.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        List<CategoryResponseDto> categories = Arrays.stream(BlogCategory.values())
                .map(category -> new CategoryResponseDto(category.name(), category.getDisplayName(), category.getColor()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }
}
