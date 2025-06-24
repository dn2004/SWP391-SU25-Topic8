package com.fu.swp391.schoolhealthmanagementsystem.dto.supply;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Phản hồi cho việc xóa cứng vật tư y tế")
public class HardDeleteResponseDto {

    @Schema(description = "ID của vật tư y tế đã bị xóa")
    private Long deletedSupplyId;

    @Schema(description = "Tên của vật tư y tế đã bị xóa")
    private String deletedSupplyName;

    @Schema(description = "Thông báo kết quả")
    private String message;
}
