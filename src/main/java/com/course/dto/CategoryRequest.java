package com.course.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    private Long id;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private Integer sort = 0;
}
