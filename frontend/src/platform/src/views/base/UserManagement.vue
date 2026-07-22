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
            <el-select v-model="studentQuery.status" clearable placeholder="全部学籍" class="filter-select narrow" @change="loadStudents(1)">
              <el-option v-for="(label, value) in studentStatusMap" :key="value" :label="label" :value="Number(value)" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="loadStudents(1)">查询</el-button>
            <span class="filter-spacer"></span>
            <el-button v-if="canWrite" type="primary" :icon="Plus" @click="openStudentForm()">新增学生</el-button>
            <el-button v-if="canWrite" :icon="Upload" @click="openImport('student')">导入</el-button>
            <el-button :icon="Download" :loading="exportingStudents" @click="doExportStudents">导出</el-button>
          </div>

          <el-table v-loading="studentLoading" :data="studentRows" @row-click="showStudentDetail" style="cursor:pointer">
            <el-table-column prop="studentNo" label="学号" width="120">
              <template #default="scope"><span class="d-num">{{ scope.row.studentNo }}</span></template>
            </el-table-column>
            <el-table-column prop="studentName" label="姓名" width="100" />
            <el-table-column prop="phone" label="电话" width="120" />
            <el-table-column label="性别" width="70" align="center">
              <template #default="scope">{{ genderText(scope.row.gender) }}</template>
            </el-table-column>
            <el-table-column prop="deptName" label="院系" min-width="150" show-overflow-tooltip />
            <el-table-column prop="majorName" label="专业" min-width="130" show-overflow-tooltip />
            <el-table-column prop="className" label="班级" width="110" />
            <el-table-column prop="originPlace" label="生源地" width="90" />
            <el-table-column prop="studentAddress" label="家庭住址" min-width="140" show-overflow-tooltip />
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
                <el-button link type="primary" @click.stop="openStudentForm(scope.row)">编辑</el-button>
                <el-button link type="danger" @click.stop="removeStudent(scope.row)">删除</el-button>
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
            <el-select v-model="staffQuery.status" clearable placeholder="全部状态" class="filter-select narrow" @change="loadStaffs(1)">
              <el-option v-for="(label, value) in staffStatusMap" :key="value" :label="label" :value="Number(value)" />
            </el-select>
            <el-select v-model="staffQuery.userType" clearable placeholder="人员类别" class="filter-select narrow" @change="loadStaffs(1)">
              <el-option label="教师" :value="2" />
              <el-option label="教职工" :value="3" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="loadStaffs(1)">查询</el-button>
            <span class="filter-spacer"></span>
            <el-button v-if="canWrite" type="primary" :icon="Plus" @click="openStaffForm()">新增教职工</el-button>
            <el-button v-if="canWrite" :icon="Upload" @click="openImport('staff')">导入</el-button>
            <el-button :icon="Download" :loading="exportingStaffs" @click="doExportStaffs">导出</el-button>
          </div>

          <el-table v-loading="staffLoading" :data="staffRows" @row-click="showStaffDetail" style="cursor:pointer">
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
            <el-table-column prop="address" label="家庭住址" min-width="150" show-overflow-tooltip />
            <el-table-column label="状态" width="90" align="center">
              <template #default="scope">
                <el-tag size="small" effect="plain" :type="staffStatusTag(scope.row.status)">
                  {{ staffStatusMap[scope.row.status] || '未知' }}
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

    <!-- 学生详情抽屉 -->
    <el-drawer v-model="detailVisible" title="学生档案" size="min(500px, 88vw)">
      <div v-if="detailStudent" class="detail-card">
        <div class="detail-head">
          <span class="detail-avatar">{{ detailStudent.studentName?.charAt(0) }}</span>
          <div>
            <h3>{{ detailStudent.studentName }}</h3>
            <el-tag size="small" effect="plain" :type="studentStatusTag(detailStudent.status)">{{ studentStatusMap[detailStudent.status] || '未知' }}</el-tag>
          </div>
        </div>
        <div class="detail-grid">
          <div class="detail-item"><label>学号</label><span class="d-num">{{ detailStudent.studentNo }}</span></div>
          <div class="detail-item"><label>性别</label><span>{{ genderText(detailStudent.gender) }}</span></div>
          <div class="detail-item"><label>出生日期</label><span>{{ detailStudent.studentBirth || '—' }}</span></div>
          <div class="detail-item"><label>年龄</label><span>{{ detailStudent.studentAge ?? '—' }}</span></div>
          <div class="detail-item"><label>院系</label><span>{{ detailStudent.deptName || '—' }}</span></div>
          <div class="detail-item"><label>专业</label><span>{{ detailStudent.majorName || '—' }}</span></div>
          <div class="detail-item"><label>班级</label><span>{{ detailStudent.className || '—' }}</span></div>
          <div class="detail-item"><label>入学年份</label><span>{{ detailStudent.enrollYear ? detailStudent.enrollYear + '级' : '—' }}</span></div>
          <div class="detail-item"><label>生源地</label><span>{{ detailStudent.originPlace || '—' }}</span></div>
          <div class="detail-item"><label>电话</label><span>{{ detailStudent.phone || '—' }}</span></div>
          <div class="detail-item"><label>家庭地址</label><span>{{ detailStudent.studentAddress || '—' }}</span></div>
        </div>
        <div class="detail-actions">
          <el-button v-if="canWrite" type="primary" @click="detailVisible = false; openStudentForm(detailStudent)">编辑</el-button>
          <el-button @click="detailVisible = false">关闭</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 学生表单 -->
    <el-dialog v-model="studentFormVisible" :title="studentForm.studentId ? '编辑学生档案' : '新增学生档案'" width="min(640px, calc(100vw - 32px))">
      <el-alert v-if="!studentForm.studentId" type="info" :closable="false" class="form-tip"
        title="创建后将同时开通登录账号，初始密码默认123321" />
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
        <el-form-item label="出生日期">
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
        <el-form-item label="电话">
          <el-input v-model="studentForm.phone" maxlength="20" placeholder="手机号" />
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

    <!-- 批量导入 -->
    <el-dialog v-model="importVisible" :title="importType === 'student' ? '批量导入学生' : '批量导入教职工'" width="min(520px, calc(100vw - 32px))">
      <el-alert type="info" :closable="false" style="margin-bottom:14px">
        <template #title>
          下载模板，按格式填写后上传。{{ importType === 'student' ? '学号和姓名为必填。' : '工号、姓名、类别为必填。' }}
        </template>
      </el-alert>
      <div style="margin-bottom:14px">
        <el-button link type="primary" @click="doDownloadTemplate">
          <el-icon><Download /></el-icon> 下载模板
        </el-button>
      </div>
      <el-upload ref="uploadRef" :auto-upload="false" :limit="1" accept=".xlsx" :on-change="(f) => importFile = f.raw" drag>
        <el-icon><UploadFilled /></el-icon>
        <div>拖拽或点击选择 .xlsx 文件</div>
      </el-upload>
      <div v-if="importResult" style="margin-top:14px">
        <el-alert :type="importResult.fail > 0 ? 'warning' : 'success'" :closable="false">
          <template #title>共 {{ importResult.total }} 行，成功 {{ importResult.success }}，失败 {{ importResult.fail }}</template>
          <span v-if="importResult.errors" style="font-size:12px;white-space:pre-wrap">{{ importResult.errors }}</span>
        </el-alert>
      </div>
      <template #footer>
        <el-button @click="importVisible = false">关闭</el-button>
        <el-button type="primary" :loading="importLoading" :disabled="!importFile" @click="doImport">开始导入</el-button>
      </template>
    </el-dialog>

    <!-- 教职工详情抽屉 -->
    <el-drawer v-model="staffDetailVisible" title="教职工档案" size="min(500px, 88vw)">
      <div v-if="detailStaff" class="detail-card">
        <div class="detail-head">
          <span class="detail-avatar">{{ detailStaff.realName?.charAt(0) }}</span>
          <div>
            <h3>{{ detailStaff.realName }}</h3>
            <el-tag size="small" effect="plain" :type="staffStatusTag(detailStaff.status)">{{ staffStatusMap[detailStaff.status] || '未知' }}</el-tag>
          </div>
        </div>
        <div class="detail-grid">
          <div class="detail-item"><label>工号</label><span class="d-num">{{ detailStaff.username }}</span></div>
          <div class="detail-item"><label>性别</label><span>{{ genderText(detailStaff.gender) }}</span></div>
          <div class="detail-item"><label>类别</label><span>{{ detailStaff.userType === 2 ? '教师' : '教职工' }}</span></div>
          <div class="detail-item"><label>院系</label><span>{{ detailStaff.deptName || '—' }}</span></div>
          <div class="detail-item"><label>职称</label><span>{{ detailStaff.title || '—' }}</span></div>
          <div class="detail-item"><label>职务</label><span>{{ detailStaff.position || '—' }}</span></div>
          <div class="detail-item"><label>电话</label><span>{{ detailStaff.phone || '—' }}</span></div>
          <div class="detail-item"><label>邮箱</label><span>{{ detailStaff.email || '—' }}</span></div>
          <div class="detail-item"><label>家庭住址</label><span>{{ detailStaff.address || '—' }}</span></div>
          <div class="detail-item"><label>注册时间</label><span>{{ detailStaff.createdAt || '—' }}</span></div>
        </div>
        <div class="detail-actions">
          <el-button v-if="canWrite" type="primary" @click="staffDetailVisible = false; openStaffForm(detailStaff)">编辑</el-button>
          <el-button @click="staffDetailVisible = false">关闭</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 教职工表单 -->
    <el-dialog v-model="staffFormVisible" :title="staffForm.userId ? '编辑教职工档案' : '新增教职工档案'" width="min(640px, calc(100vw - 32px))">
      <el-alert v-if="!staffForm.userId" type="info" :closable="false" class="form-tip"
        title="创建后将同时开通登录账号，初始密码默认123321" />
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
        <el-form-item label="家庭住址">
          <el-input v-model="staffForm.address" maxlength="128" placeholder="如：四川省成都市郫都区" />
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
import { ArrowRight, Download, Plus, Search, Upload, UploadFilled } from '@element-plus/icons-vue'
import {
  addStaff, addStudent, delStudent, disableStaff, downloadStaffTemplate, downloadStudentTemplate,
  exportStaffs, exportStudents, importStaffs, importStudents,
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
const exportingStudents = ref(false)
const exportingStaffs = ref(false)

const doExportStudents = async () => {
  exportingStudents.value = true
  try { await exportStudents({ deptId: studentQuery.deptId, enrollYear: studentQuery.enrollYear }) }
  catch { ElMessage.error('导出失败，请稍后重试') }
  finally { exportingStudents.value = false }
}
const doExportStaffs = async () => {
  exportingStaffs.value = true
  try { await exportStaffs() }
  catch { ElMessage.error('导出失败，请稍后重试') }
  finally { exportingStaffs.value = false }
}
const doDownloadTemplate = async () => {
  try {
    if (importType.value === 'student') await downloadStudentTemplate()
    else await downloadStaffTemplate()
  } catch { ElMessage.error('模板下载失败') }
}
const genderText = (gender) => ({ 1: '男', 2: '女' }[gender] || '—')
const yearOptions = [2022, 2023, 2024, 2025]
const studentStatusMap = { 1: '在读', 2: '休学', 3: '毕业', 0: '退学' }
const studentStatusTag = (status) => ({ 1: 'success', 2: 'warning', 3: 'info', 0: 'danger' }[status] || 'info')
const staffStatusMap = { 1: '在职', 0: '停用', 2: '退休/离职' }
const staffStatusTag = (s) => ({ 1: 'success', 0: 'danger', 2: 'warning' }[s] || 'info')

// ===== 院系/专业选项 =====
const deptOptions = ref([])
const studentMajorOptions = ref([])
const formMajorOptions = ref([])

const loadDeptOptions = async () => {
  const res = await listDepartmentPage({ page: 1, size: 200 })
  deptOptions.value = res.data.records
}

const loadMajorOptions = async (deptId) => {
  try {
    const res = await listMajorPage({ page: 1, size: 200, deptId })
    return res.data.records
  } catch {
    return []
  }
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
  } catch {
    // interceptor shows error toast
  } finally {
    studentLoading.value = false
  }
}

const onStudentDeptChange = async () => {
  studentQuery.majorId = null
  studentMajorOptions.value = studentQuery.deptId ? await loadMajorOptions(studentQuery.deptId) : []
  loadStudents(1)
}

const detailVisible = ref(false)
const detailStudent = ref(null)
const showStudentDetail = (row) => { detailStudent.value = row; detailVisible.value = true }

const studentFormVisible = ref(false)
const studentFormRef = ref(null)
const emptyStudentForm = {
  studentId: null, studentNo: '', studentName: '', gender: 1, studentBirth: '',
  studentAddress: '', phone: '', originPlace: '', className: '', enrollYear: null,
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
    phone: row.phone || '', originPlace: row.originPlace, className: row.className, enrollYear: row.enrollYear,
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
  saving.value = true
  try {
    await studentFormRef.value.validate()
    const payload = { ...studentForm, studentNo: Number(studentForm.studentNo) }
    if (studentForm.studentId) {
      await updateStudent(studentForm.studentId, payload)
    } else {
      await addStudent(payload)
    }
    ElMessage.success('保存成功')
    studentFormVisible.value = false
    loadStudents()
  } catch {
    // validation failed or API error (interceptor already shows toast)
  } finally {
    saving.value = false
  }
}

const removeStudent = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除学生「${row.studentName}」（${row.studentNo}）的档案吗？`, '删除确认', { type: 'warning' })
    await delStudent(row.studentId)
    ElMessage.success('删除成功')
    loadStudents()
  } catch { /* user cancelled or API failed */ }
}

// ===== 教职工档案 =====
const staffLoading = ref(false)
const staffRows = ref([])
const staffTotal = ref(0)
const staffQuery = reactive({ page: 1, size: 10, keyword: '', deptId: null, userType: null, status: null })

const loadStaffs = async (page) => {
  if (page) staffQuery.page = page
  staffLoading.value = true
  try {
    const res = await listStaffPage({ ...staffQuery })
    staffRows.value = res.data.records
    staffTotal.value = Number(res.data.total)
  } catch {
    // interceptor shows error toast
  } finally {
    staffLoading.value = false
  }
}

const staffFormVisible = ref(false)
const staffFormRef = ref(null)
const staffDetailVisible = ref(false)
const detailStaff = ref(null)
const showStaffDetail = (row) => { detailStaff.value = row; staffDetailVisible.value = true }
const emptyStaffForm = {
  userId: null, username: '', realName: '', userType: 2, gender: 1,
  phone: '', email: '', address: '', title: '', position: '', deptId: null, status: 1,
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
    gender: row.gender, phone: row.phone, email: row.email, address: row.address, title: row.title,
    position: row.position, deptId: row.deptId, status: row.status,
  } : { ...emptyStaffForm })
  staffFormVisible.value = true
}

const saveStaff = async () => {
  saving.value = true
  try {
    await staffFormRef.value.validate()
    if (staffForm.userId) {
      await updateStaff(staffForm.userId, staffForm)
    } else {
      await addStaff(staffForm)
    }
    ElMessage.success('保存成功')
    staffFormVisible.value = false
    loadStaffs()
  } catch {
    // validation failed or API error
  } finally {
    saving.value = false
  }
}

const disableStaffRow = async (row) => {
  try {
    await ElMessageBox.confirm(`确定停用「${row.realName}」（${row.username}）的账号吗？停用后无法登录。`, '停用确认', { type: 'warning' })
    await disableStaff(row.userId)
    ElMessage.success('已停用')
    loadStaffs()
  } catch { /* user cancelled or API failed */ }
}

// ===== 批量导入 =====
const importVisible = ref(false)
const importType = ref('student')
const importFile = ref(null)
const importLoading = ref(false)
const importResult = ref(null)
const uploadRef = ref(null)

const openImport = (type) => {
  importType.value = type
  importFile.value = null
  importResult.value = null
  uploadRef.value?.clearFiles()
  importVisible.value = true
}

const doImport = async () => {
  if (!importFile.value) return
  importLoading.value = true
  importResult.value = null
  try {
    const fd = new FormData()
    fd.append('file', importFile.value)
    const res = importType.value === 'student' ? await importStudents(fd) : await importStaffs(fd)
    importResult.value = res.data || { total: 0, success: 0, fail: 0, errors: '' }
    if (importResult.value.success > 0) {
      ElMessage.success(`成功导入 ${importResult.value.success} 条`)
      importType.value === 'student' ? loadStudents() : loadStaffs()
    }
  } catch {
    // interceptor shows error
  } finally {
    importLoading.value = false
  }
}

onMounted(() => {
  loadDeptOptions().catch(() => {})
  loadStudents()
  loadStaffs()
})
</script>

<style scoped>
.tab-panel { padding: 0 16px 8px; }
.tab-panel :deep(.el-tabs__header) { margin-bottom: 0; }
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  padding: 6px 0 18px;
  border-bottom: 1px solid var(--color-border-light);
  margin-bottom: 4px;
}
.filter-keyword { width: 210px; }
.filter-select { width: 170px; }
.filter-select.narrow { width: 120px; }
.filter-spacer { flex: 1; }
.pagination-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 0;
  color: var(--color-text-tertiary);
  font-size: 12.5px;
}
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); column-gap: 18px; row-gap: 4px; }
.form-span-2 { grid-column: span 2; }
.form-tip { margin-bottom: 16px; }
.detail-head {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 22px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--color-border-light);
}
.detail-avatar {
  display: inline-flex;
  width: 52px; height: 52px;
  align-items: center; justify-content: center;
  font-size: 22px; font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, var(--d-accent), #a78bfa);
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(124, 58, 237, 0.25);
}
.detail-head h3 { margin: 0 0 4px; font-size: 18px; font-weight: 650; }
.detail-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px 22px; }
.detail-item label {
  display: block;
  margin-bottom: 2px;
  color: var(--color-text-tertiary);
  font-size: 11.5px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.detail-item span { font-size: 14px; font-weight: 500; }
.detail-actions {
  display: flex;
  gap: 10px;
  margin-top: 22px;
  padding-top: 18px;
  border-top: 1px solid var(--color-border-light);
}
@media (max-width: 767px) {
  .filter-keyword, .filter-select, .filter-select.narrow { width: 100%; }
  .form-grid { grid-template-columns: 1fr; }
  .form-span-2 { grid-column: span 1; }
}
</style>
