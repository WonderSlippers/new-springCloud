package com.shop.controller;

import com.shop.anotation.PassToken;
import com.shop.evt.OrderSendEmailEvt;
import com.shop.model.ServiceRespModel;
import com.shop.service.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/message")
@Api(tags = "通知相关接口")
@CrossOrigin
@Validated
public class MessageController {
    @Autowired//创建对象
    private MessageService messageService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 发送邮箱注册验证码
     */
    @PassToken
    @ApiOperation("发送邮箱注册验证码接口")
    @RequestMapping(value = "/sendEmail", method = RequestMethod.POST)
    @ApiImplicitParam(name = "userEmail", value = "邮箱账号", required = true,paramType = "query")
    public ServiceRespModel sendEmail(@Email(message = "邮箱格式不正确") @NotBlank(message = "邮箱不能为空") String userEmail) {
        try {
            return messageService.sendEmail(userEmail);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("发送邮箱注册验证码功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 催发货,提醒签收接口
     */
    @ApiOperation("催发货,提醒签收接口")
    @RequestMapping(value = "/orderSendEmail", method = RequestMethod.POST)
    @ApiImplicitParam(name = "orderNo", value = "订单编码", required = true,paramType = "query")
    public ServiceRespModel orderSendEmail(@ModelAttribute @Validated OrderSendEmailEvt evt) {
        try {
            return messageService.orderSendEmail(evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("催发货,提醒签收功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }
}
