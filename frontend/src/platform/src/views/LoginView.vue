<template>
  <main class="login-page">
    <section class="login-visual" aria-labelledby="login-brand-title">
      <img :src="campusHero" alt="秋日校园建筑与树木" />
      <div class="visual-overlay"></div>
      <router-link class="login-brand" to="/?preview=public"><BrandMark /><span><strong>智慧校园</strong><small>服务平台</small></span></router-link>
      <div class="visual-copy"><p>SMART CAMPUS</p><h1 id="login-brand-title">让校园服务更清晰、更高效</h1><span>统一连接教学、学生事务、协同办公与数据服务。</span><div class="visual-points"><div><el-icon><CircleCheckFilled /></el-icon>按角色匹配专属服务</div><div><el-icon><CircleCheckFilled /></el-icon>面向角色的个人工作台</div><div><el-icon><CircleCheckFilled /></el-icon>覆盖办理与查询全流程</div></div></div>
      </section>

    <section class="login-panel" aria-labelledby="login-title">
      <div class="login-form-wrap">
        <div class="mobile-brand"><BrandMark /><span><strong>智慧校园</strong><small>服务平台</small></span></div>
        <p class="form-kicker">统一身份入口</p>
        <h2 id="login-title">登录平台</h2>
        <p class="form-intro">使用校园账号访问与你相关的服务与待办。</p>

        <el-form ref="ruleFormRef" :model="formData" :rules="rules" label-position="top" size="large" @submit.prevent="submitForm">
          <el-form-item label="校园账号" prop="username"><el-input v-model="formData.username" placeholder="学号、工号或管理员账号" :prefix-icon="User" autocomplete="username" /></el-form-item>
          <el-form-item label="登录密码" prop="password"><el-input v-model="formData.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password autocomplete="current-password" @keydown.enter="submitForm" /></el-form-item>
          <div class="form-options"><el-checkbox v-model="rememberAccount">记住账号</el-checkbox><button type="button" @click="showReset=true">忘记密码</button></div>
          <el-button class="login-button" native-type="submit" type="primary" size="large" :loading="buttonLoading">登录</el-button>
        </el-form>

        <button class="back-button" type="button" @click="goHome"><el-icon><ArrowLeft /></el-icon>返回平台介绍</button>
      </div>

      <el-dialog v-model="showReset" title="忘记密码" width="min(420px, calc(100vw - 32px))" :close-on-click-modal="false" @closed="resetStep=1;sentCode='';resetForm={username:'',phone:'',code:'',newPassword:'',confirmPassword:''}" class="reset-dialog">
        <template v-if="resetStep===1">
          <p class="reset-desc">输入账号和绑定的手机号，获取验证码</p>
          <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-position="top" size="large" @submit.prevent="sendCode">
            <el-form-item label="账号" prop="username">
              <el-input v-model="resetForm.username" placeholder="学号、工号或管理员账号" autocomplete="off" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="resetForm.phone" placeholder="绑定的手机号" maxlength="11" autocomplete="off" />
            </el-form-item>
          </el-form>
        </template>
        <template v-else>
          <div class="code-tip" v-if="sentCode">
            <span class="code-label">验证码：</span>
            <span class="code-value">{{ sentCode }}</span>
          </div>
          <el-form ref="resetStep2Ref" :model="resetForm" :rules="resetStep2Rules" label-position="top" size="large" @submit.prevent="doReset" autocomplete="off">
            <!-- dummy fields to prevent browser password save prompt -->
            <input type="text" style="display:none" autocomplete="off" />
            <input type="password" style="display:none" autocomplete="off" />
            <el-form-item label="验证码" prop="code">
              <el-input v-model="resetForm.code" placeholder="输入上方 6 位验证码" maxlength="6" autocomplete="off" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="resetForm.newPassword" type="password" placeholder="6-32 位新密码" show-password autocomplete="new-password" name="reset-new-password" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="resetForm.confirmPassword" type="password" placeholder="再次输入新密码" show-password autocomplete="new-password" />
            </el-form-item>
          </el-form>
        </template>
        <template #footer>
          <div class="reset-footer">
            <template v-if="resetStep===1">
              <el-button @click="showReset=false" size="large">取消</el-button>
              <el-button type="primary" size="large" :loading="resetLoading" @click="sendCode">发送验证码</el-button>
            </template>
            <template v-else>
              <el-button @click="resetStep=1;sentCode='';resetForm.code='';resetForm.newPassword='';resetForm.confirmPassword=''" size="large">上一步</el-button>
              <el-button type="primary" size="large" :loading="resetLoading" @click="doReset">重置密码</el-button>
            </template>
          </div>
        </template>
      </el-dialog>
      <footer>Copyright © 2024-2026 智慧校园服务平台</footer>
    </section>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, CircleCheckFilled, Lock, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import BrandMark from '@/components/BrandMark.vue'
import campusHero from '@/assets/images/campus-hero.jpg'
import { loginUser, resetPassword, sendResetCode } from '@/api/auth.js'
import { setAccessToken } from '@/utils/authToken.js'
import { setStoredCurrentUser } from '@/utils/authSession.js'

const ACCOUNT_KEY='smart-campus.remembered-account'
const PASSWORD_KEY='smart-campus.remembered-password'
const router=useRouter();const buttonLoading=ref(false);const ruleFormRef=ref(null);const rememberAccount=ref(true);const formData=ref({username:'',password:''})

// Reset password
const showReset=ref(false);const resetLoading=ref(false);const resetStep=ref(1);const sentCode=ref('')
const resetFormRef=ref(null);const resetStep2Ref=ref(null)
const resetForm=ref({username:'',phone:'',code:'',newPassword:'',confirmPassword:''})
const resetRules={username:[{required:true,message:'请输入账号',trigger:'blur'}],phone:[{required:true,message:'请输入手机号',trigger:'blur'},{pattern:/^1\d{10}$/,message:'手机号格式不正确',trigger:'blur'}]}
const resetStep2Rules={code:[{required:true,message:'请输入验证码',trigger:'blur'}],newPassword:[{required:true,min:6,max:32,message:'新密码6-32位',trigger:'blur'}],confirmPassword:[{required:true,message:'请再次输入密码',trigger:'blur'},{validator:(_r,v,cb)=>{if(v!==resetForm.value.newPassword)cb('两次密码不一致');else cb()},trigger:'blur'}]}
const sendCode=async()=>{const v=await resetFormRef.value?.validate().catch(()=>false);if(!v)return;resetLoading.value=true;try{const r=await sendResetCode({username:resetForm.value.username,phone:resetForm.value.phone});sentCode.value=r.data?.code||'';ElMessage.success('验证码已发送（演示模式直接显示）');resetStep.value=2}catch{}finally{resetLoading.value=false}}
const doReset=async()=>{const v=await resetStep2Ref.value?.validate().catch(()=>false);if(!v)return;resetLoading.value=true;try{await resetPassword(resetForm.value);ElMessage.success('密码重置成功，请登录');showReset.value=false;resetForm.value={username:'',phone:'',code:'',newPassword:''};resetStep.value=1}catch{}finally{resetLoading.value=false}}
const rules={username:[{required:true,message:'请输入账号',trigger:'blur'},{max:64,message:'账号长度不能超过64位',trigger:'blur'}],password:[{required:true,message:'请输入密码',trigger:'blur'},{max:128,message:'密码长度不能超过128位',trigger:'blur'}]}
const goHome=()=>router.push('/?preview=public')
const submitForm=()=>{ruleFormRef.value.validate(valid=>{if(valid)login()})}
const login=async()=>{buttonLoading.value=true;try{const result=await loginUser(formData.value);const token=result.data.token;const user=result.data.currentUser;if(!token||!user){ElMessage.error('登录返回数据异常，请重试');return};setAccessToken(token);setStoredCurrentUser(user);if(rememberAccount.value){localStorage.setItem(ACCOUNT_KEY,formData.value.username);localStorage.setItem(PASSWORD_KEY,formData.value.password)}else{localStorage.removeItem(ACCOUNT_KEY);localStorage.removeItem(PASSWORD_KEY)}ElMessage.success('登录成功');const redirect=router.currentRoute.value.query.redirect;await router.push(typeof redirect==='string'?redirect:'/home')}catch(e){console.error('登录失败',e)}finally{buttonLoading.value=false}}
onMounted(()=>{const saved=localStorage.getItem(ACCOUNT_KEY);const savedPwd=localStorage.getItem(PASSWORD_KEY);if(saved){formData.value.username=saved;if(savedPwd)formData.value.password=savedPwd}})
</script>

<style scoped>
.login-page{display:grid;min-height:100vh;grid-template-columns:minmax(0,1.45fr) minmax(420px,.75fr);background:#fff}.login-visual{position:relative;display:flex;min-height:680px;align-items:center;overflow:hidden;color:#fff;background:var(--color-brand-800)}.login-visual>img{position:absolute;inset:0;width:100%;height:100%;object-fit:cover;object-position:center}.visual-overlay{position:absolute;inset:0;background:linear-gradient(90deg,rgba(1,40,62,.88),rgba(1,58,85,.48))}.login-brand{position:absolute;z-index:2;top:34px;left:48px;display:flex;align-items:center;gap:10px;color:#fff;text-decoration:none}.login-brand>span,.mobile-brand>span{display:flex;flex-direction:column;line-height:1.2}.login-brand strong,.mobile-brand strong{font-size:17px;font-weight:600}.login-brand small,.mobile-brand small{margin-top:2px;color:rgba(255,255,255,.68);font-size:11px}.visual-copy{position:relative;z-index:1;max-width:590px;margin-left:10%;padding:48px}.visual-copy>p,.form-kicker{margin:0 0 8px;color:#ffd77a;font-size:12px;font-weight:700;letter-spacing:1.5px}.visual-copy h1{margin:0;font-size:38px;font-weight:600}.visual-copy>span{display:block;margin-top:10px;color:rgba(255,255,255,.76);font-size:16px}.visual-points{display:flex;flex-direction:column;gap:10px;margin-top:28px}.visual-points>div{display:flex;align-items:center;gap:8px;color:rgba(255,255,255,.88)}.visual-points .el-icon{color:#ffd77a}.login-panel{display:flex;min-width:0;flex-direction:column;padding:48px 9%;background:#fff}.login-form-wrap{width:100%;max-width:420px;margin:auto}.mobile-brand{display:none}.form-kicker{color:var(--color-brand-700)}.login-form-wrap h2{margin:0;font-size:30px;font-weight:600}.form-intro{margin:6px 0 28px;color:var(--color-text-secondary)}.login-form-wrap :deep(.el-form-item__label){font-weight:500}.login-form-wrap :deep(.el-input__wrapper){min-height:48px;border-radius:4px;box-shadow:0 0 0 1px var(--color-border) inset}.login-form-wrap :deep(.el-input__wrapper.is-focus){box-shadow:0 0 0 1px var(--color-brand-600) inset}.form-options{display:flex;align-items:center;justify-content:space-between;margin:-2px 0 20px}.form-options button,.back-button{color:var(--color-brand-700);background:none;border:0;cursor:pointer}.form-options button:hover{text-decoration:underline}.login-button{width:100%;height:48px;font-size:15px;font-weight:600}.demo-account{display:grid;grid-template-columns:36px minmax(0,1fr) auto;align-items:center;gap:10px;margin-top:24px;padding:13px 14px;background:var(--color-brand-50);border:1px solid #cce5ed;border-radius:8px}.demo-account>span{display:inline-flex;width:36px;height:36px;align-items:center;justify-content:center;color:var(--color-brand-700);background:#fff;border-radius:50%}.demo-account>div{min-width:0}.demo-account strong{font-size:13px;font-weight:600}.demo-account p{margin:1px 0 0;color:var(--color-text-secondary);font-size:12px}.back-button{display:flex;align-items:center;gap:6px;margin:24px auto 0;color:var(--color-text-secondary)}.back-button:hover{color:var(--color-brand-700)}.login-panel footer{margin-top:32px;color:var(--color-text-tertiary);font-size:11px;text-align:center}
.reset-dialog .reset-desc{color:#5f6b7a;margin:0 0 20px;font-size:14px}
.reset-dialog .code-tip{display:flex;align-items:center;justify-content:center;gap:6px;padding:14px 18px;margin-bottom:20px;background:#f0faf4;border:1px solid #c6e9d4;border-radius:8px}
.reset-dialog .code-tip .code-label{color:#2d7d46;font-size:13px;font-weight:500}
.reset-dialog .code-tip .code-value{color:#2d7d46;font-size:24px;font-weight:700;letter-spacing:4px}
.reset-dialog .reset-footer{display:flex;justify-content:flex-end;gap:10px}
@media(max-width:991px){.login-page{grid-template-columns:minmax(0,1fr) minmax(400px,.9fr)}.visual-copy{margin-left:0}.visual-copy h1{font-size:32px}}
@media(max-width:767px){.login-page{display:block;background:#fff}.login-visual{display:none}.login-panel{min-height:100vh;padding:28px 24px}.login-form-wrap{margin:auto}.mobile-brand{display:flex;align-items:center;gap:10px;margin-bottom:48px}.mobile-brand small{color:var(--color-text-tertiary)}.login-form-wrap h2{font-size:26px}}
</style>
