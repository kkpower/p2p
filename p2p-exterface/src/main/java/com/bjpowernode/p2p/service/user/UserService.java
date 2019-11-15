package com.bjpowernode.p2p.service.user;

import com.bjpowernode.p2p.model.user.User;

/**
 * ClassName:UserService
 * Package:com.bjpowernode.p2p.service.user
 * Description:
 *
 * @date:2019/10/19 14:58
 * @author:guoxin
 */
public interface UserService {

    /**
     * 获取平台注册总人数
     * @return
     */
    Long queryAllUserCount();

    /**
     * 根据手机号码查询用户信息
     * @param phone
     * @return
     */
    User queryUserByPhone(String phone);

    /**
     * 用户注册
     * @param phone
     * @param loginPassword
     */
    void register(String phone, String loginPassword) throws Exception;

    /**
     * 根据用户标识获取用户信息
     * @param user
     */
    void modifyUserById(User user);

    /**
     * 用户登录
     * @param loginPassword
     * @param phone
     * @return
     */
    User login(String loginPassword, String phone) throws Exception;
}
