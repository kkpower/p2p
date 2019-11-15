package com.bjpowernode.p2p.service.loan;

import com.bjpowernode.p2p.common.constant.Constants;
import com.bjpowernode.p2p.mapper.loan.BidInfoMapper;
import com.bjpowernode.p2p.mapper.loan.IncomeRecordMapper;
import com.bjpowernode.p2p.mapper.loan.LoanInfoMapper;
import com.bjpowernode.p2p.mapper.user.FinanceAccountMapper;
import com.bjpowernode.p2p.model.loan.BidInfo;
import com.bjpowernode.p2p.model.loan.IncomeRecord;
import com.bjpowernode.p2p.model.loan.LoanInfo;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ClassName:IncomeRecordServiceImpl
 * Package:com.bjpowernode.p2p.service.loan
 * Description:
 *
 * @date:2019/10/26 14:32
 * @author:guoxin
 */
@Service("incomeRecordServiceImpl")
public class IncomeRecordServiceImpl implements IncomeRecordService {

    @Autowired
    private IncomeRecordMapper incomeRecordMapper;

    @Autowired
    private LoanInfoMapper loanInfoMapper;

    @Autowired
    private BidInfoMapper bidInfoMapper;

    @Autowired
    private FinanceAccountMapper financeAccountMapper;


    @Override
    public void generateIncomePlan() throws Exception {

        //获取已满标的产品 -> 返回List<已满标产品>
        List<LoanInfo> loanInfoList = loanInfoMapper.selectLoanInfoListByProductStatus(1);

        //循环遍历，获取到每一个产品
        for (LoanInfo loanInfo : loanInfoList) {

            //获取到当前产品的所有投资记录 -> 返回List<投资记录>
            List<BidInfo> bidInfoList = bidInfoMapper.selectAllBidInfoListByLoanId(loanInfo.getId());

            //循环遍历，获取到每一条投资记录
            for (BidInfo bidInfo : bidInfoList) {

                //将当前投资记录生成对应的收益计划
                IncomeRecord incomeRecord = new IncomeRecord();
                incomeRecord.setUid(bidInfo.getUid());
                incomeRecord.setLoanId(loanInfo.getId());
                incomeRecord.setBidId(bidInfo.getId());
                incomeRecord.setBidMoney(bidInfo.getBidMoney());
                incomeRecord.setIncomeStatus(0);

                //收益时间(Date) = 满标时间(Date) + 产品周期(int 天|月)
                Date incomeDate = null;
                Double incomeMoney = null;

                if (Constants.PRODUCT_TYPE_X == loanInfo.getProductType()) {
                    //新手宝
                    incomeDate = DateUtils.addDays(loanInfo.getProductFullTime(),loanInfo.getCycle());
                    incomeMoney = bidInfo.getBidMoney() * (loanInfo.getRate() / 100 / 365) * loanInfo.getCycle();
                } else {
                    //优选或散标
                    incomeDate = DateUtils.addMonths(loanInfo.getProductFullTime(),loanInfo.getCycle());
                    incomeMoney = bidInfo.getBidMoney() * (loanInfo.getRate() / 100 / 365) * loanInfo.getCycle()*30;
                }

                incomeMoney = Math.round(incomeMoney * Math.pow(10,2)) / Math.pow(10,2);

                incomeRecord.setIncomeDate(incomeDate);
                incomeRecord.setIncomeMoney(incomeMoney);

                int insertIncomeRecordCount = incomeRecordMapper.insertSelective(incomeRecord);

                if (insertIncomeRecordCount <= 0) {
                    throw new Exception("生成收益计划失败");
                }

            }

            //更新当前产品的状态为2满标且生成收益计划
            LoanInfo updateLoanInfo = new LoanInfo();
            updateLoanInfo.setId(loanInfo.getId());
            updateLoanInfo.setProductStatus(2);
            int updateProductStatusCount = loanInfoMapper.updateByPrimaryKeySelective(updateLoanInfo);
            if (updateProductStatusCount <= 0) {
                throw new Exception("产品状态更新失败");
            }
        }

    }

    @Override
    public void generateIncomeBack() throws Exception {
        //查询收益状态为0且收益时间与当前时间一致的收益计划 -> 返回List<收益计划>
        List<IncomeRecord> incomeRecordList = incomeRecordMapper.selectIncomeRecordListByIncomeStatusAndCurDate(0);
        Map<String,Object> paramMap = new HashMap<String, Object>();

        //循环遍历，获取到每一条收益计划
        for (IncomeRecord incomeRecord : incomeRecordList) {

            paramMap.put("uid",incomeRecord.getUid());
            paramMap.put("bidMoney",incomeRecord.getBidMoney());
            paramMap.put("incomeMoney",incomeRecord.getIncomeMoney());

            //将投资的本金与利息返还给用户
            int updateAccountCount = financeAccountMapper.updateFinanceAccountByIncomeBack(paramMap);
            if (updateAccountCount <= 0) {
                throw new Exception("收益返还失败");
            }

            //将当前收益计划的状态更新为1已返还
            IncomeRecord updateIncome = new IncomeRecord();
            updateIncome.setId(incomeRecord.getId());
            updateIncome.setIncomeStatus(1);
            int i = incomeRecordMapper.updateByPrimaryKeySelective(updateIncome);
            if (i <= 0) {
                throw new Exception("收益状态更新失败");
            }
        }


    }
}
