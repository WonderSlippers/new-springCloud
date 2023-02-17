package com.shop.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.async.JmsProducer;
import com.shop.bean.*;
import com.shop.dao.mapperDao.UserMapper;
import com.shop.config.ShopProperties;
import com.shop.dao.mapperDao.*;
import com.shop.evt.*;
import com.shop.exceptions.AuditCommException;
import com.shop.exceptions.CommCommentException;
import com.shop.model.*;
import com.shop.utils.ImageUtil;
import com.shop.utils.UploadFileTool;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AdminService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private CommodityMapper commodityMapper;

    @Resource
    private CommCommentMapper commCommentMapper;

    @Resource
    private AdvertMapper advertMapper;

    @Resource
    private CommentReportMapper commentReportMapper;

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private WithdrawalMapper withdrawalMapper;

    @Resource
    private OrderMapper orderMapper;

    @Autowired
    private JmsProducer jmsProducer;

    @Autowired
    private ShopProperties shopProperties;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 商品审核
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceRespModel auditComm(HttpServletRequest request, AuditCommEvt evt) {
        //校验用户权限
        UserBean userBean = queryAdmin((String) request.getAttribute("userNo"));
        if (userBean == null)
            return ServiceRespModel.error("用户不存在");
        if (userBean.getUserRoot() != 1) {
            return ServiceRespModel.error("无操作权限");
        }
        if (evt.getAuditStatus() == 2) {
            if (evt.getAuditMsg() == null) {
                return ServiceRespModel.error("审核留言不能为空");
            }
        }
        //检验商品是否存在
        CommodityBean commodityBean = commodityMapper.queryCommByNo(evt.getCommNo());
        if (commodityBean == null) {
            return ServiceRespModel.error("商品不存在");
        }
        //查询卖家
        UserBean seller = userMapper.queryUserByNo(commodityBean.getCreateUser());
        try {
            //更新用户不合格商品数
            if (evt.getAuditStatus() == 2) {
                seller.setUnquaComm(seller.getUnquaComm() + 1);
                seller.setUpdateUser((String) request.getAttribute("userNo"));
                int info1 = userMapper.updateById(seller);
                if (info1 != 1) {
                    throw new AuditCommException("更新用户不合格商品数失败");
                }
                //发送邮件
                SendEmailModel model = new SendEmailModel();
                model.setEmail(seller.getUserEmail());
                model.setMsg(String.format("您发布的商品 %s 审核未通过，商品编码为 %s ，审核未通过原因：%s", commodityBean.getCommName(), evt.getCommNo(), evt.getAuditMsg()));
                String json = JSON.toJSONString(model);
                jmsProducer.sendMsg("mail.send.web", json);
            }
            //修改商品审核状态
            commodityBean.setAuditStatus(evt.getAuditStatus());
            commodityBean.setAuditMsg(evt.getAuditMsg());
            commodityBean.setUpdateUser((String) request.getAttribute("userNo"));
            commodityBean.setAuditor((String) request.getAttribute("userName"));
            commodityBean.setAuditTime(new Date());
            int info = commodityMapper.updateById(commodityBean);
            if (info != 1) {
                throw new AuditCommException("商品审核失败");
            }
        } catch (AuditCommException e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ServiceRespModel.error(e.getMessage());
        }
        return ServiceRespModel.success("商品审核成功");
    }

    /**
     * 查看全部商品
     */
    public ServiceRespModel commList(HttpServletRequest request, PageEvt evt, Integer auditStatus) {
        //校验用户权限
        UserBean userBean = queryAdmin((String) request.getAttribute("userNo"));
        if (userBean == null)
            return ServiceRespModel.error("用户不存在");
        if (userBean.getUserRoot() != 1) {
            return ServiceRespModel.error("无操作权限");
        }
        //返回全部商品列表
        Page<CommodityBean> page = new Page<>(evt.getCurrent(), evt.getSize());
        QueryWrapper<CommodityBean> qwComm = new QueryWrapper<>();
        qwComm.eq("status", "E")
                .eq(auditStatus != null, "auditStatus", auditStatus);
        Page<CommodityBean> commodityBeanPage = commodityMapper.selectPage(page, qwComm);
        List<CommModel> commModelList = queryCommPic(commodityBeanPage.getRecords());
        PageModel pageModel = new PageModel(commModelList, commodityBeanPage.getCurrent(), commodityBeanPage.getPages());
        return ServiceRespModel.success("全部商品列表", pageModel);
    }

    /**
     * 设置用户封禁状态
     */
    public ServiceRespModel setUserIsBan(HttpServletRequest request, SetUserIsBanEvt evt) {
        //校验用户权限
        UserBean admin = queryAdmin((String) request.getAttribute("userNo"));
        if (admin == null)
            return ServiceRespModel.error("用户不存在");
        if (admin.getUserRoot() != 1) {
            return ServiceRespModel.error("无操作权限");
        }
        //校验被操作用户是否存在
        UserBean user = userMapper.queryUserByNo(evt.getUserNo());
        if (user == null)
            return ServiceRespModel.error("用户不存在");
        //发送邮件
        if (evt.getIsBan() == 1) {
            SendEmailModel model = new SendEmailModel();
            model.setEmail(user.getUserEmail());
            model.setMsg(String.format("您的账号因多次发布不合格商品已被封禁，您将不能发布商品与购买商品"));
            String json = JSON.toJSONString(model);
            jmsProducer.sendMsg("mail.send.web", json);
        }
        //更新封禁状态
        user.setUpdateUser((String) request.getAttribute("userNo"));
        user.setIsBan(evt.getIsBan());
        int info = userMapper.updateById(user);
        if (info == 1) {
            return ServiceRespModel.success("设置用户封禁状态成功");
        }
        return ServiceRespModel.error("设置用户封禁状态失败");
    }

    /**
     * 全部用户列表
     */
    public ServiceRespModel userList(HttpServletRequest request, PageEvt evt) {
        //校验用户权限
        UserBean userBean = queryAdmin((String) request.getAttribute("userNo"));
        if (userBean == null)
            return ServiceRespModel.error("用户不存在");
        if (userBean.getUserRoot() != 1) {
            return ServiceRespModel.error("无操作权限");
        }
        //分页查询
        Page<UserBean> page = new Page<>(evt.getCurrent(), evt.getSize());
        QueryWrapper<UserBean> qwUser = new QueryWrapper<>();
        qwUser.eq("status", "E");
        Page<UserBean> userBeanPage = userMapper.selectPage(page, qwUser);
        List<AdminUserListModel> models = new ArrayList<>();
        for (UserBean user : userBeanPage.getRecords()) {
            AdminUserListModel model = new AdminUserListModel();
            model.setUserBean(user);
            model.getPhotoList().add(user.getPhotoUrl1());
            model.getPhotoList().add(user.getPhotoUrl2());
            models.add(model);
        }
        PageModel pageModel = new PageModel(models, userBeanPage.getCurrent(), userBeanPage.getPages());
        return ServiceRespModel.success("全部用户列表", pageModel);
    }

    /**
     * 用户认证信息审核
     */
    public ServiceRespModel auditUserAuthentication(HttpServletRequest request, AuditAuthenticationEvt evt) {
        //校验用户权限
        UserBean userBean = queryAdmin((String) request.getAttribute("userNo"));
        if (userBean == null)
            return ServiceRespModel.error("用户不存在");
        if (userBean.getUserRoot() != 1) {
            return ServiceRespModel.error("无操作权限");
        }
        //校验被审核用户是否存在
        UserBean user = userMapper.queryUserByNo(evt.getUserNo());
        if (user == null)
            return ServiceRespModel.error("用户不存在");
        //更新审核信息
        user.setAuthentication(evt.getAuthentication());
        user.setUpdateUser((String) request.getAttribute("userNo"));
        int info = userMapper.updateById(user);
        if (info == 1) {
            //发送邮件
            if (evt.getAuthentication() == 2) {
                SendEmailModel sendEmailModel = new SendEmailModel();
                sendEmailModel.setEmail(user.getUserEmail());
                sendEmailModel.setMsg(String.format("您好%s，您的用户认证已通过", user.getUserName()));
                String json = JSON.toJSONString(sendEmailModel);
                jmsProducer.sendMsg("mail.send.web", json);
            }
            if (evt.getAuthentication() == 3) {
                SendEmailModel sendEmailModel = new SendEmailModel();
                sendEmailModel.setEmail(user.getUserEmail());
                sendEmailModel.setMsg(String.format("您好%s，您的用户认证未能通过", user.getUserName()));
                String json = JSON.toJSONString(sendEmailModel);
                jmsProducer.sendMsg("mail.send.web", json);
            }
            return ServiceRespModel.success("设置用户认证状态成功");
        }
        return ServiceRespModel.error("设置用户认证状态失败");
    }

    /**
     * 设置商品推荐
     */
    public ServiceRespModel setCommRec(HttpServletRequest request, SetCommRecEvt evt) {
        //校验用户权限
        UserBean userBean = queryAdmin((String) request.getAttribute("userNo"));
        if (userBean == null)
            return ServiceRespModel.error("用户不存在");
        if (userBean.getUserRoot() != 1) {
            return ServiceRespModel.error("无操作权限");
        }
        //校验商品是否存在
        CommodityBean comm = commodityMapper.queryCommByNo(evt.getCommNo());
        if (comm == null)
            return ServiceRespModel.error("商品不存在");
        //设置商品推荐
        comm.setRecommend(evt.getRecommend());
        comm.setUpdateUser((String) request.getAttribute("userNo"));
        int info = commodityMapper.updateById(comm);
        if (info == 1) {
            return ServiceRespModel.success("设置商品推荐成功");
        }
        return ServiceRespModel.error("设置商品推荐失败");
    }

    /**
     * 新增广告
     */
    public ServiceRespModel insertAdvert(HttpServletRequest request, InsertAdvertEvt evt, MultipartFile picture) throws Exception {
        //校验用户权限
        UserBean userBean = queryAdmin((String) request.getAttribute("userNo"));
        if (userBean == null)
            return ServiceRespModel.error("用户不存在");
        if (userBean.getUserRoot() != 1) {
            return ServiceRespModel.error("无操作权限");
        }
        //新增广告
        String name = StringUtils.replace(picture.getOriginalFilename(), " ", "");
        String fileType = name.substring(name.lastIndexOf(".") + 1);
        if (!ImageUtil.isImage(fileType))
            return ServiceRespModel.error("仅支持图片格式上传");
        PluploadModel pluploadModel = UploadFileTool.upload(picture, shopProperties.getAttachSavePath(), shopProperties.getAttachViewPath());
        AdvertBean advertBean = new AdvertBean();
        advertBean.setCreateUser((String) request.getAttribute("userName"));
        advertBean.setPicUrl(pluploadModel.getViewPath());
        advertBean.setUrl(evt.getUrl());
        advertBean.setStatus("E");
        int info = advertMapper.insert(advertBean);
        if (info == 1) {
            return ServiceRespModel.success("新增广告成功");
        }
        return ServiceRespModel.error("新增广告失败");
    }

    /**
     * 删除广告
     */
    public ServiceRespModel deleteAdvert(HttpServletRequest request, Integer advertNo) {
        //校验用户权限
        UserBean userBean = queryAdmin((String) request.getAttribute("userNo"));
        if (userBean == null)
            return ServiceRespModel.error("用户不存在");
        if (userBean.getUserRoot() != 1) {
            return ServiceRespModel.error("无操作权限");
        }
        //查询广告是否存在
        AdvertBean advertBean = advertMapper.queryAdvertByNo(advertNo);
        if (advertBean == null) {
            return ServiceRespModel.error("广告不存在");
        }
        //删除广告
        advertBean.setStatus("E");
        advertBean.setUpdateUser((String) request.getAttribute("userNo"));
        int info = advertMapper.updateById(advertBean);
        if (info == 1) {
            return ServiceRespModel.success("删除广告成功");
        }
        return ServiceRespModel.error("删除广告失败");
    }

    /**
     * 查询所有被举报的评论
     */
    public ServiceRespModel queryCommCommentList(HttpServletRequest request, PageEvt evt) {
        //校验用户权限
        UserBean userBean = queryAdmin((String) request.getAttribute("userNo"));
        if (userBean == null)
            return ServiceRespModel.error("用户不存在");
        if (userBean.getUserRoot() != 1) {
            return ServiceRespModel.error("无操作权限");
        }
        //查询所有评论举报信息
        Page<QueryReportedCommentModel> page = new Page<>(evt.getCurrent(), evt.getSize());
        Page<QueryReportedCommentModel> queryReportedCommentModelPage = adminMapper.queryCommCommentList(page);
        PageModel pageModel = new PageModel(queryReportedCommentModelPage.getRecords(), queryReportedCommentModelPage.getCurrent(), queryReportedCommentModelPage.getPages());
        return ServiceRespModel.success("评论举报信息列表", pageModel);
    }

    /**
     * 删除评论
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceRespModel deleteCommComment(HttpServletRequest request, String commentNo) {
        //校验用户权限
        UserBean userBean = queryAdmin((String) request.getAttribute("userNo"));
        if (userBean == null)
            return ServiceRespModel.error("用户不存在");
        if (userBean.getUserRoot() != 1) {
            return ServiceRespModel.error("无操作权限");
        }
        //校验评论是否存在
        CommCommentBean commCommentBean = commCommentMapper.selectById(commentNo);
        if (commCommentBean == null) {
            return ServiceRespModel.error("评论不存在");
        }
        //删除评论
        try {
            commCommentBean.setStatus("D");
            commCommentBean.setUpdateUser((String) request.getAttribute("userNo"));
            int info = commCommentMapper.updateById(commCommentBean);
            if (info != 1) {
                throw new CommCommentException("删除评论失败");
            }
            UpdateWrapper<CommentReportBean> uwReport = new UpdateWrapper<>();
            uwReport.eq("commentNo", commentNo);
            CommentReportBean commentReportBean = new CommentReportBean();
            commentReportBean.setStatus("D");
            commentReportBean.setUpdateUser((String) request.getAttribute("userNo"));
            commentReportMapper.update(commentReportBean, uwReport);
        } catch (CommCommentException e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ServiceRespModel.error(e.getMessage());
        }
        return ServiceRespModel.success("删除评论成功");
    }

    /**
     * 审核提现
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceRespModel auditWithdrawal(HttpServletRequest request, AuditWithdrawalEvt evt) {
        //校验用户权限
        UserBean userBean = queryAdmin((String) request.getAttribute("userNo"));
        if (userBean == null)
            return ServiceRespModel.error("用户不存在");
        if (userBean.getUserRoot() != 1) {
            return ServiceRespModel.error("无操作权限");
        }
        //校验提现申请是否存在
        WithdrawalBean withdrawalBean = withdrawalMapper.queryWithdrawalByNo(evt.getFormNo());
        if (withdrawalBean == null) {
            return ServiceRespModel.error("提现申请不存在");
        }
        //校验审核状态
        if (withdrawalBean.getAuditStatus() == 2) {
            return ServiceRespModel.error("提现申请已被驳回");
        }
        if (withdrawalBean.getAuditStatus() == 1) {
            return ServiceRespModel.error("提现申请已通过");
        }
        //校验余额
        UserBean user = userMapper.queryUserByNo(withdrawalBean.getCreateUser());
        if (user == null) {
            return ServiceRespModel.error("被操作用户不存在");
        }
        if (userBean.getBalance() - withdrawalBean.getMoney() < 0) {
            return ServiceRespModel.error("余额不足");
        }
        //更新余额
        user.setBalance(user.getBalance() - withdrawalBean.getMoney());
        user.setUpdateUser((String) request.getAttribute("userNo"));
        int info1 = userMapper.updateById(user);
        if (info1 != 1) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ServiceRespModel.error("更新余额失败");
        }
        //更新审核状态
        withdrawalBean.setAuditStatus(evt.getAuditStatus());
        withdrawalBean.setUpdateUser((String) request.getAttribute("userNo"));
        int info = withdrawalMapper.updateById(withdrawalBean);
        if (info != 1) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ServiceRespModel.error("审核提现成功");
        }
        return ServiceRespModel.success("审核提现成功");
    }

    /**
     * 查询网站数据
     */
    public ServiceRespModel queryStatistic(HttpServletRequest request) {
        //校验用户权限
        UserBean userBean = queryAdmin((String) request.getAttribute("userNo"));
        if (userBean == null)
            return ServiceRespModel.error("用户不存在");
        if (userBean.getUserRoot() != 1) {
            return ServiceRespModel.error("无操作权限");
        }
        QueryWrapper<UserBean> qwUser = new QueryWrapper<>();
        qwUser.eq("status", "E");
        int totalUsers = userMapper.selectCount(qwUser);
        QueryWrapper<OrderBean> qwOrder = new QueryWrapper<>();
        qwOrder.eq("status", "E");
        int totalOrders = orderMapper.selectCount(qwOrder);
        QueryWrapper<CommodityBean> qwComm = new QueryWrapper<>();
        qwComm.eq("status", "E");
        int totalComms = commodityMapper.selectCount(qwComm);
        List<CommStModel> commStModelList = adminMapper.queryCommSt();
        List<CommStModel> commStModelList1 = new ArrayList<>();
        commStModelList1.add(new CommStModel("衣物", 0));
        commStModelList1.add(new CommStModel("数码", 0));
        commStModelList1.add(new CommStModel("食品", 0));
        commStModelList1.add(new CommStModel("图书", 0));
        commStModelList1.add(new CommStModel("化妆品", 0));
        commStModelList1.add(new CommStModel("文具", 0));
        commStModelList1.add(new CommStModel("居家", 0));
        for (CommStModel model : commStModelList) {
            commStModelList1.get(Integer.parseInt(model.getCommTag())).setNum(model.getNum());
        }
        StatisticModel statisticModel = new StatisticModel();
        statisticModel.setCommStModelList(commStModelList1);
        statisticModel.setTotalComms(totalComms);
        statisticModel.setTotalUsers(totalUsers);
        statisticModel.setTotalOrders(totalOrders);
        return ServiceRespModel.success("数据", statisticModel);
    }

    /**
     * 查询提现申请列表
     */
    public ServiceRespModel queryWithdrawalList(HttpServletRequest request, PageEvt evt) {
        //校验用户权限
        UserBean userBean = queryAdmin((String) request.getAttribute("userNo"));
        if (userBean == null)
            return ServiceRespModel.error("用户不存在");
        if (userBean.getUserRoot() != 1) {
            return ServiceRespModel.error("无操作权限");
        }
        Page<WithdrawalInfoModel> page = new Page<>(evt.getCurrent(), evt.getSize());
        Page<WithdrawalInfoModel> withdrawalBeanList = adminMapper.queryWithdrawalList(page);
        PageModel pageModel = new PageModel(withdrawalBeanList.getRecords(), withdrawalBeanList.getCurrent(), withdrawalBeanList.getPages());
        return ServiceRespModel.success("提现申请列表", pageModel);
    }

    //查询商品对应图片
    private List<CommModel> queryCommPic(List<CommodityBean> commodityBeanList) {
        List<CommModel> commModelList = new ArrayList<>();
        for (CommodityBean commodityBean : commodityBeanList) {
            CommModel model = new CommModel();
            model.setCommPicList(commodityMapper.queryPicByCommNo(commodityBean.getCommNo()));
            model.setCommodity(commodityBean);
            commModelList.add(model);
        }
        return commModelList;
    }

    //查询管理员
    private UserBean queryAdmin(String userNo) {
        QueryWrapper<UserBean> qAdmin = new QueryWrapper();
        qAdmin.eq("status", "E")
                .eq("userNo", userNo);
        UserBean userBean = userMapper.selectOne(qAdmin);
        return userBean;
    }

}
