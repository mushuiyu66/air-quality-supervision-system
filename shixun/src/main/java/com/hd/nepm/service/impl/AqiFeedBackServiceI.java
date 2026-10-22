package com.hd.nepm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hd.nepm.mapper.AqiFeedBackMapper;
import com.hd.nepm.po.AqiFeedBack;
import com.hd.nepm.service.AqiFeedBackService;
import com.hd.nepm.vo.PageResult;
import com.hd.nepm.vo.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Package: com.hd.nepm.service.impl
 * Description：
 *
 * @Author msy
 * @Create 2026/6/30 11:55
 * @Version 1.0
 */
@Service
public class AqiFeedBackServiceI implements AqiFeedBackService {
    @Autowired
    private AqiFeedBackMapper aqiFeedBackMapper;
    
    @Override
    public List<AqiFeedBack> findAll() {
        return aqiFeedBackMapper.findAll();
    }
    
    @Override
    public List<AqiFeedBack> findAllForAdmin() {
        return aqiFeedBackMapper.findAllForAdmin();
    }

    /** 管理员工单分页。页码至少为 1，每页最多 100 条，关键词在 SQL 中过滤。 */
    public PageResult<AqiFeedBack> pageForAdmin(long page, long size, String keyword) {
        long current = page < 1 ? 1 : page;
        long pageSize = size < 1 ? 10 : Math.min(size, 100);
        IPage<AqiFeedBack> result = aqiFeedBackMapper.pageForAdmin(new Page<>(current, pageSize), keyword);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public List<AqiFeedBack> findByGmId(Integer gmId) {
        return aqiFeedBackMapper.findByGmId(gmId);
    }

    @Override
    public ResponseData<AqiFeedBack> insert(AqiFeedBack aqiFeedBack) {
        // 新工单默认待指派：state=0，尚未分配网格员
        if(aqiFeedBack.getEstimatedGrade() == null || aqiFeedBack.getEstimatedGrade() == 0) {
            aqiFeedBack.setEstimatedGrade(1);
        }
        if(aqiFeedBack.getAfDate() == null || aqiFeedBack.getAfDate().isEmpty()) {
            java.time.LocalDate now = java.time.LocalDate.now();
            aqiFeedBack.setAfDate(now.toString());
        }
        if(aqiFeedBack.getAfTime() == null || aqiFeedBack.getAfTime().isEmpty()) {
            java.time.LocalTime now = java.time.LocalTime.now();
            aqiFeedBack.setAfTime(now.toString().substring(0, 5));
        }
        if(aqiFeedBack.getGmId() == null) {
            aqiFeedBack.setGmId(0);
        }
        if(aqiFeedBack.getState() == null) {
            aqiFeedBack.setState(0);
        }
        if(aqiFeedBackMapper.insert(aqiFeedBack) > 0) {
            return ResponseData.success(aqiFeedBack);
        } else
            return ResponseData.error(500, "添加失败");
    }
    
    @Override
    /** 按工单编号更新。SQL 只允许 待指派→已指派，或 已指派→已核验，避免重复操作。 */
    public ResponseData<AqiFeedBack> updateAssignment(AqiFeedBack aqiFeedBack) {
        if(aqiFeedBackMapper.updateAssignment(aqiFeedBack) > 0)
            return ResponseData.success(aqiFeedBack);
        else
            return ResponseData.error(500, "工单状态已变化，请刷新后重试");
    }
}
