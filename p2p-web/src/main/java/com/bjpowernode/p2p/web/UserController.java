package com.bjpowernode.p2p.web;

import com.alibaba.fastjson.JSONObject;
import com.bjpowernode.http.HttpClientUtils;
import com.bjpowernode.p2p.common.constant.Constants;
import com.bjpowernode.p2p.common.util.Result;
import com.bjpowernode.p2p.model.loan.BidInfo;
import com.bjpowernode.p2p.model.user.FinanceAccount;
import com.bjpowernode.p2p.model.user.User;
import com.bjpowernode.p2p.service.loan.BidInfoService;
import com.bjpowernode.p2p.service.loan.RedisService;
import com.bjpowernode.p2p.service.user.FinanceAccountService;
import com.bjpowernode.p2p.service.user.UserService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName:UserController
 * Package:com.bjpowernode.p2p.web
 * Description:
 *
 * @date:2019/10/22 10:47
 * @author:guoxin
 */
@Controller
//@RestController     //等同于 类上加@Controller + 方法上加@ResponseBody
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FinanceAccountService financeAccountService;

    @Autowired
    private BidInfoService bidInfoService;

    @Autowired
    private RedisService redisService;

    /**
     * 接口名称：验证手机号是否重复
     * 接口地址：http://localhost:8080/p2p/loan/checkPhone
     * 请求方式：http、GET
     * 请求示例：http://localhost:8080/p2p/loan/checkPhone?phone=1390000000
     * @param phone 必填项
     * @return 返回的是JSON格式的字符串：{"code":"10000","message":"success"}
     */
//    @RequestMapping(value = "/loan/checkPhone",method = RequestMethod.GET) //等同于 @GetMapping(value="/loan/checkPhone")
    @GetMapping(value = "/loan/checkPhone")
    @ResponseBody
    public Result checkPhone(@RequestParam (value = "phone",required = true) String phone) {

        //判断手机号码是否重复(手机号) -> 返回boolean|int|String
        //根据手机号查询用户信息(手机号码) -> 返回User
        User user = userService.queryUserByPhone(phone);

        //判断是否存在
        if (ObjectUtils.allNotNull(user)) {
            return Result.error("手机号码已被注册，请更换手机号码");
        }

        return Result.success();
    }


//    @RequestMapping(value = "/loan/checkCaptcha",method = RequestMethod.POST)//等同于 @PostMapping(value="/loan/checkCaptcha")
    @PostMapping(value = "/loan/checkCaptcha")
    @ResponseBody
    public Result checkCaptcha(HttpServletRequest request,
                               @RequestParam (value = "captcha",required = true) String captcha) {

        //从session中获取图形验证码
        String sessionCaptcha = (String) request.getSession().getAttribute(Constants.CAPTCHA);

        //验证用户输入的图形验证码是否正确
        if (!StringUtils.equalsIgnoreCase(captcha, sessionCaptcha)) {
            return Result.error("请输入正确的图形验证码");
        }


        return Result.success();
    }

    @PostMapping(value = "/loan/register")
    @ResponseBody
    public Result register(HttpServletRequest request,
                           @RequestParam (value = "phone",required = true) String phone,
                           @RequestParam (value = "loginPassword",required = true) String loginPassword) {

        try {

            //用户注册【1.新增用户 2.新增帐户】(手机号,登录密码)
            userService.register(phone,loginPassword);

            //将用户的信息存放到session中
            request.getSession().setAttribute(Constants.SESSION_USER,userService.queryUserByPhone(phone));

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }


        return Result.success();
    }

    @RequestMapping(value = "/loan/myFinanceAccount")
    @ResponseBody
    public FinanceAccount myFinanceAccount(HttpServletRequest request) {

        //从session中获取用户的信息
        User sessionUser = (User) request.getSession().getAttribute(Constants.SESSION_USER);

        //根据用户标识获取帐户信息
        FinanceAccount financeAccount = financeAccountService.queryFinanceAccountByUid(sessionUser.getId());

        return financeAccount;
    }

    @PostMapping(value = "/loan/verifyRealName")
    @ResponseBody
    public Result verifyRealName(HttpServletRequest request,
                                 @RequestParam (value = "realName",required = true) String realName,
                                 @RequestParam (value = "idCard",required = true) String idCard) {

        try {
            //准备参数
            Map<String,Object> paramMap = new HashMap<String, Object>();
            paramMap.put("appkey","8c74403bed1e23fa066f");
            paramMap.put("cardNo",idCard);
            paramMap.put("realName",realName);

            //调用京东万象平台的实名认证二要素接口 -> 返回json格式的数据(包含处理的结果)
//            String jsonString = HttpClientUtils.doPost("https://way.jd.com/youhuoBeijing/test", paramMap);
            //模拟报文
            String jsonString = "{\"code\":\"10000\",\"charge\":false,\"remain\":1305,\"msg\":\"查询成功\",\"result\":{\"error_code\":0,\"reason\":\"成功\",\"result\":{\"realname\":\"乐天磊\",\"idcard\":\"350721197702134399\",\"isok\":true}}}";

            //解析JSON格式的字符串，使用fastjson
            //将json格式的字符串转换为JSON对象
            JSONObject jsonObject = JSONObject.parseObject(jsonString);

            //获取通信标识code
            String code = jsonObject.getString("code");
            //判断是否通信成功
            if (!StringUtils.equals(code, "10000")) {
                return Result.error("通信异常");
            }

            //获取isok
            Boolean isok = jsonObject.getJSONObject("result").getJSONObject("result").getBoolean("isok");

            //判断是否匹配
            if (!isok) {
                return Result.error("真实姓名和身份证号码不匹配");
            }

            //从session中获取用户信息
            User sessionUser = (User) request.getSession().getAttribute(Constants.SESSION_USER);

            //实名认证，本质是将用户的信息更新到我们系统的中
            User user = new User();
            user.setId(sessionUser.getId());
            user.setIdCard(idCard);
            user.setName(realName);
            userService.modifyUserById(user);

            //将session中的用户信息进行更新
            sessionUser.setName(realName);
            sessionUser.setIdCard(idCard);
            request.getSession().setAttribute(Constants.SESSION_USER,sessionUser);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("实名认证失败");
        }


        return Result.success();
    }


    @RequestMapping(value = "/loan/logout")
    public String logout(HttpServletRequest request) {
        //将session中用户信息消除 || 让session失效
        request.getSession().removeAttribute(Constants.SESSION_USER);
//        request.getSession().invalidate();

        return "redirect:/index";
    }

    @PostMapping(value = "/loan/login")
    public @ResponseBody Result login(HttpServletRequest request,
                                      @RequestParam (value = "phone",required = true) String phone,
                                      @RequestParam (value = "loginPassword",required = true) String loginPassword,
                                      @RequestParam (value = "messageCode",required = true) String messageCode) {

        try {

            //从redis缓存中获取短信验证码
            String redisMessageCode = redisService.get(phone);

            //判断短信验证码
            if (!StringUtils.equals(messageCode, redisMessageCode)) {
                return Result.error("请输入正确的短信验证码");
            }

            //用户登录【1.根据手机号和登录密码查询用户的信息 2.更新用户的最近登录时间】 -> 返回User（最近登录时间是更新前的值）
            User user = userService.login(loginPassword,phone);

            //判断用户是否存在
            if (!ObjectUtils.allNotNull(user)) {
                return Result.error("手机号或密码有误");
            }

            //将用户的信息存放到session中
            request.getSession().setAttribute(Constants.SESSION_USER,user);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("登录失败");
        }

        return Result.success();
    }


    @RequestMapping(value = "/loan/myCenter")
    public String myCenter(HttpServletRequest request, Model model) {

        //从session中获取用户的信息
        User sessionUser = (User) request.getSession().getAttribute(Constants.SESSION_USER);

        //根据用户标识获取帐户可用余额
        FinanceAccount financeAccount = financeAccountService.queryFinanceAccountByUid(sessionUser.getId());

        //准备查询的参数
        Map<String,Object> paramMap = new HashMap<String, Object>();
        paramMap.put("uid",sessionUser.getId());
        paramMap.put("currentPage",0);
        paramMap.put("pageSize",5);

        //根据用户标识获取最近的投资记录,显示第1页，每页显示5条
        List<BidInfo> bidInfoList = bidInfoService.queryRecentlyBidInfoListByUid(paramMap);

        //根据用户标识获取最近的充值记录，显示第1页，每页显示5条

        //根据用户标识获取最近的收益记录，显示第1页，每页显示5条

        model.addAttribute("financeAccount",financeAccount);
        model.addAttribute("bidInfoList",bidInfoList);

        return "myCenter";
    }


    @PostMapping(value = "/loan/messageCode")
    public @ResponseBody Result messageCode(HttpServletRequest request,
                                            @RequestParam (value = "phone",required = true) String phone) {

        String messageCode = "";

        try {

            //准备参数
            Map<String,Object> paramMap = new HashMap<String, Object>();
            paramMap.put("appkey","3001914bf07611995964921cb0cd");
            paramMap.put("mobile",phone);

            //生成一个随机数字
            messageCode = this.getRandomCode(4);

            //短信内容
            String content = "【凯信通】您的验证码是：" + messageCode;
            paramMap.put("content",content);

            //发送短信验证码，调用京东万象平台-106短信接口
//            String jsonString = HttpClientUtils.doPost("https://way.jd.com/kaixintong/kaixintong", paramMap);
            String jsonString = "{\n" +
                    "    \"code\": \"10000\",\n" +
                    "    \"charge\": false,\n" +
                    "    \"remain\": 0,\n" +
                    "    \"msg\": \"查询成功\",\n" +
                    "    \"result\": \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" ?><returnsms>\\n <returnstatus>Success</returnstatus>\\n <message>ok</message>\\n <remainpoint>-1325012</remainpoint>\\n <taskID>112059860</taskID>\\n <successCounts>1</successCounts></returnsms>\"\n" +
                    "}";

            //解析JSON格式的字符串
            JSONObject jsonObject = JSONObject.parseObject(jsonString);

            //获取通信标识code
            String code = jsonObject.getString("code");

            //判断通信是否成功
            if (!StringUtils.equals(code, "10000")) {
                return Result.error("通信异常");
            }

            //获取业务处理结果内容
            String resultXml = jsonObject.getString("result");

            //使用dom4j+xpath解析xml格式的字符串
            Document document = DocumentHelper.parseText(resultXml);

            //获取returnstatus节点的路径表达式：/returnsms/returnstatus  或者 //returnstatus
            Node node = document.selectSingleNode("//returnstatus");

            //获取节点的文本内容
            String text = node.getText();

            //判断是否发送成功
            if (!StringUtils.equals(text, "Success")) {
                return Result.error("短信发送失败，请重试");
            }

            //将生成的验证码存放到Redis缓存中
            redisService.put(phone,messageCode);



        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("短信平台异常");
        }


        return Result.success(messageCode);
    }

    private String getRandomCode(int count) {

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < count; i++) {
            int index = (int) Math.round(Math.random() * 9);
            stringBuilder.append(index);
        }

        return stringBuilder.toString();
    }


}
