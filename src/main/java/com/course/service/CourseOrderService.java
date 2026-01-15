package com.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.dto.OrderCreateRequest;
import com.course.dto.OrderQueryRequest;
import com.course.dto.OrderVo;

public interface CourseOrderService {
    //创建订单
    String creatOrder(OrderCreateRequest request,Long userId);

    //支付订单
    void payOrder(String orderNo,Long userId);

    //取消订单
    void cancelOrder(String orderNo,Long userId);

    //查询我的订单
    Page<OrderVo> queryMyOrders(OrderQueryRequest request,Long userId);

    //查询订单详情
    OrderVo getOrderDetail(String orderNo,Long userId);

    //检查用户是否购买过课程
    boolean hasPurchased(Long userId,Long courseId);
}
