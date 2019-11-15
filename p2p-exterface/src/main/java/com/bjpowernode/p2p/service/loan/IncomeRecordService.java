package com.bjpowernode.p2p.service.loan;

/**
 * ClassName:IncomeRecordService
 * Package:com.bjpowernode.p2p.service.loan
 * Description:
 *
 * @date:2019/10/26 14:32
 * @author:guoxin
 */
public interface IncomeRecordService {

    /**
     * 生成收益计划
     */
    void generateIncomePlan() throws Exception;

    /**
     * 收益返还
     */
    void generateIncomeBack() throws Exception;

}
