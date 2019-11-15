package com.bjpowernode.p2p.service.loan;

import com.alibaba.fastjson.JSONObject;
import com.bjpowernode.http.HttpClientUtils;
import com.bjpowernode.p2p.mapper.loan.RechargeRecordMapper;
import com.bjpowernode.p2p.mapper.user.FinanceAccountMapper;
import com.bjpowernode.p2p.model.loan.RechargeRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName:RechargeRecordServiceImpl
 * Package:com.bjpowernode.p2p.service.loan
 * Description:
 *
 * @date:2019/10/28 15:25
 * @author:guoxin
 */
@Service("rechargeRecordServiceImpl")
public class RechargeRecordServiceImpl implements RechargeRecordService {

    private Logger logger = LogManager.getLogger(RechargeRecordServiceImpl.class);

    @Autowired
    private RechargeRecordMapper rechargeRecordMapper;

    @Autowired
    private FinanceAccountMapper financeAccountMapper;


    @Override
    public int addRechargeRecord(RechargeRecord rechargeRecord) {
        return rechargeRecordMapper.insertSelective(rechargeRecord);
    }

    @Override
    public int modifyRechargeRecordByRechargeNo(RechargeRecord rechargeRecord) {
        return rechargeRecordMapper.updateRechargeRecordByRechargeNo(rechargeRecord);
    }

    @Override
    public void recharge(Map<String, Object> paramMap) throws Exception {

        //更新帐户可用余额
        int updateFinanceAccountCount = financeAccountMapper.updateFinanceAccountByRecharge(paramMap);
        if (updateFinanceAccountCount <= 0) {
            throw new Exception("更新帐户可用余额失败");
        }

        //更新充值记录的状态
        RechargeRecord rechargeRecord = new RechargeRecord();
        rechargeRecord.setRechargeNo((String) paramMap.get("out_trade_no"));
        rechargeRecord.setRechargeStatus("1");
        int i = rechargeRecordMapper.updateRechargeRecordByRechargeNo(rechargeRecord);
        if (i <= 0) {
            throw new Exception("更新充值记录状态失败");
        }

    }

    @Override
    public void dealRechargeRecord() throws Exception {

        //获取充值记录状态为0的充值记录 -> 返回List
        List<RechargeRecord> rechargeRecordList = rechargeRecordMapper.selectRechargeRecordListByRechargeStatus("0");

        //循环遍历，获取到每一条充值记录
        for (RechargeRecord rechargeRecord : rechargeRecordList) {
            Map<String,Object> paramMap = new HashMap<String, Object>();
            paramMap.put("out_trade_no",rechargeRecord.getRechargeNo());

            //调用pay工程的订单查询接口 -> 返回处理的结果
            String jsonString = HttpClientUtils.doPost("http://localhost:9090/pay/api/alipayQuery", paramMap);

            //使用fastjson来解析
            JSONObject jsonObject = JSONObject.parseObject(jsonString);

            //获取alipay_trade_query_response对应的json对象
            JSONObject tradeQueryResponse = jsonObject.getJSONObject("alipay_trade_query_response");

            //获取通信标识code
            String code = tradeQueryResponse.getString("code");

            if (!StringUtils.equals("10000", code)) {
                logger.info("充值订单号为：" + rechargeRecord.getRechargeNo() + ",通信异常");
            }

            //获取处理的结果
            String tradeStatus = tradeQueryResponse.getString("trade_status");

            /*交易状态：
            WAIT_BUYER_PAY（交易创建，等待买家付款）
            TRADE_CLOSED（未付款交易超时关闭，或支付完成后全额退款）
            TRADE_SUCCESS（交易支付成功）
            TRADE_FINISHED（交易结束，不可退款）*/

            if (StringUtils.equals("TRADE_CLOSED", tradeStatus)) {
                //更新充值记录的状态为2
                RechargeRecord updateRecharge = new RechargeRecord();
                updateRecharge.setRechargeNo(rechargeRecord.getRechargeNo());
                updateRecharge.setRechargeStatus("2");
                int updateStatus = rechargeRecordMapper.updateRechargeRecordByRechargeNo(updateRecharge);
                if (updateStatus <= 0) {
                    logger.info("充值订单号为：" + rechargeRecord.getRechargeNo() + ",更新充值记录的状态失败");
                }
            }

            if (StringUtils.equals("TRADE_SUCCESS", tradeStatus)) {

                //再次查询充值记录
                RechargeRecord rechargeRecordDetail = rechargeRecordMapper.selectRechargeRecordByRechargeNo(rechargeRecord.getRechargeNo());

                if (StringUtils.equals("0", rechargeRecordDetail.getRechargeStatus())) {
                    //给用户充值
                    paramMap.put("uid",rechargeRecord.getUid());
                    paramMap.put("rechargeMoney",rechargeRecord.getRechargeMoney());
                    //更新帐户可用余额
                    int updateFinanceCount = financeAccountMapper.updateFinanceAccountByRecharge(paramMap);
                    if (updateFinanceCount <= 0) {
                        logger.info("充值订单号为：" + rechargeRecord.getRechargeNo() + ",更新帐户可用余额失败");
                    }

                    //更新充值记录的状态
                    RechargeRecord updateRechargeRecord = new RechargeRecord();
                    updateRechargeRecord.setRechargeNo(rechargeRecord.getRechargeNo());
                    updateRechargeRecord.setRechargeStatus("1");
                    int i = rechargeRecordMapper.updateRechargeRecordByRechargeNo(updateRechargeRecord);
                    if (i <= 0) {
                        logger.info("充值订单号为：" + rechargeRecord.getRechargeNo() + ",更新充值记录状态失败");
                    }

                }


            }


        }






    }

    @Override
    public RechargeRecord queryRechargeRecordByRechargeNo(String rechargeNo) {
        return rechargeRecordMapper.selectRechargeRecordByRechargeNo(rechargeNo);
    }
}
