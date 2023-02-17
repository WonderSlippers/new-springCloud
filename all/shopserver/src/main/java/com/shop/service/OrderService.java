package com.shop.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shop.async.JmsProducer;
import com.shop.bean.CommodityBean;
import com.shop.bean.OrderBean;
import com.shop.bean.UserBean;
import com.shop.config.ShopProperties;
import com.shop.dao.mapperDao.CommodityMapper;
import com.shop.dao.mapperDao.OrderMapper;
import com.shop.dao.mapperDao.UserMapper;
import com.shop.evt.DeleteOrderRecordEvt;
import com.shop.evt.SubmitOrderEvt;
import com.shop.evt.UpdateOrderStatusEvt;
import com.shop.exceptions.OrderSystemException;
import com.shop.model.CommOrderModel;
import com.shop.model.SendEmailModel;
import com.shop.model.ServiceRespModel;
import com.shop.model.UpdateOrderModel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private CommodityMapper commodityMapper;

    @Resource
    private UserMapper userMapper;

    @Autowired
    private JmsProducer jmsProducer;

    @Autowired
    private ShopProperties shopProperties;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 提交订单
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceRespModel submitOrder(HttpServletRequest request, SubmitOrderEvt evt) {
        //查询商品是否存在
        CommodityBean comm = commodityMapper.queryCommByNo(evt.getCommNo());
        if (comm == null) {
            return ServiceRespModel.error("商品不存在");
        }
        //校验购买数量合法性
        if (evt.getNum() > comm.getCommStock()) {
            return ServiceRespModel.error("库存不足");
        }
        //校验用户状态
        UserBean userBean = userMapper.queryUserByNo((String) request.getAttribute("userNo"));
        if (userBean.getIsBan() == 1) {
            return ServiceRespModel.error("用户处于封禁状态");
        }
        //校验卖家是否存在
        UserBean saleUserBean = userMapper.queryUserByNo(comm.getCreateUser());
        if (saleUserBean == null) {
            return ServiceRespModel.error("卖家不存在");
        }
        try {
            //提交订单
            DecimalFormat df = new DecimalFormat("#.00");
            Double price = Double.parseDouble(df.format(comm.getCommPrice() * evt.getNum()));//总价
            OrderBean orderBean = new OrderBean();
            orderBean.setOrderNo(StringUtils.replace(UUID.randomUUID().toString(), "-", ""));
            orderBean.setCommNo(evt.getCommNo());
            orderBean.setAddress(evt.getAddress());
            orderBean.setConsignee(evt.getConsignee());
            orderBean.setCreateUser((String) request.getAttribute("userNo"));
            orderBean.setNum(evt.getNum());
            orderBean.setPhone(evt.getPhone());
            orderBean.setPrice(price);
            orderBean.setDeTimeFrom(evt.getDeTimeFrom());
            orderBean.setDeTimeTo(evt.getDeTimeTo());
            orderBean.setSellerDisplay(0);
            orderBean.setBuyerDisplay(0);
            orderBean.setStatus("E");
            orderBean.setCreateTime(new Date());
            orderBean.setSellerNo(comm.getCreateUser());
            orderBean.setOrderStatus(0);
            int info = orderMapper.insert(orderBean);
            if (info != 1) {
                throw new OrderSystemException("订单信息提交至数据库失败");
            }
            //更新商品销量与库存
            comm.setCommSale(comm.getCommSale() + 1);
            comm.setCommStock(comm.getCommSale() - 1);
            comm.setUpdateUser((String) request.getAttribute("userNo"));
            int info3 = commodityMapper.updateById(comm);
            if (info3 != 1) {
                throw new OrderSystemException("更新商品销量与库存失败");
            }
            //将付款金额汇入网站账户
            UserBean account = userMapper.selectById(shopProperties.getAccount());
            account.setBalance(account.getBalance() + price);
            account.setUpdateUser((String) request.getAttribute("userNo"));
            int info4 = userMapper.updateById(account);
            if (info4 != 1) {
                throw new OrderSystemException("汇入款项失败");
            }
            //发送邮件
            SendEmailModel model = new SendEmailModel();
            model.setEmail(saleUserBean.getUserEmail());
            model.setMsg(String.format("您的商品 %s 出售成功，商品编码为 %s ，订单编码为 %s，了解具体信息请登录商城", comm.getCommName(), comm.getCommNo(), orderBean.getOrderNo()));
            String json = JSON.toJSONString(model);
            jmsProducer.sendMsg("mail.send.web", json);
            //发送邮件
            SendEmailModel model1 = new SendEmailModel();
            model1.setEmail(userBean.getUserEmail());
            model1.setMsg(String.format("您的订单提交成功，订单编码为 %s ，卖家联系方式为 %s", orderBean.getOrderNo(), saleUserBean.getUserEmail()));
            String json1 = JSON.toJSONString(model1);
            jmsProducer.sendMsg("mail.send.web", json1);
            logger.info(String.format("用户%s提交了一个订单", request.getAttribute("userEmail")));
        } catch (OrderSystemException e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ServiceRespModel.error(e.getMessage());
        }
        return ServiceRespModel.success("订单提交成功");
    }

    /**
     * 查看用户提交的订单列表
     */
    public ServiceRespModel queryUserSubmitOrderList(HttpServletRequest request) {
        List<CommOrderModel> commOrderModelList = orderMapper.queryUserSubmitOrderList((String) request.getAttribute("userNo"));
        for (CommOrderModel commOrderModel : commOrderModelList) {
            commOrderModel.setCommPicList(commodityMapper.queryPicByCommNo(commOrderModel.getCommNo()));
        }
        return ServiceRespModel.success("用户提交的订单列表", commOrderModelList);
    }

    /**
     * 查看用户接收的订单列表
     */
    public ServiceRespModel queryUserReceiveOrderList(HttpServletRequest request) {
        List<CommOrderModel> commOrderModelList = orderMapper.queryUserReceiveOrderList((String) request.getAttribute("userNo"));
        for (CommOrderModel commOrderModel : commOrderModelList) {
            commOrderModel.setCommPicList(commodityMapper.queryPicByCommNo(commOrderModel.getCommNo()));
        }
        return ServiceRespModel.success("用户接收的订单列表", commOrderModelList);
    }

    /**
     * 更新订单状态
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceRespModel updateOrderStatus(HttpServletRequest request, UpdateOrderStatusEvt evt) {
        OrderBean orderBean = orderMapper.queryOrderByNo(evt.getOrderNo());
        //校验订单是否存在
        if (orderBean == null) {
            return ServiceRespModel.error("订单不存在");
        }
        //查询订单对应商品
        CommodityBean commodityBean = commodityMapper.selectById(orderBean.getCommNo());
        if (commodityBean == null) {
            return ServiceRespModel.error("商品不存在");
        }
        //下单用户
        UserBean orderUser = userMapper.selectById(orderBean.getCreateUser());
        if (orderUser == null) {
            return ServiceRespModel.error("创建订单的用户不存在");
        }
        //创建商品用户
        UserBean commUser = userMapper.selectById(commodityBean.getCreateUser());
        if (commUser == null) {
            return ServiceRespModel.error("创建商品的用户不存在");
        }
        //汇款账户
        UserBean account = userMapper.selectById(shopProperties.getAccount());
        switch (evt.getOrderStatus()) {
            case 1:
                //校验用户权限
                if (!request.getAttribute("userNo").equals(commodityBean.getCreateUser())) {
                    return ServiceRespModel.error("无操作权限");
                }
                //向用户发送邮件
                SendEmailModel model = new SendEmailModel();
                model.setEmail(orderUser.getUserEmail());
                model.setMsg(String.format("您购买的商品 %s 已发货，订单编码为 %s，了解具体信息请登录商城", commodityBean.getCommName(), orderBean.getOrderNo()));
                String json = JSON.toJSONString(model);
                jmsProducer.sendMsg("mail.send.web", json);
                //订单自动处理
                UpdateOrderModel updateOrderModel = new UpdateOrderModel();
                updateOrderModel.setOrderNo(evt.getOrderNo());
                updateOrderModel.setBuyer(orderUser.getUserEmail());
                updateOrderModel.setSeller(commUser.getUserEmail());
                String updateOrderModelJson = JSON.toJSONString(updateOrderModel);
                //设置7天后自动处理
                jmsProducer.delaySend("order.handle", updateOrderModelJson, 1000 * 60 * 60 * 24 * 7L);
                orderBean.setOrderStatus(evt.getOrderStatus());
                orderBean.setUpdateUser((String) request.getAttribute("userNo"));
                int info = orderMapper.updateById(orderBean);
                if (info != 1) {
                    return ServiceRespModel.error("更新订单状态失败");
                }
                break;
            case 2:
                //校验用户权限
                if (!request.getAttribute("userNo").equals(orderBean.getCreateUser())) {
                    return ServiceRespModel.error("无操作权限");
                }
                if (orderBean.getOrderStatus() == 2) {
                    return ServiceRespModel.error("订单已完成");
                }
                orderBean.setOrderStatus(evt.getOrderStatus());
                orderBean.setUpdateUser((String) request.getAttribute("userNo"));
                int info5 = orderMapper.updateById(orderBean);
                if (info5 != 1) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return ServiceRespModel.error("更新订单状态失败");
                }
                account.setBalance(account.getBalance() - orderBean.getPrice());
                int info8 = userMapper.updateById(account);
                if (info8 != 1) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return ServiceRespModel.error("汇出款项失败");
                }
                commUser.setBalance(commUser.getBalance() + orderBean.getPrice());
                int info1 = userMapper.updateById(commUser);
                if (info1 != 1) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return ServiceRespModel.error("更新卖家余额失败");
                }
                break;
            case 3:
                //校验用户权限
                if (!request.getAttribute("userNo").equals(orderBean.getCreateUser())) {
                    return ServiceRespModel.error("无操作权限");
                }
                if (orderBean.getOrderStatus() == 2) {
                    return ServiceRespModel.error("订单已完成，无法申请取消");
                }
                //向卖家发送邮件
                SendEmailModel model1 = new SendEmailModel();
                model1.setEmail(commUser.getUserEmail());
                model1.setMsg(String.format("您有一个订单申请退款，订单编号为 %s，了解具体信息请登录商城", orderBean.getOrderNo()));
                String json1 = JSON.toJSONString(model1);
                jmsProducer.sendMsg("mail.send.web", json1);
                //订单自动处理
                UpdateOrderModel updateOrderModel1 = new UpdateOrderModel();
                updateOrderModel1.setOrderNo(evt.getOrderNo());
                updateOrderModel1.setBuyer(orderUser.getUserEmail());
                updateOrderModel1.setSeller(commUser.getUserEmail());
                String updateOrderModelJson1 = JSON.toJSONString(updateOrderModel1);
                //设置7天后自动处理
                jmsProducer.delaySend("order.handle", updateOrderModelJson1, 1000 * 60 * 60 * 24 * 7L);
                orderBean.setOrderStatus(evt.getOrderStatus());
                orderBean.setUpdateUser((String) request.getAttribute("userNo"));
                int info2 = orderMapper.updateById(orderBean);
                if (info2 != 1) {
                    return ServiceRespModel.error("更新订单状态失败");
                }
                break;
            case 4:
                //校验用户权限
                if (!request.getAttribute("userNo").equals(commodityBean.getCreateUser())) {
                    return ServiceRespModel.error("无操作权限");
                }
                if (orderBean.getOrderStatus() == 2) {
                    return ServiceRespModel.error("订单已完成，无法取消");
                }
                if (orderBean.getOrderStatus() == 4) {
                    return ServiceRespModel.error("订单已取消");
                }
                orderBean.setOrderStatus(evt.getOrderStatus());
                orderBean.setUpdateUser((String) request.getAttribute("userNo"));
                int info4 = orderMapper.updateById(orderBean);
                if (info4 != 1) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return ServiceRespModel.error("更新订单状态失败");
                }
                account.setBalance(account.getBalance() - orderBean.getPrice());
                int info7 = userMapper.updateById(account);
                if (info7 != 1) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return ServiceRespModel.error("汇出款项失败");
                }
                orderUser.setBalance(orderUser.getBalance() + orderBean.getPrice());
                int info6 = userMapper.updateById(orderUser);
                if (info6 != 1) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return ServiceRespModel.error("更新买家余额失败");
                }
                //向用户发送邮件
                SendEmailModel model2 = new SendEmailModel();
                model2.setEmail(orderUser.getUserEmail());
                model2.setMsg(String.format("您的退款申请已通过，订单编号为 %s，了解具体信息请登录商城", orderBean.getOrderNo()));
                String json2 = JSON.toJSONString(model2);
                jmsProducer.sendMsg("mail.send.web", json2);
                break;
            default:
                return ServiceRespModel.error("订单状态不合法");
        }
        return ServiceRespModel.success("更新订单状态成功");
    }

    /**
     * 删除订单记录
     */
    public ServiceRespModel deleteOrderRecord(HttpServletRequest request, DeleteOrderRecordEvt evt) {
        //校验订单是否存在
        OrderBean orderBean = orderMapper.queryOrderByNo(evt.getOrderNo());
        if (orderBean == null) {
            return ServiceRespModel.error("订单不存在");
        }
        //判断订单状态是否为可删除
        if (!(orderBean.getOrderStatus() == 2 || orderBean.getOrderStatus() == 4)) {
            return ServiceRespModel.error("仅完成或取消的订单可删除");
        }
        //删除订单
        CommodityBean commodityBean = commodityMapper.selectById(orderBean.getCommNo());
        //判断操作对象
        if (orderBean.getCreateUser().equals(request.getAttribute("userNo")) && evt.getMark() == 0) {//买家操作
            orderBean.setBuyerDisplay(1);
            orderBean.setUpdateUser((String) request.getAttribute("userNo"));
            int info = orderMapper.updateById(orderBean);
            if (info != 1) {
                return ServiceRespModel.error("删除订单记录失败");
            }
        } else if (commodityBean.getCreateUser().equals(request.getAttribute("userNo")) && evt.getMark() == 1) {//卖家操作
            orderBean.setSellerDisplay(1);
            orderBean.setUpdateUser((String) request.getAttribute("userNo"));
            int info = orderMapper.updateById(orderBean);
            if (info != 1) {
                return ServiceRespModel.error("删除订单记录失败");
            }
        } else {
            return ServiceRespModel.error("无操作权限");

        }
        return ServiceRespModel.success("删除订单记录成功");
    }


    //订单自动确认与取消
    @JmsListener(destination = "order.handle")
    @Transactional(rollbackFor = Exception.class)
    public void autoUpdateOrder(String json) {
        try {
            UpdateOrderModel model = JSONObject.parseObject(json, UpdateOrderModel.class);
            OrderBean orderBean = orderMapper.queryOrderByNo(model.getOrderNo());
            UserBean account = userMapper.selectById(shopProperties.getAccount());
            if (orderBean == null) {
                throw new OrderSystemException("订单不存在");
            }
            switch (orderBean.getOrderStatus()) {
                case 1:
                    orderBean.setOrderStatus(2);
                    orderBean.setUpdateUser("AUTO");
                    int info = orderMapper.updateById(orderBean);
                    if (info != 1) {
                        throw new OrderSystemException("更新订单状态失败");
                    }
                    account.setBalance(account.getBalance() - orderBean.getPrice());
                    int info4 = userMapper.updateById(account);
                    if (info4 != 1) {
                        throw new OrderSystemException("汇出款项失败");
                    }
                    UserBean seller = userMapper.queryUserByEmail(model.getSeller());
                    seller.setBalance(seller.getBalance() + orderBean.getPrice());
                    int info2 = userMapper.updateById(seller);
                    if (info2 != 1) {
                        throw new OrderSystemException("更新卖家余额失败");
                    }
                    //向买家发送邮件
                    SendEmailModel model1 = new SendEmailModel();
                    model1.setEmail(model.getBuyer());
                    model1.setMsg(String.format("尊敬的用户由于您的订单 %s 长时间未进行确认收货处理，系统已自动完成确认收货", orderBean.getOrderNo()));
                    String json1 = JSON.toJSONString(model1);
                    jmsProducer.sendMsg("mail.send.web", json1);
                    break;
                case 3:
                    orderBean.setOrderStatus(4);
                    orderBean.setUpdateUser("AUTO");
                    int info1 = orderMapper.updateById(orderBean);
                    if (info1 != 1) {
                        throw new OrderSystemException("更新订单状态失败");

                    }
                    account.setBalance(account.getBalance() - orderBean.getPrice());
                    int info5 = userMapper.updateById(account);
                    if (info5 != 1) {
                        throw new OrderSystemException("汇出款项失败");
                    }
                    UserBean buyer = userMapper.queryUserByEmail(model.getBuyer());
                    buyer.setBalance(buyer.getBalance() + orderBean.getPrice());
                    int info3 = userMapper.updateById(buyer);
                    if (info3 != 1) {
                        throw new OrderSystemException("更新卖家余额失败");
                    }
                    //向卖家发送邮件
                    SendEmailModel model2 = new SendEmailModel();
                    model2.setEmail(model.getBuyer());
                    model2.setMsg(String.format("尊敬的商家由于您的订单 %s 长时间未进行确认取消处理，系统已自动完成确认取消", orderBean.getOrderNo()));
                    String json2 = JSON.toJSONString(model2);
                    jmsProducer.sendMsg("mail.send.web", json2);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("订单自动确认与取消系统异常");
        }
    }
}
