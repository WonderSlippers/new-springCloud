package com.shop.dao.mapperDao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.bean.AdvertBean;
import com.shop.bean.CommodityBean;
import com.shop.dao.provider.CommodityProvider;
import com.shop.evt.PreSearchCommEvt;
import com.shop.model.RandomCommListModel;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface CommodityMapper extends BaseMapper<CommodityBean> {

    /**
     * 随机商品列表(随机选取n条数据）
     */
    @SelectProvider(type = CommodityProvider.class, method = "randomCommList")
    List<CommodityBean> randomCommList(RandomCommListModel model);

    /**
     * 查询商品对应的图片
     */
    @Select("select pictureUrl from t_commPic " +
            "where status = 'E' and commNo = #{item}")
    List<String> queryPicByCommNo(@Param("item") String commNo);

    /**
     * 商品预搜索
     */
    @Select("select commName,commNo from t_commodity where status = 'E' and auditStatus = 1 and commName like CONCAT('%',#{evt.keyName},'%') limit #{evt.num}")
    List<CommodityBean> preQueryCommByName(@Param("evt") PreSearchCommEvt evt);

    /**
     * 查询商品
     */
    @Select("select * from t_commodity where status = 'E' and commNo = #{item}")
    CommodityBean queryCommByNo(@Param("item") String commNo);

    /**
     * 删除商品
     */
    @Update("update t_commodity c left join t_commPic cp on c.commNo = cp.commNo set c.status = 'D', cp.status = 'D', c.updateTime = now(), cp.updateTime = now() where c.commNo = #{item} and c.status = 'E' and cp.status = 'E'")
    Integer deleteComm(@Param("item") String commNo);

    /**
     * 轮播广告列表
     */
    @Select("select * from t_advert where status = 'E' ORDER BY rand() LIMIT #{num}")
    List<AdvertBean> bannerList(@Param("num") Integer num);


}
