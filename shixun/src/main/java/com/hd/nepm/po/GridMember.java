package com.hd.nepm.po;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Package: com.hd.nepm.po
 * Description：
 *
 * @Author msy
 * @Create 2026/6/29 9:19
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("grid_member")
public class GridMember {
    @TableId(type= IdType.AUTO)
    private Integer gmId;
    @TableField("gm_name")
    private String gmName;
    private String gmCode;
    private String password;
    private Integer provinceId;
    private Integer cityId;
    private String tel;
    private Integer state;
    private String remarks;
    //非数据库字段，为业务需要，用于查询
    @TableField(exist = false)
    private String provinceName;
    @TableField(exist = false)
    private String cityName;
    @TableField(exist = false)
    private String token;
}
