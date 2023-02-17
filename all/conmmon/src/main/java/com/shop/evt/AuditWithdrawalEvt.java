package com.shop.evt;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AuditWithdrawalEvt {

    //申请单编码
    @NotBlank(message = "申请单编码不能为空")
    @ApiModelProperty(value = "申请单编码", required = true)
    private String formNo;
    //审核状态
    @NotNull(message = "审核状态不能为空")
    @Range(max = 2, message = "审核状态不合法")
    @ApiModelProperty(value = "审核状态(0 审核中, 1 通过, 2驳回)", required = true,example = "1")
    private Integer auditStatus;
}
