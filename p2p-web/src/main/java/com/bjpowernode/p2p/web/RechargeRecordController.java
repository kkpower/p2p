package com.bjpowernode.p2p.web;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.bjpowernode.http.HttpClientUtils;
import com.bjpowernode.p2p.common.constant.Constants;
import com.bjpowernode.p2p.common.util.DateUtils;
import com.bjpowernode.p2p.config.AlipayConfig;
import com.bjpowernode.p2p.model.loan.RechargeRecord;
import com.bjpowernode.p2p.model.user.User;
import com.bjpowernode.p2p.service.loan.RechargeRecordService;
import com.bjpowernode.p2p.service.loan.RedisService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * ClassName:RechargeRecordController
 * Package:com.bjpowernode.p2p.web
 * Description:
 *
 * @date:2019/10/28 15:10
 * @author:guoxin
 */
@Controller
public class RechargeRecordController {

    @Autowired
    private RechargeRecordService rechargeRecordService;

    @Autowired
    private RedisService redisService;

    @RequestMapping(value = "/loan/toAlipayRecharge")
    public String toAlipayRecharge(HttpServletRequest request, Model model,
                                 @RequestParam (value = "rechargeMoney",required = true) Double rechargeMoney) {

        String rechargeNo = "";

        try {
            //从session中获取用户的信息
            User sessionUser = (User) request.getSession().getAttribute(Constants.SESSION_USER);

            //生成充值记录
            RechargeRecord rechargeRecord = new RechargeRecord();
            rechargeRecord.setUid(sessionUser.getId());
            rechargeRecord.setRechargeMoney(rechargeMoney);
            rechargeRecord.setRechargeTime(new Date());
            rechargeRecord.setRechargeDesc("支付宝充值");
            rechargeRecord.setRechargeStatus("0");//充值记录状态，0：充值中，1：充值成功，2：充值失败

            //创建一个全局唯一充值订单号 = 时间戳 + redis唯一数字
            rechargeNo = DateUtils.getTimestamp() + redisService.getOnlyNumber();

            rechargeRecord.setRechargeNo(rechargeNo);

            int addRechargeCount = rechargeRecordService.addRechargeRecord(rechargeRecord);

            model.addAttribute("rechargeNo",rechargeNo);
            model.addAttribute("rechargeMoney",rechargeMoney);
            model.addAttribute("rechargeDesc","支付宝充值");

        } catch (Exception e) {
            e.printStackTrace();
        }

//        return "redirect:http://localhost:9090/pay/api/alipay?out_trade_no="+rechargeNo+"&total_amount="+rechargeMoney+"&subject=test";
        return "p2pToPay";
    }


    @RequestMapping(value = "/loan/alipayBack")
    public String alipayBack(HttpServletRequest request,Model model,
                             @RequestParam (value = "out_trade_no",required = true) String out_trade_no,
                             @RequestParam (value = "total_amount",required = true) Double total_amount) throws Exception {

        try {
            Map<String,String> params = new HashMap<String,String>();

            //获取支付宝GET过来反馈信息
            Map<String,String[]> requestParams = request.getParameterMap();

            for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                String[] values = (String[]) requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                //乱码解决，这段代码在出现乱码时使用
                valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
                params.put(name, valueStr);
            }

            //调用SDK验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(params, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);

            //——请在这里编写您的程序（以下代码仅作参考）——
            if(signVerified) {

                //准备请求参数
                Map<String,Object> paramMap = new HashMap<String, Object>();
                paramMap.put("out_trade_no",out_trade_no);

                //调用pay工程的订单查询接口
                String jsonString = HttpClientUtils.doPost("http://localhost:9090/pay/api/alipayQuery", paramMap);

                //使用fastjson解析json格式的字符串
                JSONObject jsonObject = JSONObject.parseObject(jsonString);

                //获取alipay_trade_query_response所对应的json对象
                JSONObject tradeQueryResponseJSONObject = jsonObject.getJSONObject("alipay_trade_query_response");

                //获取通信标识code
                String code = tradeQueryResponseJSONObject.getString("code");

                //判断通信是否成功
                if (!StringUtils.equals("10000", code)) {
                    model.addAttribute("trade_msg","通信失败");
                    return "toRechargeBack";
                }

                //获取处理的结果trade_status
                String tradeStatus = tradeQueryResponseJSONObject.getString("trade_status");

                /*交易状态：
                WAIT_BUYER_PAY（交易创建，等待买家付款）
                TRADE_CLOSED（未付款交易超时关闭，或支付完成后全额退款）
                TRADE_SUCCESS（交易支付成功）
                TRADE_FINISHED（交易结束，不可退款）*/

                if (StringUtils.equals("TRADE_CLOSED", tradeStatus)) {
                    //更新当前充值记录的状态为2充值失败
                    RechargeRecord rechargeRecord = new RechargeRecord();
                    rechargeRecord.setRechargeNo(out_trade_no);
                    rechargeRecord.setRechargeStatus("2");
                    int modifyRechargeRecordCount = rechargeRecordService.modifyRechargeRecordByRechargeNo(rechargeRecord);
                    model.addAttribute("trade_msg","充值失败");
                    return "toRechargeBack";
                }

                if (StringUtils.equals("TRADE_SUCCESS", tradeStatus)) {

                    //再次查询充值记录详情
                    RechargeRecord rechargeRecord = rechargeRecordService.queryRechargeRecordByRechargeNo(out_trade_no);

                    if (StringUtils.equals("0", rechargeRecord.getRechargeStatus())) {
                        User sessionUser = (User) request.getSession().getAttribute(Constants.SESSION_USER);

                        paramMap.put("uid",sessionUser.getId());
                        paramMap.put("rechargeNo",out_trade_no);
                        paramMap.put("rechargeMoney",total_amount);

                        //充值成功【1.更新用户的帐户可用余额 2.更新当前充值记录的状态为1充值成功】(用户标识,充值金额,充值订单号)
                        rechargeRecordService.recharge(paramMap);

                    }
                }


            }else {

                model.addAttribute("trade_msg","验证签名失败");
                return "toRechargeBack";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("trade_msg","充值失败");
            return "toRechargeBack";
        }

        return "redirect:/loan/myCenter";
    }


    @RequestMapping(value = "/loan/toWxpayRecharge")
    public String toWxpayRecharge(HttpServletRequest request,Model model,
                                @RequestParam (value = "rechargeMoney",required = true) Double rechargeMoney) {

        String rechargeNo = "";

        try {
            //生成一个充值记录

            //从session中获取用户的信息
            User sessionUser = (User) request.getSession().getAttribute(Constants.SESSION_USER);

            //生成充值记录
            RechargeRecord rechargeRecord = new RechargeRecord();
            rechargeRecord.setUid(sessionUser.getId());
            rechargeRecord.setRechargeMoney(rechargeMoney);
            rechargeRecord.setRechargeTime(new Date());
            rechargeRecord.setRechargeDesc("微信充值");
            rechargeRecord.setRechargeStatus("0");//充值记录状态，0：充值中，1：充值成功，2：充值失败

            //创建一个全局唯一充值订单号 = 时间戳 + redis唯一数字
            rechargeNo = DateUtils.getTimestamp() + redisService.getOnlyNumber();

            rechargeRecord.setRechargeNo(rechargeNo);

            int addRechargeCount = rechargeRecordService.addRechargeRecord(rechargeRecord);

            model.addAttribute("rechargeNo",rechargeNo);
            model.addAttribute("rechargeMoney",rechargeMoney);
            model.addAttribute("rechargeTime",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        } catch (Exception e) {
            e.printStackTrace();
        }


        return "showQRCode";
    }


    @RequestMapping(value = "/loan/generateQRCode")
    public void generateQRCode(HttpServletRequest request, HttpServletResponse response,
                               @RequestParam (value = "rechargeNo",required = true) String rechargeNo,
                               @RequestParam (value = "rechargeMoney",required = true) Double rechargeMoney) throws Exception {

        //准备Map集合参数
        Map<String,Object> paramMap = new HashMap<String, Object>();
        paramMap.put("body","微信充值");
        paramMap.put("out_trade_no",rechargeNo);
        paramMap.put("total_fee",rechargeMoney);

        //调用pay工程的统一下单API接口，返回code_url
        String jsonString = HttpClientUtils.doPost("http://localhost:9090/pay/api/wxpay", paramMap);

        //解析jsonstring
        JSONObject jsonObject = JSONObject.parseObject(jsonString);

        //获取return_code
        String returnCode = jsonObject.getString("return_code");

        if (!StringUtils.equals("SUCCESS", returnCode)) {
            response.sendRedirect(request.getContextPath() + "/toRechageBack.jsp");
        }

        //获取result_code
        String resultCode = jsonObject.getString("result_code");

        if (!StringUtils.equals("SUCCESS", resultCode)) {
            response.sendRedirect(request.getContextPath() + "/toRechargeBack.jsp");
        }

        //获取code_url
        String codeUrl = jsonObject.getString("code_url");

        Map<EncodeHintType,Object> hintTypeObjectMap = new HashMap<EncodeHintType, Object>();
        hintTypeObjectMap.put(EncodeHintType.CHARACTER_SET,"UTF-8");

        //生成二维码图片
        BitMatrix bitMatrix = new MultiFormatWriter().encode(codeUrl, BarcodeFormat.QR_CODE,200,200,hintTypeObjectMap);

        OutputStream outputStream = response.getOutputStream();

        //将矩阵对象转换为流
        MatrixToImageWriter.writeToStream(bitMatrix,"png",outputStream);

        outputStream.flush();
        outputStream.close();


    }
}
