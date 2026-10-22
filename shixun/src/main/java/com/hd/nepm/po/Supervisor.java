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
 * @Create 2026/6/29 15:43
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supervisor {
    @JsonProperty("tel_id")
    @JsonAlias("telId")
    private String telId;
    @JsonProperty("password")
    private String password;
    @JsonProperty("real_name")
    @JsonAlias("realName")
    private String realName;
    @JsonProperty("birthday")
    private String birthday;
    @JsonProperty("sex")
    private Integer sex;
    @JsonProperty("remarks")
    private String remarks;
    @TableField(exist = false)
    @JsonProperty("token")
    private String token;
}
