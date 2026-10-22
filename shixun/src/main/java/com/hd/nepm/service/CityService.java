package com.hd.nepm.service;

import com.hd.nepm.po.City;
import com.hd.nepm.vo.ResponseData;

import java.util.List;

/**
 * Package: com.hd.nepm.service
 * Description：
 *
 * @Author msy
 * @Create 2026/7/1
 * @Version 1.0
 */
public interface CityService {
    ResponseData<List<City>> getAllCity();
    ResponseData<City> getCityById(Integer cityId);
    ResponseData<City> insertCity(City city);
    ResponseData<City> updateCity(City city);
    ResponseData<City> deleteCityById(Integer cityId);
}
