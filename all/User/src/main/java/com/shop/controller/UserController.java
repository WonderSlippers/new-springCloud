package com.shop.controller;

import com.shop.anotation.PassToken;
import com.shop.evt.*;
import com.shop.model.ServiceRespModel;
import com.shop.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/user")
@Api(tags = "用户相关接口")
@CrossOrigin
@Validated
public class UserController {

    @Autowired//创建对象
    private UserService userService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 用户登录
     */
    @PassToken
    @ApiOperation("用户登录接口")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ServiceRespModel login(@ModelAttribute @Validated UserLoginEvt evt) {
        try {
            System.out.println();
            return userService.login(evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("用户登录功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 用户注册
     */
    @PassToken
    @ApiOperation("用户注册接口")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ServiceRespModel register(@ModelAttribute @Validated UserRegisterEvt evt) {
        try {
            return userService.register(evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("用户注册功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 查询用户详情
     */
    @ApiOperation("查询用户详情接口")
    @RequestMapping(value = "/userInfo", method = RequestMethod.GET)
    public ServiceRespModel queryUserInfo(HttpServletRequest request) {
        try {
            return userService.queryUserInfoByNo(request);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查询用户详情功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 修改密码
     */
    @ApiOperation("修改密码接口")
    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    public ServiceRespModel changePassword(HttpServletRequest request, @ModelAttribute @Validated ChangePasswordEvt evt) {
        try {
            return userService.changePassword(request, evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("修改密码功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 邮箱验证修改密码
     */
    @PassToken
    @ApiOperation("邮箱验证修改密码接口")
    @RequestMapping(value = "/forgetPassword", method = RequestMethod.POST)
    public ServiceRespModel changePasswordByEmail(@ModelAttribute @Validated ChangePasswordByEmailEvt evt) {
        try {
            return userService.changePasswordByEmail(evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("邮箱验证修改密码功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 修改个人信息
     */
    @ApiOperation("修改个人信息接口")
    @RequestMapping(value = "/updateUserInfo", method = RequestMethod.POST)
    public ServiceRespModel updateUserInfo(@ModelAttribute @Validated UpdateUserInfoEvt evt, MultipartFile profile, HttpServletRequest request) {
        try {
            return userService.updateUserInfo(evt, profile, request);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("修改个人信息功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 更新认证信息
     */
    @ApiOperation("更新认证信息接口")
    @RequestMapping(value = "/uploadAuthenticationInfo", method = RequestMethod.POST)
    public ServiceRespModel updateAuthenticationInfo(@ModelAttribute @Validated UpdateAuthenticationInfoEvt evt,
                                                     @NotNull(message = "照片数量不能为空") @Size(max = 2, message = "照片数量不合法") List<MultipartFile> photo,
                                                     HttpServletRequest request) {
        try {
            return userService.updateAuthenticationInfo(evt, photo, request);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("更新认证信息功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 申请提现
     */
    @ApiOperation("申请提现接口")
    @RequestMapping(value = "/applyWithdrawal", method = RequestMethod.POST)
    public ServiceRespModel applyWithdrawal(HttpServletRequest request, @ModelAttribute @Validated ApplyWithdrawalEvt evt) {
        try {
            return userService.applyWithdrawal(request, evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("申请提现功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

}
