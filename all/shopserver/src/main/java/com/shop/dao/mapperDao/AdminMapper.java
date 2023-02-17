package com.shop.dao.mapperDao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.model.CommStModel;
import com.shop.model.QueryReportedCommentModel;
import com.shop.model.WithdrawalInfoModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface AdminMapper {

    //查询评论举报信息列表
    @Select("select u.userName, u.userEmail, u.userNo, cc.content, cr.reason, cr.commentNo, u.isBan, cc.reportedNum from t_commentreport cr left join t_commcomment cc on cr.commentNo = cc.commentNo left join " +
            "t_user u on cc.createUser = u.userNo where cr.status = 'E'")
    Page<QueryReportedCommentModel> queryCommCommentList(Page<QueryReportedCommentModel> page);

    //查询商品分区对应商品数量
    @Select("select commTag, count(*) as num from t_commodity where status = 'E' group by commTag order by commTag")
    List<CommStModel> queryCommSt();

    //查询提现申请列表
    @Select("select u.userName, u.userEmail, u.userNo, w.* from t_withdrawal w left join t_user u on w.createUser = u.userNo where w.status = 'E'")
    Page<WithdrawalInfoModel> queryWithdrawalList(Page<WithdrawalInfoModel> page);

}
