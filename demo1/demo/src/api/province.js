import request from '@/utils/request'

// 获取省份列表
export const getProvinceList = () => {
    return request.get('/province')
}

// 获取省份详情
export const getProvinceById = (provinceId) => {
    return request.get(`/province/${provinceId}`)
}

// 获取城市列表(获取所有城市)
export const getCityList = () => {
    return request.get('/city')
}

// 获取城市详情
export const getCityById = (cityId) => {
    return request.get(`/city/${cityId}`)
}

// 新增省份
export const addProvince = (data) => {
    return request.post('/province/insert', data)
}

// 新增城市
export const addCity = (data) => {
    return request.post('/city/insert', data)
}

// 更新省份
export const updateProvince = (data) => {
    return request.put('/province/update', data)
}

// 更新城市
export const updateCity = (data) => {
    return request.put('/city/update', data)
}

// 删除省份
export const deleteProvince = (provinceId) => {
    return request.delete(`/province/delete/${provinceId}`)
}

// 删除城市
export const deleteCity = (cityId) => {
    return request.delete(`/city/delete/${cityId}`)
}
