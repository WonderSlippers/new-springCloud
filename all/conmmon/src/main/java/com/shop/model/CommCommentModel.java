package com.shop.model;

import lombok.Data;

import java.util.Date;

@Data
public class CommCommentModel {

    //评论编码
    private String commentNo;
    //商品编码
    private String commNo;
    //内容
    private String content;
    //被举报次数
    private Integer reportedNum;
    //创建时间
    private Date createTime;
    //创建人
    private String createUser;
    //创建人昵称
    private String userName;
    //创建人头像地址
    private String profileUrl;
}
