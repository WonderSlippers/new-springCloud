package com.shop.dao.mapperDao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.bean.UserBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserMapper extends BaseMapper<UserBean> {

    /**
     * 查询用户
     * 通过邮箱查询
     */
    @Select("select * from t_user where userEmail = #{item} and status = 'E'")
    UserBean queryUserByEmail(@Param("item") String userAccount);

    /**
     * 查询用户
     * 通过用户编码查询
     */
    @Select("select * from t_user where userNo = #{item} and status = 'E'")
    UserBean queryUserByNo(@Param("item") String userNo);

}
