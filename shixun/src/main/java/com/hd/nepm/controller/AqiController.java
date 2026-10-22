package com.hd.nepm.controller;

import com.hd.nepm.po.Aqi;
import com.hd.nepm.service.impl.AqiServiceImpl;
import com.hd.nepm.vo.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/aqi")
@CrossOrigin
public class AqiController {
    @Autowired
    private AqiServiceImpl aqiServiceImpl;

    @GetMapping("/getAll")
    public ResponseData getAll() {
        return aqiServiceImpl.selectAll();
    }

    @GetMapping("/getById/{aqiId}")
    public ResponseData getById(@PathVariable Integer aqiId) {
        return aqiServiceImpl.selectById(aqiId);
    }

    @PostMapping("/insert")
    public ResponseData insert(@RequestBody Aqi aqi) {
        return aqiServiceImpl.insert(aqi);
    }

    @PutMapping("/update")
    public ResponseData update(@RequestBody Aqi aqi) {
        return aqiServiceImpl.update(aqi);
    }

    @DeleteMapping("/delete/{aqiId}")
    public ResponseData delete(@PathVariable Integer aqiId) {
        return aqiServiceImpl.deleteById(aqiId);
    }
}
