package com.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.common.result.Result;
import com.course.dto.CourseCreateRequest;
import com.course.dto.CourseQueryRequest;
import com.course.dto.CourseUpdateRequest;
import com.course.dto.CourseVo;
import com.course.service.CourseService;
import com.course.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "课程管理")
@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @Operation(summary = "创建课程（教师）")
    @PostMapping
    public Result<Void> createCourse(@Valid @RequestBody CourseCreateRequest request){
        Long teacherId = SecurityUtils.getCurrentUsrId();
        courseService.createCourse(request,teacherId);
        return Result.success();
    }

    @Operation(summary = "更新课程（教师）")
    @PutMapping
    public Result<Void> updateCourse(@Valid @RequestBody CourseUpdateRequest request){
        Long teacherId = SecurityUtils.getCurrentUsrId();
        courseService.updateCourse(request,teacherId);
        return Result.success();
    }

    @Operation(summary = "删除课程（教师）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCourse(@PathVariable Long id){
        Long teacherId = SecurityUtils.getCurrentUsrId();
        courseService.deleteCourse(id,teacherId);
        return Result.success();
    }

    @Operation(summary = "分页查询课程")
    @GetMapping("/page")
    public Result<Page<CourseVo>> queryCourse(CourseQueryRequest request){
        Page<CourseVo> courseVoPage = courseService.queryCourses(request);
        return Result.success(courseVoPage);
    }

    @Operation(summary = "获取课程详情")
    @GetMapping("/{id}")
    public Result<CourseVo> getCourseDetail(@PathVariable Long id){
        CourseVo courseDetail = courseService.getCourseDetail(id);
        return Result.success(courseDetail);
    }

    @Operation(summary = "上架/下架课程（教师）")
    @PutMapping("/{id}/status")
    public Result<Void> updateCourseStatus(@PathVariable Long id,@RequestParam Integer status){
        Long teacherId = SecurityUtils.getCurrentUsrId();
        courseService.updateCourseStatus(id,status,teacherId);
        return Result.success();
    }

}
