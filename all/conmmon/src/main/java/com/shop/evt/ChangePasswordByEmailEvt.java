package com.shop.evt;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class ChangePasswordByEmailEvt {

    //用户邮箱
    @NotBlank(message = "用户邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @ApiModelProperty(value = "用户邮箱", required = true)
    private String userEmail;
    //用户密码
    @NotBlank(message = "新密码不能为空")
    @ApiModelProperty(value = "新密码", required = true)
    private String userPassword;
    //验证码
    @NotBlank(message = "验证码不能为空")
    @ApiModelProperty(value = "验证码", required = true)
    private String code;
    //验证码时效
    @NotBlank(message = "验证码时效不能为空")
    @ApiModelProperty(value = "验证码时效", required = true)
    private String time;
    //加密验证码
    @NotBlank(message = "加密验证码不能为空")
    @ApiModelProperty(value = "加密验证码", required = true)
    private String encryptionCode;

}
