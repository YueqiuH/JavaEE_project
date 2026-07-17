<template>
  <div class="d-page">
    <div class="d-crumb d-rise" style="--rise: 0">
      <router-link to="/home">首页</router-link>
      <el-icon><ArrowRight /></el-icon>
      <router-link :to="{ path: '/services', query: { domain: 'base' } }">基础数据</router-link>
      <el-icon><ArrowRight /></el-icon>
      <span>师生信息库</span>
    </div>

    <header class="d-head d-rise" style="--rise: 1">
      <div>
        <span class="d-head-module">基础数据 · D1</span>
        <h1>教职工与学生信息库</h1>
        <p class="d-head-desc">维护全校师生基础数字档案，支持多条件组合检索</p>
      </div>
    </header>

    <section class="d-panel tab-panel d-rise" style="--rise: 2">
      <el-tabs v-model="activeTab">
        <!-- ===== 学生档案 ===== -->
        <el-tab-pane label="学生档案" name="student">
          <div class="filter-bar">
            <el-input
              v-model="studentQuery.keyword" clearable :prefix-icon="Search" placeholder="姓名 / 学号"
              class="filter-keyword" @keyup.enter="loadStudents(1)" @clear="loadStudents(1)"
            />
            <el-select v-model="studentQuery.deptId" clearable placeholder="全部院系" class="filter-select" @change="onStudentDeptChange">
              <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
            </el-select>
            <el-select v-model="studentQuery.majorId" clearable placeholder="全部专业" class="filter-select" @change="loadStudents(1)">
              <el-option v-for="m in studentMajorOptions" :key="m.majorId" :label="m.majorName" :value="m.majorId" />
            </el-select>
            <el-select v-model="studentQuery.enrollYear" clearable placeholder="入学年份" class="filter-select narrow" @change="loadStudents(1)">
              <el-option v-for="y in yearOptions" :key="y" :label="`${y}级`" :value="y" />
            </el-select>
            <el-select v-model="studentQuery.status" clearable placeholder="学籍状态" class="filter-select narrow" @change="loadStudents(1)">
              <el-option v-for="(label, value) in studentStatusMap" :key="value" :label="label" :value="Number(value)" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="loadStudents(1)">查询</el-button>
            <span class="filter-spacer"></span>
            <el-button v-if="canWrite" type="primary" :icon="Plus" @click="openStudentForm()">新增学生</el-button>
          </div>

          <el-table v-loading="studentLoading" :data="studentRows">
            <el-table-column prop="studentNo" label="学号" width="120">
              <template #default="scope"><span class="d-num">{{ scope.row.studentNo }}</span></template>
            </el-table-column>
            <el-table-column prop="studentName" label="姓名" width="100" />
            <el-table-column label="性别" width="70" align="center">
              <template #default="scope">{{ genderText(scope.row.gender) }}</template>
            </el-table-column>
            <el-table-column prop="deptName" label="院系" min-width="150" show-overflow-tooltip />
            <el-table-column prop="majorName" label="专业" min-width="130" show-overflow-tooltip />
            <el-table-column prop="className" label="班级" width="110" />
            <el-table-column prop="originPlace" label="生源地" width="90" />
            <el-table-column label="入学年份" width="90" align="center">
              <template #default="scope">{{ scope.row.enrollYear ? `${scope.row.enrollYear}级` : '—' }}</template>
            </el-table-column>
            <el-table-column label="学籍状态" width="90" align="center">
              <template #default="scope">
                <el-tag size="small" effect="plain" :type="studentStatusTag(scope.row.status)">
                  {{ studentStatusMap[scope.row.status] || '未知' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column v-if="canWrite" label="操作" width="120" fixed="right">
              <template #default="scope">
                <el-button link type="primary" @click="openStudentForm(scope.row)">编辑</el-button>
                <el-button link type="danger" @click="removeStudent(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-row">
            <span>共 {{ studentTotal }} 名学生</span>
            <el-pagination
              background layout="prev, pager, next" :total="studentTotal"
              :page-size="studentQuery.size" :current-page="studentQuery.page" @current-change="loadStudents"
            />
          </div>
        </el-tab-pane>

        <!-- ===== 教职工档案 ===== -->
        <el-tab-pane label="教职工档案" name="staff">
          <div class="filter-bar">
            <el-input
              v-model="staffQuery.keyword" clearable :prefix-icon="Search" placeholder="工号 / 姓名 / 电话"
              class="filter-keyword" @keyup.enter="loadStaffs(1)" @clear="loadStaffs(1)"
            />
            <el-select v-model="staffQuery.deptId" clearable placeholder="全部院系" class="filter-select" @change="loadStaffs(1)">
              <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
            </el-select>
            <el-select v-model="staffQuery.userType" clearable placeholder="人员类别" class="filter-select narrow" @change="loadStaffs(1)">
              <el-option label="教师" :value="2" />
              <el-option label="教职工" :value="3" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="loadStaffs(1)">查询</el-button>
            <span class="filter-spacer"></span>
            <el-button v-if="canWrite" type="primary" :icon="Plus" @click="openStaffForm()">新增教职工</el-button>
          </div>

          <el-table v-loading="staffLoading" :data="staffRows">
            <el-table-column prop="username" label="工号" width="110">
              <template #default="scope"><span class="d-num">{{ scope.row.username }}</span></template>
            </el-table-column>
            <el-table-column prop="realName" label="姓名" width="100" />
            <el-table-column label="性别" width="70" align="center">
              <template #default="scope">{{ genderText(scope.row.gender) }}</template>
            </el-table-column>
            <el-table-column label="类别" width="90" align="center">
              <template #default="scope">
                <el-tag size="small" effect="plain">{{ scope.row.userType === 2 ? '教师' : '教职工' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="deptName" label="院系" min-width="150" show-overflow-tooltip />
            <el-table-column prop="title" label="职称" width="90" />
            <el-table-column prop="position" label="职务" width="100" />
            <el-table-column prop="phone" label="电话" width="120" />
            <el-table-column prop="email" label="邮箱" min-width="150" show-overflow-tooltip />
            <el-table-column label="状态" width="80" align="center">
              <template #default="scope">
                <el-tag size="small" effect="plain" :type="scope.row.status === 1 ? 'success' : 'danger'">
                  {{ scope.row.status === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column v-if="canWrite" label="操作" width="120" fixed="right">
              <template #default="scope">
                <el-button link type="primary" @click="openStaffForm(scope.row)">编辑</el-button>
                <el-button v-if="scope.row.status === 1" link type="danger" @click="disableStaffRow(scope.row)">停用</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-row">
            <span>共 {{ staffTotal }} 名教职工</span>
            <el-pagination
              background layout="prev, pager, next" :total="staffTotal"
              :page-size="staffQuery.size" :current-page="staffQuery.page" @current-change="loadStaffs"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>

    <!-- 学生表单 -->
    <el-dialog v-model="studentFormVisible" :title="studentForm.studentId ? '编辑学生档案' : '新增学生档案'" width="min(640px, calc(100vw - 32px))">
      <el-form ref="studentFormRef" :model="studentForm" :rules="studentRules" label-position="top" class="form-grid">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="studentForm.studentNo" placeholder="如：2026100001" />
        </el-form-item>
        <el-form-item label="姓名" prop="studentName">
          <el-input v-model="studentForm.studentName" maxlength="32" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="studentForm.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker v-model="studentForm.studentBirth" type="date" value-format="YYYY-MM-DD" style="width: 100%" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="所属院系" prop="deptId">
          <el-select v-model="studentForm.deptId" clearable placeholder="请选择" style="width: 100%" @change="onFormDeptChange">
            <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属专业" prop="majorId">
          <el-select v-model="studentForm.majorId" clearable placeholder="请先选择院系" style="width: 100%">
            <el-option v-for="m in formMajorOptions" :key="m.majorId" :label="m.majorName" :value="m.majorId" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="studentForm.className" maxlength="32" placeholder="如：软件2601" />
        </el-form-item>
        <el-form-item label="入学年份">
          <el-select v-model="studentForm.enrollYear" clearable placeholder="请选择" style="width: 100%">
            <el-option v-for="y in yearOptions" :key="y" :label="`${y}级`" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item label="生源地(省份)">
          <el-input v-model="studentForm.originPlace" maxlength="32" placeholder="如：山东" />
        </el-form-item>
        <el-form-item label="学籍状态">
          <el-select v-model="studentForm.status" style="width: 100%">
            <el-option v-for="(label, value) in studentStatusMap" :key="value" :label="label" :value="Number(value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="家庭住址" class="form-span-2">
          <el-input v-model="studentForm.studentAddress" maxlength="128" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="studentFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveStudent">保存</el-button>
      </template>
    </el-dialog>

    <!-- 教职工表单 -->
    <el-dialog v-model="staffFormVisible" :title="staffForm.userId ? '编辑教职工档案' : '新增教职工档案'" width="min(640px, calc(100vw - 32px))">
      <el-alert v-if="!staffForm.userId" type="info" :closable="false" class="form-tip"
        title="创建后将同时开通登录账号，默认密码 123321" />
      <el-form ref="staffFormRef" :model="staffForm" :rules="staffRules" label-position="top" class="form-grid">
        <el-form-item label="工号（登录账号）" prop="username">
          <el-input v-model="staffForm.username" maxlength="64" :disabled="!!staffForm.userId" placeholder="如：700010" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="staffForm.realName" maxlength="32" />
        </el-form-item>
        <el-form-item label="人员类别" prop="userType">
          <el-radio-group v-model="staffForm.userType">
            <el-radio :value="2">教师</el-radio>
            <el-radio :value="3">教职工</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="staffForm.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="所属院系">
          <el-select v-model="staffForm.deptId" clearable placeholder="请选择" style="width: 100%">
            <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
          </el-select>
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="staffForm.title" maxlength="32" placeholder="如：副教授" />
        </el-form-item>
        <el-form-item label="职务">
          <el-input v-model="staffForm.position" maxlength="32" placeholder="如：系主任" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="staffForm.phone" maxlength="20" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="staffForm.email" maxlength="64" />
        </el-form-item>
        <el-form-item v-if="staffForm.userId" label="账号状态">
          <el-radio-group v-model="staffForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="staffFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveStaff">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowRight, Plus, Search } from '@element-plus/icons-vue'
import {
  addStaff, addStudent, delStudent, disableStaff,
  listDepartmentPage, listMajorPage, listStaffPage, listStudentByConditionPage,
  updateStaff, updateStudent,
} from '@/api/base.js'
import { getStoredCurrentUser } from '@/utils/authSession.js'
import './base-d.css'

const canWrite = computed(() => {
  const permissions = getStoredCurrentUser()?.permissions || []
  return permissions.includes('base:write')
})

const activeTab = ref('student')
const saving = ref(false)
const genderText = (gender) => ({ 1: '男', 2: '女' }[gender] || '—')
const yearOptions = [2023, 2024, 2025, 2026]
const studentStatusMap = { 1: '在读', 2: '休学', 3: '毕业', 0: '退学' }
const studentStatusTag = (status) => ({ 1: 'success', 2: 'warning', 3: 'info', 0: 'danger' }[status] || 'info')

// ===== 院系/专业选项 =====
const deptOptions = ref([])
const studentMajorOptions = ref([])
const formMajorOptions = ref([])

const loadDeptOptions = async () => {
  const res = await listDepartmentPage({ page: 1, size: 200 })
  deptOptions.value = res.data.records
}

const loadMajorOptions = async (deptId) => {
  const res = await listMajorPage({ page: 1, size: 200, deptId })
  return res.data.records
}

// ===== 学生档案 =====
const studentLoading = ref(false)
const studentRows = ref([])
const studentTotal = ref(0)
const studentQuery = reactive({ page: 1, size: 10, keyword: '', deptId: null, majorId: null, enrollYear: null, status: null })

const loadStudents = async (page) => {
  if (page) studentQuery.page = page
  studentLoading.value = true
  try {
    const res = await listStudentByConditionPage({ ...studentQuery })
    studentRows.value = res.data.records
    studentTotal.value = Number(res.data.total)
  } finally {
    studentLoading.value = false
  }
}

const onStudentDeptChange = async () => {
  studentQuery.majorId = null
  studentMajorOptions.value = studentQuery.deptId ? await loadMajorOptions(studentQuery.deptId) : []
  loadStudents(1)
}

const studentFormVisible = ref(false)
const studentFormRef = ref(null)
const emptyStudentForm = {
  studentId: null, studentNo: '', studentName: '', gender: 1, studentBirth: '',
  studentAddress: '', originPlace: '', className: '', enrollYear: null,
  deptId: null, majorId: null, status: 1,
}
const studentForm = reactive({ ...emptyStudentForm })
const studentRules = {
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { pattern: /^\d+$/, message: '学号只能是数字', trigger: 'blur' },
  ],
  studentName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
}

const openStudentForm = async (row) => {
  Object.assign(studentForm, row ? {
    studentId: row.studentId, studentNo: String(row.studentNo ?? ''), studentName: row.studentName,
    gender: row.gender, studentBirth: row.studentBirth, studentAddress: row.studentAddress,
    originPlace: row.originPlace, className: row.className, enrollYear: row.enrollYear,
    deptId: row.deptId, majorId: row.majorId, status: row.status,
  } : { ...emptyStudentForm })
  formMajorOptions.value = studentForm.deptId ? await loadMajorOptions(studentForm.deptId) : []
  studentFormVisible.value = true
}

const onFormDeptChange = async () => {
  studentForm.majorId = null
  formMajorOptions.value = studentForm.deptId ? await loadMajorOptions(studentForm.deptId) : []
}

const saveStudent = async () => {
  await studentFormRef.value.validate()
  saving.value = true
  try {
    const payload = { ...studentForm, studentNo: Number(studentForm.studentNo) }
    if (studentForm.studentId) {
      await updateStudent(studentForm.studentId, payload)
    } else {
      await addStudent(payload)
    }
    ElMessage.success('保存成功')
    studentFormVisible.value = false
    loadStudents()
  } finally {
    saving.value = false
  }
}

const removeStudent = async (row) => {
  await ElMessageBox.confirm(`确定删除学生「${row.studentName}」（${row.studentNo}）的档案吗？`, '删除确认', { type: 'warning' })
  await delStudent(row.studentId)
  ElMessage.success('删除成功')
  loadStudents()
}

// ===== 教职工档案 =====
const staffLoading = ref(false)
const staffRows = ref([])
const staffTotal = ref(0)
const staffQuery = reactive({ page: 1, size: 10, keyword: '', deptId: null, userType: null })

const loadStaffs = async (page) => {
  if (page) staffQuery.page = page
  staffLoading.value = true
  try {
    const res = await listStaffPage({ ...staffQuery })
    staffRows.value = res.data.records
    staffTotal.value = Number(res.data.total)
  } finally {
    staffLoading.value = false
  }
}

const staffFormVisible = ref(false)
const staffFormRef = ref(null)
const emptyStaffForm = {
  userId: null, username: '', realName: '', userType: 2, gender: 1,
  phone: '', email: '', title: '', position: '', deptId: null, status: 1,
}
const staffForm = reactive({ ...emptyStaffForm })
const staffRules = {
  username: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  userType: [{ required: true, message: '请选择人员类别', trigger: 'change' }],
}

const openStaffForm = (row) => {
  Object.assign(staffForm, row ? {
    userId: row.userId, username: row.username, realName: row.realName, userType: row.userType,
    gender: row.gender, phone: row.phone, email: row.email, title: row.title,
    position: row.position, deptId: row.deptId, status: row.status,
  } : { ...emptyStaffForm })
  staffFormVisible.value = true
}

const saveStaff = async () => {
  await staffFormRef.value.validate()
  saving.value = true
  try {
    if (staffForm.userId) {
      await updateStaff(staffForm.userId, staffForm)
    } else {
      await addStaff(staffForm)
    }
    ElMessage.success('保存成功')
    staffFormVisible.value = false
    loadStaffs()
  } finally {
    saving.value = false
  }
}

const disableStaffRow = async (row) => {
  await ElMessageBox.confirm(`确定停用「${row.realName}」（${row.username}）的账号吗？停用后无法登录。`, '停用确认', { type: 'warning' })
  await disableStaff(row.userId)
  ElMessage.success('已停用')
  loadStaffs()
}

onMounted(() => {
  loadDeptOptions()
  loadStudents()
  loadStaffs()
})
</script>

<style scoped>
.tab-panel { padding: 0 16px 8px; }
.filter-bar { display: flex; flex-wrap: wrap; align-items: center; gap: 10px; padding: 4px 0 14px; border-bottom: 1px solid var(--color-border-light); margin-bottom: 4px; }
.filter-keyword { width: 200px; }
.filter-select { width: 170px; }
.filter-select.narrow { width: 120px; }
.filter-spacer { flex: 1; }
.pagination-row { display: flex; align-items: center; justify-content: space-between; padding: 12px 0; color: var(--color-text-tertiary); font-size: 12px; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); column-gap: 16px; }
.form-span-2 { grid-column: span 2; }
.form-tip { margin-bottom: 14px; }
@media (max-width: 767px) {
  .filter-keyword, .filter-select, .filter-select.narrow { width: 100%; }
  .form-grid { grid-template-columns: 1fr; }
  .form-span-2 { grid-column: span 1; }
}
</style>
