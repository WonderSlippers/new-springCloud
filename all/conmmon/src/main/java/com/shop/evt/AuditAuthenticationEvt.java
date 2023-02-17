package com.shop.evt;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Data
public class AuditAuthenticationEvt {
    //用户编码
    @NotBlank(message = "用户编码不能为空")
    @ApiModelProperty(value = "用户编码", required = true)
    private String userNo;
    //认证状态
    @NotNull(message = "认证状态不能为空")
    @Range(min = 2, max = 3, message = "认证状态不合法")
    @ApiModelProperty(value = "认证状态(0 未认证, 1 认证中, 2 认证通过 , 3 认证失败)", required = true, example = "0")
    private Integer authentication;
}
