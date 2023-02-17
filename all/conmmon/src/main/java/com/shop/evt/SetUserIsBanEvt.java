package com.shop.evt;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SetUserIsBanEvt {

    //封禁状态(0 正常, 1 封禁)
    @NotNull(message = "封禁状态不能为空")
    @Range(min = 0, max = 1, message = "封禁状态不合法")
    @ApiModelProperty(value = "封禁状态(0 正常, 1 封禁)", required = true, example = "0")
    private Integer isBan;
    //用户编码
    @NotBlank(message = "用户编码不能为空")
    @ApiModelProperty(value = "用户编码", required = true)
    private String userNo;

}
