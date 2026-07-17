<template>
  <div class="sp">
    <!-- 顶部 -->
    <div class="tb">
      <el-select v-model="sem" style="width:140px" @change="load">
        <el-option v-for="s in sms" :key="s" :label="s" :value="s" />
      </el-select>
      <el-select v-model="cid" style="width:280px;margin:0 8px" filterable placeholder="选择要排课的课程..."
        @change="onPick" clearable>
        <el-option v-for="t in ts" :key="t.courseId" :value="t.courseId"
          :label="t.courseName + ' (' + (t.classification||'-') + ' ' + (t.credit||0) + '学分)'" />
      </el-select>
      <el-tag v-if="picked" size="small">学分{{ picked.credit }} · 周{{ picked.weeklyFrequency||1 }}次</el-tag>
      <div style="flex:1"></div>
      <el-button-group size="small">
        <el-button @click="wk--" :disabled="wk<=1">◀</el-button>
        <el-button disabled style="min-width:80px;font-weight:600">第{{ wk }}周</el-button>
        <el-button @click="wk++" :disabled="wk>=16">▶</el-button>
      </el-button-group>
      <el-tag :type="wl.warn?'warning':'success'" size="small" style="margin-left:12px">周{{ wl.total||0 }}节</el-tag>
    </div>

    <!-- 课表 -->
    <div class="gd">
      <div class="gh">
        <div class="gh-l">节次</div>
        <div v-for="d in 7" :key="d" class="gh-d" :class="{we:d>=6}">{{ wlbl[d-1] }}<small v-if="d>=6"> 补</small></div>
      </div>
      <div v-for="p in 13" :key="p" class="gr" :class="zp(p)">
        <div class="gr-l">{{ zl(p) || p }}</div>
        <div v-for="d in 7" :key="d" class="gc" :class="cc(d,p)" @click="go(d,p)">
          <div v-if="b(d,p) && b(d,p).startPeriod===p" class="bk"
            :style="{height:(b(d,p).endPeriod-b(d,p).startPeriod+1)*44-2+'px'}">
            <div class="bk-n">{{ b(d,p).courseName }}</div>
            <div class="bk-i">{{ b(d,p).teacherName||'-' }}</div>
          </div>
        </div>
      </div>
      <div class="gl">
        <span class="ld g"></span>可选 <span class="ld b"></span>已排 <span class="ld x"></span>不可选
      </div>
    </div>

    <!-- 弹窗 -->
    <el-dialog v-model="dl" title="排课" width="400px" destroy-on-close>
      <el-form label-width="65px">
        <el-form-item label="课程"><strong>{{ picked?.courseName||'-' }}</strong></el-form-item>
        <el-form-item label="星期">
          <el-select v-model="fm.d" style="width:100%">
            <el-option v-for="d in 7" :key="d" :label="'星期'+wlbl[d-1]" :value="d" />
          </el-select>
        </el-form-item>
        <el-form-item label="时段">
          <el-select v-model="fm.s" style="width:100%" @change="os">
            <el-option v-for="s in as" :key="s.k" :label="s.l" :value="s.k" :disabled="s.x" />
          </el-select>
        </el-form-item>
        <el-form-item label="教室"><el-input-number v-model="fm.r" :min="1" :max="6" style="width:100%" /></el-form-item>
        <el-form-item label="起止周">
          <el-col :span="11"><el-input-number v-model="fm.sw" :min="1" :max="16" style="width:100%" /></el-col>
          <el-col :span="2" style="text-align:center">—</el-col>
          <el-col :span="11"><el-input-number v-model="fm.ew" :min="1" :max="16" style="width:100%" /></el-col>
        </el-form-item>
        <el-form-item v-if="showSplit" label="拆分上课">
          <el-checkbox v-model="fm.sep">允许拆分为多次上课（可跨天，每段不跨时段）</el-checkbox>
        </el-form-item>
        <el-form-item v-if="fm.sep" label="第二段星期">
          <el-select v-model="fm.d2" style="width:100%">
            <el-option v-for="d in 7" :key="d" :label="'星期'+wlbl[d-1]" :value="d" :disabled="d===fm.d" />
          </el-select>
        </el-form-item>
        <el-form-item label="周模式">
          <el-radio-group v-model="fm.p">
            <el-radio value="every">每周</el-radio><el-radio value="odd">单周</el-radio><el-radio value="even">双周</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dl=false">取消</el-button><el-button type="primary" @click="ok">确认</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref,reactive,computed,onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { courseApi,scheduleApi } from '@/api/teaching.js'

const wlbl = ['一','二','三','四','五','六','日']
const sms = ['2025-2026-1','2025-2026-2','2026-2027-1']
const sem = ref('2025-2026-1'), wk = ref(1), tid = 3
const ts = ref([]), ss = ref([]), cid = ref(null), dl = ref(false)
const wl = ref({total:0,warn:false})

// 所有可用时段——任何学分均可选（仅校验跨时段限制）
const ALL = [
  {k:'1-2',l:'1-2节',sp:1,ep:2},{k:'3-4',l:'3-4节',sp:3,ep:4},
  {k:'3-5',l:'3-5节',sp:3,ep:5},{k:'6-7',l:'6-7节',sp:6,ep:7},
  {k:'8-9',l:'8-9节',sp:8,ep:9},{k:'8-10',l:'8-10节',sp:8,ep:10},
  {k:'11-13',l:'11-13节(晚上)',sp:11,ep:13},
  {k:'1-5',l:'1-5节(上午整段)',sp:1,ep:5},{k:'6-10',l:'6-10节(下午整段)',sp:6,ep:10}
]

const picked = computed(() => ts.value.find(t => t.courseId === cid.value) || null)
const as = computed(() => ALL.map(s => ({...s, x: false }))) // 所有时段均可选

const showSplit = computed(() => {
  return picked.value && Math.floor(picked.value.credit) >= 4
})

const fm = reactive({ d:1, s:'', r:1, sw:1, ew:16, p:'every', sep:false, d2:3 })

const load = async () => {
  try {
    const [a,b] = await Promise.all([courseApi.list(sem.value), scheduleApi.getTeacherSchedule(tid, sem.value)])
    ts.value = (a?.data||[]).filter(c => c.courseId)
    ss.value = b?.data||[]
    const w = await scheduleApi.getTeacherWorkload(tid, sem.value)
    wl.value = typeof w?.data==='string' ? JSON.parse(w.data) : (w?.data||{total:0,warn:false})
  } catch { ts.value=[]; ss.value=[] }
}

const b = (d,p) => ss.value.find(s => s.weekDay===d && p>=s.startPeriod && p<=s.endPeriod) || null

const cc = (d,p) => {
  const a = []; if(d>=6) a.push('we')
  if(b(d,p)) a.push('oc')
  else if(picked.value) {
    const cr = Math.floor(picked.value.credit)||2
    a.push(ALL.some(s => s.cr.includes(cr) && p>=s.sp && p<=s.ep) ? 'vi' : 'xx')
  }
  return a
}

const zp = p => p<=5 ? 'zm' : p<=10 ? 'za' : 'ze'
const zl = p => { if(p===1)return'上午'; if(p===6)return'下午'; if(p===11)return'晚上'; return '' }
const onPick = () => {}
const go = (d,p) => { fm.d=d; fm.s=''; dl.value=true }
const os = () => { const s=ALL.find(s=>s.k===fm.s); if(s){fm.sp=s.sp;fm.ep=s.ep} }
const ok = async () => {
  if (!picked.value) { ElMessage.warning('请先选择课程'); return }
  if (!fm.s) { ElMessage.warning('请选择时段'); return }
  os()
  try {
    const data = {
      courseId:picked.value.courseId, teacherId:tid, semester:sem.value,
      weekDay:fm.d, startPeriod:fm.sp, endPeriod:fm.ep,
      classroomId:fm.r, startWeek:fm.sw, endWeek:fm.ew, weekPattern:fm.p,
      credits:picked.value.credit||2, courseName:picked.value.courseName,
      courseClassification:picked.value.classification||'必修', weeklyFrequency:picked.value.weeklyFrequency||1,
      separable: fm.sep || false
    }
    if (fm.sep) {
      data.secondSegments = [{
        weekDay: fm.d2, startPeriod: fm.sp, endPeriod: fm.ep,
        classroomId: fm.r, startWeek: fm.sw, endWeek: fm.ew, weekPattern: fm.p,
        semester: sem.value, teacherId: tid, credits: picked.value.credit, courseName: picked.value.courseName
      }]
    }
    const r = await scheduleApi.add(data)
    if (r?.code===0) { ElMessage.success('排课成功'); dl.value=false; load() }
  } catch(e) { ElMessage.error(e?.message||'排课失败') }
}

onMounted(load)
</script>

<style scoped>
.sp { height:100%; display:flex; flex-direction:column; background:#f4f6f8; }
.tb { display:flex; align-items:center; padding:10px 16px; background:#fff; border-bottom:1px solid #e4e7ed; flex-shrink:0; }

.gd { flex:1; display:flex; flex-direction:column; margin:8px; background:#fff; border-radius:4px; border:1px solid #e4e7ed; overflow:hidden; }
.gh { display:flex; background:#f8f9fb; flex-shrink:0; }
.gh-l { width:50px; text-align:center; padding:7px 0; font-size:12px; color:#888; border-right:1px solid #e4e7ed; flex-shrink:0; }
.gh-d { flex:1; text-align:center; padding:7px 0; font-size:13px; font-weight:600; border-right:1px solid #e4e7ed; }
.gh-d:last-child { border-right:none; }
.gh-d.we { color:#e9a824; }
.gh-d small { font-size:10px; color:#e9a824; }

.gr { display:flex; flex:1; }
.gr-l { width:50px; display:flex; align-items:center; justify-content:center; font-size:11px; font-weight:600; border-right:1px solid #e4e7ed; border-bottom:1px solid #eee; flex-shrink:0; color:#666; }
.zm .gr-l { color:#d97706; background:#fffbeb; }
.za .gr-l { color:#059669; background:#f0fdf4; }
.ze .gr-l { color:#2563eb; background:#eff6ff; }

.gc { flex:1; border-right:1px solid #f0f0f0; border-bottom:1px solid #f0f0f0; position:relative; cursor:pointer; min-height:44px; }
.gc:last-child { border-right:none; }
.zm .gc { background:#fefefb; }
.za .gc { background:#fcfdfb; }
.ze .gc { background:#f9fafe; }
.gc.we { background:#fffef8 !important; }
.gc.vi { background:#dcfce7 !important; }
.gc.xx { background:#f3f3f3 !important; }
.gc.oc { background:#dbeafe !important; }

.bk { position:absolute; top:1px; left:1px; right:1px; z-index:2; border-radius:4px; padding:3px 6px; color:#fff; font-size:11px; overflow:hidden; background:linear-gradient(135deg,#3B82F6,#60A5FA); cursor:pointer; }
.bk:hover { filter:brightness(1.1); }
.bk-n { font-weight:600; line-height:1.3; }
.bk-i { opacity:.85; font-size:10px; line-height:1.3; }

.gl { display:flex; gap:16px; padding:6px 12px; font-size:12px; color:#888; flex-shrink:0; border-top:1px solid #f0f0f0; }
.ld { width:10px; height:10px; border-radius:50%; display:inline-block; margin-right:4px; }
.ld.g { background:#dcfce7; border:1px solid #86efac; }
.ld.b { background:#dbeafe; border:1px solid #93c5fd; }
.ld.x { background:#f3f3f3; border:1px solid #d4d4d4; }
</style>
