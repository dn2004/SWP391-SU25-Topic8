package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.notification.NotificationResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.service.AuthorizationService;
import com.fu.swp391.schoolhealthmanagementsystem.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "API để quản lý thông báo người dùng")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthorizationService authorizationService;

    @Operation(
            summary = "Lấy danh sách thông báo của người dùng hiện tại (phân trang)",
            description = """
                    ### Mô tả
                    Lấy danh sách thông báo của người dùng đang đăng nhập.
                    - **Phân quyền:** Yêu cầu người dùng đã xác thực.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thông báo thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDto>> getMyNotifications(
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        Page<NotificationResponseDto> notifications = notificationService.getNotificationsForUser(currentUser.getUserId(), pageable);
        return ResponseEntity.ok(notifications);
    }

    @Operation(
            summary = "Đếm số lượng thông báo chưa đọc",
            description = """
                    ### Mô tả
                    Đếm số lượng thông báo chưa đọc của người dùng đang đăng nhập.
                    - **Phân quyền:** Yêu cầu người dùng đã xác thực.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đếm thông báo thành công",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        long count = notificationService.getUnreadNotificationCount(currentUser.getUserId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @Operation(
            summary = "Đánh dấu một thông báo là đã đọc",
            description = """
                    ### Mô tả
                    Đánh dấu một thông báo cụ thể là đã đọc.
                    - **Phân quyền:** Yêu cầu người dùng đã xác thực và là người nhận của thông báo.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đánh dấu đã đọc thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thông báo", content = @Content)
    })
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponseDto> markAsRead(@PathVariable Long notificationId) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        NotificationResponseDto updatedNotification = notificationService.markAsRead(notificationId, currentUser.getUserId());
        return ResponseEntity.ok(updatedNotification);
    }

    @Operation(
            summary = "Đánh dấu tất cả thông báo là đã đọc",
            description = """
                    ### Mô tả
                    Đánh dấu tất cả thông báo của người dùng hiện tại là đã đọc.
                    - **Phân quyền:** Yêu cầu người dùng đã xác thực.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Đánh dấu tất cả đã đọc thành công", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @PostMapping("/mark-all-as-read")
    public ResponseEntity<Void> markAllAsRead() {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        notificationService.markAllAsRead(currentUser.getUserId());
        return ResponseEntity.noContent().build();
    }
}
