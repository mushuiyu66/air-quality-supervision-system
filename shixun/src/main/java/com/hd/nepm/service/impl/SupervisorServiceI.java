package com.hd.nepm.service.impl;

import com.hd.nepm.config.JwtUtil;
import com.hd.nepm.config.PasswordSupport;
import com.hd.nepm.mapper.SupervisorMapper;
import com.hd.nepm.po.Supervisor;
import com.hd.nepm.service.SupervisorService;
import com.hd.nepm.vo.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Package: com.hd.nepm.service.impl
 * Description：
 *
 * @Author msy
 * @Create 2026/6/29 15:54
 * @Version 1.0
 */
@Service
public class SupervisorServiceI implements SupervisorService {
    @Autowired
    private SupervisorMapper supervisorMapper;
    @Autowired
    private PasswordSupport passwordSupport;
    @Autowired
    private JwtUtil jwtUtil;
    @Override
    public ResponseData<Supervisor> selectByTelIdAndPassword(Supervisor supervisor) {
        Supervisor supervisor1 = supervisorMapper.selectByTelIdAndPassword(supervisor.getTelId());
        if(supervisor1==null){
            return ResponseData.error(404,"用户名不存在");
        }else{
            if(passwordSupport.matches(supervisor.getPassword(), supervisor1.getPassword())){
                // 旧明文密码登录成功后改成哈希
                if (!passwordSupport.isEncoded(supervisor1.getPassword())) {
                    String encoded = passwordSupport.encode(supervisor.getPassword());
                    supervisorMapper.updatePassword(supervisor1.getTelId(), encoded);
                }
                supervisor1.setPassword(null);
                supervisor1.setToken(jwtUtil.create("supervisor", supervisor1.getTelId()));
                return ResponseData.success(supervisor1);
            }else{
                return ResponseData.error(404,"密码错误");
            }
        }
    }
    @Override
    public ResponseData<Supervisor> signUp(Supervisor supervisor) {
        Supervisor supervisor1 = supervisorMapper.selectByTelIdAndPassword(supervisor.getTelId());
        if(supervisor1==null){
            supervisor.setPassword(passwordSupport.encode(supervisor.getPassword()));
            supervisorMapper.signUp(supervisor);
            supervisor.setPassword(null);
            return ResponseData.success(supervisor);
        }else{
            return ResponseData.error(404,"手机号已注册");
        }
    }

    @Override
    public ResponseData<List<Supervisor>> getAllSupervisors() {
        List<Supervisor> list = supervisorMapper.selectAll();
        return ResponseData.success(list);
    }

    @Override
    public ResponseData<Supervisor> getSupervisorByTelId(String telId) {
        Supervisor supervisor = supervisorMapper.selectByTelId(telId);
        if (supervisor == null) {
            return ResponseData.error(404, "市民不存在");
        }
        return ResponseData.success(supervisor);
    }

    @Override
    public ResponseData<Supervisor> updateSupervisor(Supervisor supervisor) {
        Supervisor existing = supervisorMapper.selectByTelId(supervisor.getTelId());
        if (existing == null) {
            return ResponseData.error(404, "市民不存在");
        }
        if (supervisor.getPassword() == null || supervisor.getPassword().isEmpty()) {
            supervisor.setPassword(existing.getPassword());
        } else {
            supervisor.setPassword(passwordSupport.encodeIfPlain(supervisor.getPassword()));
        }
        if (supervisorMapper.updateByTelId(supervisor) > 0) {
            supervisor.setPassword(null);
            return ResponseData.success(supervisor);
        }
        else
            return ResponseData.error(500, "修改失败");
    }

    @Override
    public ResponseData<Supervisor> deleteSupervisor(String telId) {
        Supervisor existing = supervisorMapper.selectByTelId(telId);
        if (existing == null) {
            return ResponseData.error(404, "市民不存在");
        }
        if (supervisorMapper.deleteByTelId(telId) > 0)
            return ResponseData.success(null);
        else
            return ResponseData.error(500, "删除失败");
    }
}
