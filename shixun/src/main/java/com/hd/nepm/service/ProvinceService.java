package com.hd.nepm.service;

import com.hd.nepm.po.Province;
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
public interface ProvinceService {
    ResponseData<List<Province>> getAllProvince();
    ResponseData<Province> getProvinceById(Integer provinceId);
    ResponseData<Province> insertProvince(Province province);
    ResponseData<Province> updateProvince(Province province);
    ResponseData<Province> deleteProvinceById(Integer provinceId);
}
