package com.bjpowernode.p2p.service.loan;

import com.bjpowernode.p2p.model.loan.BidInfo;
import com.bjpowernode.p2p.model.vo.BidUser;
import com.bjpowernode.p2p.model.vo.PaginationVO;

import java.util.List;
import java.util.Map;

/**
 * ClassName:BidInfoService
 * Package:com.bjpowernode.p2p.service.loan
 * Description:
 *
 * @date:2019/10/19 15:51
 * @author:guoxin
 */
public interface BidInfoService {

    /**
     * 获取平台累计投资金额
     * @return
     */
    Double queryAllBidMoney();

    /**
     * 根据产品标识获取产品的最近投资记录（包含：用户信息）
     * @param paramMap
     * @return
     */
    List<BidInfo> queryBidInfoListByLoanId(Map<String, Object> paramMap);

    /**
     * 根据用户标识获取最近的投资记录（包含：产品信息）
     * @param paramMap
     * @return
     */
    List<BidInfo> queryRecentlyBidInfoListByUid(Map<String, Object> paramMap);

    /**
     * 根据用户标识分页查询投资记录
     * @param paramMap
     * @return
     */
    PaginationVO<BidInfo> queryBidInfoListByPage(Map<String, Object> paramMap);

    /**
     * 用户投资b
     * @param paramMap
     */
    void invest(Map<String, Object> paramMap) throws Exception;

    /**
     * 获取投资排行榜
     * @return
     */
    List<BidUser> queryBidUserTop();
}
