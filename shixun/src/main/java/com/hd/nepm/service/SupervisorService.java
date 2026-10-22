package com.hd.nepm.service;

import com.hd.nepm.po.Supervisor;
import com.hd.nepm.vo.ResponseData;

import java.util.List;

/**
 * Package: com.hd.nepm.service
 * Description：
 *
 * @Author msy
 * @Create 2026/6/29 15:52
 * @Version 1.0
 */
public interface SupervisorService {
    ResponseData<Supervisor>selectByTelIdAndPassword(Supervisor supervisor);
    ResponseData<Supervisor>signUp(Supervisor supervisor);
    ResponseData<List<Supervisor>> getAllSupervisors();
    ResponseData<Supervisor> getSupervisorByTelId(String telId);
    ResponseData<Supervisor> updateSupervisor(Supervisor supervisor);
    ResponseData<Supervisor> deleteSupervisor(String telId);
}
