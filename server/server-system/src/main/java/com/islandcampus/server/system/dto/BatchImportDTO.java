package com.islandcampus.server.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "批量导入DTO")
public class BatchImportDTO {

    @Schema(description = "Excel文件")
    private MultipartFile file;
}
