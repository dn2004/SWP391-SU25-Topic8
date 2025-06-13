package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.supply.MedicalSupplyRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.supply.MedicalSupplyResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.supply.MedicalSupplyStockAdjustmentDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.supply.MedicalSupplyUpdateDto;
import com.fu.swp391.schoolhealthmanagementsystem.service.MedicalSupplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medical-supplies")
@RequiredArgsConstructor
@Tag(name = "Quản lý Vật tư Y tế", description = "API cho việc quản lý vật tư y tế")
@SecurityRequirement(name = "bearerAuth") // Giả sử bạn dùng Bearer token cho Swagger
public class MedicalSupplyController {

    private final MedicalSupplyService medicalSupplyService;

    @Operation(summary = "Tạo mới một vật tư y tế",
            description = "Tạo mới một vật tư y tế. Yêu cầu vai trò MedicalStaff hoặc StaffManager.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vật tư y tế được tạo thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MedicalSupplyResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<MedicalSupplyResponseDto> createMedicalSupply(
            @Valid @RequestBody MedicalSupplyRequestDto requestDto) {
        MedicalSupplyResponseDto createdSupply = medicalSupplyService.createMedicalSupply(requestDto);
        return new ResponseEntity<>(createdSupply, HttpStatus.CREATED);
    }

    @Operation(summary = "Lấy thông tin vật tư y tế theo ID",
            description = "Lấy thông tin chi tiết của một vật tư y tế dựa trên ID. Yêu cầu vai trò SchoolAdmin, StaffManager, hoặc MedicalStaff.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm thấy vật tư y tế",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MedicalSupplyResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy vật tư y tế")
    })
    @GetMapping("/{supplyId}")
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager', 'MedicalStaff')")
    public ResponseEntity<MedicalSupplyResponseDto> getMedicalSupplyById(
            @Parameter(description = "ID của vật tư y tế cần lấy thông tin") @PathVariable Long supplyId) {
        MedicalSupplyResponseDto supply = medicalSupplyService.getMedicalSupplyById(supplyId);
        return ResponseEntity.ok(supply);
    }

    @Operation(summary = "Lấy danh sách tất cả vật tư y tế (phân trang)",
            description = "Lấy danh sách vật tư y tế có phân trang. Có thể lọc theo trạng thái hoạt động (active). Yêu cầu vai trò SchoolAdmin, StaffManager, hoặc MedicalStaff.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách vật tư y tế thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))), // Lưu ý: Schema cho Page<MedicalSupplyResponseDto>
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager', 'MedicalStaff')")
    public ResponseEntity<Page<MedicalSupplyResponseDto>> getAllMedicalSupplies(
            @Parameter(description = "Lọc theo trạng thái hoạt động (true/false). Nếu không cung cấp, tất cả vật tư sẽ được trả về.")
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        Page<MedicalSupplyResponseDto> suppliesPage = medicalSupplyService.getAllMedicalSupplies(pageable, isActive);
        return ResponseEntity.ok(suppliesPage);
    }

    @Operation(summary = "Cập nhật thông tin (metadata) của một vật tư y tế",
            description = "Cập nhật các thông tin như tên, loại, đơn vị của một vật tư y tế đã có. Số lượng tồn kho được quản lý qua API điều chỉnh tồn kho. Yêu cầu vai trò MedicalStaff hoặc StaffManager.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vật tư y tế được cập nhật thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MedicalSupplyResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ hoặc cố gắng cập nhật vật tư không hoạt động"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy vật tư y tế")
    })
    @PutMapping("/{supplyId}")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<MedicalSupplyResponseDto> updateMedicalSupply(
            @Parameter(description = "ID của vật tư y tế cần cập nhật") @PathVariable Long supplyId,
            @Valid @RequestBody MedicalSupplyUpdateDto updateDto) {
        MedicalSupplyResponseDto updatedSupply = medicalSupplyService.updateMedicalSupply(supplyId, updateDto);
        return ResponseEntity.ok(updatedSupply);
    }

    @Operation(summary = "Điều chỉnh số lượng tồn kho của vật tư y tế",
            description = "Điều chỉnh số lượng tồn kho (nhập kho, điều chỉnh tăng/giảm). Yêu cầu vai trò MedicalStaff hoặc StaffManager.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Điều chỉnh tồn kho thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MedicalSupplyResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ, không đủ tồn kho, hoặc cố gắng điều chỉnh vật tư không hoạt động"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy vật tư y tế")
    })
    @PostMapping("/{supplyId}/stock-adjustment")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<MedicalSupplyResponseDto> adjustMedicalSupplyStock(
            @Parameter(description = "ID của vật tư y tế cần điều chỉnh tồn kho") @PathVariable Long supplyId,
            @Valid @RequestBody MedicalSupplyStockAdjustmentDto adjustmentDto) {
        MedicalSupplyResponseDto updatedSupply = medicalSupplyService.adjustMedicalSupplyStock(supplyId, adjustmentDto);
        return ResponseEntity.ok(updatedSupply);
    }


    @Operation(summary = "Xóa mềm một vật tư y tế (đặt trạng thái không hoạt động)",
            description = "Đánh dấu một vật tư y tế là không hoạt động. Đây là hình thức xóa mềm. Yêu cầu vai trò StaffManager.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vật tư y tế được xóa mềm thành công"),
            @ApiResponse(responseCode = "400", description = "Vật tư đã ở trạng thái không hoạt động"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy vật tư y tế")
    })
    @DeleteMapping("/{supplyId}")
    @PreAuthorize("hasRole('StaffManager')") // Chỉ StaffManager mới có quyền xóa
    public ResponseEntity<Void> deleteMedicalSupply(
            @Parameter(description = "ID của vật tư y tế cần xóa mềm") @PathVariable Long supplyId) {
        medicalSupplyService.deleteMedicalSupply(supplyId);
        return ResponseEntity.noContent().build();
    }
}