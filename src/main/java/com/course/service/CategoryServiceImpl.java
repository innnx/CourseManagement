package com.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.course.common.exception.BusinessException;
import com.course.dto.CategoryRequest;
import com.course.entity.Category;
import com.course.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService{

    private final CategoryMapper categoryMapper;

    //创建分类
    @Override
    public void creatCategory(CategoryRequest request) {
        //检查分类名称是否已存在
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName,request.getName());
        if (categoryMapper.selectCount(wrapper)>0){
            throw new BusinessException("分类已存在");
        }
        Category category = new Category();
        category.setName(request.getName());
        category.setSort(request.getSort());

        categoryMapper.insert(category);
        log.info("创建分类成功：{}",request.getName());
    }

    //更新分类
    @Override
    public void updateCategory(CategoryRequest request) {
        Category categoryById = getCategoryById(request.getId());
        if (categoryById == null){
            throw new BusinessException("分类不存在");
        }
        //如果修改了名称，检查是否重复
        if (!categoryById.getName().equals(request.getName())){
            LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Category::getName,request.getName());
            if (categoryMapper.selectCount(wrapper)>0){
                throw new BusinessException("分类名称已存在");
            }
        }
        categoryById.setName(request.getName());
        categoryById.setSort(request.getSort());

        categoryMapper.updateById(categoryById);
        log.info("更新分类成功：{}",request.getName());
    }

    //删除分类
    @Override
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        if (category == null){
            throw new BusinessException("分类不存在");
        }
        categoryMapper.deleteById(id);
        log.info("删除分类成功ID：{}",id);
    }

    //获取所有分类
    @Override
    public List<Category> getAllCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        return categoryMapper.selectList(wrapper);
    }

    //根据id获取分类
    @Override
    public Category getCategoryById(Long id) {
        return categoryMapper.selectById(id);
    }
}
