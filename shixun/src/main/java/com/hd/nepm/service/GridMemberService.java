package com.hd.nepm.service;

import com.hd.nepm.po.GridMember;
import com.hd.nepm.vo.ResponseData;

import java.util.List;

/**
 * Package: com.hd.nepm.service
 * Description：
 *
 * @Author msy
 * @Create 2026/6/29 10:46
 * @Version 1.0
 */
public interface GridMemberService {
    List<GridMember> getAllGridMembersWithProvinceAndCity();
    ResponseData<GridMember> selectGridMemberByCodeAndPassword(GridMember gridMember);
    ResponseData<GridMember> insertGridMember(GridMember gridMember);
    ResponseData<GridMember> deleteGridMemberById(Integer gmId);
    ResponseData<GridMember> updateStateById(Integer gmId, Integer state);
    List<GridMember> getGridMembersByCityId(Integer cityId);
    List<GridMember> getGridMembersByName(String gmName);
    ResponseData<GridMember> updateGridMember(GridMember gridMember);
}
