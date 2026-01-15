package com.course.dto;

import com.course.common.constant.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVo {
    private Long id;
    private String orderNo;
    private Long userId;
    private String username;    //用户名
    private Long courseId;
    private String courseTitle; //课程标题
    private BigDecimal price;
    private OrderStatus status;     //订单状态
    private LocalDateTime payTime;
    private LocalDateTime createTime;
}
