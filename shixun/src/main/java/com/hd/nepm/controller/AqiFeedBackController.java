package com.hd.nepm.controller;

import com.hd.nepm.config.AuthContext;
import com.hd.nepm.config.LoginUser;
import com.hd.nepm.po.AqiFeedBack;
import com.hd.nepm.service.impl.AqiFeedBackServiceI;
import com.hd.nepm.vo.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/aqiFeedBack")
@CrossOrigin
public class AqiFeedBackController {
    @Autowired
    private AqiFeedBackServiceI aqiFeedBackServicei;
    
    @PostMapping("/insert")
    public ResponseData insert(@RequestBody AqiFeedBack aqiFeedBack) {
        System.out.println("接收到的反馈数据: " + aqiFeedBack);
        return ResponseData.success(aqiFeedBackServicei.insert(aqiFeedBack));
    }
    
    @GetMapping("/findAll")
    public ResponseData findAll() {
        return ResponseData.success(aqiFeedBackServicei.findAll());
    }
    
    @GetMapping("/findAllForAdmin")
    public ResponseData findAllForAdmin(@RequestParam(defaultValue = "1") long page,
                                        @RequestParam(defaultValue = "10") long size,
                                        @RequestParam(required = false) String keyword) {
        return ResponseData.success(aqiFeedBackServicei.pageForAdmin(page, size, keyword));
    }

    /** 网格员编号取自令牌，不使用前端传入的 gmId。 */
    @GetMapping("/findByGmId")
    public ResponseData findByGmId() {
        LoginUser user = AuthContext.get();
        if (user == null || !"grid".equals(user.getRole())) {
            return ResponseData.error(403, "无权查询工单");
        }
        return ResponseData.success(aqiFeedBackServicei.findByGmId(Integer.valueOf(user.getUid())));
    }

        @PutMapping("/updateAssignment")
        public ResponseData updateAssignment(@RequestBody AqiFeedBack aqiFeedBack) {
            return ResponseData.success(aqiFeedBackServicei.updateAssignment(aqiFeedBack));
    }
}
