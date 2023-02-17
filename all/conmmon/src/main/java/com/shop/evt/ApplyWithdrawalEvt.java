package com.shop.evt;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ApplyWithdrawalEvt {

    //真实姓名
    @NotBlank(message = "真实姓名不能为空")
    @ApiModelProperty(value = "真实姓名", required = true)
    private String realName;
    //户名
    @NotBlank(message = "户名不能为空")
    @ApiModelProperty(value = "户名", required = true)
    private String cardName;
    //银行卡号
    @NotBlank(message = "银行卡号不能为空")
    @ApiModelProperty(value = "银行卡号", required = true)
    private String cardNo;
    //提现金额
    @NotNull(message = "提现金额不能为空")
    @ApiModelProperty(value = "提现金额", required = true, example = "1.00")
    private Double money;
}
