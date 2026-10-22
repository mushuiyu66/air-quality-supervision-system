package com.hd.nepm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hd.nepm.po.Province;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Package: com.hd.nepm.mapper
 * Description：
 *
 * @Author msy
 * @Create 2026/7/1
 * @Version 1.0
 */
@Mapper
public interface ProvinceMapper extends BaseMapper<Province> {
    @Select("SELECT * FROM grid_province")
    List<Province> selectAllProvince();

    @Select("SELECT * FROM grid_province WHERE province_id = #{provinceId}")
    Province selectProvinceById(Integer provinceId);

    @Insert("INSERT INTO grid_province (province_name, province_abbr, remarks) VALUES (#{provinceName}, #{provinceAbbr}, #{remarks})")
    int insertProvince(Province province);

    @Update("UPDATE grid_province SET province_name = #{provinceName}, province_abbr = #{provinceAbbr}, remarks = #{remarks} WHERE province_id = #{provinceId}")
    int updateProvince(Province province);

    @Delete("DELETE FROM grid_province WHERE province_id = #{provinceId}")
    int deleteProvinceById(Integer provinceId);
}
