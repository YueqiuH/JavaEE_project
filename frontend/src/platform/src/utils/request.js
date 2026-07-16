import axios from 'axios';
import { ElMessage } from 'element-plus';
import { getStorage } from "@/utils/localStorage.js";
import Router from '@/router'

const URL = "http://localhost:8888";

// create an axios instance
const service = axios.create({
    baseURL: URL,
    timeout: 1000000,
    crossDomain: true
})


// http request 拦截器
service.interceptors.request.use(
    config => {
        config.headers['Token'] = getStorage('Token')
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

service.interceptors.response.use(
    response => {
        if (response.data) {//服务器返回了数据
            //对自己的返回结果做判断
            if (response.data.code == 200) {
                return response.data
            } else if (response.data.code == 401) {
                //登录失效, 跳转到登录页面
                ElMessage({
                    message: response.data.msg,
                    type: "error",
                    duration: 2000
                });
                //跳转到登录页面
                Router.push({ path: "/" })
                return false;
            } else {
                ElMessage({
                    message: response.data.msg,
                    type: "error",
                    duration: 2000
                });
            }
            return -1;
        }

    },
    error => {
        return Promise.reject(error)
    }
)

export default service
