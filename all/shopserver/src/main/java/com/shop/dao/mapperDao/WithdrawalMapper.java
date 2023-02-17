package com.shop.dao.mapperDao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.bean.WithdrawalBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface WithdrawalMapper extends BaseMapper<WithdrawalBean> {
    /**
     * 查询提现申请
     */
    @Select("select * from t_withdrawal where formNo = #{formNo} and status = 'E'")
    WithdrawalBean queryWithdrawalByNo(@Param("formNo") String formNo);
}
