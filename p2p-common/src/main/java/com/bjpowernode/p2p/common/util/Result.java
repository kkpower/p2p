package com.bjpowernode.p2p.common.util;

import java.util.HashMap;

/**
 * ClassName:Result
 * Package:com.bjpowernode.p2p.common.util
 * Description:
 *
 * @date:2019/10/22 10:50
 * @author:guoxin
 */
public class Result extends HashMap<Object,Object> {


    /**
     * 成功的响应
     * @return
     */
    public static Result success() {
        Result result = new Result();
        result.put("code","10000");
        result.put("message","success");
        return result;
    }

    public static Result success(String data) {
        Result result = new Result();
        result.put("code","10000");
        result.put("data",data);
        result.put("message","success");
        return result;
    }


    /**
     * 错误响应
     * @param message 响应的消息
     * @return
     */
    public static Result error(String message) {
        Result result = new Result();
        result.put("code","99999");
        result.put("message",message);
        return result;
    }
}
