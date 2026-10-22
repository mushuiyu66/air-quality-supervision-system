package com.hd.nepm.service;

import com.hd.nepm.po.Aqi;
import com.hd.nepm.vo.ResponseData;

import java.util.List;

public interface AqiService {
    ResponseData<List<Aqi>> selectAll();
    ResponseData<Aqi> selectById(Integer aqiId);
    ResponseData<Aqi> insert(Aqi aqi);
    ResponseData<Aqi> update(Aqi aqi);
    ResponseData<String> deleteById(Integer aqiId);
}
