package com.hd.nepm.service;

import com.hd.nepm.po.AqiFeedBack;
import com.hd.nepm.vo.ResponseData;

import java.util.List;

public interface AqiFeedBackService {
    List<AqiFeedBack> findAll();
    List<AqiFeedBack> findAllForAdmin();
    List<AqiFeedBack> findByGmId(Integer gmId);
    ResponseData<AqiFeedBack> insert(AqiFeedBack aqiFeedBack);
    ResponseData<AqiFeedBack> updateAssignment(AqiFeedBack aqiFeedBack);

}
