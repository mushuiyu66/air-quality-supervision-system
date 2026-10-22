import request from '@/utils/request'

// 网格员登录
export const gmLogin = (data) => {
    return request.post('/gridMember/gridlogin', data)
}

// 网格员注册
export const gmRegister = (data) => {
    return request.post('/gridMember', data)
}

// 获取所有网格员列表（带省市信息）
export const getGridMemberList = () => {
    return request.get('/gridMember')
}

// 根据姓名搜索网格员
export const getGridMembersByName = (gmName) => {
    return request.get(`/gridMember/findByName?gmName=${gmName}`)
}

// 获取所有网格员(用于下拉列表)
export const getAllGridMembers = () => {
    return request.get('/gridMember')
}

// 根据城市ID获取网格员列表（用于指派工单）
export const getGridMembersByCityId = (cityId) => {
    return request.get(`/gridMember/findByCityId?cityId=${cityId}`)
}

// 根据ID获取单个网格员信息
export const getGmById = (gmId) => {
    return request.get(`/gridMember/${gmId}`)
}

// 新增网格员
export const addGridMember = (data) => {
    // 根据后端SQL: INSERT INTO grid_member (tel,gm_code, gm_name, password, province_id, city_id)
    // 传递数据库表中所有需要的字段
    const backendData = {
        tel: data.tel,
        gmCode: data.gmCode,
        gmName: data.gmName,
        password: data.password,
        provinceId: data.provinceId,
        cityId: data.cityId
    }
    console.log('addGridMember - 发送给后端的原始数据:', data)
    console.log('addGridMember - 转换后的后端数据:', backendData)
    return request.post('/gridMember/insert', backendData)
}

// 删除网格员
export const deleteGridMember = (data) => {
    // 传递gmId给后端 POST /gridMember/delete
    return request.post('/gridMember/delete', data)
}

// 更新网格员状态
export const updateGridMemberState = (data) => {
    // 传递gmId和state给后端 POST /gridMember/updateState
    return request.post('/gridMember/updateState', data)
}

// ==================== 编辑功能 ====================

// 更新网格员信息(待后端实现)
export const updateGridMember = (data) => {
    // 暂时使用POST请求,等待后端提供PUT /gridMember/update接口
    return request.post('/gridMember/update', data)
}

// ==================== 新增 API 接口 ====================

// 获取分配给当前网格员的所有工单 (state=1 已指派)
export const getAssignedFeedbacks = (gmId) => {
    return request.get(`/aqiFeedBack/findByGmId?gmId=${gmId}`)
}

// 【调试】获取所有工单（用于排查问题）
export const getAllFeedbacksForDebug = () => {
    return request.get('/aqiFeedBack/findAll')
}

// 确认工单 - 调用 /statistics/confirm 接口
export const confirmFeedback = (afId) => {
    // 后端 /statistics/confirm 接口需要 AqiFeedBack 对象
    return request.post('/statistics/confirm', { afId })
}

// 获取所有 AQI 标准
export const getAqiList = () => {
    return request.get('/aqi/getAll')
}

// 获取当前网格员的历史统计记录
export const getMyStatistics = (gmId) => {
    return request.get(`/statistics?gm_id=${gmId}`)
}

// 新增统计数据记录
export const addStatistics = (data) => {
    // 使用后端的 /statistics/confirm 接口来创建统计记录
    return request.post('/statistics/confirm', data)
}

// 获取所有统计数据（分配给网格员的待确认任务）
export const getAllStatistics = () => {
    return request.get('/statistics/getAll')
}

// 确认统计数据（执行任务）
export const confirmStatistics = (data) => {
    return request.post('/statistics/confirm', data)
}

// 根据反馈 ID 查询对应的检测统计记录
export const getStatisticsByAfId = (afId) => {
    return request.get(`/statistics/getByFdId?fdId=${afId}`)
}
