package com.hd.nepm.controller;

import com.hd.nepm.po.Admins;
import com.hd.nepm.service.impl.AdminsServiceI;
import com.hd.nepm.vo.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Package: com.hd.nepm.controller
 * Description：
 *
 * @Author msy
 * @Create 2026/6/24 10:12
 * @Version 1.0
 */
@RestController
@RequestMapping("/admins")
@CrossOrigin//实现跨域
public class AdminsController {
    @Autowired
    private AdminsServiceI adminsServiceI;
    //restful风格
    //增加post，删除delete，修改put，查询get，局部修改patch
    @PostMapping("/login")
    public ResponseData selectAdminsByCodeAndPassword(@RequestBody Admins admins){
        ResponseData<Admins> responseData=adminsServiceI.selectAdminsByCodeAndPassword(admins);
        return responseData;
    }
    @PostMapping("/insert")
    public ResponseData insertAdmins(@RequestBody Admins admins){
        ResponseData<Admins> responseData=adminsServiceI.insertAdmins(admins);
        return responseData;
    }
}
