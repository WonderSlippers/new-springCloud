package com.shop.controller;

import com.shop.anotation.PassToken;
import com.shop.evt.InsertCommCommentEvt;
import com.shop.evt.QueryCommCommentListEvt;
import com.shop.evt.ReportCommentEvt;
import com.shop.model.ServiceRespModel;
import com.shop.service.CommCommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/commComment")
@Api(tags = "商品评论相关接口")
@CrossOrigin
@Validated
public class CommCommentController {

    @Autowired
    private CommCommentService commCommentService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 发布评论
     */
    @ApiOperation("发布评论接口")
    @RequestMapping(value = "/insertCommComment", method = RequestMethod.POST)
    public ServiceRespModel insertCommComment(HttpServletRequest request, @Validated @ModelAttribute InsertCommCommentEvt evt) {
        try {
            return commCommentService.insertCommComment(request, evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("发布评论功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 举报评论
     */
    @PassToken
    @ApiOperation("举报评论接口")
    @ApiImplicitParam(name = "commentNo", value = "评论编码", required = true, paramType = "query")
    @RequestMapping(value = "/reportCommComment", method = RequestMethod.POST)
    public ServiceRespModel reportCommComment(@ModelAttribute @Validated ReportCommentEvt evt) {
        try {
            return commCommentService.reportCommComment(evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("举报评论功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 查询商品评论列表
     */
    @PassToken
    @ApiOperation("查询商品评论列表接口")
    @RequestMapping(value = "/queryCommCommentList", method = RequestMethod.GET)
    public ServiceRespModel queryCommCommentList(@ModelAttribute @Validated QueryCommCommentListEvt evt) {
        try {
            return commCommentService.queryCommCommentList(evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查询商品评论列表功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }
}
