package com.shop.dao.mapperDao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.bean.CommCommentBean;
import com.shop.evt.QueryCommCommentListEvt;
import com.shop.model.CommCommentModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface CommCommentMapper extends BaseMapper<CommCommentBean> {

    /**
     * 随机评论列表(随机选取n条数据）
     */
    @Select("select c.*, u.* from t_commComment c left join t_user u on c.createUser = u.userNo where c.status = 'E' and c.commNo = #{evt.commNo} order by rand() limit #{evt.num}")
    List<CommCommentModel> randomCommCommentList(@Param("evt") QueryCommCommentListEvt evt);

    /**
     * 查询商品评论
     */
    @Select("select * from t_commComment where status = 'E' and commentNo = #{commentNo}")
    CommCommentBean queryCommCommentByNo(@Param("commentNo") String commentNo);
}
