package com.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.course.common.constant.OrderStatus;
import com.course.entity.CourseOrder;
import com.course.mapper.CourseOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService{

    private final CourseOrderMapper orderMapper;

    @Override
    public void statisticsDailyOrders() {
        //统计昨天的订单数据
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startTime = LocalDateTime.of(yesterday, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(yesterday,LocalTime.MAX);

        //查询昨天所有订单
        LambdaQueryWrapper<CourseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(CourseOrder::getCreateTime,startTime,endTime);
        List<CourseOrder> courseOrders = orderMapper.selectList(wrapper);

        //统计订单总数
        int orderNum = courseOrders.size();

        //统计已支付订单数
        long paidOrders = courseOrders.stream()
                .filter(o->o.getStatus() == OrderStatus.PAID)
                .count();

        //统计总收入
        BigDecimal totalIncome = courseOrders.stream()
                .filter(o->o.getStatus() == OrderStatus.PAID)
                .map(CourseOrder::getPrice)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        log.info("======每日订单统计======");
        log.info("日期：{}",yesterday);
        log.info("总订单数:{}",orderNum);
        log.info("已支付订单数:{}",paidOrders);
        log.info("总收入:{}元",totalIncome);

    }
}
