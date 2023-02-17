package com.shop.controller;

import com.shop.evt.*;
import com.shop.model.ServiceRespModel;
import com.shop.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.hibernate.validator.constraints.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/admin")
@Api(tags = "管理员相关接口")
@CrossOrigin
@Validated
public class AdminController {

    @Autowired
    private AdminService adminService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 商品审核
     */
    @ApiOperation("商品审核接口")
    @RequestMapping(value = "/auditComm", method = RequestMethod.POST)
    public ServiceRespModel auditComm(HttpServletRequest request, @ModelAttribute @Validated AuditCommEvt evt) {
        try {
            return adminService.auditComm(request, evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("商品审核功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 查看全部商品
     */
    @ApiOperation("查看全部商品接口")
    @ApiImplicitParam(name = "auditStatus", value = "审核状态(0 = 审核中,1 = 通过,2 = 不通过)", example = "0", dataType = "int", paramType = "query")
    @RequestMapping(value = "/commList", method = RequestMethod.GET)
    public ServiceRespModel commList(HttpServletRequest request, @Range(min = 0, max = 2, message = "审核状态不合法") Integer auditStatus, @ModelAttribute @Validated PageEvt evt) {
        try {
            return adminService.commList(request, evt, auditStatus);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查看全部商品功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 设置用户封禁状态
     */
    @ApiOperation("设置用户封禁状态接口")
    @RequestMapping(value = "/setUserIsBan", method = RequestMethod.POST)
    public ServiceRespModel setUserIsBan(HttpServletRequest request, @ModelAttribute @Validated SetUserIsBanEvt evt) {
        try {
            return adminService.setUserIsBan(request, evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("设置用户封禁状态功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 查看全部用户
     */
    @ApiOperation("查看全部用户接口")
    @RequestMapping(value = "/userList", method = RequestMethod.POST)
    public ServiceRespModel userList(HttpServletRequest request, @ModelAttribute @Validated PageEvt evt) {
        try {
            return adminService.userList(request, evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查看全部用户功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 用户认证信息审核
     */
    @ApiOperation("用户认证信息审核接口")
    @RequestMapping(value = "/auditUserAuthentication", method = RequestMethod.POST)
    public ServiceRespModel auditUserAuthentication(HttpServletRequest request, @ModelAttribute @Validated AuditAuthenticationEvt evt) {
        try {
            return adminService.auditUserAuthentication(request, evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("用户认证信息审核功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 设置商品推荐
     */
    @ApiOperation("设置商品推荐接口")
    @RequestMapping(value = "/setCommRec", method = RequestMethod.POST)
    public ServiceRespModel setCommRec(HttpServletRequest request, @ModelAttribute @Validated SetCommRecEvt evt) {
        try {
            return adminService.setCommRec(request, evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("设置商品推荐功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 新增广告
     */
    @ApiOperation("新增广告接口")
    @RequestMapping(value = "/insertAdvert", method = RequestMethod.POST)
    public ServiceRespModel insertAdvert(HttpServletRequest request, @ModelAttribute @Validated InsertAdvertEvt
            evt, @NotNull(message = "图片不能为空") MultipartFile picture) {
        try {
            return adminService.insertAdvert(request, evt, picture);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("新增广告功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 删除广告
     */
    @ApiOperation("删除广告接口")
    @ApiImplicitParam(name = "advertNo", value = "广告编码", required = true, example = "1", paramType = "query")
    @RequestMapping(value = "/deleteAdvert", method = RequestMethod.POST)
    public ServiceRespModel deleteAdvert(HttpServletRequest request, @NotBlank(message = "广告编码不能为空") Integer advertNo) {
        try {
            return adminService.deleteAdvert(request, advertNo);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("删除广告功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 查询所有评论举报信息
     */
    @ApiOperation("查询所有评论举报信息")
    @RequestMapping(value = "/queryCommCommentList", method = RequestMethod.GET)
    public ServiceRespModel queryCommCommentList(HttpServletRequest request, @ModelAttribute @Validated PageEvt evt) {
        try {
            return adminService.queryCommCommentList(request, evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查询所有评论举报信息功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 删除评论
     */
    @ApiOperation("删除评论接口")
    @ApiImplicitParam(name = "commentNo", value = "评论编码", required = true, paramType = "query")
    @RequestMapping(value = "/deleteCommComment", method = RequestMethod.POST)
    public ServiceRespModel deleteCommComment(HttpServletRequest request, @NotBlank(message = "评论编码不能为空") String commentNo) {
        try {
            return adminService.deleteCommComment(request, commentNo);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("删除评论功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 审核提现
     */
    @ApiOperation("审核提现接口")
    @RequestMapping(value = "/auditWithdrawal", method = RequestMethod.POST)
    public ServiceRespModel auditWithdrawal(HttpServletRequest request, @ModelAttribute @Validated AuditWithdrawalEvt evt) {
        try {
            return adminService.auditWithdrawal(request, evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("删除评论功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 查询网站数据
     */
    @ApiOperation("查询网站数据接口")
    @RequestMapping(value = "/queryStatistic", method = RequestMethod.GET)
    public ServiceRespModel queryStatistic(HttpServletRequest request) {
        try {
            return adminService.queryStatistic(request);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查询网站数据功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 查询提现申请列表
     */
    @ApiOperation("查询提现申请列表接口")
    @RequestMapping(value = "/queryWithdrawalList", method = RequestMethod.GET)
    public ServiceRespModel queryWithdrawalList(HttpServletRequest request, @ModelAttribute @Validated PageEvt evt) {
        try {
            return adminService.queryWithdrawalList(request, evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查询提现申请列表功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }
}
