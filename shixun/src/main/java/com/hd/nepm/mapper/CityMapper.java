package com.hd.nepm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hd.nepm.po.City;
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
public interface CityMapper extends BaseMapper<City> {
    @Select("SELECT * FROM grid_city")
    List<City> selectAllCity();

    @Select("SELECT * FROM grid_city WHERE city_id = #{cityId}")
    City selectCityById(Integer cityId);

    @Insert("INSERT INTO grid_city (city_name, province_id, remarks) VALUES (#{cityName}, #{provinceId}, #{remarks})")
    int insertCity(City city);

    @Update("UPDATE grid_city SET city_name = #{cityName}, province_id = #{provinceId}, remarks = #{remarks} WHERE city_id = #{cityId}")
    int updateCity(City city);

    @Delete("DELETE FROM grid_city WHERE city_id = #{cityId}")
    int deleteCityById(Integer cityId);
}
