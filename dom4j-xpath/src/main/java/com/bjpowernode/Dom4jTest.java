package com.bjpowernode;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import java.util.List;

/**
 * ClassName:Dom4jTest
 * Package:com.bjpowernode
 * Description:
 *
 * @date:2019/10/25 10:53
 * @author:guoxin
 */
public class Dom4jTest {

    public static void main(String[] args) throws Exception {
        String xmlString = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><bookstore><book><title lang=\"eng\">Harry Potter</title><price>29.99</price></book><book><title lang=\"eng\">Learning XML</title><price>39.95</price></book></bookstore>";

        //将xml格式的字符串转换为Document对象
        Document document = DocumentHelper.parseText(xmlString);

        //第一个title节点的路径表达式：/bookstore/book[1]/title   //title[1]
//        Node node = document.selectSingleNode("/bookstore/book[1]/title");
        List<Node> nodes = document.selectNodes("//book");

        //获取节点的文本内容
//        String text = node.getText();

        System.out.println(nodes.size());
    }
}
