<template>
  <div class="d-page sch-console">
    <PageBreadcrumb domain="teaching" title="排课与课表" />
    <!-- ===== 顶部标题 ===== -->
    <header class="d-head d-rise" style="--rise: 1">
      <div>
        <h1>排课与课表</h1>
        <p class="d-head-desc">按周查看课程安排、教室分配与调课信息</p>
      </div>
    </header>
    <!-- ===== 操作栏 ===== -->
    <div class="sch-topbar">
      <span class="role-badge" :class="'role-'+role">{{ roleLabel }}</span>
      <el-select v-model="sem" style="width:150px" @change="loadAll">
        <el-option v-for="s in sms" :key="s" :label="s" :value="s" />
      </el-select>

      <!-- 学生/辅导员：选择查看谁的课表 -->
      <template v-if="role==='student'||role==='counselor'">
        <span style="margin-left:8px;font-size:13px;color:#666">查看课表</span>
      </template>

      <!-- 教务处：统计 + 操作按钮 -->
      <template v-if="role==='admin'">
        <div class="top-stats">
          <span>📋 {{ allCourses.length }}门</span>
          <span style="color:#059669">已排 {{ scheduledCount }}</span>
          <span style="color:#d97706">待排 {{ unscheduledCount }}</span>
          <span style="color:#999">停开 {{ suspendedCount }}</span>
        </div>
      </template>

      <div style="flex:1"></div>

      <el-button v-if="role==='admin'" size="small" type="primary" @click="showAddCourse=true">+ 新增课程</el-button>

      <el-button-group size="small">
        <el-button @click="wk--" :disabled="wk<=1">◀</el-button>
        <el-button disabled style="min-width:80px;font-weight:600">第{{ wk }}周</el-button>
        <el-button @click="wk++" :disabled="wk>=16">▶</el-button>
      </el-button-group>
    </div>

    <!-- ===== 主体 ===== -->
    <section class="d-panel d-rise" style="--rise: 2;flex:1;display:flex;flex-direction:column;overflow:hidden">
    <div class="sch-body">
      <!-- 教务处：左侧课程管理 -->
      <div class="sch-left" v-if="role==='admin'">
        <div class="left-filter">
          <el-input v-model="courseFilter" placeholder="搜索课程..." size="small" clearable :prefix-icon="Search" />
          <el-select v-model="courseStatusFilter" size="small" style="width:100px">
            <el-option label="全部" value="all" /><el-option label="已排" value="scheduled" />
            <el-option label="待排" value="unscheduled" /><el-option label="停开" value="suspended" />
          </el-select>
        </div>
        <div class="course-list" v-loading="loading">
          <div v-for="c in filteredCourseList" :key="c.courseId" class="cl-item"
            :class="{'cl-scheduled':c._scheduled, 'cl-suspended':!c.isActive}"
            @click="selectCourse(c)">
            <div class="cli-top">
              <span :class="!c.isActive?'dot-off':c._scheduled?'dot-on':'dot-pending'"></span>
              <strong>{{ c.courseName }}</strong>
              <el-tag :type="c.classification==='必修'?'danger':'success'" size="small">{{ c.classification }}</el-tag>
            </div>
            <div class="cli-meta">{{ c.courseCode }} · {{ c.credit }}学分 · {{ c._scheduled ? '已排'+c._scheduleCount+'条' : !c.isActive ? '已停开' : '待排' }}</div>
            <div class="cli-actions" @click.stop>
              <template v-if="c._scheduled">
                <el-button size="small" type="primary" @click="pickAndSchedule(c)">查看/补排</el-button>
                <el-button size="small" type="danger" @click="suspendCourse(c)">停开</el-button>
              </template>
              <template v-else-if="!c.isActive">
                <el-button size="small" type="success" @click="restoreCourse(c)">恢复</el-button>
              </template>
              <template v-else>
                <el-button size="small" type="warning" @click="pickAndSchedule(c)">排课</el-button>
              </template>
            </div>
          </div>
          <el-empty v-if="filteredCourseList.length===0" description="无匹配课程" :image-size="60" />
        </div>
      </div>

      <!-- 课表网格（所有角色可见） -->
      <div class="sch-right">
        <div class="gd">
          <div class="gh">
            <div class="gh-l">节次</div>
            <div v-for="d in 7" :key="d" class="gh-d" :class="{we:d>=6}">{{ wlbl[d-1] }}<small v-if="d>=6"> 补</small></div>
          </div>
          <div v-for="p in 13" :key="p" class="gr" :class="zp(p)">
            <div class="gr-l">{{ zl(p) || p }}</div>
            <div v-for="d in 7" :key="d" class="gc" :class="cc(d,p)" @click="role==='admin' ? go(d,p) : null">
              <div v-if="b(d,p) && b(d,p).startPeriod===p" class="bk"
                :style="{height:(b(d,p).endPeriod-b(d,p).startPeriod+1)*44-2+'px'}"
                @click.stop="showScheduleDetail(b(d,p))">
                <div class="bk-n">{{ b(d,p).courseName }}</div>
                <div class="bk-i">{{ b(d,p).teacherName || '教师'+(b(d,p).teacherId||'-') }} · {{ b(d,p).classroomName || '教室'+(b(d,p).classroomId||'-') }}</div>
                <div v-if="role==='admin'" class="bk-act" @click.stop="deleteSchedule(b(d,p))">✕</div>
              </div>
            </div>
          </div>
          <div class="gl">
            <span class="ld b"></span>已排课程
            <span v-if="role==='teacher'||role==='admin'"><span class="ld g"></span>可选时段</span>
          </div>
        </div>
      </div>
    </div>
    </section>

    <!-- 排课弹窗 -->
    <el-dialog v-model="dl" title="排课" width="440px" destroy-on-close>
      <el-form label-width="65px">
        <el-form-item label="课程"><strong>{{ picked?.courseName||'-' }}</strong></el-form-item>
        <el-form-item label="星期"><el-select v-model="fm.d" style="width:100%"><el-option v-for="d in 7" :key="d" :label="'星期'+wlbl[d-1]" :value="d" /></el-select></el-form-item>
        <el-form-item label="时段"><el-select v-model="fm.s" style="width:100%" @change="os"><el-option v-for="s in as" :key="s.k" :label="s.l" :value="s.k" /></el-select></el-form-item>
        <el-form-item label="教师"><el-select v-model="fm.tid" style="width:100%" filterable><el-option v-for="t in teachers" :key="t.userId" :label="t.realName+' ('+(t.title||'教师')+')'" :value="t.userId" /></el-select></el-form-item>
        <el-form-item label="教室"><el-select v-model="fm.r" style="width:100%" filterable><el-option v-for="r in classrooms" :key="r.classroomId" :label="r.classroomName+' ('+r.building+'·'+r.capacity+'人)'" :value="r.classroomId" /></el-select></el-form-item>
        <el-form-item label="起止周">
          <el-col :span="11"><el-input-number v-model="fm.sw" :min="1" :max="16" style="width:100%" /></el-col>
          <el-col :span="2" style="text-align:center">—</el-col>
          <el-col :span="11"><el-input-number v-model="fm.ew" :min="1" :max="16" style="width:100%" /></el-col>
        </el-form-item>
        <el-form-item label="周模式"><el-radio-group v-model="fm.p"><el-radio value="every">每周</el-radio><el-radio value="odd">单周</el-radio><el-radio value="even">双周</el-radio></el-radio-group></el-form-item>
        <el-form-item label="类型"><el-radio-group v-model="fm.type"><el-radio value="正常">正常</el-radio><el-radio value="补课">补课</el-radio></el-radio-group></el-form-item>
      </el-form>
      <template #footer><el-button @click="dl=false">取消</el-button><el-button type="primary" @click="ok">确认排课</el-button></template>
    </el-dialog>

    <!-- 新增课程弹窗 -->
    <el-dialog v-model="showAddCourse" title="新增课程" width="460px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="课程名称"><el-input v-model="nc.courseName" placeholder="如：高等数学" /></el-form-item>
        <el-form-item label="课程代码"><el-input v-model="nc.courseCode" placeholder="如：MATH101" /></el-form-item>
        <el-form-item label="性质"><el-select v-model="nc.classification" style="width:100%"><el-option label="必修" value="必修" /><el-option label="选修" value="选修" /><el-option label="限选" value="限选" /></el-select></el-form-item>
        <el-form-item label="学分"><el-input-number v-model="nc.credit" :min="1" :max="5" :step="0.5" style="width:100%" /></el-form-item>
        <el-form-item label="周频次"><el-input-number v-model="nc.weeklyFrequency" :min="1" :max="2" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showAddCourse=false">取消</el-button><el-button type="primary" @click="addCourse">确认</el-button></template>
    </el-dialog>
    <!-- 课程详情弹窗 -->
    <el-dialog v-model="detailDl" title="课程详情" width="420px">
      <el-descriptions :column="1" border v-if="detailSch" size="small">
        <el-descriptions-item label="课程名称">{{ detailSch.courseName }}</el-descriptions-item>
        <el-descriptions-item label="课程代码">{{ detailSch.courseCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="授课教师">{{ detailSch.teacherName || detailSch.teacherId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="上课教室">{{ detailSch.classroomName || detailSch.classroomId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="星期">{{ '周' + wlbl[(detailSch.weekDay||1)-1] }}</el-descriptions-item>
        <el-descriptions-item label="节次">{{ detailSch.startPeriod }}-{{ detailSch.endPeriod }} 节</el-descriptions-item>
        <el-descriptions-item label="教学周">{{ detailSch.startWeek }}-{{ detailSch.endWeek }} 周</el-descriptions-item>
        <el-descriptions-item label="周模式">{{ detailSch.weekPattern==='odd'?'单周':detailSch.weekPattern==='even'?'双周':'每周' }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detailSch.scheduleType || '正常' }}</el-descriptions-item>
        <el-descriptions-item label="学分">{{ detailSch.credits || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="detailDl=false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref,reactive,computed,onMounted } from 'vue'
import { ElMessage,ElMessageBox } from 'element-plus'
import { courseApi,scheduleApi,scoreApi } from '@/api/teaching.js'
import { getStoredCurrentUser } from '@/utils/authSession.js'
import PageBreadcrumb from '@/components/business/PageBreadcrumb.vue'
import './teaching-d.css'

const wlbl = ['一','二','三','四','五','六','日']
const sms = ['2025-2026-1','2025-2026-2','2026-2027-1']
const sem = ref('2025-2026-2'), wk = ref(1), wl = ref({total:0,warn:false})
function dedupByKey(arr,key){const seen=new Set();return arr.filter(v=>{const k=v[key];if(seen.has(k))return false;seen.add(k);return true})}

// ===== 身份 =====
const currentUser = computed(() => { try { return getStoredCurrentUser() } catch { return null } })
const ut = computed(() => currentUser.value?.user?.userType || 1)
const uid = computed(() => currentUser.value?.user?.userId || 1)
const role = computed(() => ({ 1:'student',2:'counselor',3:'teacher',4:'admin' }[ut.value]||'student'))
const roleLabel = computed(() => ({ student:'🧑‍🎓 学生',counselor:'📋 辅导员',teacher:'👨‍🏫 教职工',admin:'🔧 教务处' }[role.value]))

// ===== 课程数据 =====
const loading = ref(false), dl = ref(false), showAddCourse = ref(false), detailDl = ref(false)
const allCourses = ref([]), allSchedules = ref([]), picked = ref(null), detailSch = ref(null)
const courseFilter = ref(''), courseStatusFilter = ref('all'), classrooms = ref([]), teachers = ref([]), scoredCourseIds = ref(new Set())

const coursesWithStatus = computed(() => allCourses.value.map(c => {
  const ss = allSchedules.value.filter(s => s.courseId === c.courseId)
  return { ...c, _scheduled: ss.length>0, _scheduleCount: ss.length, _scored: scoredCourseIds.value.has(c.courseId) }
}).filter(c => !c._scored))
const filteredCourseList = computed(() => {
  let list = coursesWithStatus.value
  if (courseFilter.value) { const kw = courseFilter.value.toLowerCase(); list = list.filter(c => (c.courseName||'').toLowerCase().includes(kw) || (c.courseCode||'').toLowerCase().includes(kw)) }
  if (courseStatusFilter.value==='scheduled') list = list.filter(c => c._scheduled&&c.isActive)
  else if (courseStatusFilter.value==='unscheduled') list = list.filter(c => !c._scheduled&&c.isActive)
  else if (courseStatusFilter.value==='suspended') list = list.filter(c => !c.isActive)
  return list
})
const scheduledCount = computed(() => allCourses.value.filter(c => c._scheduled).length)
const unscheduledCount = computed(() => allCourses.value.filter(c => c.isActive && !c._scheduled).length)
const suspendedCount = computed(() => allCourses.value.filter(c => !c.isActive).length)

const ALL = [
  {k:'1-2',l:'1-2节',sp:1,ep:2},{k:'3-4',l:'3-4节',sp:3,ep:4},{k:'3-5',l:'3-5节',sp:3,ep:5},
  {k:'6-7',l:'6-7节',sp:6,ep:7},{k:'8-9',l:'8-9节',sp:8,ep:9},{k:'8-10',l:'8-10节',sp:8,ep:10},
  {k:'11-13',l:'11-13节(晚上)',sp:11,ep:13},{k:'1-5',l:'1-5节(上午整段)',sp:1,ep:5},{k:'6-10',l:'6-10节(下午整段)',sp:6,ep:10}
]
const as = computed(() => ALL.map(s => ({...s, x: false})))
const fm = reactive({ d:1, s:'', sp:1, ep:2, r:1, sw:1, ew:16, p:'every', type:'正常', tid:null })
const nc = reactive({ courseName:'', courseCode:'', classification:'必修', credit:2, weeklyFrequency:1 })

const b = (d,p) => allSchedules.value.find(s => s.weekDay===d && p>=s.startPeriod && p<=s.endPeriod) || null
const cc = (d,p) => { const a = []; if(d>=6) a.push('we'); if(b(d,p)) a.push('oc'); else if(picked.value) a.push('vi'); return a }
const zp = p => p<=5?'zm':p<=10?'za':'ze'
const zl = p => { if(p===1)return'上午'; if(p===6)return'下午'; if(p===11)return'晚上'; return'' }

// ===== 数据加载 =====
async function loadAll() {
  loading.value = true
  try {
    const tid = role.value === 'admin' ? 0 : (role.value === 'student' ? uid.value : uid.value)
    const api = role.value === 'student'
      ? scheduleApi.getStudentSchedule(uid.value, sem.value)
      : scheduleApi.getTeacherSchedule(tid, sem.value)
    const p = [courseApi.list(sem.value), api]
    if(role.value === 'admin') p.push(scheduleApi.getClassrooms())
    if(role.value === 'admin') p.push(courseApi.listTeachers())
    if(role.value === 'admin') p.push(scoreApi.getTeacherClasses(0, sem.value))
    const results = await Promise.all(p)
    allCourses.value = dedupByKey((results[0]?.data||[]).filter(c => c.courseId), 'courseId')
    allSchedules.value = (results[1]?.data||[]).filter(s => s.scheduleId)
    if(role.value === 'admin' && results[2]) classrooms.value = results[2].data||[]
    if(role.value === 'admin' && results[3]) teachers.value = results[3].data||[]
    if(role.value === 'admin' && results[4]) scoredCourseIds.value = new Set((results[4].data||[]).map(c => c.courseId))
    try { const w = await scheduleApi.getTeacherWorkload(uid.value, sem.value); wl.value = typeof w?.data==='string'?JSON.parse(w.data):(w?.data||{total:0,warn:false}) } catch { wl.value = {total:0,warn:false} }
  } catch { allCourses.value=[]; allSchedules.value=[] }
  finally { loading.value=false }
}

// ===== 交互 =====
function selectCourse(c) { picked.value = c }
function pickAndSchedule(c) { picked.value=c; fm.d=1;fm.s=''; const defR = classrooms.value.length>0 ? classrooms.value[0].classroomId : 1; fm.r=defR; fm.tid = teachers.value.length>0 ? teachers.value[0].userId : uid.value; fm.sw=1;fm.ew=16;fm.p='every';fm.type='正常';dl.value=true }
function showScheduleDetail(s) { detailSch.value = s; detailDl.value = true }
function go(d,p) { if(!picked.value){ElMessage.info('请先选择课程');return}; fm.d=d; fm.s=''; dl.value=true }
const os = () => { const s=ALL.find(s=>s.k===fm.s); if(s){fm.sp=s.sp;fm.ep=s.ep} }

async function ok() {
  if(!picked.value){ElMessage.warning('请选择课程');return}
  if(!fm.s){ElMessage.warning('请选择时段');return}
  os()
  try {
    const r = await scheduleApi.add({
      courseId:picked.value.courseId, teacherId:fm.tid||uid.value, semester:sem.value,
      weekDay:fm.d, startPeriod:fm.sp, endPeriod:fm.ep, classroomId:fm.r,
      startWeek:fm.sw, endWeek:fm.ew, weekPattern:fm.p,
      credits:picked.value.credit||2, courseName:picked.value.courseName,
      courseClassification:picked.value.classification||'必修',
      weeklyFrequency:picked.value.weeklyFrequency||1, scheduleType:fm.type
    })
    if(r?.code===0){ElMessage.success('排课成功');dl.value=false;loadAll()}
    else ElMessage.error(r?.msg||'失败')
  } catch(e) { ElMessage.error(e?.response?.data?.msg||e?.message||'失败') }
}

async function deleteSchedule(s) {
  ElMessageBox.confirm(`删除「${s.courseName}」的排课？`,'删除',{type:'warning'}).then(async()=>{
    const r = await scheduleApi.delete(s.scheduleId)
    if(r?.code===0){ElMessage.success('已删除');loadAll()}else ElMessage.error(r?.msg||'失败')
  }).catch(()=>{})
}

async function suspendCourse(c) {
  ElMessageBox.confirm(`停开「${c.courseName}(${c.courseCode})」？将删除所有排课和选课记录。`,'停开',{type:'danger'}).then(async()=>{
    const r = await scheduleApi.suspendCourse(c.courseId, sem.value)
    if(r?.code===0){ElMessage.success('已停开');loadAll()}else ElMessage.error(r?.msg||'失败')
  }).catch(()=>{})
}

async function restoreCourse(c) {
  ElMessageBox.confirm(`恢复「${c.courseName}」？`,'恢复',{type:'info'}).then(async()=>{
    const r = await courseApi.add({...c, isActive:1})
    if(r?.code===0){ElMessage.success('已恢复');loadAll()}else ElMessage.error(r?.msg||'失败')
  }).catch(()=>{})
}

async function addCourse() {
  if(!nc.courseName.trim()){ElMessage.warning('请输入课程名称');return}
  if(!nc.courseCode.trim()){ElMessage.warning('请输入课程代码');return}
  try {
    const r = await courseApi.add({...nc, isActive:1})
    if(r?.code===0){ElMessage.success('课程已添加');showAddCourse.value=false;nc.courseName='';nc.courseCode='';loadAll()}
    else ElMessage.error(r?.msg||'失败')
  } catch(e) { ElMessage.error(e?.response?.data?.msg||e?.message||'失败') }
}

onMounted(loadAll)
</script>

<style scoped>
.sch-console{display:flex;flex-direction:column;overflow:hidden}
.sch-topbar{display:flex;align-items:center;gap:10px;padding:10px 16px;background:#fafafa;border-bottom:1px solid var(--color-border-light);flex-shrink:0;flex-wrap:wrap;border-radius:12px 12px 0 0}
.role-badge{padding:2px 10px;border-radius:4px;font-size:12px;font-weight:600;color:#fff}
.role-student{background:#3B82F6}.role-counselor{background:#d97706}.role-teacher{background:#059669}.role-admin{background:#7c3aed}
.top-stats{display:flex;gap:14px;font-size:13px}

.sch-body{flex:1;display:flex;gap:8px;padding:8px;overflow:hidden;background:#fff;border-radius:0 0 12px 12px}
.sch-left{width:320px;flex-shrink:0;display:flex;flex-direction:column;gap:6px;overflow:hidden}
.left-filter{display:flex;gap:6px;background:#fff;padding:8px;border-radius:6px;flex-shrink:0}
.course-list{flex:1;overflow-y:auto;background:#fff;border-radius:6px;padding:4px;display:flex;flex-direction:column;gap:4px}
.cl-item{padding:10px 12px;border:1px solid #e4e7ed;border-radius:6px;cursor:pointer;transition:.15s}
.cl-item:hover{border-color:#3B82F6;background:#f8faff}
.cl-item.cl-scheduled{border-left:3px solid #059669}
.cl-item.cl-suspended{border-left:3px solid #ccc;opacity:.7}
.cli-top{display:flex;align-items:center;gap:8px;margin-bottom:4px}
.cli-top strong{font-size:13px}
.dot-on,.dot-pending,.dot-off{width:8px;height:8px;border-radius:50%;flex-shrink:0}
.dot-on{background:#059669}.dot-pending{background:#d97706}.dot-off{background:#ccc}
.cli-meta{font-size:11px;color:#888;margin-bottom:6px}
.cli-actions{display:flex;gap:6px}

.sch-right{flex:1;overflow:hidden;min-width:0}
.gd{height:100%;display:flex;flex-direction:column;background:#fff;border-radius:6px;border:1px solid #e4e7ed;overflow:hidden}
.gh{display:flex;background:#f8f9fb;flex-shrink:0}
.gh-l{width:50px;text-align:center;padding:7px 0;font-size:12px;color:#888;border-right:1px solid #e4e7ed;flex-shrink:0}
.gh-d{flex:1;text-align:center;padding:7px 0;font-size:13px;font-weight:600;border-right:1px solid #e4e7ed}.gh-d:last-child{border-right:none}.gh-d.we{color:#e9a824}.gh-d small{font-size:10px;color:#e9a824}
.gr{display:flex;flex:1}
.gr-l{width:50px;display:flex;align-items:center;justify-content:center;font-size:11px;font-weight:600;border-right:1px solid #e4e7ed;border-bottom:1px solid #eee;flex-shrink:0;color:#666}
.zm .gr-l{color:#d97706;background:#fffbeb}.za .gr-l{color:#059669;background:#f0fdf4}.ze .gr-l{color:#2563eb;background:#eff6ff}
.gc{flex:1;border-right:1px solid #f0f0f0;border-bottom:1px solid #f0f0f0;position:relative;min-height:44px}.gc:last-child{border-right:none}
.gc.we{background:#fffef8}.gc.vi{background:#dcfce7}.gc.oc{background:#dbeafe}
.bk{position:absolute;top:1px;left:1px;right:1px;z-index:2;border-radius:4px;padding:3px 6px;color:#fff;font-size:11px;overflow:hidden;background:linear-gradient(135deg,#3B82F6,#60A5FA)}
.bk:hover .bk-act{display:flex}.bk-n{font-weight:600;line-height:1.3}.bk-i{opacity:.85;font-size:10px;line-height:1.3}
.bk-act{display:none;position:absolute;top:2px;right:4px;color:#fff;font-size:11px;cursor:pointer;padding:0 4px;border-radius:2px;background:rgba(255,255,255,.2)}.bk-act:hover{background:rgba(239,68,68,.7)}
.gl{display:flex;gap:16px;padding:6px 12px;font-size:12px;color:#888;flex-shrink:0;border-top:1px solid #f0f0f0;align-items:center}
.ld{width:10px;height:10px;border-radius:2px;display:inline-block;margin-right:4px}.ld.b{background:#dbeafe}.ld.g{background:#dcfce7}

@media(max-width:1100px){.sch-body{flex-direction:column}.sch-left{width:100%;max-height:260px}}
</style>
