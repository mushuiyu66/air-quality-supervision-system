package com.hd.nepm.service.impl;

import com.hd.nepm.mapper.ProvinceMapper;
import com.hd.nepm.po.Province;
import com.hd.nepm.service.ProvinceService;
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
public class ProvinceServiceI implements ProvinceService {
    @Autowired
    private ProvinceMapper provinceMapper;

    @Override
    public ResponseData<List<Province>> getAllProvince() {
        List<Province> list = provinceMapper.selectAllProvince();
        return ResponseData.success(list);
    }

    @Override
    public ResponseData<Province> getProvinceById(Integer provinceId) {
        Province province = provinceMapper.selectProvinceById(provinceId);
        if (province == null) {
            return ResponseData.error(404, "省份不存在");
        }
        return ResponseData.success(province);
    }

    @Override
    public ResponseData<Province> insertProvince(Province province) {
        int rows = provinceMapper.insertProvince(province);
        if (rows > 0) {
            return ResponseData.success(province);
        } else {
            return ResponseData.error(2001, "插入失败");
        }
    }

    @Override
    public ResponseData<Province> updateProvince(Province province) {
        int rows = provinceMapper.updateProvince(province);
        if (rows > 0) {
            return ResponseData.success(province);
        } else {
            return ResponseData.error(2001, "修改失败");
        }
    }

    @Override
    public ResponseData<Province> deleteProvinceById(Integer provinceId) {
        Province province = provinceMapper.selectProvinceById(provinceId);
        if (province == null) {
            return ResponseData.error(404, "省份不存在");
        }
        int rows = provinceMapper.deleteProvinceById(provinceId);
        if (rows > 0) {
            return ResponseData.success(province);
        } else {
            return ResponseData.error(2001, "删除失败");
        }
    }
}
