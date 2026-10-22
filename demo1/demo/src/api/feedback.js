import request from '@/utils/request'

// 获取所有工单列表(管理员用)
export const getFeedbackList = (params) => {
    return request.get('/aqiFeedBack/findAllForAdmin', { params })
}

// 更新工单分配(指派网格员)
export const assignFeedback = (data) => {
    return request.put('/aqiFeedBack/updateAssignment', data)
}
