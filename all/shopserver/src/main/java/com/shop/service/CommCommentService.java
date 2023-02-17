package com.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shop.bean.CommCommentBean;
import com.shop.bean.CommentReportBean;
import com.shop.bean.CommodityBean;
import com.shop.dao.mapperDao.CommCommentMapper;
import com.shop.dao.mapperDao.CommentReportMapper;
import com.shop.dao.mapperDao.CommodityMapper;
import com.shop.evt.InsertCommCommentEvt;
import com.shop.evt.QueryCommCommentListEvt;
import com.shop.evt.ReportCommentEvt;
import com.shop.exceptions.CommCommentException;
import com.shop.model.CommCommentModel;
import com.shop.model.ServiceRespModel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class CommCommentService {

    @Resource
    private CommCommentMapper commCommentMapper;

    @Resource
    private CommodityMapper commodityMapper;

    @Resource
    private CommentReportMapper commentReportMapper;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 发布评论
     */
    public ServiceRespModel insertCommComment(HttpServletRequest request, InsertCommCommentEvt evt) {
        //校验商品是否存在
        QueryWrapper<CommodityBean> commodityBeanQueryWrapper = new QueryWrapper<>();
        commodityBeanQueryWrapper
                .eq("auditStatus", 1)
                .eq("commNo", evt.getCommNo())
                .eq("status", "E");
        CommodityBean commodityBean = commodityMapper.selectOne(commodityBeanQueryWrapper);
        if (commodityBean == null) {
            return ServiceRespModel.error("商品不存在");
        }
        //保存评论至数据库
        CommCommentBean commCommentBean = new CommCommentBean();
        commCommentBean.setCommentNo(StringUtils.replace(UUID.randomUUID().toString(), "-", ""));
        commCommentBean.setCreateUser((String) request.getAttribute("userNo"));
        commCommentBean.setCommNo(evt.getCommNo());
        commCommentBean.setCreateTime(new Date());
        commCommentBean.setReportedNum(0);
        commCommentBean.setContent(evt.getContent());
        commCommentBean.setStatus("E");
        int info = commCommentMapper.insert(commCommentBean);
        if (info != 1) {
            return ServiceRespModel.error("发布评论失败");
        }
        return ServiceRespModel.success("发布评论成功");
    }

    /**
     * 举报评论
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceRespModel reportCommComment(ReportCommentEvt evt) {
        //校验评论是否存在
        CommCommentBean commCommentBean = commCommentMapper.queryCommCommentByNo(evt.getCommentNo());
        if (commCommentBean == null) {
            return ServiceRespModel.error("评论不存在");
        }
        try {
            //评论被举报次数加一
            commCommentBean.setReportedNum(commCommentBean.getReportedNum() + 1);
            int info = commCommentMapper.updateById(commCommentBean);
            if (info != 1) {
                throw new CommCommentException("评论被举报次数更新失败");
            }
            //新增举报信息
            CommentReportBean commentReportBean = new CommentReportBean();
            commentReportBean.setReportNo(StringUtils.replace(UUID.randomUUID().toString(), "-", ""));
            commentReportBean.setStatus("E");
            commentReportBean.setCommentNo(evt.getCommentNo());
            commentReportBean.setReason(evt.getReason());
            int info1 = commentReportMapper.insert(commentReportBean);
            if (info1 != 1) {
                throw new CommCommentException("新增举报信息失败");
            }
        } catch (CommCommentException e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ServiceRespModel.error(e.getMessage());
        }
        return ServiceRespModel.success("举报评论成功");
    }

    /**
     * 查询商品评论列表
     */
    public ServiceRespModel queryCommCommentList(QueryCommCommentListEvt evt) {
        List<CommCommentModel> commCommentModelList = commCommentMapper.randomCommCommentList(evt);
        return new ServiceRespModel(1, "商品评论列表", commCommentModelList);
    }
}
