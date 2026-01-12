package com.course.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseVo {
    private Long id;
    private String title;
    private String description;
    private String coverImage;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName;  // 分类名称
    private Long teacherId;
    private String teacherName;   // 教师姓名
    private Integer studentCount;
    private Integer status; //1上架 2下架
    private LocalDateTime createTime;
}
