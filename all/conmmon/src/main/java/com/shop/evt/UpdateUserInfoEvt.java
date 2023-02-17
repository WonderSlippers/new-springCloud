package com.shop.evt;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UpdateUserInfoEvt {

    //用户名称
    @Length(min = 1, max = 20, message = "用户名称长度不能超过20")
    @ApiModelProperty(value = "用户名称")
    private String userName;
    //用户简介
    @Length(min = 1, max = 40, message = "用户简介长度不能超过40")
    @ApiModelProperty(value = "用户简介(限制长度为40)")
    private String userInfo;
    //用户性别
    @ApiModelProperty(value = "用户性别(男,女")
    private String userSex;

}
