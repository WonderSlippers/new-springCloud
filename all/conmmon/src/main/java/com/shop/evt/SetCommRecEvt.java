package com.shop.evt;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SetCommRecEvt {
    //商品编码
    @NotBlank(message = "商品编码不能为空")
    @ApiModelProperty(value = "商品编码", required = true)
    private String commNo;
    //是否为推荐商品(0 未推荐, 1 推荐)
    @NotNull(message = "是否为推荐商品不能为空")
    @Range(min = 0, max = 1, message = "是否为推荐商品不合法")
    @ApiModelProperty(value = "是否为推荐商品(0 未推荐, 1 推荐)", required = true, example = "0")
    private Integer recommend;
}
