package com.shop.evt;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateAuthenticationInfoEvt {
    //用户真实姓名
    @NotBlank(message = "用户真实姓名不能为空")
    @ApiModelProperty(value = "用户真实姓名", required = true)
    private String userRealName;
    //学号
    @NotBlank(message = "学号不能为空")
    @ApiModelProperty(value = "学号", required = true)
    private String sno;
    //学院
    @NotBlank(message = "学院不能为空")
    @ApiModelProperty(value = "学院", required = true)
    private String college;
}
