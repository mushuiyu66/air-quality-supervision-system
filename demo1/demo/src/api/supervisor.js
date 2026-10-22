import request from '@/utils/request'

// 公众监督员登录
export const supervisorLogin = (data) => {
    return request.post('/supervisor/SupervisorLogin', data)
}

// 公众监督员注册
export const supervisorRegister = (data) => {
    return request.post('/supervisor/signUp', data)
}

// 获取监督员列表
export const getSupervisorList = () => {
    return request.get('/supervisor')
}

// 根据电话获取监督员信息S
export const getSupervisorByTel = (tel) => {
    return request.get(`/supervisor/${tel}`)
}

// 更新监督员信息
export const updateSupervisor = (data) => {
    return request.post('/supervisor/update', data)
}

// 新增监督员（使用注册接口）
export const addSupervisor = (data) => {
    return request.post('/supervisor/signUp', data)
}

// 删除监督员
export const deleteSupervisor = (tel) => {
    return request.post('/supervisor/delete', { tel_id: tel })
}

// ==================== 市民反馈相关 API ====================

// 提交反馈 (对应后端 POST /aqiFeedBack/insert)
export const submitFeedback = (data) => {
    return request.post('/aqiFeedBack/insert', data)
}

// 获取所有反馈列表 (对应后端 GET /aqiFeedBack/findAll)
export const getMyFeedbacks = () => {
    return request.get('/aqiFeedBack/findAll')
}

// 根据反馈 ID 查询对应的检测统计记录
export const getStatisticsByAfId = (afId) => {
    return request.get(`/statistics/getByFdId?fdId=${afId}`)
}
