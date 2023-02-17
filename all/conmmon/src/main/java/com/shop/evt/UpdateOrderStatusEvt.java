package com.shop.evt;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UpdateOrderStatusEvt {
    //订单编号
    @NotBlank(message = "订单编号不能为空")
    @ApiModelProperty(value = "订单编号", required = true)
    private String orderNo;
    //订单状态
    @NotNull(message = "订单状态不能为空")
    @Range(min = 0, max = 4, message = "订单状态不合法")
    @ApiModelProperty(value = "订单状态", required = true, example = "1")
    private Integer orderStatus;
}
