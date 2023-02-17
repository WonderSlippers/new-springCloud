package com.shop.model;

import lombok.Data;

import java.util.List;

@Data
public class StatisticModel {
    //用户数量
    private Integer totalUsers;
    //订单数量
    private Integer totalOrders;
    //商品数量
    private Integer totalComms;
    //商品分区对应商品数量
    private List<CommStModel> commStModelList;
}
