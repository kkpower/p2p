package com.bjpowernode.p2p.web;

import com.bjpowernode.p2p.common.constant.Constants;
import com.bjpowernode.p2p.model.loan.LoanInfo;
import com.bjpowernode.p2p.service.loan.BidInfoService;
import com.bjpowernode.p2p.service.loan.LoanInfoService;
import com.bjpowernode.p2p.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ClassName:IndexController
 * Package:com.bjpowernode.p2p.web
 * Description:
 *
 * @date:2019/10/19 10:39
 * @author:guoxin
 */
@Controller
public class IndexController {

    @Autowired
    private LoanInfoService loanInfoService;

    @Autowired
    private UserService userService;

    @Autowired
    private BidInfoService bidInfoService;

    @RequestMapping(value = "/index")
    public String index(Model model) {

        //创建一个固定的线程池
        /*ExecutorService executorService = Executors.newFixedThreadPool(100);

        for (int i = 0; i < 10000; i++) {

            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    //获取平台历史平均年化收益率
                    Double historyAverageRate = loanInfoService.queryHistoryAverageRate();
                    model.addAttribute(Constants.HISTORY_AVERAGE_RATE,historyAverageRate);
                }
            });

        }

        executorService.shutdown();*/

        //获取平台历史平均年化收益率
        Double historyAverageRate = loanInfoService.queryHistoryAverageRate();
        model.addAttribute(Constants.HISTORY_AVERAGE_RATE,historyAverageRate);

        //获取平台注册总人数
        Long allUserCount = userService.queryAllUserCount();
        model.addAttribute(Constants.ALL_USER_COUNT,allUserCount);

        //获取平台累计投资金额
        Double allBidMoney = bidInfoService.queryAllBidMoney();
        model.addAttribute(Constants.ALL_BID_MONEY,allBidMoney);

        //根据产品类型获取产品信息列表（产品类型,页码,每页显示的条数），把以下查询看作是一个分页功能
        //使用了MySQL数据库中的limit 起始下标,截取长度
        //准备查询的参数
        Map<String,Object> paramMap = new HashMap<String,Object>();
        paramMap.put("currentPage",0);

        //获取新手宝产品，产品类型0，显示1个，显示第1页
        paramMap.put("productType",Constants.PRODUCT_TYPE_X);
        paramMap.put("pageSize",1);
        List<LoanInfo> xLoanInfoList = loanInfoService.queryLoanInfoListByProductType(paramMap);
        model.addAttribute("xLoanInfoList",xLoanInfoList);

        //获取优选产品，产品类型1，显示4个，显示第1页
        paramMap.put("productType",Constants.PRODUCT_TYPE_U);
        paramMap.put("pageSize",4);
        List<LoanInfo> uLoanInfoList = loanInfoService.queryLoanInfoListByProductType(paramMap);
        model.addAttribute("uLoanInfoList",uLoanInfoList);

        //获取散标产品，产品类型2，显示8个，显示第1页
        paramMap.put("productType",Constants.PRODUCT_TYPE_S);
        paramMap.put("pageSize",8);
        List<LoanInfo> sLoanInfoList = loanInfoService.queryLoanInfoListByProductType(paramMap);
        model.addAttribute("sLoanInfoList",sLoanInfoList);

        return "index";
    }
}
