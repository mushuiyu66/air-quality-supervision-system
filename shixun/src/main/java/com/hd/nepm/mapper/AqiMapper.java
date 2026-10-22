package com.hd.nepm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hd.nepm.po.Aqi;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AqiMapper extends BaseMapper<Aqi> {
    @Select("SELECT * FROM aqi")
    List<Aqi> selectAll();

    @Select("SELECT * FROM aqi WHERE aqi_id = #{aqiId}")
    Aqi selectById(Integer aqiId);

    @Insert("INSERT INTO aqi (chinese_explain, aqi_explain, color, health_impact, take_steps, " +
            "so2_min, so2_max, co_min, co_max, spm_min, spm_max, remarks) " +
            "VALUES (#{chineseExplain}, #{aqiExplain}, #{color}, #{healthImpact}, #{takeSteps}, " +
            "#{so2Min}, #{so2Max}, #{coMin}, #{coMax}, #{spmMin}, #{spmMax}, #{remarks})")
    int insert(Aqi aqi);

    @Update("UPDATE aqi SET chinese_explain = #{chineseExplain}, aqi_explain = #{aqiExplain}, " +
            "color = #{color}, health_impact = #{healthImpact}, take_steps = #{takeSteps}, " +
            "so2_min = #{so2Min}, so2_max = #{so2Max}, co_min = #{coMin}, co_max = #{coMax}, " +
            "spm_min = #{spmMin}, spm_max = #{spmMax}, remarks = #{remarks} " +
            "WHERE aqi_id = #{aqiId}")
    int update(Aqi aqi);

    @Delete("DELETE FROM aqi WHERE aqi_id = #{aqiId}")
    int deleteById(Integer aqiId);

    @Select("SELECT aqi_id FROM aqi WHERE #{value} BETWEEN so2_min AND so2_max LIMIT 1")
    Integer getSo2Level(Integer value);

    @Select("SELECT aqi_id FROM aqi WHERE #{value} BETWEEN co_min AND co_max LIMIT 1")
    Integer getCoLevel(Integer value);

    @Select("SELECT aqi_id FROM aqi WHERE #{value} BETWEEN spm_min AND spm_max LIMIT 1")
    Integer getSpmLevel(Integer value);
}
