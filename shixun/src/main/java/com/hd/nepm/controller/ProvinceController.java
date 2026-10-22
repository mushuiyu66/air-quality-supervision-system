package com.hd.nepm.controller;

import com.hd.nepm.po.Province;
import com.hd.nepm.service.impl.ProvinceServiceI;
import com.hd.nepm.vo.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Package: com.hd.nepm.controller
 * Description：
 *
 * @Author msy
 * @Create 2026/7/1
 * @Version 1.0
 */
@RestController
@RequestMapping("/province")
@CrossOrigin
public class ProvinceController {
    @Autowired
    private ProvinceServiceI provinceServiceI;

    @GetMapping
    public ResponseData getAllProvince() {
        return provinceServiceI.getAllProvince();
    }

    @GetMapping("/{provinceId}")
    public ResponseData getProvinceById(@PathVariable Integer provinceId) {
        return provinceServiceI.getProvinceById(provinceId);
    }

    @PostMapping("/insert")
    public ResponseData insertProvince(@RequestBody Province province) {
        return provinceServiceI.insertProvince(province);
    }

    @PutMapping("/update")
    public ResponseData updateProvince(@RequestBody Province province) {
        return provinceServiceI.updateProvince(province);
    }

    @DeleteMapping("/delete/{provinceId}")
    public ResponseData deleteProvinceById(@PathVariable Integer provinceId) {
        return provinceServiceI.deleteProvinceById(provinceId);
    }
}
