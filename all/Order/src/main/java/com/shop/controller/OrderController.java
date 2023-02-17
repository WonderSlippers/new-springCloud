package com.shop.controller;

import com.shop.evt.DeleteOrderRecordEvt;
import com.shop.evt.SubmitOrderEvt;
import com.shop.evt.UpdateOrderStatusEvt;
import com.shop.model.ServiceRespModel;
import com.shop.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/order")
@Api(tags = "订单相关接口")
@CrossOrigin
@Validated
public class OrderController {

    @Autowired
    private OrderService orderService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 提交订单
     */
    @ApiOperation("提交订单接口")
    @RequestMapping(value = "/submitOrder", method = RequestMethod.POST)
    public ServiceRespModel submitOrder( HttpServletRequest request, @ModelAttribute @Validated SubmitOrderEvt evt) {
        try {
            return orderService.submitOrder(request, evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("提交订单功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 查看用户提交的订单列表
     */
    @ApiOperation("查看用户提交的订单列表接口")
    @RequestMapping(value = "/queryUserSubmitOrderList", method = RequestMethod.GET)
    public ServiceRespModel queryUserSubmitOrderList(HttpServletRequest request) {
        try {
            return orderService.queryUserSubmitOrderList(request);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查看用户提交的订单列表功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 查看用户接收的订单列表
     */
    @ApiOperation("查看用户接收的订单列表接口")
    @RequestMapping(value = "/queryUserReceiveOrderList", method = RequestMethod.GET)
    public ServiceRespModel queryUserReceiveOrderList(HttpServletRequest request) {
        try {
            return orderService.queryUserReceiveOrderList(request);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查看用户接收的订单列表功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 更新订单状态
     */
    @ApiOperation("更新订单状态接口")
    @RequestMapping(value = "/updateOrderStatus", method = RequestMethod.POST)
    public ServiceRespModel updateOrderStatus(HttpServletRequest request, @ModelAttribute @Validated UpdateOrderStatusEvt evt) {
        try {
            return orderService.updateOrderStatus(request, evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("更新订单状态功能异常");

            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 删除订单记录
     */
    @ApiOperation("删除订单记录接口")
    @RequestMapping(value = "/deleteOrderRecord", method = RequestMethod.POST)
    public ServiceRespModel deleteOrderRecord(HttpServletRequest request, @ModelAttribute @Validated DeleteOrderRecordEvt evt) {
        try {
            return orderService.deleteOrderRecord(request, evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("删除订单记录功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

}
