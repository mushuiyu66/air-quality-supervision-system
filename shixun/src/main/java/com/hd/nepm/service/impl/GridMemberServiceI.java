package com.hd.nepm.service.impl;

import com.hd.nepm.config.JwtUtil;
import com.hd.nepm.config.PasswordSupport;
import com.hd.nepm.mapper.GridMapper;
import com.hd.nepm.po.GridMember;
import com.hd.nepm.service.GridMemberService;
import com.hd.nepm.vo.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Package: com.hd.nepm.service.impl
 * Description：
 *
 * @Author msy
 * @Create 2026/6/29 10:46
 * @Version 1.0
 */
@Service
public class GridMemberServiceI implements GridMemberService {
    @Autowired
    private GridMapper gridMapper;
    @Autowired
    private PasswordSupport passwordSupport;
    @Autowired
    private JwtUtil jwtUtil;
    @Override
    public List<GridMember> getAllGridMembersWithProvinceAndCity() {
        return gridMapper.getAllGridMembersWithProvinceAndCity();
    }
    @Override
    public ResponseData<GridMember> selectGridMemberByCodeAndPassword(GridMember gridMember){
        GridMember result = gridMapper.selectGridMemberByCode(gridMember.getGmCode());
        if(result==null)
        {
            return ResponseData.error(404,"用户名不存在");
        }
        else{
            if(passwordSupport.matches(gridMember.getPassword(), result.getPassword())) {
                // 旧明文密码登录成功后改成哈希
                if (!passwordSupport.isEncoded(result.getPassword())) {
                    String encoded = passwordSupport.encode(gridMember.getPassword());
                    gridMapper.updatePassword(result.getGmId(), encoded);
                }
                result.setPassword(null);
                result.setToken(jwtUtil.create("grid", String.valueOf(result.getGmId())));
                return ResponseData.success(result);
            }
            else
                return ResponseData.error(404,"密码错误");
        }
    }
    @Override
    public ResponseData<GridMember> insertGridMember(GridMember gridMember) {
        GridMember existing = gridMapper.selectGridMemberByCode(gridMember.getGmCode());
        if (existing != null) {
            return ResponseData.error(400, "该网格员编号已存在");
        }
        if(gridMember.getState() == null) {
            gridMember.setState(0);
        }
        gridMember.setPassword(passwordSupport.encode(gridMember.getPassword()));
        if(gridMapper.insertGridMember(gridMember) > 0) {
            gridMember.setPassword(null);
            return ResponseData.success(gridMember);
        }
        else
            return ResponseData.error(500, "添加失败");
    }
    @Override
    public ResponseData<GridMember> deleteGridMemberById(Integer gmId) {
        if(gridMapper.deleteGridMemberById(gmId) > 0)
            return ResponseData.success(null);
        else
            return ResponseData.error(500, "删除失败");
    }
    @Override
    public ResponseData<GridMember> updateStateById(Integer gmId, Integer state) {
        if(gridMapper.updateStateById(gmId, state) > 0)
            return ResponseData.success(null);
        else
            return ResponseData.error(500, "修改失败");
    }

    @Override
    public List<GridMember> getGridMembersByCityId(Integer cityId) {
        return gridMapper.getGridMembersByCityId(cityId);
    }

    @Override
    public List<GridMember> getGridMembersByName(String gmName) {
        return gridMapper.getGridMembersByName(gmName);
    }

    @Override
    public ResponseData<GridMember> updateGridMember(GridMember gridMember) {
        if (gridMember.getGmId() == null) {
            return ResponseData.error(400, "网格员ID不能为空");
        }
        GridMember existing = gridMapper.getGridMemberById(gridMember.getGmId());
        if (existing == null) {
            return ResponseData.error(404, "网格员不存在");
        }
        if (gridMember.getPassword() == null || gridMember.getPassword().isEmpty()) {
            gridMember.setPassword(existing.getPassword());
        } else {
            gridMember.setPassword(passwordSupport.encodeIfPlain(gridMember.getPassword()));
        }
        if (gridMapper.updateGridMember(gridMember) > 0) {
            gridMember.setPassword(null);
            return ResponseData.success(gridMember);
        } else {
            return ResponseData.error(500, "修改失败");
        }
    }
}

