package com.shop.evt;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class QueryCommCommentListEvt {
    //商品编码
    @NotBlank(message = "商品编码不能为空")
    @ApiModelProperty(value = "商品编码", required = true)
    private String commNo;
    //查询条数
    @NotNull(message = "查询条数不能为空")
    @ApiModelProperty(value = "查询条数", required = true, example = "1")
    private Integer num;
}
