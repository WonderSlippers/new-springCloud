package com.shop.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shop.async.JmsProducer;
import com.shop.bean.OrderBean;
import com.shop.bean.UserBean;
import com.shop.dao.mapperDao.OrderMapper;
import com.shop.dao.mapperDao.UserMapper;
import com.shop.evt.OrderSendEmailEvt;
import com.shop.exceptions.SendMailException;
import com.shop.model.SendEmailModel;
import com.shop.model.ServiceRespModel;
import com.shop.model.VerificationCodeModel;
import com.shop.utils.Md5Util;
import com.shop.utils.VerificationCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MessageService {

    @Value("${mail.fromMail.sender}")
    private String sender;// 发送者

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private UserMapper userMapper;

    @Autowired
    private JmsProducer jmsProducer;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 发送邮箱注册验证码
     */
    public ServiceRespModel sendEmail(String email) {
        //生成验证码及其他信息
        SimpleMailMessage message = new SimpleMailMessage();
        String code = VerificationCode.generateCode(4);   //随机数生成4位验证码
        SendEmailModel model = new SendEmailModel();
        model.setEmail(email);
        model.setMsg("您的验证码为：" + code + " 有效期为5分钟");
        //发送邮件
        String json = JSON.toJSONString(model);
        jmsProducer.sendMsg("mail.send.web", json);
        VerificationCodeModel verificationCodeModel = new VerificationCodeModel();
        String time = String.valueOf(System.currentTimeMillis() + 300000);
        String vCode = Md5Util.MD5(code + email + time);
        verificationCodeModel.setCode(vCode);
        verificationCodeModel.setTime(time);
        logger.info(String.format("注册邮件已发送至%s", email));
        return ServiceRespModel.success("邮件发送成功",verificationCodeModel);
    }

    /**
     * 催发货,提醒签收接口
     */
    public ServiceRespModel orderSendEmail(OrderSendEmailEvt evt) {
        //校验订单是否存在
        OrderBean orderBean = orderMapper.queryOrderByNo(evt.getOrderNo());
        if (orderBean == null) {
            return ServiceRespModel.error("订单不存在");
        }
        //催发货
        if (evt.getType() == 0) {
            //发送邮件
            UserBean userBean = userMapper.selectById(orderBean.getSellerNo());
            SendEmailModel model = new SendEmailModel();
            model.setEmail(userBean.getUserEmail());
            model.setMsg(String.format("您有一笔订单买家提醒您发货，订单编号为 %s", evt.getOrderNo()));
            String json = JSON.toJSONString(model);
            jmsProducer.sendMsg("mail.send.web", json);
        } else {
            UserBean userBean = userMapper.selectById(orderBean.getCreateUser());
            SendEmailModel model = new SendEmailModel();
            model.setEmail(userBean.getUserEmail());
            model.setMsg(String.format("您有一笔订单卖家提醒您收货，订单编号为 %s", evt.getOrderNo()));
            String json = JSON.toJSONString(model);
            jmsProducer.sendMsg("mail.send.web,web", json);
        }
        return ServiceRespModel.success("操作成功");
    }


    /**
     * 发送邮件信息
     */
    @JmsListener(destination = "mail.send.web")
    public void sendEmailMsg(String json) {
        SendEmailModel model = JSONObject.parseObject(json, SendEmailModel.class);
        try {
            //生成其他信息
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(sender);
            message.setTo(model.getEmail());
            message.setSubject("转小二");// 标题
            message.setText("【转小二】 " + model.getMsg());// 内容
            //发送邮件
            javaMailSender.send(message);
        } catch (MailSendException e) {
            logger.error("目标邮箱 " + model.getEmail() + " 不存在，邮件发送失败");
            throw new SendMailException("目标邮箱 " + model.getEmail() + " 不存在，邮件发送失败");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("发送邮件系统异常");
        }
    }
}
