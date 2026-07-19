<template>
  <main class="login-page">
    <section class="login-visual" aria-labelledby="login-brand-title">
      <img :src="campusHero" alt="秋日校园建筑与树木" />
      <div class="visual-overlay"></div>
      <router-link class="login-brand" to="/?preview=public"><BrandMark /><span><strong>智慧校园</strong><small>服务平台</small></span></router-link>
      <div class="visual-copy"><p>SMART CAMPUS</p><h1 id="login-brand-title">让校园服务更清晰、更高效</h1><span>统一连接教学、学生事务、协同办公与数据服务。</span><div class="visual-points"><div><el-icon><CircleCheckFilled /></el-icon>24 项校园服务统一入口</div><div><el-icon><CircleCheckFilled /></el-icon>面向角色的个人工作台</div><div><el-icon><CircleCheckFilled /></el-icon>完整覆盖办理与查询流程</div></div></div>
      <p class="photo-credit">校园图片：note thanun / Unsplash</p>
    </section>

    <section class="login-panel" aria-labelledby="login-title">
      <div class="login-form-wrap">
        <div class="mobile-brand"><BrandMark /><span><strong>智慧校园</strong><small>服务平台</small></span></div>
        <p class="form-kicker">统一身份入口</p>
        <h2 id="login-title">登录平台</h2>
        <p class="form-intro">使用校园账号访问与你相关的服务与待办。</p>

        <el-form ref="ruleFormRef" :model="formData" :rules="rules" label-position="top" size="large" @submit.prevent="submitForm(ruleFormRef)">
          <el-form-item label="校园账号" prop="username"><el-input v-model="formData.username" placeholder="学号、工号或管理员账号" :prefix-icon="User" autocomplete="username" /></el-form-item>
          <el-form-item label="登录密码" prop="password"><el-input v-model="formData.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password autocomplete="current-password" @keydown.enter="submitForm(ruleFormRef)" /></el-form-item>
          <div class="form-options"><el-checkbox v-model="rememberAccount">记住账号</el-checkbox><button type="button" @click="ElMessage.info('请联系平台管理员重置密码')">忘记密码</button></div>
          <el-button class="login-button" native-type="submit" type="primary" size="large" :loading="buttonLoading" @click="submitForm(ruleFormRef)">登录</el-button>
        </el-form>

        <div class="demo-account"><span><el-icon><InfoFilled /></el-icon></span><div><strong>本地演示账号</strong><p>学生：600001　密码：123321</p></div><el-button text @click="fillDemoAccount">填入</el-button></div>
        <button class="back-button" type="button" @click="goHome"><el-icon><ArrowLeft /></el-icon>返回平台介绍</button>
      </div>
      <footer>Copyright © 2024-2026 智慧校园服务平台</footer>
    </section>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, CircleCheckFilled, InfoFilled, Lock, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import BrandMark from '@/components/BrandMark.vue'
import campusHero from '@/assets/images/campus-hero.jpg'
import { loginUser } from '@/api/auth.js'
import { setAccessToken } from '@/utils/authToken.js'
import { setStoredCurrentUser } from '@/utils/authSession.js'

const ACCOUNT_KEY='smart-campus.remembered-account'
const router=useRouter();const buttonLoading=ref(false);const ruleFormRef=ref(null);const rememberAccount=ref(true);const formData=ref({username:'600001',password:'123321'})
const rules={username:[{required:true,message:'请输入账号',trigger:'blur'},{max:64,message:'账号长度不能超过64位',trigger:'blur'}],password:[{required:true,message:'请输入密码',trigger:'blur'},{max:128,message:'密码长度不能超过128位',trigger:'blur'}]}
const goHome=()=>router.push('/?preview=public')
const fillDemoAccount=()=>{formData.value.username='600001';formData.value.password='123321';ElMessage.success('已填入学生演示账号')}
const submitForm=(formEl)=>{if(!formEl)return;formEl.validate(valid=>{if(valid)login()})}
const login=async()=>{buttonLoading.value=true;try{const result=await loginUser(formData.value);const token=result.data.token;const user=result.data.currentUser;if(!token||!user){ElMessage.error('登录返回数据异常，请重试');return};setAccessToken(token);setStoredCurrentUser(user);if(rememberAccount.value)localStorage.setItem(ACCOUNT_KEY,formData.value.username);else localStorage.removeItem(ACCOUNT_KEY);ElMessage.success('登录成功');const redirect=router.currentRoute.value.query.redirect;await router.push(typeof redirect==='string'?redirect:'/home')}catch(e){console.error('登录失败',e);ElMessage.error('登录失败，请检查账号密码')}finally{buttonLoading.value=false}}
onMounted(()=>{const saved=localStorage.getItem(ACCOUNT_KEY);if(saved)formData.value.username=saved})
</script>

<style scoped>
.login-page{display:grid;min-height:100vh;grid-template-columns:minmax(0,1.45fr) minmax(420px,.75fr);background:#fff}.login-visual{position:relative;display:flex;min-height:680px;align-items:center;overflow:hidden;color:#fff;background:var(--color-brand-800)}.login-visual>img{position:absolute;inset:0;width:100%;height:100%;object-fit:cover;object-position:center}.visual-overlay{position:absolute;inset:0;background:linear-gradient(90deg,rgba(1,40,62,.88),rgba(1,58,85,.48))}.login-brand{position:absolute;z-index:2;top:34px;left:48px;display:flex;align-items:center;gap:10px;color:#fff;text-decoration:none}.login-brand>span,.mobile-brand>span{display:flex;flex-direction:column;line-height:1.2}.login-brand strong,.mobile-brand strong{font-size:17px;font-weight:600}.login-brand small,.mobile-brand small{margin-top:2px;color:rgba(255,255,255,.68);font-size:11px}.visual-copy{position:relative;z-index:1;max-width:590px;margin-left:10%;padding:48px}.visual-copy>p,.form-kicker{margin:0 0 8px;color:#ffd77a;font-size:12px;font-weight:700;letter-spacing:1.5px}.visual-copy h1{margin:0;font-size:38px;font-weight:600}.visual-copy>span{display:block;margin-top:10px;color:rgba(255,255,255,.76);font-size:16px}.visual-points{display:flex;flex-direction:column;gap:10px;margin-top:28px}.visual-points>div{display:flex;align-items:center;gap:8px;color:rgba(255,255,255,.88)}.visual-points .el-icon{color:#ffd77a}.photo-credit{position:absolute;z-index:1;bottom:22px;left:48px;margin:0;color:rgba(255,255,255,.54);font-size:11px}.login-panel{display:flex;min-width:0;flex-direction:column;padding:48px 9%;background:#fff}.login-form-wrap{width:100%;max-width:420px;margin:auto}.mobile-brand{display:none}.form-kicker{color:var(--color-brand-700)}.login-form-wrap h2{margin:0;font-size:30px;font-weight:600}.form-intro{margin:6px 0 28px;color:var(--color-text-secondary)}.login-form-wrap :deep(.el-form-item__label){font-weight:500}.login-form-wrap :deep(.el-input__wrapper){min-height:48px;border-radius:4px;box-shadow:0 0 0 1px var(--color-border) inset}.login-form-wrap :deep(.el-input__wrapper.is-focus){box-shadow:0 0 0 1px var(--color-brand-600) inset}.form-options{display:flex;align-items:center;justify-content:space-between;margin:-2px 0 20px}.form-options button,.back-button{color:var(--color-brand-700);background:none;border:0;cursor:pointer}.form-options button:hover{text-decoration:underline}.login-button{width:100%;height:48px;font-size:15px;font-weight:600}.demo-account{display:grid;grid-template-columns:36px minmax(0,1fr) auto;align-items:center;gap:10px;margin-top:24px;padding:13px 14px;background:var(--color-brand-50);border:1px solid #cce5ed;border-radius:8px}.demo-account>span{display:inline-flex;width:36px;height:36px;align-items:center;justify-content:center;color:var(--color-brand-700);background:#fff;border-radius:50%}.demo-account>div{min-width:0}.demo-account strong{font-size:13px;font-weight:600}.demo-account p{margin:1px 0 0;color:var(--color-text-secondary);font-size:12px}.back-button{display:flex;align-items:center;gap:6px;margin:24px auto 0;color:var(--color-text-secondary)}.back-button:hover{color:var(--color-brand-700)}.login-panel footer{margin-top:32px;color:var(--color-text-tertiary);font-size:11px;text-align:center}
@media(max-width:991px){.login-page{grid-template-columns:minmax(0,1fr) minmax(400px,.9fr)}.visual-copy{margin-left:0}.visual-copy h1{font-size:32px}}
@media(max-width:767px){.login-page{display:block;background:#fff}.login-visual{display:none}.login-panel{min-height:100vh;padding:28px 24px}.login-form-wrap{margin:auto}.mobile-brand{display:flex;align-items:center;gap:10px;margin-bottom:48px}.mobile-brand small{color:var(--color-text-tertiary)}.login-form-wrap h2{font-size:26px}}
</style>
