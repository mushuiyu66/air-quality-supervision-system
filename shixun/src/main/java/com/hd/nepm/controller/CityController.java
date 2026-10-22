package com.hd.nepm.controller;

import com.hd.nepm.po.City;
import com.hd.nepm.service.impl.CityServiceI;
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
@RequestMapping("/city")
@CrossOrigin
public class CityController {
    @Autowired
    private CityServiceI cityServiceI;

    @GetMapping
    public ResponseData getAllCity() {
        return cityServiceI.getAllCity();
    }

    @GetMapping("/{cityId}")
    public ResponseData getCityById(@PathVariable Integer cityId) {
        return cityServiceI.getCityById(cityId);
    }

    @PostMapping("/insert")
    public ResponseData insertCity(@RequestBody City city) {
        System.out.println("接收到的城市数据: " + city);
        return cityServiceI.insertCity(city);
    }

    @PutMapping("/update")
    public ResponseData updateCity(@RequestBody City city) {
        System.out.println("更新的城市数据: " + city);
        return cityServiceI.updateCity(city);
    }

    @DeleteMapping("/delete/{cityId}")
    public ResponseData deleteCityById(@PathVariable Integer cityId) {
        return cityServiceI.deleteCityById(cityId);
    }
}
