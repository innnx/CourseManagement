package com.course.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.common.exception.BusinessException;
import com.course.dto.CourseCreateRequest;
import com.course.dto.CourseQueryRequest;
import com.course.dto.CourseUpdateRequest;
import com.course.dto.CourseVo;
import com.course.entity.Category;
import com.course.entity.Course;
import com.course.entity.User;
import com.course.mapper.CourseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService{

    private final CourseMapper courseMapper;
    private final CategoryService categoryService;
    private final UserService userService;
    private final RedisTemplate<String,Object> redisTemplate;

    private static final String COURSE_CACHE_KEY = "course:detail:";

    //创建课程
    @Override
    public void createCourse(CourseCreateRequest request, Long teacherId) {
        //验证分类是否存在
        Category category = categoryService.getCategoryById(request.getCategoryId());
        if (category == null){
            throw new BusinessException("分类不存在");
        }
        Course course = new Course();
        //hutool工具类提供属性复制方法
        BeanUtil.copyProperties(request,course);
        course.setTeacherId(teacherId);
        course.setStudentCount(0);
        course.setStatus(1);    //默认上架

        courseMapper.insert(course);
        log.info("创建课程成功：{},教师id：{}",request.getTitle(),teacherId);
    }

    /*
    @CacheEvict(value = "course", key = "#request.id")
    value = "course"：
    含义：指定缓存的名字（Cache Name），相当于一个“文件夹”或“命名空间”。
    作用：将所有课程相关的缓存都统一存放在名为 course 的分区下，方便批量管理。
    key = "#request.id"：
    含义：指定具体要删除哪一条缓存。
    底层语法：使用 SpEL (Spring Expression Language) 表达式。
    逻辑：#request 指向方法参数中的 request 对象，.id 则是读取该对象里的 id 属性。最终生成的 Key 可能是 course::101。
    */
    //更新课程
    @Override
    @CacheEvict(value = "course",key = "#request.id")
    public void updateCourse(CourseUpdateRequest request, Long teacherId) {
        //验证课程是否存在
        Course course = courseMapper.selectById(request.getId());
        if (course == null){
            throw new BusinessException("课程不存在");
        }
        //只有课程的教师本人才能修改
        if (!course.getTeacherId().equals(teacherId)){
            throw new BusinessException("无权限修改课程");
        }
        //如果修改了分类，验证分类是否存在
        if (request.getCategoryId() != null){
            Category category = categoryService.getCategoryById(request.getCategoryId());
            if (category == null){
                throw new BusinessException("分类不存在");
            }
        }
        BeanUtil.copyProperties(request,course,"teacherId","studentCount");
        courseMapper.updateById(course);
        //删除Redis缓存
        redisTemplate.delete(COURSE_CACHE_KEY + request.getId());
        log.info("更新课程成功：ID={}",request.getId());
    }

    //删除课程
    @Override
    @CacheEvict(value = "course",key = "#id")
    public void deleteCourse(Long id, Long teacherId) {
        Course course = courseMapper.selectById(id);
        if (course == null){
            throw new BusinessException("课程不存在");
        }
        if (!course.getTeacherId().equals(teacherId)){
            throw new BusinessException("无权限删除");
        }
        courseMapper.deleteById(id);
        redisTemplate.delete(COURSE_CACHE_KEY+id);
        log.info("删除课程成功：ID=",id);
    }

    //分页查询课程
    @Override
    public Page<CourseVo> queryCourses(CourseQueryRequest request) {
        // 1. 创建分页对象，传入前端传来的页码和大小
        Page<Course> page = new Page<>(request.getPageNum(), request.getPageSize());
        // 2. 使用 LambdaQueryWrapper 构建过滤条件
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        //关键词搜索
        if (StringUtils.hasText(request.getKeyword())){
            wrapper.like(Course::getTitle,request.getKeyword());
        }
        //分类筛选
        if (request.getCategoryId()!=null){
            wrapper.eq(Course::getCategoryId,request.getCategoryId());
        }
        //教师筛选
        if (request.getTeacherId()!=null){
            wrapper.eq(Course::getTeacherId,request.getTeacherId());
        }
        //状态筛选
        if (request.getStatus()!=null){
            wrapper.eq(Course::getStatus,request.getStatus());
        }
        //按创建时间倒叙
        wrapper.orderByDesc(Course::getCreateTime);
        // 3. 执行查询，结果会自动回填到 page 对象中
        Page<Course> coursePage = courseMapper.selectPage(page, wrapper);
        //转换为 VO
        Page<CourseVo> voPage = new Page<>();
        //将携带查询结构的page对象复制到vo对象，忽略查询到的记录
        BeanUtil.copyProperties(coursePage,voPage,"records");
        voPage.setRecords(coursePage.getRecords().stream().map(this::convertToVo).toList());
        return voPage;
    }

    //转换VO
    private CourseVo convertToVo(Course course) {
        //创建vo视图对象
        CourseVo courseVo = new CourseVo();
        //将当前课程的属性复制到新的vo对象
        BeanUtil.copyProperties(course,courseVo);
        //填充额外的分类名称
        Category category = categoryService.getCategoryById(course.getCategoryId());
        if (category != null){
            courseVo.setCategoryName(category.getName());
        }
        //填充额外的教师名称
        User teacher = userService.getUserById(course.getTeacherId());
        if (teacher != null){
            courseVo.setTeacherName(teacher.getNickname());
        }
        return courseVo;
    }

    //获取课程详情
    @Override
    //@Cacheable(value = "course",key = "#id")
    public CourseVo getCourseDetail(Long id) {
        //先从redis 获取缓存
        String cacheKey = COURSE_CACHE_KEY + id;
        CourseVo courseVo = (CourseVo)redisTemplate.opsForValue().get(cacheKey);
        if (courseVo != null){
            log.debug("从Redis缓存获取课程详情：ID={}",id);
            return courseVo;
        }
        //缓存未命中，从数据库查询
        Course course = courseMapper.selectById(id);
        if (course == null){
            throw new BusinessException("课程不存在");
        }
        CourseVo courseVo1 = convertToVo(course);
        //存入redis 缓存，设置1小时过期
        redisTemplate.opsForValue().set(cacheKey,courseVo1,1, TimeUnit.HOURS);
        return courseVo1;
    }

    //上架/下架课程
    @Override
    public void updateCourseStatus(Long id, Integer status, Long teacherId) {
        Course course = courseMapper.selectById(id);
        if (course == null){
            throw new BusinessException("课程不存在");
        }
        if (!course.getTeacherId().equals(teacherId)){
            throw new BusinessException("无权限修改");
        }
        course.setStatus(status);
        courseMapper.updateById(course);
        //更新后删除缓存
        redisTemplate.delete(COURSE_CACHE_KEY + id);
        log.info("更新状态成功：ID={},Status={}",id,status);
    }
}
