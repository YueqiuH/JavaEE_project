<template>
  <div class="d-page">
    <div class="d-crumb d-rise" style="--rise: 0">
      <router-link to="/home">首页</router-link>
      <el-icon><ArrowRight /></el-icon>
      <router-link :to="{ path: '/services', query: { domain: 'base' } }">基础数据</router-link>
      <el-icon><ArrowRight /></el-icon>
      <span>院系专业管理</span>
    </div>

    <header class="d-head d-rise" style="--rise: 1">
      <div>
        <h1>院系与专业资源管理</h1>
        <p class="d-head-desc">维护各院系介绍、专业设置与培养方案等基础结构数据</p>
      </div>
    </header>

    <div class="panel-grid">
      <!-- 院系面板 -->
      <section class="d-panel d-rise" style="--rise: 2">
        <div class="d-toolbar">
          <h2>院系列表</h2>
          <el-input
            v-model="deptQuery.keyword"
            clearable
            :prefix-icon="Search"
            placeholder="名称 / 编号"
            class="toolbar-search"
            @keyup.enter="loadDepartments(1)"
            @clear="loadDepartments(1)"
          >
            <template #append>
              <el-button :icon="Search" @click="loadDepartments(1)" />
            </template>
          </el-input>
          <el-button v-if="canWrite" type="primary" :icon="Plus" @click="openDeptForm()">新增院系</el-button>
        </div>
        <el-table
          :data="deptRows"
          highlight-current-row
          :row-class-name="deptRowClass"
          @row-click="selectDept"
        >
          <el-table-column prop="deptCode" label="编号" width="80">
            <template #default="scope"><span class="d-num">{{ scope.row.deptCode }}</span></template>
          </el-table-column>
          <el-table-column prop="deptName" label="院系名称" min-width="170" show-overflow-tooltip />
          <el-table-column prop="majorCount" label="专业数" width="80" align="center" />
          <el-table-column prop="studentCount" label="在读学生" width="90" align="center" />
          <el-table-column v-if="canWrite" label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button link type="primary" @click.stop="openDeptForm(scope.row)">编辑</el-button>
              <el-button link type="danger" @click.stop="removeDept(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="d-foot">
          <span v-if="selectedDept">
            已选：{{ selectedDept.deptName }}
            <el-button link type="primary" @click="clearSelectedDept">查看全部专业</el-button>
          </span>
          <span v-else>共 {{ deptTotal }} 个院系</span>
          <el-pagination
            background
            layout="prev, pager, next"
            :total="deptTotal"
            :page-size="deptQuery.size"
            :current-page="deptQuery.page"
            @current-change="loadDepartments"
          />
        </div>
      </section>

      <!-- 专业面板 -->
      <section class="d-panel d-rise" style="--rise: 3">
        <div class="d-toolbar">
          <h2>{{ selectedDept ? `${selectedDept.deptName} · 专业` : '全部专业' }}</h2>
          <el-input
            v-model="majorQuery.keyword"
            clearable
            :prefix-icon="Search"
            placeholder="名称 / 编号"
            class="toolbar-search"
            @keyup.enter="loadMajors(1)"
            @clear="loadMajors(1)"
          >
            <template #append>
              <el-button :icon="Search" @click="loadMajors(1)" />
            </template>
          </el-input>
          <el-button v-if="canWrite" type="primary" :icon="Plus" @click="openMajorForm()">新增专业</el-button>
        </div>
        <el-table v-loading="majorLoading" :data="majorRows">
          <el-table-column prop="majorCode" label="编号" width="80">
            <template #default="scope"><span class="d-num">{{ scope.row.majorCode }}</span></template>
          </el-table-column>
          <el-table-column prop="majorName" label="专业名称" min-width="160" show-overflow-tooltip />
          <el-table-column v-if="!selectedDept" prop="deptName" label="所属院系" min-width="140" show-overflow-tooltip />
          <el-table-column prop="studentCount" label="在读学生" width="90" align="center" />
          <el-table-column label="培养方案" min-width="160">
            <template #default="scope">
              <span class="plan-text">{{ scope.row.cultivationPlan || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="canWrite" label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button link type="primary" @click="openMajorForm(scope.row)">编辑</el-button>
              <el-button link type="danger" @click="removeMajor(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="d-foot">
          <span>共 {{ majorTotal }} 个专业</span>
          <el-pagination
            background
            layout="prev, pager, next"
            :total="majorTotal"
            :page-size="majorQuery.size"
            :current-page="majorQuery.page"
            @current-change="loadMajors"
          />
        </div>
      </section>
    </div>

    <!-- 院系表单 -->
    <el-dialog v-model="deptFormVisible" :title="deptForm.deptId ? '编辑院系' : '新增院系'" width="min(520px, calc(100vw - 32px))">
      <el-form ref="deptFormRef" :model="deptForm" :rules="deptRules" label-position="top">
        <el-form-item label="院系名称" prop="deptName">
          <el-input v-model="deptForm.deptName" maxlength="64" placeholder="如：计算机与人工智能学院" />
        </el-form-item>
        <el-form-item label="院系编号" prop="deptCode">
          <el-input v-model="deptForm.deptCode" maxlength="16" placeholder="如：CS" />
        </el-form-item>
        <el-form-item label="院系简介">
          <el-input v-model="deptForm.description" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="院系介绍、教研室分布等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deptFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveDept">保存</el-button>
      </template>
    </el-dialog>

    <!-- 专业表单 -->
    <el-dialog v-model="majorFormVisible" :title="majorForm.majorId ? '编辑专业' : '新增专业'" width="min(520px, calc(100vw - 32px))">
      <el-form ref="majorFormRef" :model="majorForm" :rules="majorRules" label-position="top">
        <el-form-item label="所属院系" prop="deptId">
          <el-select v-model="majorForm.deptId" placeholder="请选择院系" style="width: 100%">
            <el-option v-for="dept in deptOptions" :key="dept.deptId" :label="dept.deptName" :value="dept.deptId" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业名称" prop="majorName">
          <el-input v-model="majorForm.majorName" maxlength="64" placeholder="如：软件工程" />
        </el-form-item>
        <el-form-item label="专业编号" prop="majorCode">
          <el-input v-model="majorForm.majorCode" maxlength="16" placeholder="如：CS01" />
        </el-form-item>
        <el-form-item label="培养方案">
          <el-input v-model="majorForm.cultivationPlan" type="textarea" :rows="4" maxlength="1000" show-word-limit placeholder="培养目标、课程体系等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="majorFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveMajor">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowRight, Plus, Search } from '@element-plus/icons-vue'
import {
  addDepartment, addMajor, delDepartment, delMajor,
  listDepartmentPage, listMajorPage, updateDepartment, updateMajor,
} from '@/api/base.js'
import { getStoredCurrentUser } from '@/utils/authSession.js'
import './base-d.css'

const canWrite = computed(() => {
  const permissions = getStoredCurrentUser()?.permissions || []
  return permissions.includes('base:write')
})

// ===== 院系 =====
const deptRows = ref([])
const deptTotal = ref(0)
const deptQuery = reactive({ page: 1, size: 10, keyword: '' })
const selectedDept = ref(null)

const loadDepartments = async (page) => {
  if (page) deptQuery.page = page
  try {
    const res = await listDepartmentPage({ ...deptQuery })
    deptRows.value = res.data.records
    deptTotal.value = Number(res.data.total)
  } catch {
    // interceptor shows error toast
  }
}

const deptRowClass = ({ row }) => (row.deptId === selectedDept.value?.deptId ? 'is-selected-dept' : '')

const selectDept = (row) => {
  selectedDept.value = row.deptId === selectedDept.value?.deptId ? null : row
  loadMajors(1)
}

const clearSelectedDept = () => {
  selectedDept.value = null
  loadMajors(1)
}

// ===== 专业 =====
const majorLoading = ref(false)
const majorRows = ref([])
const majorTotal = ref(0)
const majorQuery = reactive({ page: 1, size: 10, keyword: '' })

const loadMajors = async (page) => {
  if (page) majorQuery.page = page
  majorLoading.value = true
  try {
    const res = await listMajorPage({ ...majorQuery, deptId: selectedDept.value?.deptId })
    majorRows.value = res.data.records
    majorTotal.value = Number(res.data.total)
  } catch {
    // interceptor shows error toast
  } finally {
    majorLoading.value = false
  }
}

// ===== 院系表单 =====
const saving = ref(false)
const deptFormVisible = ref(false)
const deptFormRef = ref(null)
const deptForm = reactive({ deptId: null, deptName: '', deptCode: '', description: '' })
const deptRules = {
  deptName: [{ required: true, message: '请输入院系名称', trigger: 'blur' }],
  deptCode: [{ required: true, message: '请输入院系编号', trigger: 'blur' }],
}

const openDeptForm = (row) => {
  Object.assign(deptForm, row
    ? { deptId: row.deptId, deptName: row.deptName, deptCode: row.deptCode, description: row.description }
    : { deptId: null, deptName: '', deptCode: '', description: '' })
  deptFormVisible.value = true
}

const saveDept = async () => {
  saving.value = true
  try {
    await deptFormRef.value.validate()
    if (deptForm.deptId) {
      await updateDepartment(deptForm.deptId, deptForm)
    } else {
      await addDepartment(deptForm)
    }
    ElMessage.success('保存成功')
    deptFormVisible.value = false
    await Promise.all([loadDepartments(), loadDeptOptions()])
  } catch {
    // validation failed or API error
  } finally {
    saving.value = false
  }
}

const removeDept = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除院系「${row.deptName}」吗？`, '删除确认', { type: 'warning' })
    await delDepartment(row.deptId)
    ElMessage.success('删除成功')
    if (selectedDept.value?.deptId === row.deptId) selectedDept.value = null
    await Promise.all([loadDepartments(), loadMajors(1), loadDeptOptions()])
  } catch { /* user cancelled or API failed */ }
}

// ===== 专业表单 =====
const deptOptions = ref([])
const majorFormVisible = ref(false)
const majorFormRef = ref(null)
const majorForm = reactive({ majorId: null, deptId: null, majorName: '', majorCode: '', cultivationPlan: '' })
const majorRules = {
  deptId: [{ required: true, message: '请选择所属院系', trigger: 'change' }],
  majorName: [{ required: true, message: '请输入专业名称', trigger: 'blur' }],
  majorCode: [{ required: true, message: '请输入专业编号', trigger: 'blur' }],
}

const loadDeptOptions = async () => {
  try {
    const res = await listDepartmentPage({ page: 1, size: 200 })
    deptOptions.value = res.data.records
  } catch { /* interceptor shows error */ }
}

const openMajorForm = (row) => {
  Object.assign(majorForm, row
    ? { majorId: row.majorId, deptId: row.deptId, majorName: row.majorName, majorCode: row.majorCode, cultivationPlan: row.cultivationPlan }
    : { majorId: null, deptId: selectedDept.value?.deptId ?? null, majorName: '', majorCode: '', cultivationPlan: '' })
  majorFormVisible.value = true
}

const saveMajor = async () => {
  saving.value = true
  try {
    await majorFormRef.value.validate()
    if (majorForm.majorId) {
      await updateMajor(majorForm.majorId, majorForm)
    } else {
      await addMajor(majorForm)
    }
    ElMessage.success('保存成功')
    majorFormVisible.value = false
    await Promise.all([loadMajors(), loadDepartments()])
  } catch {
    // validation failed or API error
  } finally {
    saving.value = false
  }
}

const removeMajor = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除专业「${row.majorName}」吗？`, '删除确认', { type: 'warning' })
    await delMajor(row.majorId)
    ElMessage.success('删除成功')
    await Promise.all([loadMajors(), loadDepartments()])
  } catch { /* user cancelled or API failed */ }
}

onMounted(() => {
  loadDepartments()
  loadMajors()
  loadDeptOptions()
})
</script>

<style scoped>
.panel-grid { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1.15fr); gap: 18px; align-items: start; }
.toolbar-search { width: 180px; }
.plan-text { display: -webkit-box; overflow: hidden; -webkit-box-orient: vertical; -webkit-line-clamp: 2; color: var(--color-text-secondary); line-height: 1.6; }
:deep(.el-table .is-selected-dept) {
  background: var(--d-accent-soft) !important;
  box-shadow: inset 3px 0 0 var(--d-accent);
}
:deep(.el-table__row) { cursor: pointer; transition: background var(--d-transition); }
@media (max-width: 1199px) { .panel-grid { grid-template-columns: 1fr; } }
@media (max-width: 767px) { .toolbar-search { width: 100%; order: 3; } }
</style>
