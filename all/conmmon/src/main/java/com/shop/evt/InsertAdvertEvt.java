package com.shop.evt;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class InsertAdvertEvt {
    //链接
    @URL(message = "链接格式不正确")
    @ApiModelProperty(value = "链接")
    private String url;

}
