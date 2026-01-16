package com.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.BaseTest;
import com.course.common.exception.BusinessException;
import com.course.dto.CourseCreateRequest;
import com.course.dto.CourseQueryRequest;
import com.course.dto.CourseUpdateRequest;
import com.course.dto.CourseVo;
import com.course.entity.Category;
import com.course.entity.Course;
import com.course.entity.User;
import com.course.mapper.CategoryMapper;
import com.course.mapper.CourseMapper;
import com.course.mapper.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("课程服务测试")
public class CourseServiceTest extends BaseTest {
    @Autowired
    private CourseService courseService;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long teacherId;
    private Integer categoryId;
    private Long courseId;

    @BeforeEach
    void setUp() {
        //创建测试教师
        User teacher = new User();
        teacher.setUsername("testteacher");
        teacher.setPassword(passwordEncoder.encode("Test1234"));
        teacher.setNickname("测试教师");
        teacher.setRole("TEACHER");
        teacher.setStatus(1);
        userMapper.insert(teacher);
        teacherId = teacher.getId();

        //创建测试分类
        Category category = new Category();
        category.setName("测试分类");
        category.setSort(1);
        categoryMapper.insert(category);
        categoryId = category.getId();
    }

    @AfterEach
    void tearDown(){
        //清理测试数据
        if (courseId != null){
            courseMapper.deleteById(courseId);
        }
        if (categoryId != null){
            categoryMapper.deleteById(categoryId);
        }
        if (teacherId != null){
            userMapper.deleteById(teacherId);
        }
    }

    @Test
    @DisplayName("创建课程-成功")
    void testCreateCourseSuccess(){
        //准备请求数据
        CourseCreateRequest request = new CourseCreateRequest();
        request.setTitle("测试课程");
        request.setDescription("这是一个测试课程");
        request.setPrice(new BigDecimal("99"));
        request.setCategoryId(Long.valueOf(categoryId));
        //创建课程
        courseService.createCourse(request,teacherId);
        //验证课程是否创建成功
        Course course = courseMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course>()
                        .eq(Course::getTitle,"测试课程")
        );
        Integer iCategroyId = Math.toIntExact(course.getCategoryId());
        assertNotNull(course,"课程应该被成功创建");
        assertEquals("测试课程",course.getTitle());
        assertEquals(teacherId,course.getTeacherId());
        assertEquals(categoryId,iCategroyId);
        assertEquals(0,new BigDecimal("99").compareTo(course.getPrice()));
        assertEquals(1,course.getStatus());

         courseId = course.getId();
    }

    @Test
    @DisplayName("创建课程-分类不存在")
    void testCreateCourseWithInvalidCategory(){
        CourseCreateRequest request = new CourseCreateRequest();
        request.setTitle("测试课程");
        request.setDescription("这是一个测试课程");
        request.setPrice(new BigDecimal("99"));
        request.setCategoryId(9999L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                ()->courseService.createCourse(request,teacherId)
        );
        assertEquals("分类不存在",exception.getMessage());
    }

    @Test
    @DisplayName("分页查询课程")
    void testQueryCourse(){
        //创建几个测试课程
        for (int i =1;i<=3;i++){
            CourseCreateRequest request = new CourseCreateRequest();
            request.setTitle("测试课程"+i);
            request.setDescription("这是一个测试课程"+i);
            request.setPrice(new BigDecimal("99"));
            request.setCategoryId(Long.valueOf(categoryId));
            courseService.createCourse(request,teacherId);
        }
        //查询课程
        CourseQueryRequest queryRequest = new CourseQueryRequest();
        queryRequest.setPageNum(1);
        queryRequest.setPageSize(10);
        Page<CourseVo> page = courseService.queryCourses(queryRequest);
        assertNotNull(page);
        assertTrue(page.getRecords().size() >= 3,"至少应该有3条记录");
    }

    @Test
    @DisplayName("更新课程 - 成功")
    void testUpdateCourseSuccess() {
        // 先创建课程
        CourseCreateRequest createRequest = new CourseCreateRequest();
        createRequest.setTitle("原始标题");
        createRequest.setDescription("原始描述");
        createRequest.setPrice(new BigDecimal("90"));
        createRequest.setCategoryId(Long.valueOf(categoryId));
        courseService.createCourse(createRequest, teacherId);

        Course course = courseMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course>()
                        .eq(Course::getTitle, "原始标题")
        );
        courseId = course.getId();

        // 更新课程
        CourseUpdateRequest updateRequest = new CourseUpdateRequest();
        updateRequest.setId(courseId);
        updateRequest.setTitle("更新后的标题");
        updateRequest.setPrice(new BigDecimal("199.00"));

        courseService.updateCourse(updateRequest, teacherId);

        // 验证更新结果
        Course updatedCourse = courseMapper.selectById(courseId);
        assertEquals("更新后的标题", updatedCourse.getTitle());
        assertEquals(0, new BigDecimal("199.00").compareTo(updatedCourse.getPrice()));
    }

    @Test
    @DisplayName("删除课程 - 非课程所有者")
    void testDeleteCourseByNonOwner() {
        // 创建课程
        CourseCreateRequest createRequest = new CourseCreateRequest();
        createRequest.setTitle("测试课程");
        createRequest.setDescription("描述");
        createRequest.setPrice(new BigDecimal("99"));
        createRequest.setCategoryId(Long.valueOf(categoryId));
        courseService.createCourse(createRequest, teacherId);

        Course course = courseMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course>()
                        .eq(Course::getTitle, "测试课程")
        );
        courseId = course.getId();

        // 尝试用其他用户ID删除
        Long otherTeacherId = 99999L;

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> courseService.deleteCourse(courseId, otherTeacherId)
        );

        assertEquals("无权限删除", exception.getMessage());
    }

}
