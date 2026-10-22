package com.hd.nepm.service;

import com.hd.nepm.po.AqiFeedBack;
import com.hd.nepm.po.Statistics;
import com.hd.nepm.vo.ResponseData;

import java.util.List;

public interface StatisticsService {
    List<Statistics> selectAll();
    ResponseData<Statistics> confirm(AqiFeedBack aqiFeedBack);
    ResponseData<Statistics> getByFdId(Integer fdId);
}
