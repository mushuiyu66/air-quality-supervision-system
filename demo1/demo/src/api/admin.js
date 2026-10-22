import request from '@/utils/request'

// 系统管理员登录
export const adminLogin = (data) => {
    return request.post('/admins/login', data)
}

// 系统管理员注册
export const adminRegister = (data) => {
    return request.post('/admins/insert', data)
}

// 获取管理员列表
export const getAdminList = () => {
    return request.get('/admins')
}

// 根据ID获取管理员信息
export const getAdminById = (id) => {
    return request.get(`/admins/${id}`)
}

// 更新管理员信息
export const updateAdmin = (data) => {
    return request.put(`/admins/${data.admin_id}`, data)
}

// 删除管理员
export const deleteAdmin = (id) => {
    return request.delete(`/admins/${id}`)
}

// 新增管理员
export const addAdmin = (data) => {
    return request.post('/admins', data)
}

// 获取所有统计数据
export const getAllStatistics = (params) => {
    return request.get('/statistics/page', { params })
}

// 获取省份列表
export const getProvinceList = () => {
    return request.get('/province')
}

// 获取城市列表
export const getCityList = (provinceId) => {
    if (provinceId) {
        return request.get(`/city/byProvince/${provinceId}`)
    }
    return request.get('/city')
}