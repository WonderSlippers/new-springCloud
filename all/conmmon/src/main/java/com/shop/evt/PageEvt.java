package com.shop.evt;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PageEvt {

    //当前页
    @NotNull(message = "当前页不能为空")
    @ApiModelProperty(value = "当前页", required = true, example = "1")
    private Long current;
    //每页记录条数
    @NotNull(message = "每页记录条数不能为空")
    @ApiModelProperty(value = "每页记录条数", required = true, example = "1")
    private Long size;
}
