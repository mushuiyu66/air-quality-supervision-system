package com.hd.nepm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hd.nepm.po.Admins;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * Package: com.hd.nepm.mapper
 * Description：
 *
 * @Author msy
 * @Create 2026/6/24 9:37
 * @Version 1.0
 */
@Mapper
public interface AdminsMapper extends BaseMapper<Admins> {
    //根据名字查询管理员
    @Select("SELECT *from admins where admin_code=#{adminCode}")
    Admins selectAdminsByCode(String adminCode);
    //添加insert
    @Insert("INSERT INTO admins (admin_code, password,remarks) VALUES (#{adminCode},#{password},#{remarks})")
    int insertAdmins(Admins admins);

    @Update("UPDATE admins SET password = #{password} WHERE admin_code = #{adminCode}")
    int updatePassword(@Param("adminCode") String adminCode, @Param("password") String password);
    //修改update
    //删除delete
}
