package com.shop.evt;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DeleteOrderRecordEvt {
    //订单编码
    @NotBlank(message = "订单编码不能为空")
    @ApiModelProperty(value = "订单编码", required = true)
    private String orderNo;
    //操作对象标记
    @NotNull(message = "操作对象标记不能为空")
    @Range(min = 0, max = 1, message = "操作对象标记不合法")
    @ApiModelProperty(value = "操作对象标记(0 买家查看订单, 1 卖家查看订单)", required = true, example = "0")
    private Integer mark;
}
