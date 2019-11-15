package com.bjpowernode.p2p.web;

import com.bjpowernode.p2p.common.constant.Constants;
import com.bjpowernode.p2p.common.util.Result;
import com.bjpowernode.p2p.model.loan.BidInfo;
import com.bjpowernode.p2p.model.user.User;
import com.bjpowernode.p2p.model.vo.PaginationVO;
import com.bjpowernode.p2p.service.loan.BidInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ClassName:BidInfoController
 * Package:com.bjpowernode.p2p.web
 * Description:
 *
 * @date:2019/10/24 15:17
 * @author:guoxin
 */
@Controller
public class BidInfoController {

    @Autowired
    private BidInfoService bidInfoService;

    @RequestMapping(value = "/loan/myInvest")
    public String myInvest(HttpServletRequest request, Model model,
                           @RequestParam (value = "currentPage",defaultValue = "1") Integer currentPage) {

        //从session中获取用户的信息
        User sessionUser = (User) request.getSession().getAttribute(Constants.SESSION_USER);

        //准备查询参数
        Map<String,Object> paramMap = new HashMap<String, Object>();
        paramMap.put("uid",sessionUser.getId());
        int pageSize = 10;
        paramMap.put("currentPage",(currentPage-1)*pageSize);
        paramMap.put("pageSize",pageSize);

        //根据用户标识分页查询最近投资记录 -> 返回分页查询模型对象
        PaginationVO<BidInfo> paginationVO = bidInfoService.queryBidInfoListByPage(paramMap);

        //计算总页数
        int totalPage = paginationVO.getTotal().intValue() / pageSize;
        int mod = paginationVO.getTotal().intValue() % pageSize;
        if (mod > 0) {
            totalPage = totalPage + 1;
        }

        model.addAttribute("totalRows",paginationVO.getTotal());
        model.addAttribute("totalPage",totalPage);
        model.addAttribute("bidInfoList",paginationVO.getDataList());
        model.addAttribute("currentPage",currentPage);


        return "myInvest";
    }


    @PostMapping(value = "/loan/invest")
    public @ResponseBody Result invest(HttpServletRequest request,
                                       @RequestParam (value = "loanId",required = true) Integer loanId,
                                       @RequestParam (value = "bidMoney",required = true) Double bidMoney) {
        try {

            //从session中获取用户的信息
            User sessionUser = (User) request.getSession().getAttribute(Constants.SESSION_USER);

            //准备投资参数
            Map<String,Object> paramMap = new HashMap<String, Object>();
            paramMap.put("uid",sessionUser.getId());
            paramMap.put("loanId",loanId);
            paramMap.put("bidMoney",bidMoney);
            paramMap.put("phone",sessionUser.getPhone());

            //处理用户的投资请求【1.更新产品剩余可投资金额 2.更新帐户可用余额 3.新增投资记录 4.判断产品是否满标】(用户标识,产品标识,投资金额)
            bidInfoService.invest(paramMap);



            //创建一个固定的线程池
            /*ExecutorService executorService = Executors.newFixedThreadPool(100);

            Map<String,Object> paramMap = new HashMap<String, Object>();
            paramMap.put("uid",1);
            paramMap.put("loanId",3);
            paramMap.put("bidMoney",1.0);

            for (int i = 0; i < 10000; i++) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            bidInfoService.invest(paramMap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            executorService.shutdown();*/

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }


        return Result.success();
    }
}
