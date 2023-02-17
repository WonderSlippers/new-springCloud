package com.shop.evt;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class ReleaseCommEvt {

    //商品名称
    @NotBlank(message = "商品名称不能为空")
    @Length(min = 1, max = 30, message = "商品名称长度不能超过30")
    @ApiModelProperty(value = "商品名称(限制长度30)", required = true)
    private String commName;
    //商品标签
    @NotNull(message = "商品标签不能为空")
    @Range(min = 0, max = 6, message = "商品标签不合法")
    @ApiModelProperty(value = "商品标签(0 衣物,1 数码,2 食品,3 图书,4 化妆品,5 文具,6 居家)", required = true, example = "0")
    private Integer commTag;
    //商品描述
    @NotBlank(message = "商品描述不能为空")
    @Length(min = 1, max = 150, message = "商品描述长度不能超过30")
    @ApiModelProperty(value = "商品描述(限制长度150)", required = true)
    private String commDesc;
    //商品价格
    @NotNull(message = "商品价格不能为空")
    @Min(value = 0, message = "商品价格不合法")
    @ApiModelProperty(value = "商品价格", required = true, example = "0")
    private Double commPrice;
    //商品库存
    @NotNull(message = "商品库存不能为空")
    @Min(value = 0, message = "商品库存不合法")
    @ApiModelProperty(value = "商品库存", required = true, example = "0")
    private Integer commStock;
    //是否包邮(0 包邮，1 不包邮）
    @NotNull(message = "是否包邮不能为空")
    @Range(min = 0, max = 1, message = "是否包邮不能为空")
    @ApiModelProperty(value = "是否包邮(0 包邮，1 不包邮）", required = true, example = "0")
    private Integer isFreeShipping;
    //自定义标签
    @Size(max = 8, message = "自定义标签数量不合法")
    @ApiModelProperty(value = "自定义标签(限制数量8)")
    private List<String> customTags;

}
