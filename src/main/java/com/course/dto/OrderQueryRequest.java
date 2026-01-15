package com.course.dto;

import com.course.common.constant.OrderStatus;
import lombok.Data;

@Data
public class OrderQueryRequest {
    private Long userId;            //用户id 学生查自己的订单
    private Long courseId;          //课程 id
    private OrderStatus status;     //订单状态
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
