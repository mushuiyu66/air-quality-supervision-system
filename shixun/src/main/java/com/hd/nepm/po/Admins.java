package com.hd.nepm.po;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Package: com.hd.nepm.vo
 * Description：
 *
 * @Author msy
 * @Create 2026/6/24 9:18
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admins {
    private Integer adminId;
    private String adminCode;
    private String password;
    private String remarks;
    @TableField(exist = false)
    private String token;

}

