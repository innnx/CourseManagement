package com.course.common.task;

import com.course.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsTask {

    private final StatisticsService statisticsService;

    /*
      每天凌晨1点执行订单统计
      cron表达式: 秒 分 时 日 月 周
    */
    @Scheduled(cron = "0 0 1 * * ?")
    public void dailyStatistics(){
        log.info("开始执行定时任务:订单统计");
        try {
            statisticsService.statisticsDailyOrders();
            log.info("每日订单统计完成");
        } catch (Exception e) {
            log.error("每日订单任务执行失败",e);
        }
    }

    /**
     * 测试用: 每分钟执行一次
     */
    /*@Scheduled(cron = "0 * * * * ?")
    public void testTask() {
        log.info("测试定时任务执行...");
        statisticsService.statisticsDailyOrders();
    }*/
}
