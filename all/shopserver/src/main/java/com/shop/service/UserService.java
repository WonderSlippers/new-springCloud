package com.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shop.bean.CommodityBean;
import com.shop.bean.OrderBean;
import com.shop.bean.UserBean;
import com.shop.bean.WithdrawalBean;
import com.shop.config.ShopProperties;
import com.shop.dao.mapperDao.CommodityMapper;
import com.shop.dao.mapperDao.OrderMapper;
import com.shop.dao.mapperDao.UserMapper;
import com.shop.dao.mapperDao.WithdrawalMapper;
import com.shop.evt.*;
import com.shop.model.PluploadModel;
import com.shop.model.ServiceRespModel;
import com.shop.model.UpdateUserModel;
import com.shop.model.UserLoginModel;
import com.shop.utils.ImageUtil;
import com.shop.utils.JwtUtils;
import com.shop.utils.Md5Util;
import com.shop.utils.UploadFileTool;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private CommodityMapper commodityMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private WithdrawalMapper withdrawalMapper;

    @Autowired
    private ShopProperties shopProperties;


    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 登录
     */
    public ServiceRespModel login(UserLoginEvt evt) {
        // 查询用户是否存在
        UserBean userBean = userMapper.queryUserByEmail(evt.getUserEmail());
        if (userBean == null)
            return ServiceRespModel.error("用户不存在");
        // 验证密码是否正确
        if (!(Md5Util.MD5(evt.getUserPassword()).equals(userBean.getUserPassword())))
            return ServiceRespModel.error("密码错误");
        //生成令牌
        String token = JwtUtils.createToken(userBean);
        //更新已发布商品与已卖出商品数量
        QueryWrapper<CommodityBean> commodityBeanWrapper = new QueryWrapper<>();
        commodityBeanWrapper.eq("createUser", userBean.getUserNo());
        userBean.setReleaseCommNum(commodityMapper.selectCount(commodityBeanWrapper));
        QueryWrapper<OrderBean> orderBeanQueryWrapper = new QueryWrapper<>();
        orderBeanQueryWrapper
                .eq("sellerNo", userBean.getUserNo())
                .eq("orderStatus", 2);
        userBean.setSoldCommNum(orderMapper.selectCount(orderBeanQueryWrapper));
        userBean.setLastLoginTime(new Date());
        int info = userMapper.updateById(userBean);
        if (info != 1) {
            return ServiceRespModel.error("更新用户信息失败");
        }
        //返回用户信息与令牌
        UserLoginModel userLoginModel = new UserLoginModel();
        userBean.setUserPassword(null);
        userLoginModel.setToken(token);
        userLoginModel.setUserBean(userBean);
        logger.info(String.format("用户%s登录成功", evt.getUserEmail()));
        return ServiceRespModel.success("登录成功", userLoginModel);
    }

    /**
     * 注册
     */
    public ServiceRespModel register(UserRegisterEvt evt) {
        // 校验用户是否存在
        UserBean userBean = userMapper.queryUserByEmail(evt.getUserEmail());
        if (userBean != null)
            return ServiceRespModel.error("用户已存在");
        //校验验证码
        if (!(Md5Util.MD5(evt.getCode() + evt.getUserEmail() + evt.getTime()).equals(evt.getEncryptionCode()))) {
            return ServiceRespModel.error("验证码错误");
        }
        if (System.currentTimeMillis() - Long.parseLong(evt.getTime()) > 0) {
            return ServiceRespModel.error("验证码已过期");
        }
        // 将用户数据保存至数据库
        UserBean addBean = new UserBean();
        addBean.setUserNo(StringUtils.replace(UUID.randomUUID().toString(), "-", ""));
        addBean.setUserName(evt.getUserName());
        addBean.setUserEmail(evt.getUserEmail());
        addBean.setUserPassword(Md5Util.MD5(evt.getUserPassword()));
        addBean.setIsBan(0);
        addBean.setUserRoot(0);
        addBean.setUnquaComm(0);
        addBean.setAuthentication(0);
        addBean.setBalance(0.00);
        addBean.setStatus("E");
        addBean.setCreateTime(new Date());
        int info = userMapper.insert(addBean);
        if (info == 1) {
            logger.info(String.format("用户%s注册成功", evt.getUserEmail()));
            return ServiceRespModel.success("注册成功");
        }
        return ServiceRespModel.error("注册失败");
    }

    /**
     * 查询用户信息
     */
    public ServiceRespModel queryUserInfoByNo(HttpServletRequest request) {
        // 校验用户是否存在
        UserBean userInfo = userMapper.queryUserByNo((String) request.getAttribute("userNo"));
        if (userInfo == null)
            return ServiceRespModel.error("用户不存在");
        //更新已发布商品与已卖出商品数量
        QueryWrapper<CommodityBean> commodityBeanWrapper = new QueryWrapper<>();
        commodityBeanWrapper.eq("createUser", userInfo.getUserNo());
        userInfo.setReleaseCommNum(commodityMapper.selectCount(commodityBeanWrapper));
        QueryWrapper<OrderBean> orderBeanQueryWrapper = new QueryWrapper<>();
        orderBeanQueryWrapper
                .eq("sellerNo", userInfo.getUserNo())
                .eq("orderStatus", 2);
        userInfo.setSoldCommNum(orderMapper.selectCount(orderBeanQueryWrapper));
        int info = userMapper.updateById(userInfo);
        if (info != 1) {
            return ServiceRespModel.error("更新用户信息失败");
        }
        // 返回用户信息
        return ServiceRespModel.success("用户信息", userInfo);
    }

    /**
     * 修改密码
     */
    public ServiceRespModel changePassword(HttpServletRequest request, ChangePasswordEvt evt) {
        // 校验用户是否存在
        UserBean userInfo = userMapper.queryUserByNo((String) request.getAttribute("userNo"));
        if (userInfo == null)
            return ServiceRespModel.error("用户不存在");
        // 校验旧密码是否正确
        if (!Md5Util.MD5(evt.getOldPassword()).equals(userInfo.getUserPassword())) {
            return ServiceRespModel.error("密码错误");
        }
        //更新密码
        userInfo.setUserPassword(Md5Util.MD5(evt.getNewPassword()));
        userInfo.setUpdateUser((String) request.getAttribute("userNo"));
        int info = userMapper.updateById(userInfo);
        if (info == 1) {
            return ServiceRespModel.success("密码修改成功");
        }
        return ServiceRespModel.error("密码修改失败");
    }

    /**
     * 邮箱验证修改密码
     */
    public ServiceRespModel changePasswordByEmail(ChangePasswordByEmailEvt evt) {
        //校验用户是否存在
        UserBean userBean = userMapper.queryUserByEmail(evt.getUserEmail());
        if (userBean == null) {
            return ServiceRespModel.error("用户不存在");
        }
        //校验验证码
        if (!(Md5Util.MD5(evt.getCode() + evt.getUserEmail() + evt.getTime()).equals(evt.getEncryptionCode()))) {
            return ServiceRespModel.error("验证码错误");
        }
        if (System.currentTimeMillis() - Long.parseLong(evt.getTime()) > 0) {
            return ServiceRespModel.error("验证码已过期");
        }
        //更新密码
        userBean.setUserPassword(Md5Util.MD5(evt.getUserPassword()));
        int info = userMapper.updateById(userBean);
        if (info == 1) {
            return ServiceRespModel.success("邮箱验证修改密码成功");
        }
        return ServiceRespModel.error("邮箱验证修改密码失败");
    }

    /**
     * 修改个人信息
     */
    public ServiceRespModel updateUserInfo(UpdateUserInfoEvt evt, MultipartFile profile, HttpServletRequest request) throws Exception {
        // 校验用户是否存在
        UserBean userInfo = userMapper.queryUserByNo((String) request.getAttribute("userNo"));
        if (userInfo == null)
            return ServiceRespModel.error("用户不存在");
        //更新个人信息
        if (profile != null) {
            String name = StringUtils.replace(profile.getOriginalFilename(), " ", "");
            String fileType = name.substring(name.lastIndexOf(".") + 1);
            if (!ImageUtil.isImage(fileType))
                return ServiceRespModel.error("仅支持图片格式上传");
            PluploadModel pluploadModel = UploadFileTool.upload(profile, shopProperties.getAttachSavePath(), shopProperties.getAttachViewPath());
            userInfo.setProfileUrl(pluploadModel.getViewPath());
        }
        if (evt.getUserName() != null) {
            userInfo.setUserName(evt.getUserName());
        }
        if (evt.getUserInfo() != null) {
            userInfo.setUserInfo(evt.getUserInfo());
        }
        if (evt.getUserSex() != null) {
            userInfo.setUserSex(evt.getUserSex());
        }
        // 将个人信息存至数据库
        int info = userMapper.updateById(userInfo);
        if (info != 1) {
            return ServiceRespModel.error("修改信息失败");
        }
        return ServiceRespModel.success("修改信息成功");
    }

    /**
     * 更新认证信息
     */
    public ServiceRespModel updateAuthenticationInfo(UpdateAuthenticationInfoEvt evt, List<MultipartFile> photo, HttpServletRequest request) throws Exception {
        //查询用户
        UserBean userBean = userMapper.queryUserByNo((String) request.getAttribute("userNo"));
        //上传图片
        UpdateUserModel model = new UpdateUserModel();
        for (int a = 0; a < photo.size(); a++) {
            String name = StringUtils.replace(photo.get(a).getOriginalFilename(), " ", "");
            String fileType = name.substring(name.lastIndexOf(".") + 1);
            if (!ImageUtil.isImage(fileType))
                return ServiceRespModel.error("仅支持图片格式上传");
            PluploadModel pluploadModel = UploadFileTool.upload(photo.get(a), shopProperties.getAttachSavePath(), shopProperties.getAttachViewPath());
            if (a == 1) {
                model.setPhotoUrl1(pluploadModel.getViewPath());
            } else {
                model.setPhotoUrl2(pluploadModel.getViewPath());
            }
        }
        //将认证信息保存至数据库
        userBean.setUserNo((String) request.getAttribute("userNo"));
        userBean.setUserRealName(evt.getUserRealName());
        userBean.setCollege(evt.getCollege());
        userBean.setSno(evt.getSno());
        userBean.setAuthentication(1);
        userBean.setUpdateUser((String) request.getAttribute("userNo"));
        int info = userMapper.updateById(userBean);
        if (info == 0) {
            return ServiceRespModel.error("更新认证信息失败");
        }
        return ServiceRespModel.success("更新认证信息成功");
    }

    /**
     * 申请提现
     */
    public ServiceRespModel applyWithdrawal(HttpServletRequest request, ApplyWithdrawalEvt evt) {
        //校验用户状态
        UserBean userBean = userMapper.queryUserByNo((String) request.getAttribute("userNo"));
        if (userBean.getIsBan() == 1) {
            return ServiceRespModel.error("用户处于封禁状态");
        }
        //提交提现信息
        WithdrawalBean withdrawalBean = new WithdrawalBean();
        withdrawalBean.setFormNo(StringUtils.replace(UUID.randomUUID().toString(), "-", ""));
        withdrawalBean.setCreateUser((String) request.getAttribute("userNo"));
        withdrawalBean.setCardName(evt.getCardName());
        withdrawalBean.setCardNo(evt.getCardNo());
        withdrawalBean.setRealName(evt.getRealName());
        withdrawalBean.setMoney(evt.getMoney());
        withdrawalBean.setStatus("E");
        withdrawalBean.setAuditStatus(0);
        int info = withdrawalMapper.insert(withdrawalBean);
        if (info != 1) {
            return ServiceRespModel.error("申请提现失败");
        }
        return ServiceRespModel.success("申请提现成功");
    }
}
