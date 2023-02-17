package com.shop.model;

import lombok.Data;

@Data
public class QueryReportedCommentModel {
    //评论编码
    private String commentNo;
    //用户名
    private String userName;
    //邮箱
    private String userEmail;
    //用户编码
    private String userNo;
    //被举报次数
    private Integer reportedNum;
    //封禁状态
    private Integer isBan;
    //评论内容
    private String content;
    //举报原因
    private String reason;
}
