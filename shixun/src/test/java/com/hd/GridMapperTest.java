package com.hd;

import com.hd.nepm.mapper.GridMapper;
import com.hd.nepm.NepmServerApplication;
import com.hd.nepm.po.GridMember;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * Package: com.hd
 * Description：
 *
 * @Author msy
 * @Create 2026/6/29 10:20
 * @Version 1.0
 */
@SpringBootTest(classes = NepmServerApplication.class)
public class GridMapperTest {
    @Autowired
    private GridMapper gridMapper; // 声明一个GridMapper类型的变量，用于测试GridMapper类的功能
    @Test
    public void testGetAllGridMembersWithProvinceAndCity() {
        List<GridMember> gridMembers = gridMapper.getAllGridMembersWithProvinceAndCity();
        for (GridMember gridMember : gridMembers) {
            System.out.println(gridMember);
        }
    }

}
