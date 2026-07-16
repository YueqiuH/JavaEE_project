<template>
    <div class="login-page">
        <!-- 顶部导航 -->
        <header class="login-navbar">
            <div class="navbar-inner">
                <div class="logo" @click="goHome">
                    <svg viewBox="0 0 40 40" width="36" height="36">
                        <defs>
                            <linearGradient id="navLogoGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                                <stop offset="0%" style="stop-color:#3B82F6;stop-opacity:1" />
                                <stop offset="100%" style="stop-color:#06B6D4;stop-opacity:1" />
                            </linearGradient>
                        </defs>
                        <rect x="4" y="4" width="32" height="32" rx="8" fill="url(#navLogoGrad)" />
                        <text x="20" y="27" text-anchor="middle" fill="white" font-size="18" font-weight="bold" font-family="sans-serif">智</text>
                    </svg>
                    <span class="logo-text">智驭校园</span>
                </div>
                <a href="/" class="back-link" @click.prevent="goHome">← 返回首页</a>
            </div>
        </header>

        <!-- 登录主体 -->
        <div class="login-body">
            <div class="login-card">
                <div class="login-header">
                    <h2>欢迎回来</h2>
                    <p>登录智驭校园，开启智慧教育之旅</p>
                </div>
                <el-form ref="ruleFormRef" :model="formData" :rules="rules" label-width="auto">
                    <el-form-item prop="username">
                        <el-input
                            v-model="formData.username"
                            placeholder="请输入账号"
                            :prefix-icon="UserFilled"
                            size="large"
                        />
                    </el-form-item>
                    <el-form-item prop="password">
                        <el-input
                            v-model="formData.password"
                            type="password"
                            placeholder="请输入密码"
                            :prefix-icon="Lock"
                            size="large"
                            @keydown.enter="submitForm(ruleFormRef)"
                        />
                    </el-form-item>
                    <el-form-item>
                        <el-button
                            class="btn-submit"
                            size="large"
                            @click="submitForm(ruleFormRef)"
                            :loading="buttonLoading"
                        >
                            登 录
                        </el-button>
                    </el-form-item>
                </el-form>
                <div class="login-footer-text">
                    <span>测试账号：600001 / 123321</span>
                </div>
            </div>
        </div>

        <!-- 底部版权 -->
        <div class="login-copyright">
            Copyright © 2024-2026 智驭校园. All Rights Reserved.
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, Lock } from '@element-plus/icons-vue'
import { loginUser } from '@/api/auth.js'
import { setAccessToken } from '@/utils/authToken.js'
import { setStoredCurrentUser } from '@/utils/authSession.js'

const router = useRouter()

const buttonLoading = ref(false)
const ruleFormRef = ref(null)

const formData = ref({
    username: '600001',
    password: '123321'
})

const rules = {
    username: [
        { required: true, message: '请输入账号', trigger: 'blur' },
        { max: 64, message: '账号长度不能超过64位', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { max: 128, message: '密码长度不能超过128位', trigger: 'blur' }
    ]
}

const goHome = () => {
    router.push('/')
}

const submitForm = (formEl) => {
    if (!formEl) return
    formEl.validate((valid) => {
        if (valid) {
            login()
        }
    })
}

const login = async () => {
    buttonLoading.value = true
    try {
        const result = await loginUser(formData.value)
        setAccessToken(result.data.token)
        setStoredCurrentUser(result.data.currentUser)
        ElMessage({ message: '登录成功', type: 'success' })
        const redirect = router.currentRoute.value.query.redirect
        await router.push(typeof redirect === 'string' ? redirect : '/home')
    } catch {
        // 请求拦截器统一展示登录失败信息。
    } finally {
        buttonLoading.value = false
    }
}
</script>

<style scoped>
.login-page {
    min-height: 100vh;
    background: linear-gradient(135deg, #0F172A 0%, #1E3A5F 40%, #1A3A5C 70%, #0F172A 100%);
    display: flex;
    flex-direction: column;
    position: relative;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.login-page::before {
    content: '';
    position: absolute;
    inset: 0;
    background:
        radial-gradient(ellipse at 30% 20%, rgba(59,130,246,0.2) 0%, transparent 50%),
        radial-gradient(ellipse at 70% 80%, rgba(6,182,212,0.15) 0%, transparent 50%);
    pointer-events: none;
}

/* 导航 */
.login-navbar {
    position: relative;
    z-index: 1;
    height: 72px;
    display: flex;
    align-items: center;
}
.navbar-inner {
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 40px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.logo {
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
}
.logo-text {
    font-size: 20px;
    font-weight: 700;
    letter-spacing: 2px;
    background: linear-gradient(135deg, #3B82F6, #06B6D4);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}
.back-link {
    color: rgba(255,255,255,0.6);
    text-decoration: none;
    font-size: 14px;
    transition: color 0.2s;
}
.back-link:hover { color: #fff; }

/* 登录卡片 */
.login-body {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    z-index: 1;
}
.login-card {
    background: rgba(255,255,255,0.05);
    backdrop-filter: blur(20px);
    border: 1px solid rgba(255,255,255,0.1);
    border-radius: 20px;
    padding: 48px 40px;
    width: 420px;
}
.login-header {
    text-align: center;
    margin-bottom: 36px;
}
.login-header h2 {
    font-size: 28px;
    font-weight: 700;
    color: #fff;
    margin-bottom: 8px;
}
.login-header p {
    font-size: 14px;
    color: rgba(255,255,255,0.5);
}
.btn-submit {
    width: 100%;
    height: 48px !important;
    background: linear-gradient(135deg, #3B82F6, #2563EB) !important;
    border: none !important;
    color: #fff !important;
    font-size: 16px !important;
    font-weight: 600;
    letter-spacing: 4px;
    border-radius: 10px !important;
    box-shadow: 0 4px 20px rgba(59,130,246,0.4);
    transition: all 0.3s;
}
.btn-submit:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 30px rgba(59,130,246,0.5);
}
.login-footer-text {
    text-align: center;
    color: rgba(255,255,255,0.3);
    font-size: 13px;
    margin-top: 8px;
}

/* Element Plus 输入框覆写 */
:deep(.el-input__wrapper) {
    background: rgba(255,255,255,0.08) !important;
    border: 1px solid rgba(255,255,255,0.15) !important;
    border-radius: 10px !important;
    box-shadow: none !important;
}
:deep(.el-input__wrapper:hover) {
    border-color: rgba(255,255,255,0.3) !important;
}
:deep(.el-input__wrapper.is-focus) {
    border-color: #3B82F6 !important;
}
:deep(.el-input__inner) {
    color: #fff !important;
}
:deep(.el-input__inner::placeholder) {
    color: rgba(255,255,255,0.35) !important;
}
:deep(.el-input .el-input__prefix) {
    color: rgba(255,255,255,0.4) !important;
}

/* 版权 */
.login-copyright {
    position: relative;
    z-index: 1;
    text-align: center;
    padding: 24px;
    color: rgba(255,255,255,0.25);
    font-size: 13px;
}
</style>
