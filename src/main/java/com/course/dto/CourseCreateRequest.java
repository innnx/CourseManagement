package com.course.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseCreateRequest {
    @NotBlank(message = "课程标题不能为空")
    @Size(max = 100,message = "标题长度不能超过100")
    private String title;

    @NotBlank(message = "课程描述不能为空")
    private String description;

    private String coverImage;

    @NotBlank(message = "价格不能为空")
    @DecimalMin(value = "0.00",message = "价格不能为负数")
    private BigDecimal price;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
}
