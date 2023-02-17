package com.shop.model;

import lombok.Data;

@Data
public class WithdrawalInfoModel {
    //用户编码
    private String userNo;
    //用户名称
    private String userName;
    //用户邮箱
    private String userEmail;
    //申请单编码
    private String formNo;
    //真实姓名
    private String realName;
    //户名
    private String cardName;
    //银行卡号
    private String cardNo;
    //提现金额
    private Double money;
    //审核状态(0 审核中, 1 通过, 2驳回)
    private Integer auditStatus;
}
