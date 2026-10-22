package com.hd.nepm.po;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Aqi {
    @JsonProperty("aqi_id")
    @JsonAlias("aqiId")
    private Integer aqiId;

    @JsonProperty("chinese_explain")
    @JsonAlias("chineseExplain")
    private String chineseExplain;

    @JsonProperty("aqi_explain")
    @JsonAlias("aqiExplain")
    private String aqiExplain;

    @JsonProperty("color")
    private String color;

    @JsonProperty("health_impact")
    @JsonAlias("healthImpact")
    private String healthImpact;

    @JsonProperty("take_steps")
    @JsonAlias("takeSteps")
    private String takeSteps;

    @JsonProperty("so2_min")
    @JsonAlias("so2Min")
    private Integer so2Min;

    @JsonProperty("so2_max")
    @JsonAlias("so2Max")
    private Integer so2Max;

    @JsonProperty("co_min")
    @JsonAlias("coMin")
    private Integer coMin;

    @JsonProperty("co_max")
    @JsonAlias("coMax")
    private Integer coMax;

    @JsonProperty("spm_min")
    @JsonAlias("spmMin")
    private Integer spmMin;

    @JsonProperty("spm_max")
    @JsonAlias("spmMax")
    private Integer spmMax;

    @JsonProperty("remarks")
    private String remarks;
}
