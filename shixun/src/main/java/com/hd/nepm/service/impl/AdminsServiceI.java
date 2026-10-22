package com.hd.nepm.service.impl;

import com.hd.nepm.config.JwtUtil;
import com.hd.nepm.config.PasswordSupport;
import com.hd.nepm.mapper.AdminsMapper;
import com.hd.nepm.po.Admins;
import com.hd.nepm.service.AdminsService;
import com.hd.nepm.vo.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Package: com.hd.nepm.service.impl
 * Description：
 *
 * @Author msy
 * @Create 2026/6/24 9:50
 * @Version 1.0
 */
@Service
public class AdminsServiceI implements AdminsService {
    @Autowired
    private AdminsMapper adminsMapper;
    @Autowired
    private PasswordSupport passwordSupport;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public ResponseData<Admins> insertAdmins(Admins admins) {
        admins.setPassword(passwordSupport.encode(admins.getPassword()));
        int rows = adminsMapper.insertAdmins(admins);
        if (rows > 0) {
            admins.setPassword(null);
            return ResponseData.success(admins);
        } else {
            return ResponseData.error(2001, "插入失败");
        }
    }
    @Override
    public ResponseData<Admins> selectAdminsByCodeAndPassword(Admins admins) {
        Admins admins1 = adminsMapper.selectAdminsByCode(admins.getAdminCode());
        if (admins1 == null)
            return ResponseData.error(400, "用户名不存在");
        if (!passwordSupport.matches(admins.getPassword(), admins1.getPassword())) {
            return ResponseData.error(1001, "密码错误");
        }
        // 旧明文密码登录成功后立刻改成哈希，响应里不再带回密码
        if (!passwordSupport.isEncoded(admins1.getPassword())) {
            String encoded = passwordSupport.encode(admins.getPassword());
            adminsMapper.updatePassword(admins1.getAdminCode(), encoded);
        }
        admins1.setPassword(null);
        admins1.setToken(jwtUtil.create("admin", admins1.getAdminCode()));
        return ResponseData.success(admins1);
    }
}