package com.shop.evt;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class PreSearchCommEvt {
    //关键字
    @NotBlank(message = "关键字不能为空")
    @ApiModelProperty(value = "关键字", required = true)
    private String keyName;
    //查询条数
    @NotNull(message = "查询条数不能为空")
    @ApiModelProperty(value = "查询条数", required = true,example = "1")
    private Integer num;
}
