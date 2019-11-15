package com.bjpowernode.p2p.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * ClassName:PaginationVO
 * Package:com.bjpowernode.p2p.model.vo
 * Description:
 *
 * @date:2019/10/21 11:13
 * @author:guoxin
 */
@Data
public class PaginationVO<T> implements Serializable {

    private Long total;

    private List<T> dataList;

}
