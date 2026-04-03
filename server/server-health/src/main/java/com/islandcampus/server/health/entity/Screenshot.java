package com.islandcampus.server.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.islandcampus.server.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("screenshot")
public class Screenshot extends BaseEntity {
    private Long computerId;
    private String imageUrl;
    private Long imageSizeKb;
    private Integer width;
    private Integer height;
    private Integer blurred = 0; // 0=否 1=是(隐私保护)
    private LocalDateTime capturedAt;
}
