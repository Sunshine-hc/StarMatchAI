package com.hc.starmatchai.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "分页查询基础参数")
public class PageQueryDTO {

    @Schema(description = "页码", example = "1")
    @NotNull(message = "页码必传")
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    @NotNull(message = "每页大小必传")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer size = 10;

    @Schema(description = "排序字段")
    private String orderBy;

    @Schema(description = "排序方式(asc/desc)", example = "desc")
    private String orderDirection = "desc";
}