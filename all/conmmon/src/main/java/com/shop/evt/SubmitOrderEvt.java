package com.shop.evt;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.util.Date;

@Data
public class SubmitOrderEvt {
    //商品编码
    @NotBlank(message = "商品编码不能为空")
    @ApiModelProperty(value = "商品编码", required = true)
    private String commNo;
    //收货地址
    @NotBlank(message = "收货地址不能为空")
    @Length(min = 1, max = 80, message = "收货地址长度不能超过30")
    @ApiModelProperty(value = "收货地址(限制长度80)", required = true)
    private String address;
    //收货人
    @NotBlank(message = "收货人不能为空")
    @ApiModelProperty(value = "收货人", required = true)
    private String consignee;
    //收货人手机号
    @NotBlank(message = "收货人手机号不能为空")
    @Pattern(regexp = "^[1][3,4,5,6,7,8,9][0-9]{9}$", message = "手机号格式不正确")
    @ApiModelProperty(value = "收货人手机号", required = true, example = "0")
    private String phone;
    //购买数量
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量不合法")
    @ApiModelProperty(value = "购买数量", required = true, example = "1")
    private Integer num;
    //送达时间From
    @Future
    @NotNull(message = "送达时间From不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "送达时间From", required = true, example = "2021-10-10 19:00:00")
    private Date deTimeFrom;
    //送达时间To
    @Future
    @NotNull(message = "送达时间To不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "送达时间To", required = true, example = "2021-10-11 19:00:00")
    private Date deTimeTo;
}
