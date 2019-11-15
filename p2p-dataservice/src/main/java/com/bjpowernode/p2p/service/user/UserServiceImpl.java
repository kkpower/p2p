package com.bjpowernode.p2p.service.user;

import com.bjpowernode.p2p.common.constant.Constants;
import com.bjpowernode.p2p.mapper.user.FinanceAccountMapper;
import com.bjpowernode.p2p.mapper.user.UserMapper;
import com.bjpowernode.p2p.model.user.FinanceAccount;
import com.bjpowernode.p2p.model.user.User;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * ClassName:UserServiceImpl
 * Package:com.bjpowernode.p2p.service.user
 * Description:
 *
 * @date:2019/10/19 14:58
 * @author:guoxin
 */
@Service("userServiceImpl")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FinanceAccountMapper financeAccountMapper;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @Override
    public Long queryAllUserCount() {

        //获取指定key的操作对象
        BoundValueOperations<Object, Object> valueOperations = redisTemplate.boundValueOps(Constants.ALL_USER_COUNT);

        //获取该操作对象的value
        Long allUserCount = (Long) valueOperations.get();

        //判断是否为空
        if (!ObjectUtils.allNotNull(allUserCount)) {

            //设置同步代码块
            synchronized (this) {

                //再次从redis缓存中获取该值
                allUserCount = (Long) valueOperations.get();

                //再次进行判断
                if (!ObjectUtils.allNotNull(allUserCount)) {

                    //去数据库查询
                    allUserCount = userMapper.selectAllUserCount();

                    //并存放到redis缓存中
                    valueOperations.set(allUserCount,15, TimeUnit.HOURS);
                }

            }

        }




        return allUserCount;
    }

    @Override
    public User queryUserByPhone(String phone) {
        return userMapper.selectUserByPhone(phone);
    }

    @Override
    public void register(String phone, String loginPassword) throws Exception {

        //新增用户
        User user = new User();
        user.setPhone(phone);
        user.setLoginPassword(loginPassword);
        user.setAddTime(new Date());
        user.setLastLoginTime(new Date());
        int insertUserCount = userMapper.insertSelective(user);
        if (insertUserCount <= 0) {
            throw new Exception("手机号为：" + phone + ",新增用户失败");
        }

//        int i = 10/0;

        //根据手机号查询用户信息
        User userDetail = userMapper.selectUserByPhone(phone);

        //新增帐户
        FinanceAccount financeAccount = new FinanceAccount();
        financeAccount.setUid(userDetail.getId());
        financeAccount.setAvailableMoney(888.0);
        int insertFinanceAccountCount = financeAccountMapper.insertSelective(financeAccount);
        if (insertFinanceAccountCount <= 0) {
            throw new Exception("手机号为：" + phone + ",新增帐户失败");
        }
    }

    @Override
    public void modifyUserById(User user) {
        userMapper.updateByPrimaryKeySelective(user);
    }

    @Override
    public User login(String loginPassword, String phone) throws Exception {

        //根据手机号和密码查询用户的信息
        User user = userMapper.selectUserByPhoneAndLoginPassword(loginPassword,phone);

        //判断用户是否存在
        if (ObjectUtils.allNotNull(user)) {
            //更新最近登录时间
            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setLastLoginTime(new Date());
            int i = userMapper.updateByPrimaryKeySelective(updateUser);
            if (i <= 0) {
                throw new Exception("更新失败");
            }
        }

        return user;
    }



}
