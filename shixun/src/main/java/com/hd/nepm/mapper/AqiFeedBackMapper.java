package com.hd.nepm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hd.nepm.po.AqiFeedBack;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Package: com.hd.nepm.mapper
 * Description：
 *
 * @Author msy
 * @Create 2026/6/30 11:46
 * @Version 1.0
 */
@Mapper
public interface AqiFeedBackMapper extends BaseMapper<AqiFeedBack> {
    /** 工单只存省市 id，用 LEFT JOIN 带出名称；对不上字典时工单仍然保留。 */
    @Select("SELECT af.af_id afId, af.tel_id telId, af.province_id provinceId, af.city_id cityId, " +
            "af.address, af.information, af.estimated_grade estimatedGrade, af.state, " +
            "af.af_date afDate, af.af_time afTime, " +
            "p.province_name provinceName, c.city_name cityName " +
            "FROM aqi_feedback af " +
            "LEFT JOIN grid_province p ON af.province_id = p.province_id " +
            "LEFT JOIN grid_city c ON af.city_id = c.city_id")
    List<AqiFeedBack> findAll();
    
    @Select("SELECT af.*, p.province_name provinceName, c.city_name cityName " +
            "FROM aqi_feedback af " +
            "LEFT JOIN grid_province p ON af.province_id = p.province_id " +
            "LEFT JOIN grid_city c ON af.city_id = c.city_id")
    List<AqiFeedBack> findAllForAdmin();

    @Select({"<script>",
            "SELECT af.*, p.province_name provinceName, c.city_name cityName ",
            "FROM aqi_feedback af ",
            "LEFT JOIN grid_province p ON af.province_id = p.province_id ",
            "LEFT JOIN grid_city c ON af.city_id = c.city_id ",
            "<where>",
            "<if test='keyword != null and keyword != \"\"'>",
            " AND (af.address LIKE CONCAT('%', #{keyword}, '%')",
            " OR af.information LIKE CONCAT('%', #{keyword}, '%')",
            " OR af.tel_id LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "</where>",
            " ORDER BY af.af_id DESC",
            "</script>"})
    IPage<AqiFeedBack> pageForAdmin(Page<AqiFeedBack> page, @Param("keyword") String keyword);
    
    @Insert("INSERT INTO aqi_feedback (tel_id, province_id, city_id, address, information, estimated_grade, af_date, af_time, gm_id, state) " +
            "VALUES (#{telId}, #{provinceId}, #{cityId}, #{address}, #{information}, #{estimatedGrade}, #{afDate}, #{afTime}, #{gmId}, #{state})")
    int insert(AqiFeedBack aqiFeedBack);

    /** 只按 af_id 更新，且必须满足当前状态，避免同一手机号的多张工单被一起改掉。 */
    @Update("UPDATE aqi_feedback SET gm_id = #{gmId}, assign_date = #{assignDate}, " +
            "assign_time = #{assignTime}, state = #{state}, remarks = #{remarks} " +
            "WHERE af_id = #{afId} AND ((#{state} = 1 AND state = 0) OR (#{state} = 2 AND state = 1))")
    int updateAssignment(AqiFeedBack aqiFeedBack);

    /** 影响行数为 0 表示工单不是已指派，调用方据此判断并发核验失败。 */
    @Update("UPDATE aqi_feedback SET state = 2 WHERE af_id = #{afId} AND state = 1")
    int updateStatus(AqiFeedBack aqiFeedBack);

    @Select("SELECT af.*, p.province_name provinceName, c.city_name cityName " +
            "FROM aqi_feedback af " +
            "LEFT JOIN grid_province p ON af.province_id = p.province_id " +
            "LEFT JOIN grid_city c ON af.city_id = c.city_id " +
            "WHERE af.af_id = #{afId}")
    AqiFeedBack selectById(Integer afId);

    @Select("SELECT af.*, p.province_name provinceName, c.city_name cityName " +
            "FROM aqi_feedback af " +
            "LEFT JOIN grid_province p ON af.province_id = p.province_id " +
            "LEFT JOIN grid_city c ON af.city_id = c.city_id " +
            "WHERE af.gm_id = #{gmId}")
    List<AqiFeedBack> findByGmId(Integer gmId);

    @Select("SELECT city_id FROM grid_member WHERE gm_id = #{gmId}")
    Integer selectCityIdByGmId(Integer gmId);

    @Select("SELECT af.city_id FROM aqi_feedback af WHERE af.af_id = #{afId}")
    Integer selectCityIdByAfId(Integer afId);

}
