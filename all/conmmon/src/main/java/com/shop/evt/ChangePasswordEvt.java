package com.shop.evt;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ChangePasswordEvt {

    //原密码
    @NotBlank(message = "原密码不能为空")
    @ApiModelProperty(value = "原密码", required = true)
    private String oldPassword;
    //新密码
    @NotBlank(message = "新密码不能为空")
    @ApiModelProperty(value = "新密码", required = true)
    private String newPassword;

}
