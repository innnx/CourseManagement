package com.course.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseUpdateRequest {
    @NotNull(message = "课程id不能为空")
    private Long id;

    @Size(max = 100,message = "课程标题不能超过100")
    private String title;

    private String description;

    private String coverImage;

    @DecimalMin(value = "0.00",message = "价格不能为负数")
    private BigDecimal price;

    private Long categoryId;

    private Integer status; //1上架 0下架
}
