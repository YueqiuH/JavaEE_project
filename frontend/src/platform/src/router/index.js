//1、导入我们需要跳转的vue页面
import HomeView from "@/views/HomeView.vue";
import MainView from "@/views/MainView.vue";

//2.导入vue路由对象
import {
    createRouter,
    createWebHistory
} from 'vue-router'

//3.创建路由对象
const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'home',
            component: HomeView,
            meta: {
                name: "主页"
            },
            children: [
                {
                    path: '/main',
                    name: 'main',
                    component: MainView,
                    meta: {
                        name: "首页"
                    }
                }
            ]
        }
    ]
})

//4.export导出
export default router
