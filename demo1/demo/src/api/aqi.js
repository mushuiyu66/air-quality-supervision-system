import request from '@/utils/request'

// 获取AQI标准列表
export const getAqiList = () => {
    return request.get('/aqi/getAll')
}

// 根据ID获取AQI标准信息
export const getAqiById = (id) => {
    return request.get(`/aqi/getById/${id}`)
}

// 新增AQI标准
export const addAqi = (data) => {
    return request.post('/aqi/insert', data)
}

// 更新AQI标准
export const updateAqi = (data) => {
    return request.put('/aqi/update', data)
}

// 删除AQI标准
export const deleteAqi = (id) => {
    return request.delete(`/aqi/delete/${id}`)
}
