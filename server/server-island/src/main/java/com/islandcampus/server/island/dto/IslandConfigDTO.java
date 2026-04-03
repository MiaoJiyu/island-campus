package com.islandcampus.server.island.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "灵动岛配置详情DTO")
public class IslandConfigDTO {

    @Schema(description = "位置: top/bottom/left/right/center")
    private String position;

    @Schema(description = "高度(px)")
    private Integer height;

    @Schema(description = "背景颜色")
    private String backgroundColor;

    @Schema(description = "文字颜色")
    private String textColor;

    @Schema(description = "圆角半径(px)")
    private Integer borderRadius;

    @Schema(description = "是否显示Logo")
    private Boolean showLogo;

    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "是否显示日期时间")
    private Boolean showDateTime;

    @Schema(description = "日期时间格式")
    private String dateTimeFormat;

    @Schema(description = "是否显示当前课程")
    private Boolean showCurrentCourse;

    @Schema(description = "是否显示天气")
    private Boolean showWeather;

    @Schema(description = "是否显示跑马灯")
    private Boolean showMarquee;

    @Schema(description = "是否显示模式图标")
    private Boolean showModeIcon;

    @Schema(description = "是否显示健康状态点")
    private Boolean showHealthDot;

    @Schema(description = "是否显示消息红点")
    private Boolean showMessageBadge;

    @Schema(description = "全屏时自动隐藏")
    private Boolean autoHideFullscreen;
}
