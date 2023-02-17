package com.shop.dao.mapperDao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.bean.OrderBean;
import com.shop.model.CommOrderModel;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface OrderMapper extends BaseMapper<OrderBean> {

    /**
     * 查看用户提交的订单列表
     */
    @Select("select c.commName, c.commDesc, o.* ,cu.profileUrl as buyerProfile,cu.userName as buyerName,su.profileUrl as sellerProfile,su.userName as sellerName " +
            "from t_order o left join t_commodity c on o.commNo = c.commNo left join t_user cu on o.createUser = cu.userNo left join t_user su on o.sellerNo= su.userNo " +
            "where o.status = 'E' and o.createUser = #{item} and o.buyerDisplay = 0")
    List<CommOrderModel> queryUserSubmitOrderList(@Param("item") String userNo);

    /**
     * 查看用户接收的订单列表
     */
    @Select("select c.commName, c.commDesc, o.* ,cu.profileUrl as buyerProfile,cu.userName as buyerName,su.profileUrl as sellerProfile,su.userName as sellerName " +
            "from t_order o left join t_commodity c on o.commNo = c.commNo left join t_user cu on o.createUser = cu.userNo left join t_user su on o.sellerNo= su.userNo " +
            "where o.status = 'E' and c.createUser = #{item} and o.sellerDisplay = 0")
    List<CommOrderModel> queryUserReceiveOrderList(@Param("item") String userNo);

    /**
     * 查看订单
     */
    @Select("select * from t_order where status = 'E' and orderNo = #{item}")
    OrderBean queryOrderByNo(@Param("item") String orderNo);
}
