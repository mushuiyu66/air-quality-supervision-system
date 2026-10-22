package com.hd.nepm.service;

import com.hd.nepm.po.Admins;
import com.hd.nepm.vo.ResponseData;

/**
 * Package: com.hd.nepm.service
 * Description：
 *
 * @Author msy
 * @Create 2026/6/24 9:49
 * @Version 1.0
 */
public interface AdminsService {
    //登录功能
    ResponseData<Admins> selectAdminsByCodeAndPassword(Admins admins);
    ResponseData<Admins> insertAdmins(Admins admins);
}
