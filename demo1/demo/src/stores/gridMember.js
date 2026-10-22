import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useGridMemberStore = defineStore('gridMemberInfo', () => {
    // 定义用户信息
    const info = ref({})
    
    // 修改用户信息方法
    const setInfo = (data) => {
        info.value = data
    }
    
    // 清空用户信息方法
    const clearInfo = () => {
        info.value = {}
    }
    
    return { info, setInfo, clearInfo }
})
