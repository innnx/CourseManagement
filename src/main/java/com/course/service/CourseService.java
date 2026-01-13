package com.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.dto.CourseCreateRequest;
import com.course.dto.CourseQueryRequest;
import com.course.dto.CourseUpdateRequest;
import com.course.dto.CourseVo;

public interface CourseService {
    //创建课程
    void createCourse(CourseCreateRequest request,Long teacherId);

    //更新课程
    void updateCourse(CourseUpdateRequest request,Long teacherId);

    //删除课程
    void deleteCourse(Long id,Long teacherId);

    //分页查询课程
    Page<CourseVo> queryCourses(CourseQueryRequest request);

    //获取课程详情
    CourseVo getCourseDetail(Long id);

    //上/下架课程
    void updateCourseStatus(Long id,Integer status,Long teacherId);
}
