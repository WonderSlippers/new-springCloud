package com.shop.evt;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class ReportCommentEvt {
    //评论编码
    @NotBlank(message = "评论编码不能为空")
    @ApiModelProperty(value = "评论编码", required = true)
    private String commentNo;
    //举报原因
    @NotBlank(message = "举报原因不能为空")
    @Length(min = 1, max = 100, message = "举报原因长度不能超过100")
    @ApiModelProperty(value = "举报原因(限制100字)", required = true)
    private String reason;
}
