package com.hd.nepm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hd.nepm.po.Supervisor;
import org.apache.ibatis.annotations.*;

/**
 * Package: com.hd.nepm.mapper
 * Description：
 *
 * @Author msy
 * @Create 2026/6/29 15:50
 * @Version 1.0
 */
@Mapper
public interface SupervisorMapper extends BaseMapper<Supervisor> {
    @Select("SELECT * FROM supervisor WHERE tel_id = #{telId}")
    Supervisor selectByTelIdAndPassword(String telId);

    @Insert("INSERT INTO supervisor (tel_id, password,real_name,birthday,sex,remarks) VALUES (#{telId}, #{password},#{realName},#{birthday},#{sex},#{remarks})")
    void signUp(Supervisor supervisor);

    @Select("SELECT * FROM supervisor")
    java.util.List<Supervisor> selectAll();

    @Select("SELECT * FROM supervisor WHERE tel_id = #{telId}")
    Supervisor selectByTelId(String telId);

    @Update("UPDATE supervisor SET password = #{password}, real_name = #{realName}, birthday = #{birthday}, sex = #{sex}, remarks = #{remarks} WHERE tel_id = #{telId}")
    int updateByTelId(Supervisor supervisor);

    @Update("UPDATE supervisor SET password = #{password} WHERE tel_id = #{telId}")
    int updatePassword(@Param("telId") String telId, @Param("password") String password);

    @Delete("DELETE FROM supervisor WHERE tel_id = #{telId}")
    int deleteByTelId(String telId);
}
