package com.shop.dao.mapperDao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.bean.AdvertBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface AdvertMapper extends BaseMapper<AdvertBean> {

    /**
     * 查询广告
     */
    @Select("select * from t_advert where status = 'E' and advertNo = #{advertNo}")
    AdvertBean queryAdvertByNo(@Param("advertNo") Integer advertNo);
}
