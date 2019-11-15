package com.bjpowernode.p2p.service.loan;

/**
 * ClassName:RedisService
 * Package:com.bjpowernode.p2p.service.loan
 * Description:
 *
 * @date:2019/10/25 12:22
 * @author:guoxin
 */
public interface RedisService {

    /**
     * 将key和value存放到redis缓存
     * @param key
     * @param value
     */
    void put(String key, String value);

    /**
     * 从redis缓存中获取key对应的值
     * @param key
     * @return
     */
    String get(String key);

    /**
     * 从redis中获取唯一数字
     * @return
     */
    Long getOnlyNumber();
}
