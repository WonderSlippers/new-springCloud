package com.shop.evt;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class InsertCommCommentEvt {
    //商品编码
    @NotBlank(message = "商品编码不能为空")
    @ApiModelProperty(value = "商品编码", required = true)
    private String commNo;
    //内容
    @NotBlank(message = "内容不能为空")
    @Length(min = 1, max = 80, message = "内容长度不能超过80")
    @ApiModelProperty(value = "内容(限制长度80)", required = true)
    private String content;
}
