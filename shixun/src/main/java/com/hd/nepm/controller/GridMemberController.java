package com.hd.nepm.controller;

import com.hd.nepm.po.GridMember;
import com.hd.nepm.service.impl.GridMemberServiceI;
import com.hd.nepm.vo.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Package: com.hd.nepm.controller
 * Description：
 *
 * @Author msy
 * @Create 2026/6/29 10:52
 * @Version 1.0
 */
@RestController
@RequestMapping("/gridMember")
@CrossOrigin
public class GridMemberController {
    @Autowired
    private GridMemberServiceI gridMemberServicei;
    @GetMapping
    public ResponseData getAllGridMembersWithProvinceAndCity() {
        return ResponseData.success(gridMemberServicei.getAllGridMembersWithProvinceAndCity());
    }
    @PostMapping("/gridlogin")
    public ResponseData selectGridMemberByCodeAndPassword(@RequestBody GridMember gridMember) {
        return gridMemberServicei.selectGridMemberByCodeAndPassword(gridMember);
    }
    @PostMapping("/insert")
    public ResponseData insertGridMember(@RequestBody GridMember gridMember) {
        return ResponseData.success(gridMemberServicei.insertGridMember(gridMember));
    }

    @PostMapping("/delete")
    public ResponseData deleteGridMember(@RequestBody GridMember gridMember) {
        return ResponseData.success(gridMemberServicei.deleteGridMemberById(gridMember.getGmId()));
    }
    @PostMapping("/updateState")
    public ResponseData updateState(@RequestBody GridMember gridMember) {
        return ResponseData.success(gridMemberServicei.updateStateById(gridMember.getGmId(), gridMember.getState()));
    }

    @GetMapping("/findByCityId")
    public ResponseData getGridMembersByCityId(@RequestParam("cityId") Integer cityId) {
        return ResponseData.success(gridMemberServicei.getGridMembersByCityId(cityId));
    }

    @GetMapping("/findByName")
    public ResponseData getGridMembersByName(@RequestParam("gmName") String gmName) {
        return ResponseData.success(gridMemberServicei.getGridMembersByName(gmName));
    }

    @PostMapping("/update")
    public ResponseData updateGridMember(@RequestBody GridMember gridMember) {
        return ResponseData.success(gridMemberServicei.updateGridMember(gridMember));
    }
}
