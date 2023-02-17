package com.shop.evt;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AuditCommEvt {
    //商品编码
    @NotBlank(message = "商品编码不能为空")
    @ApiModelProperty(value = "商品编码", required = true)
    private String commNo;
    //审核状态
    @NotNull(message = "审核状态不能为空")
    @Range(min = 1, max = 2, message = "审核状态不合法")
    @ApiModelProperty(value = "审核状态(0 = 审核中,1 = 通过,2 = 不通过)", required = true, example = "0")
    private Integer auditStatus;
    //审核留言
    @Length(min = 1, max = 80, message = "审核留言长度不能超过80")
    @ApiModelProperty(value = "审核留言(审核不通过时留言不能为空)(限制长度80)")
    private String auditMsg;
}
