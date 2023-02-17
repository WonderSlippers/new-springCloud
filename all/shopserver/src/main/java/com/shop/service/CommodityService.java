package com.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.bean.CommPicBean;
import com.shop.bean.CommodityBean;
import com.shop.bean.UserBean;
import com.shop.config.ShopProperties;
import com.shop.dao.mapperDao.CommPicMapper;
import com.shop.dao.mapperDao.CommodityMapper;
import com.shop.dao.mapperDao.UserMapper;
import com.shop.evt.PageEvt;
import com.shop.evt.PreSearchCommEvt;
import com.shop.evt.ReleaseCommEvt;
import com.shop.exceptions.CommReleaseException;
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
import java.util.UUID;

@Service
public class CommodityService {

    @Resource
    private CommodityMapper commodityMapper;

    @Resource
    private CommPicMapper commPicMapper;

    @Resource
    private UserMapper userMapper;

    @Autowired
    private ShopProperties shopProperties;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 初始商品列表
     */
    public ServiceRespModel initialCommList(Integer num) {
        //查询商品
        RandomCommListModel model = new RandomCommListModel();
        model.setNum(num);
        List<CommodityBean> commodityBeanList = commodityMapper.randomCommList(model);
        //查询商品对应的图片
        List<CommModel> commModelList = queryCommPic(commodityBeanList);
        return ServiceRespModel.success("初始商品列表", commModelList);
    }

    /**
     * 推荐商品列表
     */
    public ServiceRespModel recommendCommList(Integer num) {
        //校验入参合法性
        if (num == null) {
            return ServiceRespModel.error("查询条数不能为空");
        }
        //查询商品
        RandomCommListModel model = new RandomCommListModel();
        model.setNum(num);
        model.setRecommend(1);
        List<CommodityBean> commodityBeanList = commodityMapper.randomCommList(model);
        //查询商品对应的图片
        List<CommModel> commModelList = queryCommPic(commodityBeanList);
        return ServiceRespModel.success("推荐商品列表", commModelList);
    }

    /**
     * 发布商品
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceRespModel releaseComm(ReleaseCommEvt evt, List<MultipartFile> commPicList, HttpServletRequest request) throws Exception {
        //校验用户状态
        UserBean userBean = userMapper.queryUserByNo((String) request.getAttribute("userNo"));
        if (userBean == null)
            return ServiceRespModel.error("用户不存在");
        if (userBean.getIsBan() == 1) {
            return ServiceRespModel.error("用户处于封禁状态");
        }
        if (userBean.getAuthentication() != 2) {
            return new ServiceRespModel(2, "用户认证未通过", null);
        }
        //校验商品价格
        String price = evt.getCommPrice() + "";
        if (price.length() - (price + "").indexOf(".") - 1 > 2) {
            return ServiceRespModel.error("商品价格不合法");
        }
        //校验图片格式
        if (commPicList != null) {
            for (MultipartFile file : commPicList) {
                String name = StringUtils.replace(file.getOriginalFilename(), " ", "");
                String fileType = name.substring(name.lastIndexOf(".") + 1);
                if (!ImageUtil.isImage(fileType))
                    return ServiceRespModel.error("仅支持图片格式上传");
            }
        }
        //将商品信息存至数据库
        try {
            CommodityBean addComm = new CommodityBean();
            String commNo = StringUtils.replace(UUID.randomUUID().toString(), "-", "");
            addComm.setCommNo(commNo);
            addComm.setCommName(evt.getCommName());
            addComm.setCommDesc(evt.getCommDesc());
            addComm.setCommPrice(evt.getCommPrice());
            addComm.setCommStock(evt.getCommStock());
            addComm.setCommTag(evt.getCommTag());
            addComm.setCommSale(0);
            addComm.setCreateUser((String) request.getAttribute("userNo"));
            addComm.setUserName(userBean.getUserName());
            addComm.setIsFreeShipping(evt.getIsFreeShipping());
            addComm.setStatus("E");
            addComm.setCreateTime(new Date());
            addComm.setRecommend(0);
            addComm.setAuditStatus(0);
            addComm.setIsFreeShipping(evt.getIsFreeShipping());
            //校验自定义标签长度
            StringBuffer tags = new StringBuffer();
            if (evt.getCustomTags() != null) {
                for (String tag : evt.getCustomTags()) {
                    tags.append(tag + "_");
                }
                if (tags.toString().length() > 80) {
                    throw new CommReleaseException("自定义标签长度超出限制");
                }
                addComm.setCustomTags(tags.toString());
            }
            if (commPicList != null) {
                int flag = 0;
                for (MultipartFile file : commPicList) {
                    file = ImageUtil.compressFile(file, shopProperties.getAttachSavePath() + file.getOriginalFilename(), 0.2f);
                    PluploadModel pluploadModel = UploadFileTool.upload(file, shopProperties.getAttachSavePath(), shopProperties.getAttachViewPath());
                    CommPicBean addCommPic = new CommPicBean();
                    addCommPic.setCommPicNo(StringUtils.replace(UUID.randomUUID().toString(), "-", ""));
                    addCommPic.setCommNo(commNo);
                    addCommPic.setPictureUrl(pluploadModel.getViewPath());
                    addCommPic.setCreateUser((String) request.getAttribute("userNo"));
                    addCommPic.setCreateTime(new Date());
                    addCommPic.setStatus("E");
                    int info1 = commPicMapper.insert(addCommPic);
                    if (info1 == 1) {
                        flag++;
                    }
                }
                if (commPicList.size() != flag) {
                    throw new CommReleaseException("商品图片上传失败");
                }
            }
            int info = commodityMapper.insert(addComm);
            if (info != 1) {
                throw new CommReleaseException("商品发布失败");
            }
        } catch (CommReleaseException e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ServiceRespModel.error(e.getMessage());
        }
        logger.info(String.format("用户%s发布一件商品", request.getAttribute("userEmail")));
        return ServiceRespModel.success("商品发布成功");
    }

    /**
     * 商品搜索
     */
    public ServiceRespModel searchComm(String keyName, PageEvt evt) {
        //查询商品
        Page<CommodityBean> page = new Page<>(evt.getCurrent(), evt.getSize());
        QueryWrapper<CommodityBean> qwComm = new QueryWrapper<>();
        qwComm
                .eq("status", "E")
                .eq("auditStatus", 1)
                .and(qw -> qw.like("commName", keyName).or().like("customTags", keyName));
        Page<CommodityBean> commodityBeanPage = commodityMapper.selectPage(page, qwComm);
        List<CommModel> commModelList = queryCommPic(commodityBeanPage.getRecords());
        PageModel pageModel = new PageModel(commModelList, commodityBeanPage.getCurrent(), commodityBeanPage.getPages());
        return ServiceRespModel.success("商品列表", pageModel);
    }

    /**
     * 商品预搜索
     */
    public ServiceRespModel preSearchComm(PreSearchCommEvt evt) {
        //搜索商品
        List<CommodityBean> commodityBeanList = commodityMapper.preQueryCommByName(evt);
        return ServiceRespModel.success("商品预搜索列表", commodityBeanList);
    }

    /**
     * 查看商品
     */
    public ServiceRespModel queryCommByNo(String commNo) {
        CommModel model = new CommModel();
        //查询商品详情
        CommodityBean comm = commodityMapper.queryCommByNo(commNo);
        if (comm == null) {
            return ServiceRespModel.error("商品不存在");
        }
        model.setCommodity(comm);
        //查询商品对应图片
        List<String> commPic = commodityMapper.queryPicByCommNo(comm.getCommNo());
        model.setCommPicList(commPic);
        return ServiceRespModel.success("商品详情", model);
    }

    /**
     * 删除商品
     */
    public ServiceRespModel deleteComm(HttpServletRequest request, String commNo) {
        //校验商品是否存在
        CommodityBean commodityBean = commodityMapper.queryCommByNo(commNo);
        if (commodityBean == null) {
            return ServiceRespModel.error("商品不存在");
        }
        //校验用户权限
        if (!request.getAttribute("userNo").equals(commodityBean.getCreateUser())) {
            return ServiceRespModel.error("无操作权限");
        }
        //删除商品
        int info = commodityMapper.deleteComm(commNo);
        if (info != 1) {
            return ServiceRespModel.error("商品删除失败");
        }
        return ServiceRespModel.success("商品删除成功");
    }

    /**
     * 通过标签搜索商品
     */
    public ServiceRespModel queryCommByTag(Integer commTag, PageEvt evt) {
        //查询商品
        Page<CommodityBean> page = new Page<>(evt.getCurrent(), evt.getSize());
        QueryWrapper<CommodityBean> qwComm = new QueryWrapper<>();
        qwComm
                .eq("status", "E")
                .eq("auditStatus", 1)
                .eq("commTag", commTag);
        Page<CommodityBean> commodityBeanPage = commodityMapper.selectPage(page, qwComm);
        List<CommModel> commModelList = queryCommPic(commodityBeanPage.getRecords());
        PageModel pageModel = new PageModel(commModelList, commodityBeanPage.getCurrent(), commodityBeanPage.getPages());
        return ServiceRespModel.success("商品列表", pageModel);
    }

    /**
     * 查询用户发布的商品
     */
    public ServiceRespModel queryUserComm(HttpServletRequest request) {
        QueryWrapper<CommodityBean> qwComm = new QueryWrapper<>();
        qwComm
                .eq("status", "E")
                .eq("createUser", request.getAttribute("userNo"));
        List<CommodityBean> commodityBeanList = commodityMapper.selectList(qwComm);
        List<CommModel> commModelList = queryCommPic(commodityBeanList);
        return ServiceRespModel.success("用户发布的商品列表", commModelList);
    }

    /**
     * 轮播广告列表
     */
    public ServiceRespModel bannerList(Integer num) {
        return ServiceRespModel.success("轮播广告列表", commodityMapper.bannerList(num));
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
}
