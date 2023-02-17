package com.shop.dao.mapperDao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.bean.CommentReportBean;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface CommentReportMapper extends BaseMapper<CommentReportBean> {
}
