package com.bjpowernode.p2p.service.loan;

import com.bjpowernode.p2p.common.constant.Constants;
import com.bjpowernode.p2p.mapper.loan.LoanInfoMapper;
import com.bjpowernode.p2p.model.loan.LoanInfo;
import com.bjpowernode.p2p.model.vo.PaginationVO;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ClassName:LoanInfoServiceImpl
 * Package:com.bjpowernode.p2p.service.loan
 * Description:
 *
 * @date:2019/10/19 10:54
 * @author:guoxin
 */
@Service("loanInfoServiceImpl")
public class LoanInfoServiceImpl implements LoanInfoService {

    @Autowired
    private LoanInfoMapper loanInfoMapper;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @Override
    public Double queryHistoryAverageRate() {
        //好处：减少对数据库的访问，减轻后台的压力，提升系统的性能，用户体验也会变好

        //设置redisTemplate对象的key的序列化方式为字符串序列化方式，目的就是提高可读性
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        //首先去redis缓存中获取历史平均年化收益率
        /*Double historyAverageRate = (Double) redisTemplate.opsForValue().get(Constants.HISTORY_AVERAGE_RATE);

        //判断是否存在
//        if (null == historyAverageRate)
        if (!ObjectUtils.allNotNull(historyAverageRate)) {

            System.out.println("从数据库查询。。。。。。");

            //不存在:去数据库查询，并存放到redis缓存中
            historyAverageRate = loanInfoMapper.selectHistoryAverageRate();

            //将该值存放到redis缓存中
            redisTemplate.opsForValue().set(Constants.HISTORY_AVERAGE_RATE,historyAverageRate,15, TimeUnit.MINUTES);
        } else {
            System.out.println("从Redis缓存中查询。。。。。。");
        }*/

        //以上代码在多线程高并发的时候可以产生：缓存穿透现象
        //通过双重检测+同步代码块可以解决以上现象

        //首先redis缓存中获取该值
        Double historyAverageRate = (Double) redisTemplate.opsForValue().get(Constants.HISTORY_AVERAGE_RATE);

        //第一次判断是否为空
        if (!ObjectUtils.allNotNull(historyAverageRate)) {

            //设置同步代码块
            synchronized (this) {

                //再次从redis缓存中查询（设置为同步）
                historyAverageRate = (Double) redisTemplate.opsForValue().get(Constants.HISTORY_AVERAGE_RATE);

                //再次进行判断是否为空
                if (!ObjectUtils.allNotNull(historyAverageRate)) {


                    System.out.println("从数据库中查询。。。。。。");

                    //从数据库查询
                    historyAverageRate = loanInfoMapper.selectHistoryAverageRate();

                    //并存放到redis缓存中
                    redisTemplate.opsForValue().set(Constants.HISTORY_AVERAGE_RATE, historyAverageRate, 15, TimeUnit.MINUTES);
                } else {
                    System.out.println("从Redis缓存中查询。。。。。。");
                }

            }

        } else {
            System.out.println("从Redis缓存中查询。。。。。。");
        }




        //存在：直接返回
        return historyAverageRate;
    }

    @Override
    public List<LoanInfo> queryLoanInfoListByProductType(Map<String, Object> paramMap) {

        return loanInfoMapper.selectLoanInfoListByProductType(paramMap);
    }

    @Override
    public PaginationVO<LoanInfo> queryLoanInfoListByPage(Map<String, Object> paramMap) {
        PaginationVO<LoanInfo> paginationVO = new PaginationVO<>();

        //查询产品的总记录数
        Long total = loanInfoMapper.selectTotal(paramMap);

        paginationVO.setTotal(total);

        //查询每页显示的数据
        List<LoanInfo> loanInfoList = loanInfoMapper.selectLoanInfoListByProductType(paramMap);

        paginationVO.setDataList(loanInfoList);

        return paginationVO;
    }

    @Override
    public LoanInfo queryLoanInfoById(Integer id) {
        return loanInfoMapper.selectByPrimaryKey(id);
    }
}
