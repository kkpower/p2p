package com.bjpowernode.p2p.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ClassName:DateUtils
 * Package:com.bjpowernode.p2p.common.util
 * Description:
 *
 * @date:2019/10/28 15:22
 * @author:guoxin
 */
public class DateUtils {

    /**
     * 生成时间戳
     * @return 日期格式(yyyyMMddHHmmssSSS)
     */
    public static String getTimestamp() {
        return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
    }
}
