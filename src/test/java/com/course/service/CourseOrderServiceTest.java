package com.course.service;

import com.course.BaseTest;
import com.course.common.constant.OrderStatus;
import com.course.common.exception.BusinessException;
import com.course.dto.OrderCreateRequest;
import com.course.entity.Category;
import com.course.entity.Course;
import com.course.entity.CourseOrder;
import com.course.entity.User;
import com.course.mapper.CategoryMapper;
import com.course.mapper.CourseMapper;
import com.course.mapper.CourseOrderMapper;
import com.course.mapper.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("订单服务测试")
class CourseOrderServiceTest extends BaseTest {
    
    @Autowired
    private CourseOrderService orderService;
    
    @Autowired
    private CourseOrderMapper orderMapper;
    
    @Autowired
    private CourseMapper courseMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private Long studentId;
    private Long teacherId;
    private Long categoryId;
    private Long courseId;
    
    @BeforeEach
    void setUp() {
        // 创建学生
        User student = new User();
        student.setUsername("teststudent");
        student.setPassword(passwordEncoder.encode("Test1234"));
        student.setNickname("测试学生");
        student.setRole("STUDENT");
        student.setStatus(1);
        userMapper.insert(student);
        studentId = student.getId();
        
        // 创建教师
        User teacher = new User();
        teacher.setUsername("testteacher2");
        teacher.setPassword(passwordEncoder.encode("Test1234"));
        teacher.setNickname("测试教师2");
        teacher.setRole("TEACHER");
        teacher.setStatus(1);
        userMapper.insert(teacher);
        teacherId = teacher.getId();
        
        // 创建分类
        Category category = new Category();
        category.setName("测试分类2");
        category.setSort(1);
        categoryMapper.insert(category);
        categoryId = Long.valueOf(category.getId());
        
        // 创建课程
        Course course = new Course();
        course.setTitle("测试课程2");
        course.setDescription("描述");
        course.setPrice(new BigDecimal("99.00"));
        course.setCategoryId(categoryId);
        course.setTeacherId(teacherId);
        course.setStudentCount(0);
        course.setStatus(1);
        courseMapper.insert(course);
        courseId = course.getId();
    }
    
    @AfterEach
    void tearDown() {
        // 清理订单
        orderMapper.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseOrder>()
                .eq(CourseOrder::getUserId, studentId)
        );
        
        if (courseId != null) courseMapper.deleteById(courseId);
        if (categoryId != null) categoryMapper.deleteById(categoryId);
        if (studentId != null) userMapper.deleteById(studentId);
        if (teacherId != null) userMapper.deleteById(teacherId);
    }
    
    @Test
    @DisplayName("创建订单 - 成功")
    void testCreateOrderSuccess() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCourseId(courseId);
        
        String orderNo = orderService.creatOrder(request, studentId);
        
        assertNotNull(orderNo, "订单号不应为空");
        assertTrue(orderNo.startsWith("ORD"), "订单号应该以ORD开头");
        
        // 验证订单
        CourseOrder order = orderMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseOrder>()
                .eq(CourseOrder::getOrderNo, orderNo)
        );
        
        assertNotNull(order);
        assertEquals(studentId, order.getUserId());
        assertEquals(courseId, order.getCourseId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }
    
    @Test
    @DisplayName("创建订单 - 课程不存在")
    void testCreateOrderCourseNotExists() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCourseId(99999L);
        
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> orderService.creatOrder(request, studentId)
        );
        
        assertEquals("课程不存在", exception.getMessage());
    }
    
    @Test
    @DisplayName("支付订单 - 成功")
    void testPayOrderSuccess() {
        // 先创建订单
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCourseId(courseId);
        String orderNo = orderService.creatOrder(request, studentId);
        
        // 获取课程原始学生数
        Course courseBefore = courseMapper.selectById(courseId);
        int studentCountBefore = courseBefore.getStudentCount();
        
        // 支付订单
        orderService.payOrder(orderNo, studentId);
        
        // 验证订单状态
        CourseOrder order = orderMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseOrder>()
                .eq(CourseOrder::getOrderNo, orderNo)
        );
        
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertNotNull(order.getPayTime());
        
        // 验证课程学生数增加
        Course courseAfter = courseMapper.selectById(courseId);
        assertEquals(studentCountBefore + 1, courseAfter.getStudentCount());
    }
    
    @Test
    @DisplayName("重复购买 - 应该失败")
    void testCreateOrderDuplicate() {
        // 第一次购买
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCourseId(courseId);
        String orderNo = orderService.creatOrder(request, studentId);
        orderService.payOrder(orderNo, studentId);
        
        // 第二次购买
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> orderService.creatOrder(request, studentId)
        );
        
        assertEquals("您已购买过该课程", exception.getMessage());
    }
    
    @Test
    @DisplayName("取消订单 - 成功")
    void testCancelOrderSuccess() {
        // 创建订单
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCourseId(courseId);
        String orderNo = orderService.creatOrder(request, studentId);
        
        // 取消订单
        orderService.cancelOrder(orderNo, studentId);
        
        // 验证订单状态
        CourseOrder order = orderMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseOrder>()
                .eq(CourseOrder::getOrderNo, orderNo)
        );
        
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }
    
    @Test
    @DisplayName("检查是否购买 - 未购买")
    void testHasPurchasedFalse() {
        boolean purchased = orderService.hasPurchased(studentId, courseId);
        assertFalse(purchased);
    }
    
    @Test
    @DisplayName("检查是否购买 - 已购买")
    void testHasPurchasedTrue() {
        // 创建并支付订单
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCourseId(courseId);
        String orderNo = orderService.creatOrder(request, studentId);
        orderService.payOrder(orderNo, studentId);
        
        // 检查是否购买
        boolean purchased = orderService.hasPurchased(studentId, courseId);
        assertTrue(purchased);
    }
}