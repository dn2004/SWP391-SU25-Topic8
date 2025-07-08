package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.supply.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicalSupplyStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SupplyTransactionType;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/medical-supplies")
@RequiredArgsConstructor
@Tag(name = "Quản lý Vật tư Y tế", description = "API cho việc quản lý vật tư y tế")
@SecurityRequirement(name = "bearerAuth") // Giả sử bạn dùng Bearer token cho Swagger
public class MedicalSupplyController {

    private final MedicalSupplyService medicalSupplyService;

    @Operation(summary = "Tạo mới một vật tư y tế",
            description = """
### Mô tả
Tạo mới một vật tư y tế trong kho.
- **Phân quyền:** Yêu cầu vai trò `MedicalStaff` hoặc `StaffManager`.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vật tư y tế được tạo thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MedicalSupplyResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<MedicalSupplyResponseDto> createMedicalSupply(
            @Valid @RequestBody MedicalSupplyRequestDto requestDto) {
        MedicalSupplyResponseDto createdSupply = medicalSupplyService.createMedicalSupply(requestDto);
        return new ResponseEntity<>(createdSupply, HttpStatus.CREATED);
    }

    @Operation(summary = "Lấy thông tin vật tư y tế theo ID",
            description = """
### Mô tả
Lấy thông tin chi tiết của một vật tư y tế dựa trên ID.
- **Phân quyền:** Yêu cầu vai trò `SchoolAdmin`, `StaffManager`, hoặc `MedicalStaff`.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm thấy vật tư y tế",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MedicalSupplyResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy vật tư y tế", content = @Content)
    })
    @GetMapping("/{supplyId}")
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager', 'MedicalStaff')")
    public ResponseEntity<MedicalSupplyResponseDto> getMedicalSupplyById(
            @Parameter(description = "ID của vật tư y tế cần lấy thông tin") @PathVariable Long supplyId) {
        MedicalSupplyResponseDto supply = medicalSupplyService.getMedicalSupplyById(supplyId);
        return ResponseEntity.ok(supply);
    }

    @Operation(summary = "Lấy lịch sử giao dịch của một vật tư y tế (phân trang)",
            description = """
### Mô tả
Trả về danh sách các giao dịch (nhập, xuất, điều chỉnh) của một vật tư y tế cụ thể.
- **Phân quyền:** Yêu cầu vai trò `SchoolAdmin`, `StaffManager`, hoặc `MedicalStaff`.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách giao dịch thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy vật tư y tế", content = @Content)
    })
    @GetMapping("/{supplyId}/transactions")
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager', 'MedicalStaff')")
    public ResponseEntity<Page<SupplyTransactionResponseDto>> getTransactionsForSupply(
            @Parameter(description = "ID của vật tư y tế cần xem lịch sử") @PathVariable Long supplyId,
            @Parameter(description = "Loại giao dịch (nhập, xuất, điều chỉnh). Nếu không cung cấp, sẽ lấy tất cả loại giao dịch.")
            @RequestParam(required = false)
            SupplyTransactionType transactionType,
            @Parameter(description = "Thông tin phân trang và sắp xếp")
            @ParameterObject Pageable pageable) {
        Page<SupplyTransactionResponseDto> transactions = medicalSupplyService.getTransactionsForSupply(supplyId, transactionType, pageable);
        return ResponseEntity.ok(transactions);
    }

    @Operation(summary = "Lấy danh sách tất cả vật tư y tế (phân trang)",
            description = """
### Mô tả
Trả về một trang danh sách các vật tư y tế trong hệ thống, hỗ trợ lọc và tìm kiếm.
- **Phân quyền:** Yêu cầu vai trò `SchoolAdmin`, `StaffManager`, hoặc `MedicalStaff`.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách vật tư y tế thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))), // Lưu ý: Schema cho Page<MedicalSupplyResponseDto>
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager', 'MedicalStaff')")
    public ResponseEntity<Page<MedicalSupplyResponseDto>> getAllMedicalSupplies(
            @Parameter(description = "Lọc theo tên vật tư (tìm kiếm một phần)") @RequestParam(required = false) String name,
            @Parameter(description = "Lọc theo danh mục vật tư") @RequestParam(required = false) String category,
            @Parameter(description = "Lọc theo trạng thái vật tư") @RequestParam(required = false) MedicalSupplyStatus status,
            @Parameter(description = "Lọc theo ngày hết hạn từ (YYYY-MM-DD)") @RequestParam(required = false) LocalDate expiredDateFrom,
            @Parameter(description = "Lọc theo ngày hết hạn đến (YYYY-MM-DD)") @RequestParam(required = false) LocalDate expiredDateTo,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        Page<MedicalSupplyResponseDto> suppliesPage = medicalSupplyService.getAllMedicalSupplies(
            name, category, status, expiredDateFrom, expiredDateTo, pageable);
        return ResponseEntity.ok(suppliesPage);
    }

    @Operation(summary = "Cập nhật thông tin (metadata) của một vật tư y tế",
            description = """
### Mô tả
Cập nhật các thông tin mô tả như tên, loại, đơn vị của một vật tư. Không dùng để thay đổi số lượng tồn kho.
- **Phân quyền:** Yêu cầu vai trò `MedicalStaff` hoặc `StaffManager`.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vật tư y tế được cập nhật thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MedicalSupplyResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ hoặc cố gắng cập nhật vật tư không hoạt động", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy vật tư y tế", content = @Content)
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
            description = """
### Mô tả
Thực hiện điều chỉnh số lượng tồn kho (nhập kho, xuất kho, kiểm kê).
- **Phân quyền:** Yêu cầu vai trò `MedicalStaff` hoặc `StaffManager`.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Điều chỉnh tồn kho thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MedicalSupplyResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ, không đủ tồn kho, hoặc cố gắng điều chỉnh vật tư không hoạt động", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy vật tư y tế", content = @Content)
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
            description = """
### Mô tả
Đánh dấu một vật tư y tế là không hoạt động (DISPOSED). Đây là hình thức xóa mềm.
- **Phân quyền:** Yêu cầu vai trò `StaffManager`.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vật tư y tế được xóa mềm thành công", content = @Content),
            @ApiResponse(responseCode = "400", description = "Vật tư đã ở trạng thái không hoạt động", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy vật tư y tế", content = @Content)
    })
    @PostMapping("/{supplyId}/dispose")
    @PreAuthorize("hasRole('StaffManager')") // Chỉ StaffManager mới có quyền xóa
    public ResponseEntity<Void> disposeMedicalSupply(
            @Parameter(description = "ID của vật tư y tế cần xóa mềm") @PathVariable Long supplyId) {
        medicalSupplyService.disposeMedicalSupply(supplyId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Xóa cứng một vật tư y tế",
            description = """
### Mô tả
Xóa hoàn toàn một vật tư y tế khỏi hệ thống.
- **Điều kiện:** Chỉ có thể thực hiện nếu vật tư không có giao dịch liên quan đến sự cố y tế.
- **Phân quyền:** Người tạo vật tư, `StaffManager`, hoặc `SchoolAdmin`.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vật tư y tế đã được xóa cứng thành công", content = @Content),
            @ApiResponse(responseCode = "400", description = "Không thể xóa cứng vật tư này vì nó có giao dịch liên quan đến sự cố y tế", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền xóa cứng vật tư này", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy vật tư y tế", content = @Content)
    })
    @DeleteMapping("/{supplyId}/delete")
    @PreAuthorize("hasAnyRole('MedicalStaff','StaffManager', 'SchoolAdmin')")
    public ResponseEntity<String> deleteMedicalSupply(
            @Parameter(description = "ID của vật tư y tế cần xóa cứng") @PathVariable Long supplyId) {
        medicalSupplyService.deleteMedicalSupply(supplyId);
        return ResponseEntity.ok().body("Vật tư y tế đã được xóa cứng thành công");
    }
}
