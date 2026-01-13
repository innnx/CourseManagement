package com.course.service;

import com.course.dto.CategoryRequest;
import com.course.entity.Category;

import java.util.List;

public interface CategoryService {
    //创建分类
    void creatCategory(CategoryRequest request);

    //更新分类
    void updateCategory(CategoryRequest request);

    //删除分类
    void deleteCategory(Long id);

    //获取所有分类
    List<Category> getAllCategories();

    //根据id获取分类
    Category getCategoryById(Long id);
}
