package com.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.course.common.result.Result;
import com.course.dto.OrderCreateRequest;
import com.course.dto.OrderQueryRequest;
import com.course.dto.OrderVo;
import com.course.service.CourseOrderService;
import com.course.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订单管理")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class CourseOrderController {
    private final CourseOrderService courseOrderService;

    @Operation(summary = "创建订单")
    @PostMapping
    public Result<String> createOrder(@Valid @RequestBody OrderCreateRequest request){
        Long usrId = SecurityUtils.getCurrentUsrId();
        String orderNo = courseOrderService.creatOrder(request, usrId);
        return Result.success(orderNo);
    }

    @Operation(summary = "支付订单")
    @PutMapping("/{orderNo}/pay")
    public Result<Void> payOrder(@PathVariable String orderNo){
        Long usrId = SecurityUtils.getCurrentUsrId();
        courseOrderService.payOrder(orderNo,usrId);
        return Result.success();
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{orderNo}/cancel")
    public Result<Void> cancelOrder(@PathVariable String orderNo){
        Long usrId = SecurityUtils.getCurrentUsrId();
        courseOrderService.cancelOrder(orderNo,usrId);
        return Result.success();
    }

    @Operation(summary = "查询我的订单")
    @GetMapping("/my")
    public Result<Page<OrderVo>> queryMyOrder(OrderQueryRequest request){
        Long usrId = SecurityUtils.getCurrentUsrId();
        Page<OrderVo> voPage = courseOrderService.queryMyOrders(request, usrId);
        return Result.success(voPage);
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{orderNo}")
    public Result<OrderVo> getOrderDetail(@PathVariable String orderNo){
        Long usrId = SecurityUtils.getCurrentUsrId();
        OrderVo orderDetail = courseOrderService.getOrderDetail(orderNo, usrId);
        return Result.success(orderDetail);
    }

    @Operation(summary = "检查是否购买过课程")
    @GetMapping("/check/{courseId}")
    public Result<Boolean> hasPurchased(@PathVariable Long courseId){
        Long usrId = SecurityUtils.getCurrentUsrId();
        boolean b = courseOrderService.hasPurchased(usrId, courseId);
        return Result.success(b);
    }

}
