package com.shop.controller;

import com.shop.anotation.PassToken;
import com.shop.evt.PageEvt;
import com.shop.evt.PreSearchCommEvt;
import com.shop.evt.ReleaseCommEvt;
import com.shop.model.ServiceRespModel;
import com.shop.service.CommodityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/commodity")
@Api(tags = "商品相关接口")
@CrossOrigin
@Validated
public class CommodityController {

    @Autowired
    private CommodityService commodityService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${server.port}")
    private String port;

    /**
     * 初始商品列表
     */
    @PassToken
    @ApiOperation("初始商品列表接口")
    @ApiImplicitParam(name = "num", value = "查询条数", required = true,paramType = "query")
    @RequestMapping(value = "/initialCommList", method = RequestMethod.GET)
    public ServiceRespModel initialCommList(@NotNull(message = "查询条数不能为空") Integer num) {
        try {
            return commodityService.initialCommList(num);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("初始商品列表功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 推荐商品列表
     */
    @PassToken
    @ApiOperation("推荐商品列表接口")
    @ApiImplicitParam(name = "num", value = "查询条数", required = true,paramType = "query")
    @RequestMapping(value = "/recommendCommList", method = RequestMethod.GET)
    public ServiceRespModel recommendCommList(@NotNull(message = "查询条数不能为空") Integer num) {
        try {
            return commodityService.recommendCommList(num);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("推荐商品列表功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 轮播广告列表
     */
    @PassToken
    @ApiOperation("轮播广告列表接口")
    @ApiImplicitParam(name = "num", value = "查询条数", required = true, paramType = "query")
    @RequestMapping(value = "/bannerList", method = RequestMethod.GET)
    public ServiceRespModel bannerList(@NotNull(message = "查询条数不能为空") Integer num) {
        try {
            return commodityService.bannerList(num);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("轮播广告列表功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }


    /**
     * 商品发布
     */
    @ApiOperation("商品发布接口")
    @RequestMapping(value = "/releaseComm", method = RequestMethod.POST)
    public ServiceRespModel releaseComm(@ModelAttribute @Validated ReleaseCommEvt evt, @Size(max = 5, message = "上传的图片数量不合法") List<MultipartFile> commPicList, HttpServletRequest request) {
        try {
            return commodityService.releaseComm(evt, commPicList, request);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("商品发布功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 商品搜索
     */
    @PassToken
    @ApiOperation("商品搜索接口")
    @ApiImplicitParam(name = "keyName", value = "搜索关键字", required = true,paramType = "query")
    @RequestMapping(value = "/searchComm", method = RequestMethod.GET)
    public ServiceRespModel searchComm(@NotBlank(message = "搜索关键字不能为空") String keyName, @ModelAttribute @Validated PageEvt evt) {
        try {
            return commodityService.searchComm(keyName, evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("商品搜索功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 商品预搜索
     */
    @PassToken
    @ApiOperation("商品预搜索接口")
    @RequestMapping(value = "/preSearchComm", method = RequestMethod.GET)
    public ServiceRespModel preSearchComm(@ModelAttribute @Validated PreSearchCommEvt evt) {
        try {
            return commodityService.preSearchComm(evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("商品预搜索功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 查看商品
     */
    @PassToken
    @ApiOperation("查看商品接口")
    @ApiImplicitParam(name = "commNo", value = "商品编码", required = true,paramType = "query")
    @RequestMapping(value = "/queryCommByNo", method = RequestMethod.GET)
    public ServiceRespModel queryCommByNo(@NotBlank(message = "商品编码不能为空") String commNo) {
        try {
            System.out.println("访问端口测试："+port);
            return commodityService.queryCommByNo(commNo);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查看商品功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 删除商品
     */
    @ApiOperation("删除商品接口")
    @ApiImplicitParam(name = "commNo", value = "商品编码", required = true,paramType = "query")
    @RequestMapping(value = "/deleteComm", method = RequestMethod.POST)
    public ServiceRespModel deleteComm(HttpServletRequest request, @NotBlank(message = "商品编码不能为空") String commNo) {
        try {
            return commodityService.deleteComm(request, commNo);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("删除商品功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 商品标签分类
     */
    @PassToken
    @ApiOperation("商品标签分类接口")
    @ApiImplicitParam(name = "commTag", value = "商品标签", required = true,paramType = "query")
    @RequestMapping(value = "/queryCommByTag", method = RequestMethod.GET)
    public ServiceRespModel queryCommByTag(@NotNull(message = "商品标签不能为空") Integer commTag, @ModelAttribute @Validated PageEvt evt) {
        try {
            return commodityService.queryCommByTag(commTag, evt);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("商品标签分类功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

    /**
     * 查看用户发布的商品接口
     */
    @ApiOperation("查看用户发布的商品接口")
    @RequestMapping(value = "/queryUserComm", method = RequestMethod.GET)
    public ServiceRespModel queryUserComm(HttpServletRequest request) {
        try {
            return commodityService.queryUserComm(request);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查看用户发布的商品功能异常");
            return ServiceRespModel.error("系统异常");
        }
    }

}
