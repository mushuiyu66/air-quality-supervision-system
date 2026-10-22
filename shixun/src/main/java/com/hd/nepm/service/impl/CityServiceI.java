package com.hd.nepm.service.impl;

import com.hd.nepm.mapper.CityMapper;
import com.hd.nepm.po.City;
import com.hd.nepm.service.CityService;
import com.hd.nepm.vo.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Package: com.hd.nepm.service.impl
 * Description：
 *
 * @Author msy
 * @Create 2026/7/1
 * @Version 1.0
 */
@Service
public class CityServiceI implements CityService {
    @Autowired
    private CityMapper cityMapper;

    @Override
    public ResponseData<List<City>> getAllCity() {
        List<City> list = cityMapper.selectAllCity();
        return ResponseData.success(list);
    }

    @Override
    public ResponseData<City> getCityById(Integer cityId) {
        City city = cityMapper.selectCityById(cityId);
        if (city == null) {
            return ResponseData.error(404, "城市不存在");
        }
        return ResponseData.success(city);
    }

    @Override
    public ResponseData<City> insertCity(City city) {
        int rows = cityMapper.insertCity(city);
        if (rows > 0) {
            return ResponseData.success(city);
        } else {
            return ResponseData.error(2001, "插入失败");
        }
    }

    @Override
    public ResponseData<City> updateCity(City city) {
        int rows = cityMapper.updateCity(city);
        if (rows > 0) {
            return ResponseData.success(city);
        } else {
            return ResponseData.error(2001, "修改失败");
        }
    }

    @Override
    public ResponseData<City> deleteCityById(Integer cityId) {
        City city = cityMapper.selectCityById(cityId);
        if (city == null) {
            return ResponseData.error(404, "城市不存在");
        }
        int rows = cityMapper.deleteCityById(cityId);
        if (rows > 0) {
            return ResponseData.success(city);
        } else {
            return ResponseData.error(2001, "删除失败");
        }
    }
}
