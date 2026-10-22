package com.hd.nepm.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Package: com.hd.nepm.po
 * Description：
 *
 * @Author msy
 * @Create 2026/6/29 9:36
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("grid_city")
public class City {
    @TableId(value="city_id",type= IdType.AUTO)
    @JsonProperty("city_id")
    private Integer cityId;
    
    @JsonProperty("city_name")
    private String cityName;
    
    @JsonProperty("province_id")
    private Integer provinceId;
    
    @JsonProperty("remarks")
    private String remarks;
}
