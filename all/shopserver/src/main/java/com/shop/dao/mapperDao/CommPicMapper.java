package com.shop.dao.mapperDao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.bean.CommPicBean;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface CommPicMapper extends BaseMapper<CommPicBean> {
}
