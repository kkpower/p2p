package com.bjpowernode.p2p.timer;

import com.bjpowernode.p2p.model.loan.IncomeRecord;
import com.bjpowernode.p2p.service.loan.IncomeRecordService;
import com.bjpowernode.p2p.service.loan.RechargeRecordService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ClassName:TimerManager
 * Package:com.bjpowernode.p2p.timer
 * Description:
 *
 * @date:2019/10/26 12:19
 * @author:guoxin
 */
@Component
public class TimerManager {

    private Logger logger = LogManager.getLogger(TimerManager.class);

    @Autowired
    private IncomeRecordService incomeRecordService;

    @Autowired
    private RechargeRecordService rechargeRecordService;

//    @Scheduled(cron = "0/5 * * * * ?")
    public void generateIncomePlan() throws Exception {
        logger.info("-----------生成收益计划开始------------");

        incomeRecordService.generateIncomePlan();

        logger.info("-----------生成收益计划结束------------");
    }


//    @Scheduled(cron = "0/5 * * * * ?")
    public void generateIncomeBack() throws Exception {
        logger.info("-----------收益返还开始------------");

        incomeRecordService.generateIncomeBack();

        logger.info("-----------收益返还结束------------");

    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void dealRechargeRecord() throws Exception {
        logger.info("-----------处理掉单开始------------");

        rechargeRecordService.dealRechargeRecord();

        logger.info("-----------处理掉单结束------------");

    }
}
