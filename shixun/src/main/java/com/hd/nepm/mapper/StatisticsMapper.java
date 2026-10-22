package com.hd.nepm.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hd.nepm.po.Statistics;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StatisticsMapper {
    /** 核验表联省市字典和原工单，一次取出地址与举报人电话。 */
    @Select("SELECT s.*, p.province_name provinceName, c.city_name cityName, f.tel_id telId" +
            " FROM statistics s" +
            " LEFT JOIN grid_province p ON s.province_id = p.province_id" +
            " LEFT JOIN grid_city c ON s.city_id = c.city_id" +
            " LEFT JOIN aqi_feedback f ON s.fd_id = f.af_id")
    List<Statistics> selectAllStatistics();

    @Select({"<script>",
            "SELECT s.*, p.province_name provinceName, c.city_name cityName, f.tel_id telId",
            " FROM statistics s",
            " LEFT JOIN grid_province p ON s.province_id = p.province_id",
            " LEFT JOIN grid_city c ON s.city_id = c.city_id",
            " LEFT JOIN aqi_feedback f ON s.fd_id = f.af_id",
            "<where>",
            "<if test='provinceId != null'> AND s.province_id = #{provinceId}</if>",
            "<if test='cityId != null'> AND s.city_id = #{cityId}</if>",
            "<if test='startDate != null and startDate != \"\"'> AND s.confirm_date &gt;= #{startDate}</if>",
            "<if test='endDate != null and endDate != \"\"'> AND s.confirm_date &lt;= #{endDate}</if>",
            "</where>",
            " ORDER BY s.confirm_date DESC, s.confirm_time DESC",
            "</script>"})
    IPage<Statistics> pageStatistics(Page<Statistics> page,
                                    @Param("provinceId") Integer provinceId,
                                    @Param("cityId") Integer cityId,
                                    @Param("startDate") String startDate,
                                    @Param("endDate") String endDate);

    @Select("SELECT s.*, p.province_name provinceName, c.city_name cityName, f.tel_id telId" +
            " FROM statistics s" +
            " LEFT JOIN grid_province p ON s.province_id = p.province_id" +
            " LEFT JOIN grid_city c ON s.city_id = c.city_id" +
            " LEFT JOIN aqi_feedback f ON s.fd_id = f.af_id" +
            " WHERE s.fd_id = #{fdId}")
    Statistics selectByFdId(Integer fdId);

    @Insert("INSERT INTO statistics (province_id, city_id, address, so2_value, so2_level, " +
            "co_value, co_level, spm_value, spm_level, aqi_id, confirm_date, confirm_time, " +
            "gm_id, fd_id, information, remarks) " +
            "VALUES (#{provinceId}, #{cityId}, #{address}, #{so2Value}, #{so2Level}, " +
            "#{coValue}, #{coLevel}, #{spmValue}, #{spmLevel}, #{aqiId}, #{confirmDate}, #{confirmTime}, " +
            "#{gmId}, #{fdId}, #{information}, #{remarks})")
    int insert(Statistics statistics);
}
