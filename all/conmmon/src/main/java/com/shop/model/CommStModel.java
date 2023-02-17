package com.shop.model;

import lombok.Data;

@Data
public class CommStModel {
    //分区
    private String commTag;
    //数量
    private Integer num;

    public CommStModel(String commTag, Integer num) {
        this.commTag = commTag;
        this.num = num;
    }
}
