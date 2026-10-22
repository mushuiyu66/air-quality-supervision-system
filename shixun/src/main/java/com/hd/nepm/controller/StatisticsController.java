package com.hd.nepm.controller;

import com.hd.nepm.po.AqiFeedBack;
import com.hd.nepm.po.Statistics;
import com.hd.nepm.service.impl.StatisticsServiceI;
import com.hd.nepm.vo.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/statistics")
@CrossOrigin
public class StatisticsController {
    @Autowired
    private StatisticsServiceI statisticsServicei;

    @GetMapping("/getAll")
    public ResponseData getAll() {
        return ResponseData.success(statisticsServicei.selectAll());
    }

    @GetMapping("/page")
    public ResponseData page(@RequestParam(defaultValue = "1") long page,
                             @RequestParam(defaultValue = "10") long size,
                             @RequestParam(required = false) Integer provinceId,
                             @RequestParam(required = false) Integer cityId,
                             @RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate) {
        return ResponseData.success(statisticsServicei.page(page, size, provinceId, cityId, startDate, endDate));
    }

    @PostMapping("/confirm")
    public ResponseData confirm(@RequestBody AqiFeedBack aqiFeedBack) {
        return statisticsServicei.confirm(aqiFeedBack);
    }

    @GetMapping("/getByFdId")
    public ResponseData getByFdId(@RequestParam("fdId") Integer fdId) {
        return statisticsServicei.getByFdId(fdId);
    }
}
