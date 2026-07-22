<template>
  <div class="profile-page">
    <div class="profile-container">
      <div class="profile-header">
        <div class="header-avatar" :style="{ background: avatarColor }">
          <span>{{ avatarChar }}</span>
        </div>
        <div class="header-info">
          <h1>{{ displayName }}</h1>
          <p>{{ roleLabel }} · {{ subLabel }}</p>
        </div>
      </div>

      <div class="profile-tabs">
        <h2 class="section-title">{{ showInfo ? '个人信息' : '修改密码' }}</h2>
        <!-- ===== 个人信息 ===== -->
        <template v-if="showInfo">
          <!-- 学生 -->
          <template v-if="isStudent">
            <div class="info-grid">
              <div class="info-item"><label>学号</label><span>{{ studentInfo?.studentNo || '-' }}</span></div>
              <div class="info-item"><label>姓名</label><span>{{ studentInfo?.studentName || '-' }}</span></div>
              <div class="info-item"><label>性别</label><span>{{ genderLabel(studentInfo?.gender) }}</span></div>
              <div class="info-item"><label>院系</label><span>{{ studentInfo?.deptName || '-' }}</span></div>
              <div class="info-item"><label>专业</label><span>{{ studentInfo?.majorName || '-' }}</span></div>
              <div class="info-item"><label>班级</label><span>{{ studentInfo?.className || '-' }}</span></div>
              <div class="info-item"><label>电话</label><span>{{ studentInfo?.phone || '-' }}</span></div>
              <div class="info-item"><label>生源地</label><span>{{ studentInfo?.originPlace || '-' }}</span></div>
              <div class="info-item"><label>入学年份</label><span>{{ studentInfo?.enrollYear ? studentInfo.enrollYear+'级' : '-' }}</span></div>
              <div class="info-item"><label>学籍状态</label><span :class="studentInfo?.status===1?'text-green':'text-red'">{{ statusLabel(studentInfo?.status) }}</span></div>
            </div>
          </template>
          <!-- 教职工/教师/辅导员 -->
          <template v-else-if="isStaff">
            <div class="info-grid">
              <div class="info-item"><label>工号</label><span>{{ user?.username || '-' }}</span></div>
              <div class="info-item"><label>姓名</label><span>{{ user?.realName || '-' }}</span></div>
              <div class="info-item"><label>性别</label><span>{{ genderLabel(user?.gender) }}</span></div>
              <div class="info-item"><label>类别</label><span>{{ roleLabel }}</span></div>
              <div class="info-item"><label>院系</label><span>{{ user?.deptName || '-' }}</span></div>
              <div class="info-item"><label>职称</label><span>{{ user?.title || '-' }}</span></div>
              <div class="info-item"><label>职务</label><span>{{ user?.position || '-' }}</span></div>
              <div class="info-item"><label>电话</label><span>{{ user?.phone || '-' }}</span></div>
              <div class="info-item"><label>邮箱</label><span>{{ user?.email || '-' }}</span></div>
              <div class="info-item"><label>状态</label><span :class="user?.status===1?'text-green':'text-red'">{{ user?.status===1?'在职':'停用' }}</span></div>
            </div>
          </template>
          <!-- 管理员 -->
          <template v-else>
            <div class="info-grid">
              <div class="info-item"><label>账号</label><span>{{ user?.username || '-' }}</span></div>
              <div class="info-item"><label>姓名</label><span>{{ user?.realName || '-' }}</span></div>
              <div class="info-item"><label>角色</label><span>{{ roleLabel }}</span></div>
              <div class="info-item"><label>状态</label><span class="text-green">在职</span></div>
            </div>
          </template>
        </template>

        <!-- ===== 修改密码 ===== -->
        <template v-else>
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-position="top" size="large" style="max-width:420px" autocomplete="off">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" placeholder="输入当前密码" show-password autocomplete="off" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" placeholder="6-32 位新密码" show-password autocomplete="off" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="再次输入新密码" show-password autocomplete="off" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="pwdLoading" @click="savePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { changePassword, getCurrentUser } from '@/api/auth.js'
import { getStudentByNo } from '@/api/base.js'

const route = useRoute()
const showInfo = computed(() => route.query.tab !== 'password')
const user = ref(null)
const studentInfo = ref(null)

const isStudent = computed(() => user.value?.userType === 1)
const isStaff = computed(() => user.value?.userType === 2 || user.value?.userType === 3)

const roleLabel = computed(() => {
  const t = user.value?.userType
  return t === 1 ? '学生' : t === 2 ? '教师' : t === 3 ? '教职工' : t === 4 ? '管理员' : '用户'
})
const subLabel = computed(() => {
  if (studentInfo.value) return studentInfo.value.studentNo
  if (isStaff.value) return user.value?.username
  return ''
})
const displayName = computed(() => user.value?.realName || user.value?.username || '用户')
const avatarChar = computed(() => (displayName.value || '?')[0])
const avatarColor = computed(() => {
  const t = user.value?.userType
  const colors = ['#3973b7', '#2d7d46', '#c77800', '#8a4d8f', '#c2413b']
  return `linear-gradient(135deg, ${colors[t] || '#5f6b7a'}, ${colors[(t||0)+1] || '#8a919c'})`
})

function genderLabel(g) { return g === 1 ? '男' : g === 2 ? '女' : '-' }
function statusLabel(s) { return s === 1 ? '在读' : s === 2 ? '休学' : s === 3 ? '毕业' : s === 0 ? '退学' : '-' }

// Password form
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdLoading = ref(false)
const pwdFormRef = ref(null)
const validateConfirm = (_r, v) => v ? (v === pwdForm.newPassword ? Promise.resolve() : Promise.reject('两次密码不一致')) : Promise.reject('请再次输入')
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 6, max: 32, message: '新密码 6-32 位', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirm, trigger: 'blur' }],
}
async function savePassword() {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return
  pwdLoading.value = true
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''; pwdForm.newPassword = ''; pwdForm.confirmPassword = ''
  } catch { /* interceptor */ }
  finally { pwdLoading.value = false }
}

onMounted(async () => {
  try {
    const res = await getCurrentUser()
    user.value = res.data?.user

    if (isStudent.value && user.value?.username) {
      try {
        const r = await getStudentByNo(Number(user.value.username))
        studentInfo.value = r.data
      } catch { /* non-critical */ }
    }
  } catch { /* */ }
})
</script>

<style scoped>
.profile-page { min-height: calc(100vh - var(--header-height)); padding: 40px 0 60px; background: #f5f6f8; }
.profile-container { max-width: 680px; margin: 0 auto; padding: 0 20px; }
.profile-header { display: flex; align-items: center; gap: 20px; padding: 32px; margin-bottom: 24px; background: #fff; border-radius: 12px; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.header-avatar { display: flex; width: 72px; height: 72px; flex-shrink: 0; align-items: center; justify-content: center; font-size: 30px; font-weight: 600; color: #fff; border-radius: 50%; }
.header-info h1 { margin: 0; font-size: 22px; font-weight: 600; }
.header-info p { margin: 4px 0 0; color: #8a919c; font-size: 14px; }
.profile-tabs { padding: 28px 32px; background: #fff; border-radius: 12px; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.section-title { margin: 0 0 28px; font-size: 20px; font-weight: 600; }
.info-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px 32px; }
.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-item label { color: #8a919c; font-size: 13px; }
.info-item span { font-size: 15px; font-weight: 500; color: #1d2129; }
.text-green { color: #16865b; } .text-red { color: #c2413b; }
.edit-form { max-width: 500px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 0 20px; }
@media (max-width: 640px) {
  .profile-page { padding: 20px 0 40px; }
  .profile-header { padding: 20px; flex-direction: column; text-align: center; }
  .profile-tabs { padding: 20px 16px; }
  .info-grid { grid-template-columns: repeat(2, 1fr); gap: 16px; }
  .form-row { grid-template-columns: 1fr; }
}
</style>
