package com.shop.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@ApiModel(value = "应答模型")
public class ServiceRespModel {

    //应答码 (1 = 成功 -1 = 接口失败)
    @ApiModelProperty(value = "应答码(1 成功, -1 接口失败, 2 跳转 )")
    private Integer code;
    //应答消息
    @ApiModelProperty(value = "应答消息")
    private String msg;
    //应答体
    @ApiModelProperty(value = "应答体")
    private Object obj;


    public static ServiceRespModel success(String msg) {
        return new ServiceRespModel(1, msg, null);
    }

    public static ServiceRespModel error(String msg) {
        return new ServiceRespModel(-1, msg, null);
    }

    public static ServiceRespModel success(String msg, Object obj) {
        return new ServiceRespModel(1, msg, obj);
    }

    public static ServiceRespModel error(String msg, Object obj) {
        return new ServiceRespModel(-1, msg, obj);
    }
}
