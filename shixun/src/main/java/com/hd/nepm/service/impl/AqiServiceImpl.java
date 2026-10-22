package com.hd.nepm.service.impl;

import com.hd.nepm.mapper.AqiMapper;
import com.hd.nepm.po.Aqi;
import com.hd.nepm.service.AqiService;
import com.hd.nepm.vo.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AqiServiceImpl implements AqiService {
    @Autowired
    private AqiMapper aqiMapper;

    /**
     * 全表结果共用一个缓存键。sync=true 保证并发未命中时只有一个请求查库。
     * 查询失败抛异常，异常不会写入 Redis。
     */
    @Override
    @Cacheable(cacheNames = "aqi:list", key = "'all'", sync = true)
    public ResponseData<List<Aqi>> selectAll() {
    List<Aqi> list = aqiMapper.selectAll();
    if (list == null || list.isEmpty()) {
        throw new RuntimeException("AQI标准查询失败");
    }
    return ResponseData.success(list);
}

    @Override
    /** 按标准 id 缓存。查不到仍返回错误对象，该结果也会被缓存。 */
    @Cacheable(cacheNames = "aqi:detail", key = "#aqiId", sync = true)
    public ResponseData<Aqi> selectById(Integer aqiId) {
        Aqi aqi = aqiMapper.selectById(aqiId);
        if (aqi != null) {
            return ResponseData.success(aqi);
        } else {
            return ResponseData.error(500, "未找到对应记录");
        }
    }

    @Override
    /** 标准变更后清空列表和详情缓存，避免继续用旧的浓度区间。 */
    @CacheEvict(cacheNames = {"aqi:list", "aqi:detail"}, allEntries = true)
    public ResponseData<Aqi> insert(Aqi aqi) {
        int rows = aqiMapper.insert(aqi);
        if (rows > 0) {
            return ResponseData.success(aqi);
        } else {
            return ResponseData.error(500, "添加失败");
        }
    }

    @Override
    @CacheEvict(cacheNames = {"aqi:list", "aqi:detail"}, allEntries = true)
    public ResponseData<Aqi> update(Aqi aqi) {
        int rows = aqiMapper.update(aqi);
        if (rows > 0) {
            return ResponseData.success(aqi);
        } else {
            return ResponseData.error(500, "更新失败");
        }
    }

    @Override
    @CacheEvict(cacheNames = {"aqi:list", "aqi:detail"}, allEntries = true)
    public ResponseData<String> deleteById(Integer aqiId) {
        int rows = aqiMapper.deleteById(aqiId);
        if (rows > 0) {
            return ResponseData.success("删除成功");
        } else {
            return ResponseData.error(500, "删除失败");
        }
    }
}
