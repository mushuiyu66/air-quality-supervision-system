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
 * @Create 2026/6/29 9:33
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("grid_province")

public class Province {
    @TableId(value = "province_id", type = IdType.AUTO)
    @JsonProperty("province_id")
    private Integer provinceId;
    
    @JsonProperty("province_name")
    private String provinceName;
    
    @JsonProperty("province_abbr")
    private Character provinceAbbr;
    
    @JsonProperty("remarks")
    private String remarks;
}
