package com.shop.evt;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class UserLoginEvt {

    //用户邮箱
    @NotBlank(message = "用户邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @ApiModelProperty(value = "用户邮箱", required = true)
    private String userEmail;
    //用户密码
    @NotBlank(message = "用户密码不能为空")
    @ApiModelProperty(value = "用户密码", required = true)
    private String userPassword;

}
