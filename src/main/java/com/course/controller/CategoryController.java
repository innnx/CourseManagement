package com.course.controller;

import com.course.common.result.Result;
import com.course.dto.CategoryRequest;
import com.course.entity.Category;
import com.course.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name= "分类管理")
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(summary = "获取所有分类")
    @GetMapping("/list")
    public Result<List<Category>> getAllCategories(){
        List<Category> allCategories = categoryService.getAllCategories();
        return Result.success(allCategories);
    }

    @Operation(summary = "创建分类（仅限管理员）")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> createCategory(@Valid @RequestBody CategoryRequest request){
        categoryService.creatCategory(request);
        return Result.success();
    }

    @Operation(summary = "更新分类（仅限管理员）")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateCategory(@Valid @RequestBody CategoryRequest request){
        categoryService.updateCategory(request);
        return Result.success();
    }

    @Operation(summary = "删除分类（仅限管理员）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return Result.success();
    }

}
