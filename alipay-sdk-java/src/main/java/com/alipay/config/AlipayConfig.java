package com.alipay.config;

import java.io.FileWriter;
import java.io.IOException;

/* *
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *修改日期：2017-04-05
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 */

public class AlipayConfig {

//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    public static String app_id = "2016101300673364";

    // 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCMShk5wBYV/duHX7khIyv/eLlQRZECVLkMaWJLpXjRp+w2xjA8VH8fikIzfaAJ6L8aBYTnRHj7uqbQZKZYbLSEvkdVJ4++vtNgMgSvlVoNBvemBWK/tPyra3Y42xS0RwMY/uWqqu/nH9KqJ3VdqIiA6L5b2Ms/SvOn3Yu2cyP+ccuQCHgl9scbeelzRtMKI0ecW8IzMJ1e1pScAHWqsRqIWy4N7C7FQfwlf970wUuLJQELbHv0C2vZRYMFCLYO9LU/ARdGw7W+Vg+LE6B0CGOPxvbARHe+FHV+PoGNhPuV0HDYecTkx1/Z8rz4PG3Cs1dsEcL+a3ZibM5R2wLt/aCBAgMBAAECggEAGew/SQqTijDEdflcLiFfqe9W3txb9UBOiVqXzT6gWq1a2qw4kY1TiDJ2FSmrfulBFr/aVRmz+V/+HxfaHRDJMpYufC+5Qhfk2yui7gb9Atc8v7LTEnWREuPijpL8+mr8yYugSK8ZcW3R8Zsk9s+Dmb5oHnSq6eEhsO0hOe+AwS1jgyiIhBIg13L6uIPURYQWebkt+LnHtLgI5cIR3okHxz3Os5lx3zMy5VbwwiejinYg5gFIf7D8q1Q8jsH1vAu7Tr8rbXkuadEcqgrp3BlqQk8BzZOMooz7kJ/og1IJ0ye98OL7zr/xB7X0l1HV7Urdvh5jtmGFuq89EDqbMTcdmQKBgQDQdZYQW7s4iw1jGaW85FIyt0krdE941zuuUgzRpNSiJxc8RhEjOsHDa74q7wcqGih7q+c9Jk5kCrlvEiBWiW3sSwGsKGH+lcZbODloCWt7gzhb9uTkT2aS5OmxbCBtA25pm86pGfaAazMMa0dYZEC8kyPAQ8gdNNnTctzV487CqwKBgQCsSJKLYE9TAHej/5r7UHf/7amdxBXFZCNtSlqj98NkYE0/P6EXczmHZUlVH/lYJf2Bxs30agNKbpjDmPgXLpHVX4DflYLoLl0WDGyijtG0melgIhauqHDKFdq6q0DZYW7Qz3vz4uuZC5VcpFQkkMkLNFWZGXx91GMVQ7VtAwgJgwKBgQCmy08VK7Cz318Iyj640x+KAOmE4X2tPNo98tza6b5ROGH5Ej8oJx1eldkFVVgwMdnuim9tmDblCUeyPZiuZSwVAzcFXxJ5t7GMz4HvYooju6zeuMAQhg+WvZ7XHaMcVsCDs+05lNKNkji4WSYu7Zh2ymyyrAl0hRM9gwx7/9kyHQKBgDWjUpy6G3hICPeyONpNr06q+8f2qKCQEMeSAq5SES9LHEG42bqnUR3qVu59ye+7xDWHY8/YwbWGwWEEWxk8koLP/RufPOZ0qN6Yd6oUtQ15bcNUWaN/Jb6FF1hOwR3sy8ePEZRY7ej0RkhVkSzhNvaaEJxd5vh2C3MvZWjPLM1pAoGBAKaL3cFcxoiHDP+c9by1P89fjR+D2myo4xetXILiCBdbTzNfoLxkm0w/yLT6lCAn891tupHGfcpBr0XRmC3+NxFRvWCemU7vU7p3MYN0kXPpPQGCZOJdH2PcUBR5PMUVHT9odzvmHbsL+eLiC3l1rM+Fs4n0vj4+yytiwRTJ3LfL";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgLEy9LreWbLCJqoJ+5mmoGZQA1Zugrb1RvhFM8wIWGxsm8s44rqrL6Dtnbhjz6kxkOU1uDzGPG1RtynJuxkt984iN0khgdnrAh3X8Ipigqdk0dLXiPDxQ6L67mC150LpDpP9xXqgDA+Fn0EI/TxvP/DzhjTXkCtJ935kPmkEVC3nyAv3XVCd+KnyFMKMrLSdjHTs9sI9PJ/e6ZciE4TNoqwnS0T7PUvxvkNsxQ+h5t4agOTFq88OfbmgqoGspBa3t6cA7fniVsfaXXhJzEVvPuGz4cfkCGmWLBNftrEYNs0/dX0qFxllhiTQ/BuvGwgRzkTLyB1Kt3Zlf4otLXlGxwIDAQAB";

    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = "http://localhost:8083/alipay.trade.page.pay-JAVA-UTF-8/notify_url.jsp";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String return_url = "http://localhost:8083/alipay.trade.page.pay-JAVA-UTF-8/return_url.jsp";

    // 签名方式
    public static String sign_type = "RSA2";

    // 字符编码格式
    public static String charset = "utf-8";

    // 支付宝网关
    public static String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    // 支付宝网关
    public static String log_path = "C:\\";


//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    /**
     * 写日志，方便测试（看网站需求，也可以改成把记录存入数据库）
     * @param sWord 要写入日志里的文本内容
     */
    public static void logResult(String sWord) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(log_path + "alipay_log_" + System.currentTimeMillis()+".txt");
            writer.write(sWord);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

