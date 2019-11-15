package com.bjpowernode.p2p.service.user;

import com.bjpowernode.p2p.model.user.FinanceAccount;

/**
 * ClassName:FinanceAccountService
 * Package:com.bjpowernode.p2p.service.user
 * Description:
 *
 * @date:2019/10/22 15:10
 * @author:guoxin
 */
public interface FinanceAccountService {

    /**
     * 根据用户标识获取帐户信息
     * @param uid
     * @return
     */
    FinanceAccount queryFinanceAccountByUid(Integer uid);
}
