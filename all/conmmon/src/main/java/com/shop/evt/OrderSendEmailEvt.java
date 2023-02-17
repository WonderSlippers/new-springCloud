package com.shop.evt;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class OrderSendEmailEvt {
    //订单编码
    @NotBlank(message = "订单编码不能为空")
    @ApiModelProperty(value = "订单编码", required = true)
    private String orderNo;
    //功能类型(0 催发货, 1 提醒签收)
    @NotNull(message = "功能类型不能为空")
    @Range(min = 0, max = 1, message = "功能类型不合法")
    @ApiModelProperty(value = "功能类型(0 催发货, 1 提醒签收)", required = true, example = "0")
    private Integer type;

}
