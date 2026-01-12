package com.course.dto;

import lombok.Data;

@Data
public class CourseQueryRequest {
    private String keyword;     //关键词搜索（课程标题）
    private Long  categoryId;   //分类id
    private Long teacherId;     //教师id
    private Integer status;     //状态
    private Integer pageNum;    //当前页
    private Integer pageSize;   //页码大小
}
