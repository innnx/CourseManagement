package com.course.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.common.constant.OrderStatus;
import com.course.common.exception.BusinessException;
import com.course.dto.OrderCreateRequest;
import com.course.dto.OrderQueryRequest;
import com.course.dto.OrderVo;
import com.course.entity.Course;
import com.course.entity.CourseOrder;
import com.course.entity.User;
import com.course.mapper.CourseMapper;
import com.course.mapper.CourseOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseOrderServiceImpl implements CourseOrderService{

    private final CourseMapper courseMapper;
    private final CourseOrderMapper orderMapper;
    private final UserService userService;
    private final RedisTemplate<String,Object> redisTemplate;

    private static final String ORDER_LOCK_KEY = "order:lock:";

    //创建订单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String creatOrder(OrderCreateRequest request, Long userId) {
        //获取课程信息
        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null){
            throw new BusinessException("课程不存在");
        }
        if (course.getStatus() == 0){
            throw new BusinessException("课程已下架");
        }
        //检查是否已购买
        if (hasPurchased(userId,course.getId())){
            throw new BusinessException("您已购买过该课程");
        }
        //使用 Redis分布式锁防止重复下单
        String lockKey = ORDER_LOCK_KEY + userId + ":" + request.getCourseId();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey,"1",10, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)){
            throw new BusinessException("订单创建中，请勿重复提交");
        }
        try {
            //生成订单号
            String orderNo = generateOrderNo();
            //创建订单
            CourseOrder order = new CourseOrder();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setCourseId(request.getCourseId());
            order.setPrice(course.getPrice());
            order.setStatus(OrderStatus.PENDING);
            orderMapper.insert(order);
            log.info("创建订单成功：orderNo={},userId={},courseId={}",orderNo,userId,request.getCourseId());
            return orderNo;
        }finally {
            //释放锁
            redisTemplate.delete(lockKey);
        }
    }

    //支付订单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(String orderNo, Long userId) {
        //查询订单
        CourseOrder order = getOrderByOrderNo(orderNo);
        if (order == null){
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)){
            throw new BusinessException("无权限支付该订单");
        }
        if (order.getStatus() != OrderStatus.PENDING){
            throw new BusinessException("订单状态不正确：当前状态："+order.getStatus());
        }
        //更新订单状态
        LambdaUpdateWrapper<CourseOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CourseOrder::getOrderNo,orderNo)
                .eq(CourseOrder::getStatus,OrderStatus.PENDING) //乐观锁
                .set(CourseOrder::getStatus,OrderStatus.PAID)
                .set(CourseOrder::getPayTime, LocalDateTime.now());
        int updated = orderMapper.update(null,updateWrapper);
        if (updated == 0){
            throw new BusinessException("当前订单状态已变更，请刷新后重试");
        }
        //增加课程学生数
        Course course = courseMapper.selectById(order.getCourseId());
        course.setStudentCount(course.getStudentCount()+1);
        courseMapper.updateById(course);
        log.info("订单支付成功：orderNo={},userId={}",orderNo,userId);
    }

    //取消订单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo, Long userId) {
        CourseOrder order = getOrderByOrderNo(orderNo);
        if (order == null){
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)){
            throw new BusinessException("无权限操作该订单");
        }
        if (order.getStatus() != OrderStatus.PENDING){
            throw new BusinessException("只能取消待支付的订单");
        }
        //更新订单状态
        LambdaUpdateWrapper<CourseOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CourseOrder::getOrderNo,orderNo)
                .eq(CourseOrder::getStatus,OrderStatus.PENDING)     //乐观锁
                .set(CourseOrder::getStatus,OrderStatus.CANCELLED);
        int updated = orderMapper.update(null, updateWrapper);
        if (updated == 0){
            throw new BusinessException("取消失败，当前状态已变更");
        }
        log.info("取消订单成功：orderNo={},userId={}",orderNo,userId);
    }

    //查询我的订单
    @Override
    public Page<OrderVo> queryMyOrders(OrderQueryRequest request, Long userId) {
        Page<CourseOrder> page = new Page<>(request.getPageNum(),request.getPageSize());
        LambdaQueryWrapper<CourseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseOrder::getUserId,userId);      // 只能查询自己的订单
        //匹配课程
        if (request.getCourseId() != null){
            wrapper.eq(CourseOrder::getCourseId,request.getCourseId());
        }
        //匹配状态
        if (request.getStatus() != null){
            wrapper.eq(CourseOrder::getStatus,request.getStatus());
        }
        //创建时间倒叙
        wrapper.orderByDesc(CourseOrder::getCreateTime);
        Page<CourseOrder> courseOrderPage = orderMapper.selectPage(page, wrapper);
        //转换VO
        Page<OrderVo> voPage = new Page<>();
        BeanUtil.copyProperties(courseOrderPage,voPage,"records");
        voPage.setRecords(courseOrderPage.getRecords().stream().map(this::convertToVo).toList());
        return voPage;
    }

    //获取订单详情
    @Override
    public OrderVo getOrderDetail(String orderNo, Long userId) {
        CourseOrder order = getOrderByOrderNo(orderNo);
        if (order == null){
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)){
            throw new BusinessException("无权限查看该订单");
        }
        return convertToVo(order);
    }

    //检查是否购买过该课程
    @Override
    public boolean hasPurchased(Long userId, Long courseId) {
        LambdaQueryWrapper<CourseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseOrder::getCourseId,courseId)
                .eq(CourseOrder::getUserId,userId)
                .eq(CourseOrder::getStatus, OrderStatus.PAID);
        return orderMapper.selectCount(wrapper) > 0;
    }

    //生成订单号
    private String generateOrderNo() {
        //使用 hutool生成雪花id
        return "ORD" + IdUtil.getSnowflakeNextIdStr();
    }

    //根据单号查询订单
    private CourseOrder getOrderByOrderNo(String orderNo) {
        LambdaQueryWrapper<CourseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseOrder::getOrderNo,orderNo);
        return orderMapper.selectOne(wrapper);
    }

    //转换VO
    private OrderVo convertToVo(CourseOrder order){
        OrderVo orderVo = new OrderVo();
        BeanUtil.copyProperties(order,orderVo);
        //填充课程信息
        Course course = courseMapper.selectById(order.getCourseId());
        if (course != null){
            orderVo.setCourseTitle(course.getTitle());
        }
        //填充用户信息
        User user = userService.getUserById(order.getUserId());
        if (user != null){
            orderVo.setUsername(user.getUsername());
        }
        //填充状态名称
        orderVo.setStatus(order.getStatus());
        return orderVo;
    }
}
