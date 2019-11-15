package com.bjpowernode.p2p.service.loan;

import com.bjpowernode.p2p.model.loan.RechargeRecord;

import java.util.Map;

/**
 * ClassName:RechargeRecordService
 * Package:com.bjpowernode.p2p.service.loan
 * Description:
 *
 * @date:2019/10/28 15:25
 * @author:guoxin
 */
public interface RechargeRecordService {

    /**
     * 新增充值记录
     * @param rechargeRecord
     * @return
     */
    int addRechargeRecord(RechargeRecord rechargeRecord);

    /**
     * 根据充值订单号更新充值记录
     * @param rechargeRecord
     * @return
     */
    int modifyRechargeRecordByRechargeNo(RechargeRecord rechargeRecord);

    /**
     * 用户充值
     * @param paramMap
     */
    void recharge(Map<String, Object> paramMap) throws Exception;

    /**
     * 处理掉单
     */
    void dealRechargeRecord() throws Exception;

    /**
     * 根据充值订单号查询充值记录
     * @param rechargeNo
     * @return
     */
    RechargeRecord queryRechargeRecordByRechargeNo(String rechargeNo);
}
