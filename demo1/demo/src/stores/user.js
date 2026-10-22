import { defineStore } from "pinia"
import {ref} from 'vue'

export const useUserStore = defineStore('userInfo', () => {
    //定义用户信息
    const info = ref({})
    const setInfo = (data) => {    //定义修改用户信息方法
        info.value = data
    }
    const clearInfo = () => { //定义清空用户信息方法
        info.value = {}
    }
    return { info, setInfo, clearInfo }
})