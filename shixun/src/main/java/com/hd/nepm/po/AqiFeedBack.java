package com.hd.nepm.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Package: com.hd.nepm.po
 * Description：
 *
 * @Author msy
 * @Create 2026/6/30 11:40
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AqiFeedBack {
    @JsonProperty("af_id")
    @JsonAlias("afId")
    private Integer afId;

    @JsonProperty("tel_id")
    @JsonAlias("telId")
    private String telId;
    
    @JsonProperty("province_id")
    @JsonAlias("provinceId")
    private Integer provinceId;
    
    @JsonProperty("city_id")
    @JsonAlias("cityId")
    private Integer cityId;
    
    @JsonProperty("address")
    private String address;
    
    @JsonProperty("information")
    private String information;
    
    @JsonProperty("estimated_grade")
    @JsonAlias("estimatedGrade")
    private Integer estimatedGrade;
    
    @JsonProperty("af_date")
    @JsonAlias("afDate")
    private String afDate;
    
    @JsonProperty("af_time")
    @JsonAlias("afTime")
    private String afTime;
    
    @JsonProperty("gm_id")
    @JsonAlias("gmId")
    private Integer gmId;
    
    @JsonProperty("assign_date")
    @JsonAlias("assignDate")
    private String assignDate;
    
    @JsonProperty("assign_time")
    @JsonAlias("assignTime")
    private String assignTime;
    
    @JsonProperty("state")
    private Integer state;
    
    @JsonProperty("remarks")
    private String remarks;

    @TableField(exist = false)
    @JsonProperty("so2_value")
    @JsonAlias("so2Value")
    private Integer so2Value;

    @TableField(exist = false)
    @JsonProperty("co_value")
    @JsonAlias("coValue")
    private Integer coValue;

    @TableField(exist = false)
    @JsonProperty("spm_value")
    @JsonAlias("spmValue")
    private Integer spmValue;

    @TableField(exist = false)
    @JsonProperty("province_name")
    @JsonAlias("provinceName")
    private String provinceName;
    
    @TableField(exist = false)
    @JsonProperty("city_name")
    @JsonAlias("cityName")
    private String cityName;
}
