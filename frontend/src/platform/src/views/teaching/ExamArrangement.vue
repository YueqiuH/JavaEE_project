<template>
  <div class="d-page ea-console">
    <PageBreadcrumb domain="teaching" title="考试与补考" />
    <!-- ===== 顶部标题 ===== -->
    <header class="d-head d-rise" style="--rise: 1">
      <div>
        <h1>考试与补考</h1>
        <p class="d-head-desc">考试安排、监考分配与补考报名管理</p>
      </div>
    </header>
    <!-- ===== 操作栏 ===== -->
    <div class="d-toolbar">
      <span class="role-badge" :class="role">{{ roleLabel }}</span>
      <el-select v-model="semester" style="width:150px" @change="loadAll">
        <el-option v-for="s in ['2025-2026-1','2025-2026-2']" :key="s" :label="s" :value="s" />
      </el-select>
      <el-tag size="small" type="info">当前第 {{ currentWeek }} 周</el-tag>
      <div style="flex:1"></div>
      <el-button size="small" @click="loadAll">刷新</el-button>
    </div>

    <div class="d-panel d-rise ea-body" style="--rise: 2">
      <!-- ===== 教务处视图 ===== -->
      <template v-if="role==='admin'">
        <div class="admin-panel">
          <div class="section-title">📋 统一排考控制台</div>
          <p style="color:var(--color-text-secondary);font-size:13px;margin-bottom:12px">使用 <strong>AI 智能学习助理</strong> 一键完成考试编排、考场分配和监考指派。</p>
          <div class="admin-actions">
          </div>

          <el-table :data="exams" stripe size="small" max-height="calc(100vh - 280px)">
            <el-table-column type="index" width="40" />
            <el-table-column prop="examName" label="考试名称" width="180" />
            <el-table-column prop="examType" label="类型" width="80" />
            <el-table-column prop="examDate" label="日期" width="100" />
            <el-table-column label="时间" width="120">
              <template #default="{row}">{{ row.startTime }} - {{ row.endTime }}</template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{row}">
                <el-button size="small" type="primary" @click="assignRooms(row)">分配考场</el-button>
                <el-button size="small" @click="showStudents(row)">考生名单</el-button>
                <el-button size="small" type="warning" @click="showInvigilator(row)">监考</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>

      <!-- ===== 学生视图 ===== -->
      <template v-if="role==='student'">
        <div class="student-panel">
          <div class="section-title">📅 我的考试安排</div>
          <el-table :data="myExams" stripe size="small" empty-text="暂无考试安排">
            <el-table-column type="index" width="40" />
            <el-table-column prop="examName" label="考试" width="180" />
            <el-table-column prop="examDate" label="日期" width="100" />
            <el-table-column label="时间" width="120">
              <template #default="{row}">{{ row.startTime }} - {{ row.endTime }}</template>
            </el-table-column>
            <el-table-column label="考场" width="100">
              <template #default="{row}">{{ getExamRoom(row) }}</template>
            </el-table-column>
            <el-table-column label="座位号" width="70">
              <template #default="{row}">{{ getSeatNo(row) }}</template>
            </el-table-column>
          </el-table>

          <!-- 不及格课程 → 补考报名 -->
          <div class="section-title" style="margin-top:16px">
            📝 补考/缓考报名
          </div>
          <div class="resit-section" v-if="failedCourses.length>0">
            <el-table :data="failedCourses" stripe size="small" max-height="300">
              <el-table-column type="index" width="40" />
              <el-table-column prop="courseName" label="课程" width="130" />
              <el-table-column prop="courseCode" label="代码" width="80" />
              <el-table-column prop="scoreScore" label="成绩" width="60">
                <template #default="{row}">
                  <span style="color:#EF4444">{{ row.scoreScore ?? '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="补考状态" width="100">
                <template #default="{row}">
                  <el-tag v-if="row._resitStatus===1" type="warning" size="small">已报名</el-tag>
                  <el-tag v-else-if="row._resitStatus===3" type="info" size="small">已锁定</el-tag>
                  <el-tag v-else type="danger" size="small">可报名</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="{row}">
                  <el-button
                    v-if="row._resitStatus===0"
                    size="small" type="warning"
                    @click="applyResit(row, '补考')"
                  >
                    补考报名
                  </el-button>
                  <span v-else-if="row._resitStatus===1" style="color:#d97706;font-size:12px">等待考试</span>
                  <span v-else-if="row._resitStatus===3" style="color:#888;font-size:12px">已锁定</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-empty v-else description="无不及格课程，无需补考" :image-size="60" />
        </div>
      </template>

      <!-- ===== 教师视图 ===== -->
      <template v-if="role==='teacher'">
        <div class="teacher-panel">
          <div class="section-title">👀 我的监考安排</div>
          <el-table :data="myInvigilations" stripe size="small" empty-text="暂无监考安排">
            <el-table-column type="index" width="40" />
            <el-table-column prop="examDate" label="日期" width="100" />
            <el-table-column prop="startTime" label="开始" width="80" />
            <el-table-column prop="endTime" label="结束" width="80" />
            <el-table-column prop="duty" label="职责" width="70">
              <template #default="{row}">
                <el-tag :type="row.duty==='主监考'?'danger':'warning'" size="small">{{ row.duty }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="classroomId" label="教室ID" width="70" />
          </el-table>
        </div>
      </template>
    </div>

    <!-- 监考指派弹窗 -->
    <el-dialog v-model="invDlg.visible" title="指派监考" width="400px">
      <el-form label-width="80px">
        <el-form-item label="考试">{{ invDlg.examName }}</el-form-item>
        <el-form-item label="主监考ID"><el-input-number v-model="invDlg.mainId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="副监考ID"><el-input-number v-model="invDlg.subId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="教室ID"><el-input-number v-model="invDlg.roomId" :min="1" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="invDlg.visible=false">取消</el-button>
        <el-button type="primary" @click="doAssignInvigilators">确认指派</el-button>
      </template>
    </el-dialog>

    <!-- 考生名单弹窗 -->
    <el-dialog v-model="studentDlg.visible" title="考生名单（按学号升序）" width="600px">
      <el-table :data="studentDlg.students" stripe size="small" max-height="400">
        <el-table-column prop="seat_no" label="座位" width="60" />
        <el-table-column prop="student_no" label="学号" width="90" />
        <el-table-column prop="student_name" label="姓名" width="90" />
        <el-table-column prop="classroom_name" label="考场" width="120" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { examApi, scoreApi } from '@/api/teaching.js'
import { getStoredCurrentUser } from '@/utils/authSession.js'
import PageBreadcrumb from '@/components/business/PageBreadcrumb.vue'
import './teaching-d.css'

const semester = ref('2025-2026-1')
const loading = ref(false)
const currentWeek = ref(18)

// ===== 身份 =====
const currentUser = computed(() => {
  try { return getStoredCurrentUser() } catch { return null }
})
const userType = computed(() => currentUser.value?.user?.userType || 1)
const role = computed(() => {
  const t = userType.value
  if (t === 4) return 'admin'
  if (t === 3) return 'teacher'
  if (t === 2) return 'counselor'
  return 'student'
})
const roleLabel = computed(() =>
  ({ admin:'🔧 教务处', teacher:'👨‍🏫 教职工', counselor:'📋 辅导员', student:'🧑‍🎓 学生' }[role.value])
)
const userId = computed(() => currentUser.value?.user?.userId || 1)

// ===== 状态 =====
const exams = ref([])
const myExams = ref([])
const myInvigilations = ref([])
const failedCourses = ref([])

const invDlg = reactive({ visible:false, examId:null, examName:'', mainId:null, subId:null, roomId:1 })
const studentDlg = reactive({ visible:false, students:[] })

// ===== 加载 =====
async function loadAll() {
  loading.value = true
  try {
    const [twRes, examRes, weekRes] = await Promise.all([
      scoreApi.getTimeWindow(),
      examApi.list(semester.value, null),
      scoreApi.getCurrentWeek()
    ])
    currentWeek.value = weekRes?.data?.week || 18
    exams.value = examRes?.data || []

    if (role.value === 'student') {
      const [myE, scoreR, resitR] = await Promise.all([
        examApi.getStudentExams(userId.value, semester.value),
        scoreApi.getStudentReport(userId.value, semester.value),
        examApi.getResitStatus(userId.value, semester.value)
      ])
      myExams.value = myE?.data || []
      const scores = (scoreR?.data?.courses || []).filter(s => s.status === 0)
      const resitMap = {}
      ;(resitR?.data || []).forEach(r => { resitMap[r.courseId || r.course_id] = r.status })
      failedCourses.value = scores.map(s => ({
        ...s, _resitStatus: resitMap[s.courseId || s.course_id] || 0
      }))
    }

    if (role.value === 'teacher') {
      const invR = await examApi.getInvigilations(userId.value, semester.value)
      myInvigilations.value = invR?.data || []
    }
  } catch { /* ignore */ }
  finally { loading.value = false }
}

// ===== 教务处操作 =====
async function scheduleExams() {
  loading.value = true
  try { const res = await examApi.scheduleExams(semester.value); ElMessage.success(res?.data?.message || '排考完成'); loadAll() }
  catch (e) { ElMessage.error(e?.message || '失败') }
  finally { loading.value = false }
}

async function assignAllRooms() {
  loading.value = true
  try { const res = await examApi.assignAllRooms(semester.value); ElMessage.success(res?.data?.message || '分配完成'); loadAll() }
  catch (e) { ElMessage.error(e?.message || '失败') }
  finally { loading.value = false }
}

async function assignAllInvigilators() {
  loading.value = true
  try { const res = await examApi.assignAllInvigilators(semester.value); ElMessage.success(res?.data?.message || '指派完成'); loadAll() }
  catch (e) { ElMessage.error(e?.message || '失败') }
  finally { loading.value = false }
}

async function assignRooms(row) {
  loading.value = true
  try {
    const res = await examApi.assignRooms(row.examId)
    ElMessage.success(res?.data?.message || '考场分配完成')
  } catch (e) { ElMessage.error(e?.message || '失败') }
  finally { loading.value = false }
}

function showInvigilator(row) {
  invDlg.examId = row.examId
  invDlg.examName = row.examName
  invDlg.visible = true
}

async function doAssignInvigilators() {
  try {
    await examApi.assignInvigilators({
      examId: invDlg.examId, classroomId: invDlg.roomId,
      mainTeacherId: invDlg.mainId, subTeacherId: invDlg.subId
    })
    ElMessage.success('监考指派完成')
    invDlg.visible = false
  } catch (e) { ElMessage.error(e?.response?.data?.msg || '指派失败') }
}

async function showStudents(row) {
  try {
    const res = await examApi.getStudents(row.examId)
    studentDlg.students = res?.data || []
    studentDlg.visible = true
  } catch { /* ignore */ }
}

async function freezeResitList() {
  try {
    const res = await examApi.freezeResit(semester.value)
    ElMessage.success(res?.data?.message || '已冻结')
  } catch (e) { ElMessage.error(e?.message || '失败') }
}

// ===== 学生操作 =====
async function applyResit(row, type) {
  try {
    await examApi.applyResit({
      studentId: userId.value,
      courseId: row.courseId || row.course_id,
      semester: semester.value,
      applyType: type
    })
    ElMessage.success(type + '报名成功')
    loadAll()
  } catch (e) { ElMessage.error(e?.response?.data?.msg || '报名失败') }
}

function getExamRoom(row) { return row.classroom_name || row._classroomName || '-' }
function getSeatNo(row) { return row.seat_no || row._seatNo || '-' }

onMounted(loadAll)
</script>

<style scoped>
.ea-console { display:flex; flex-direction:column; overflow:hidden; }
.ea-topbar { display:flex; align-items:center; gap:10px; padding:10px 16px; flex-shrink:0; }
.ea-actions { display:flex; gap:8px; }
.role-badge { padding:2px 10px; border-radius:4px; font-size:12px; font-weight:600; color:#fff; }
.role-badge.admin { background:#7c3aed; }
.role-badge.teacher { background:#059669; }
.role-badge.counselor { background:#d97706; }
.role-badge.student { background:#3B82F6; }

.ea-body { flex:1; overflow-y:auto; padding:14px 18px; display:flex; flex-direction:column; gap:12px; }
.admin-panel, .student-panel, .teacher-panel { background:#fff; border-radius:6px; padding:12px; }
.section-title { font-size:14px; font-weight:600; padding-bottom:8px; margin-bottom:8px; border-bottom:1px solid #f0f0f0; }
.admin-actions { display:flex; gap:10px; margin-bottom:12px; }
.resit-hint { padding:8px 12px; margin-bottom:8px; background:#fefce8; border:1px solid #fde68a; border-radius:6px; font-size:12px; color:#d97706; }
</style>
