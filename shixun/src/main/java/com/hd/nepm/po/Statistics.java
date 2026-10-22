package com.hd.nepm.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Statistics {
    @JsonProperty("province_id")
    @JsonAlias("provinceId")
    private int provinceId;

    @JsonProperty("city_id")
    @JsonAlias("cityId")
    private int cityId;

    @JsonProperty("address")
    private String address;

    @JsonProperty("so2_value")
    @JsonAlias("so2Value")
    private int so2Value;

    @JsonProperty("so2_level")
    @JsonAlias("so2Level")
    private int so2Level;

    @JsonProperty("co_value")
    @JsonAlias("coValue")
    private int coValue;

    @JsonProperty("co_level")
    @JsonAlias("coLevel")
    private int coLevel;

    @JsonProperty("spm_value")
    @JsonAlias("spmValue")
    private int spmValue;

    @JsonProperty("spm_level")
    @JsonAlias("spmLevel")
    private int spmLevel;

    @JsonProperty("aqi_id")
    @JsonAlias("aqiId")
    private int aqiId;

    @JsonProperty("confirm_date")
    @JsonAlias("confirmDate")
    private String confirmDate;

    @JsonProperty("confirm_time")
    @JsonAlias("confirmTime")
    private String confirmTime;

    @JsonProperty("gm_id")
    @JsonAlias("gmId")
    private long gmId;

    @JsonProperty("fd_id")
    @JsonAlias("fdId")
    private long fdId;

    @JsonProperty("information")
    private String information;

    @JsonProperty("remarks")
    private String remarks;

    @TableField(exist = false)
    @JsonProperty("tel_id")
    @JsonAlias("telId")
    private String telId;

    @TableField(exist = false)
    @JsonProperty("province_name")
    @JsonAlias("provinceName")
    private String provinceName;

    @TableField(exist = false)
    @JsonProperty("city_name")
    @JsonAlias("cityName")
    private String cityName;
}
