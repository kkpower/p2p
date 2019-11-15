package com.bjpowernode.p2p.service.loan;

import com.bjpowernode.p2p.common.constant.Constants;
import com.bjpowernode.p2p.mapper.loan.BidInfoMapper;
import com.bjpowernode.p2p.mapper.loan.LoanInfoMapper;
import com.bjpowernode.p2p.mapper.user.FinanceAccountMapper;
import com.bjpowernode.p2p.model.loan.BidInfo;
import com.bjpowernode.p2p.model.loan.LoanInfo;
import com.bjpowernode.p2p.model.vo.BidUser;
import com.bjpowernode.p2p.model.vo.PaginationVO;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * ClassName:BidInfoServiceImpl
 * Package:com.bjpowernode.p2p.service.loan
 * Description:
 *
 * @date:2019/10/19 15:52
 * @author:guoxin
 */
@Service("bidInfoServiceImpl")
public class BidInfoServiceImpl implements BidInfoService {

    @Autowired
    private BidInfoMapper bidInfoMapper;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @Autowired
    private LoanInfoMapper loanInfoMapper;

    @Autowired
    private FinanceAccountMapper financeAccountMapper;


    @Override
    public Double queryAllBidMoney() {

        //从redis缓存中获取累计投资金额
        Double allBidMoney = (Double) redisTemplate.opsForValue().get(Constants.ALL_BID_MONEY);

        //判断是否为空
        if (!ObjectUtils.allNotNull(allBidMoney)) {

            //设置同步代码块，解决缓存穿透问题
            synchronized (this) {

                //再次从redis缓存中获取该值
                allBidMoney = (Double) redisTemplate.opsForValue().get(Constants.ALL_BID_MONEY);

                //再次判断是否为空
                if (!ObjectUtils.allNotNull(allBidMoney)) {

                    //去数据库查询
                    allBidMoney = bidInfoMapper.selectAllBidMoney();

                    //并存放到redis缓存中
                    redisTemplate.opsForValue().set(Constants.ALL_BID_MONEY,allBidMoney,15, TimeUnit.SECONDS);
                }
            }

        }


        return allBidMoney;
    }

    @Override
    public List<BidInfo> queryBidInfoListByLoanId(Map<String, Object> paramMap) {
        return bidInfoMapper.selectBidInfoListByLoanId(paramMap);
    }

    @Override
    public List<BidInfo> queryRecentlyBidInfoListByUid(Map<String, Object> paramMap) {
        return bidInfoMapper.selectRecenterBidInfoListByUid(paramMap);
    }

    @Override
    public PaginationVO<BidInfo> queryBidInfoListByPage(Map<String, Object> paramMap) {
        PaginationVO<BidInfo> paginationVO = new PaginationVO<>();

        paginationVO.setTotal(bidInfoMapper.selectTotal(paramMap));

        paginationVO.setDataList(bidInfoMapper.selectRecenterBidInfoListByUid(paramMap));

        return paginationVO;
    }

    @Override
    public void invest(Map<String, Object> paramMap) throws Exception {

        Integer uid = (Integer) paramMap.get("uid");
        Integer loanId = (Integer) paramMap.get("loanId");
        Double bidMoney = (Double) paramMap.get("bidMoney");
        String phone = (String) paramMap.get("phone");

        //更新产品剩余可投资金额
        //超卖现象：实际销售的数量超过了库存数量
        //数据库乐观锁机制解决超卖
        LoanInfo loanInfo = loanInfoMapper.selectByPrimaryKey(loanId);
        paramMap.put("version",loanInfo.getVersion());

        int updateLeftProductMoneyCount = loanInfoMapper.updateLeftProductMoneyByLoanId(paramMap);
        if (updateLeftProductMoneyCount <= 0) {
            throw new Exception("更新产品剩余可投资金额失败");
        }

        //更新帐户可用余额
        int updateFinanceAccountCount = financeAccountMapper.updateFinanceAccountByBid(paramMap);
        if (updateFinanceAccountCount <= 0) {
            throw new Exception("更新帐户可用余额失败");
        }

        //新增投资记录
        BidInfo bidInfo = new BidInfo();
        bidInfo.setUid(uid);
        bidInfo.setLoanId(loanId);
        bidInfo.setBidMoney(bidMoney);
        bidInfo.setBidTime(new Date());
        bidInfo.setBidStatus(1);
        int insertSelective = bidInfoMapper.insertSelective(bidInfo);
        if (insertSelective <= 0) {
            throw new Exception("新增投资记录失败");
        }

        //再次查询产品详情
        LoanInfo loanInfoDetail = loanInfoMapper.selectByPrimaryKey(loanId);

        //判断产品是否满标
        if (0 == loanInfoDetail.getLeftProductMoney()) {
            //产品已满标 -> 更新产品的状态及满标时间
            LoanInfo updateLoanInfo = new LoanInfo();
            updateLoanInfo.setId(loanId);
            updateLoanInfo.setProductStatus(1);
            updateLoanInfo.setProductFullTime(new Date());
            int i = loanInfoMapper.updateByPrimaryKeySelective(updateLoanInfo);
            if (i <= 0) {
                throw new Exception("更新产品状态失败");
            }
        }

        //将当前投资的信息存放到redis缓存中
        redisTemplate.opsForZSet().incrementScore(Constants.INVEST_TOP,phone,bidMoney);

    }

    @Override
    public List<BidUser> queryBidUserTop() {
        List<BidUser> bidUserList = new ArrayList<BidUser>();

        //从redis缓存中获取投资排行榜
        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisTemplate.opsForZSet().reverseRangeWithScores(Constants.INVEST_TOP, 0, 5);

        //获取迭代器
        Iterator<ZSetOperations.TypedTuple<Object>> iterator = typedTuples.iterator();

        //循环遍历
        while (iterator.hasNext()) {

            ZSetOperations.TypedTuple<Object> next = iterator.next();
            String phone = (String) next.getValue();
            Double score = next.getScore();

            BidUser bidUser = new BidUser();
            bidUser.setPhone(phone);
            bidUser.setScore(score);

            bidUserList.add(bidUser);
        }

        return bidUserList;
    }
}
