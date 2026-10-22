package com.hd.nepm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hd.nepm.po.Admins;
import com.hd.nepm.po.GridMember;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Package: com.hd.nepm.mapper
 * Description：
 *
 * @Author msy
 * @Create 2026/6/29 9:47
 * @Version 1.0
 */
@Mapper
public interface GridMapper extends BaseMapper<GridMember> {
    @Select("SELECT *from grid_member where gm_code=#{gmCode}")
    GridMember selectGridMemberByCode(String gmCode);

    @Select("SELECT m.*, p.province_name provinceName, c.city_name cityName " +
            "FROM grid_member m " +
            "LEFT JOIN grid_province p ON m.province_id = p.province_id " +
            "LEFT JOIN grid_city c ON m.city_id = c.city_id " +
            "WHERE m.gm_id = #{gmId}")
    GridMember getGridMemberById(Integer gmId);

    @Select("SELECT m.*, p.province_name provinceName, c.city_name cityName " +
            "FROM grid_member m " +
            "LEFT JOIN grid_province p ON m.province_id = p.province_id " +
            "LEFT JOIN grid_city c ON m.city_id = c.city_id")
    List<GridMember> getAllGridMembersWithProvinceAndCity();

    @Select("SELECT m.*, p.province_name provinceName, c.city_name cityName " +
            "FROM grid_member m " +
            "LEFT JOIN grid_province p ON m.province_id = p.province_id " +
            "LEFT JOIN grid_city c ON m.city_id = c.city_id " +
            "WHERE m.city_id = #{cityId}")
    List<GridMember> getGridMembersByCityId(Integer cityId);

    @Select("SELECT m.*, p.province_name provinceName, c.city_name cityName " +
            "FROM grid_member m " +
            "LEFT JOIN grid_province p ON m.province_id = p.province_id " +
            "LEFT JOIN grid_city c ON m.city_id = c.city_id " +
            "WHERE m.gm_name LIKE CONCAT('%', #{gmName}, '%')")
    List<GridMember> getGridMembersByName(@Param("gmName") String gmName);

    @Insert("INSERT INTO grid_member (tel,gm_code, gm_name,password, province_id, city_id) VALUES (#{tel},#{gmCode}, #{gmName}, #{password}, #{provinceId}, #{cityId})")
    int insertGridMember(GridMember gridMember);

    @Delete("DELETE FROM grid_member WHERE gm_id = #{gmId}")
    int deleteGridMemberById(Integer gmId);

    @Update("UPDATE grid_member SET state = #{state} WHERE gm_id = #{gmId}")
    int updateStateById(@Param("gmId") Integer gmId, @Param("state") Integer state);

    @Update("UPDATE grid_member SET tel=#{tel}, gm_name=#{gmName}, password=#{password}, province_id=#{provinceId}, city_id=#{cityId}, remarks=#{remarks} WHERE gm_id = #{gmId}")
    int updateGridMember(GridMember gridMember);

    @Update("UPDATE grid_member SET password = #{password} WHERE gm_id = #{gmId}")
    int updatePassword(@Param("gmId") Integer gmId, @Param("password") String password);
}

