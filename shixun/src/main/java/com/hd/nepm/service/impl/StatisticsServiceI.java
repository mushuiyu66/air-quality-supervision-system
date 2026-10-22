package com.hd.nepm.service.impl;

import com.hd.nepm.mapper.AqiFeedBackMapper;
import com.hd.nepm.mapper.AqiMapper;
import com.hd.nepm.mapper.StatisticsMapper;
import com.hd.nepm.po.AqiFeedBack;
import com.hd.nepm.po.Statistics;
import com.hd.nepm.service.StatisticsService;
import com.hd.nepm.vo.PageResult;
import com.hd.nepm.vo.ResponseData;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;

@Service
public class StatisticsServiceI implements StatisticsService {
    @Autowired
    private StatisticsMapper statisticsMapper;

    @Autowired
    private AqiFeedBackMapper aqiFeedBackMapper;

    @Autowired
    private AqiMapper aqiMapper;

    @Override
    public List<Statistics> selectAll() {
        return statisticsMapper.selectAllStatistics();
    }

    /** 统计分页。省、市、日期条件下推到 SQL，不再一次查出全表。 */
    public PageResult<Statistics> page(long page, long size, Integer provinceId, Integer cityId, String startDate, String endDate) {
        long current = page < 1 ? 1 : page;
        long pageSize = size < 1 ? 10 : Math.min(size, 100);
        IPage<Statistics> result = statisticsMapper.pageStatistics(new Page<>(current, pageSize), provinceId, cityId, startDate, endDate);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 现场核验。先把工单从已指派改为已核验，更新到行才写入统计；
     * 并发时只有一个请求能改到 state=1 的行。插入失败则回滚状态。
     */
    @Override
    @Transactional
    public ResponseData<Statistics> confirm(AqiFeedBack aqiFeedBack) {
        AqiFeedBack feedback = aqiFeedBackMapper.selectById(aqiFeedBack.getAfId());
        if (feedback == null) {
            return ResponseData.error(500, "未找到对应的反馈记录");
        }

        if (feedback.getState() == null || feedback.getState() != 1) {
            return ResponseData.error(500, "该反馈单尚未指派，无法确认");
        }

        Integer so2Value = aqiFeedBack.getSo2Value() != null ? aqiFeedBack.getSo2Value() : 0;
        Integer coValue = aqiFeedBack.getCoValue() != null ? aqiFeedBack.getCoValue() : 0;
        Integer spmValue = aqiFeedBack.getSpmValue() != null ? aqiFeedBack.getSpmValue() : 0;

        Integer so2Level = aqiMapper.getSo2Level(so2Value);
        Integer coLevel = aqiMapper.getCoLevel(coValue);
        Integer spmLevel = aqiMapper.getSpmLevel(spmValue);

        // 浓度落在标准表区间之外时，按一级处理，避免空指针
        if (so2Level == null) so2Level = 1;
        if (coLevel == null) coLevel = 1;
        if (spmLevel == null) spmLevel = 1;

        Statistics statistics = new Statistics();
        statistics.setProvinceId(feedback.getProvinceId());
        statistics.setCityId(feedback.getCityId());
        statistics.setAddress(feedback.getAddress());
        statistics.setInformation(feedback.getInformation());
        statistics.setFdId(aqiFeedBack.getAfId());
        statistics.setGmId(feedback.getGmId() != null ? feedback.getGmId() : 0);
        statistics.setConfirmDate(java.time.LocalDate.now().toString());
        statistics.setConfirmTime(java.time.LocalTime.now().toString().substring(0, 8));

        statistics.setSo2Value(so2Value);
        statistics.setSo2Level(so2Level);
        statistics.setCoValue(coValue);
        statistics.setCoLevel(coLevel);
        statistics.setSpmValue(spmValue);
        statistics.setSpmLevel(spmLevel);

        // 综合 AQI 等级取各污染物等级的最大值
        int aqiId = Math.max(Math.max(so2Level, coLevel), spmLevel);
        statistics.setAqiId(aqiId);

        statistics.setRemarks(aqiFeedBack.getRemarks());

        AqiFeedBack updateFeedback = new AqiFeedBack();
        updateFeedback.setAfId(aqiFeedBack.getAfId());
        int updated = aqiFeedBackMapper.updateStatus(updateFeedback);
        if (updated == 0) {
            return ResponseData.error(500, "该工单已被核验或状态已变化");
        }

        int rows = statisticsMapper.insert(statistics);
        if (rows > 0) {
            return ResponseData.success(statistics);
        }
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return ResponseData.error(500, "核验记录添加失败");
    }

    @Override
    public ResponseData<Statistics> getByFdId(Integer fdId) {
        Statistics statistics = statisticsMapper.selectByFdId(fdId);
        if (statistics == null) {
            return ResponseData.error(500, "未找到该反馈的检测记录");
        }
        return ResponseData.success(statistics);
    }
}
