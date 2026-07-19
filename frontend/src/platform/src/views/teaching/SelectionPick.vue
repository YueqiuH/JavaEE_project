<template>
  <div class="pk">
    <div class="pk-top"><span>🎯 选课界面</span><el-input v-model="kw" size="small" style="width:200px" placeholder="搜索..." clearable /></div>
    <div class="pk-body">
      <div class="pk-left"><el-table :data="filtered" stripe size="small" max-height="calc(100vh - 130px)" @row-click="pick=$event" highlight-current-row>
        <el-table-column prop="courseCode" label="代码" width="80" /><el-table-column prop="courseName" label="课程" width="120" />
        <el-table-column label="性质" width="60"><template #default="{r}"><el-tag :type="r.classification==='必修'?'danger':'success'" size="small">{{ r.classification }}</el-tag></template></el-table-column>
        <el-table-column prop="teacherName" label="教师" /><el-table-column label="容量" width="80"><template #default="{r}"><el-progress :percentage="pct(r)" :stroke-width="4" style="width:40px"/><span style="font-size:10px">{{ r.currentCount||0 }}/{{ r.maxCapacity||60 }}</span></template></el-table-column>
        <el-table-column label="操作" width="70"><template #default="{r}"><el-button size="small" type="primary" @click.stop="sel(r)">选课</el-button></template></el-table-column>
      </el-table></div>
      <div class="pk-right">
        <div class="pk-title">📅 我的课表</div>
        <div class="mg"><div class="mgl">节</div><div v-for="d in 7" :key="d" class="mgd" :class="{we:d>=6}">{{ wl[d-1] }}</div></div>
        <div v-for="p in 13" :key="p" class="mr" :class="zp(p)"><div class="mrl">{{ p }}</div>
          <div v-for="d in 7" :key="d" class="mc" :class="{we:d>=6}">
            <div v-if="gs(d,p)&&gs(d,p).startPeriod===p" class="mb" :style="{height:(gs(d,p).endPeriod-gs(d,p).startPeriod+1)*28-1+'px'}"><div class="mbn">{{ gs(d,p).courseName }}</div></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref,computed,onMounted } from 'vue'; import { ElMessage } from 'element-plus'
import { selectCourse,getMySelection,getCourseList } from '@/api/teaching.js'; import { getStoredCurrentUser } from '@/utils/authSession.js'
const wl=['一','二','三','四','五','六','日']; const sem=ref('2025-2026-1'); const kw=ref(''); const courses=ref([]); const selected=ref([]); const pick=ref(null)
const uid=computed(()=>{const u=getStoredCurrentUser();return u?.user?.userId||1})
const filtered=computed(()=>courses.value.filter(c=>!kw.value||(c.courseName||'').includes(kw.value)))
const load=async()=>{try{const[cr,sr]=await Promise.all([getCourseList(sem.value),getMySelection(uid.value,sem.value)]);courses.value=(cr?.data||[]).filter(c=>c.courseId);selected.value=(sr?.data||[]).map(s=>({...s,courseName:s.course_name,weekDay:s.week_day,startPeriod:s.start_period,endPeriod:s.end_period}))}catch{courses.value=[];selected.value=[]}}
const gs=(d,p)=>selected.value.find(s=>s.weekDay===d&&p>=s.startPeriod&&p<=s.endPeriod)||null
const pct=r=>Math.round((r.currentCount||0)/(r.maxCapacity||60)*100)
const zp=p=>p<=5?'zm':p<=10?'za':'ze'
const sel=r=>{ElMessage.info(`选课: ${r.courseName}`); selectCourse({studentId:uid.value,courseId:r.courseId,semester:sem.value}).then(res=>{if(res?.code===0){ElMessage.success('选课成功');load()}}).catch(e=>ElMessage.error(e?.message||'失败'))}
onMounted(load)
</script>
<style scoped>
.pk{height:100%;display:flex;flex-direction:column;padding:8px}.pk-top{display:flex;justify-content:space-between;align-items:center;margin-bottom:6px}.pk-body{flex:1;display:flex;gap:8px;overflow:hidden}.pk-left{flex:1;overflow:auto}.pk-right{width:220px;min-width:200px;overflow:auto;border-left:1px solid #e4e7ed;padding-left:8px}.pk-title{font-weight:600;margin-bottom:4px;font-size:13px}
.mg{display:flex}.mgl{width:24px;text-align:center;font-size:9px;color:#888;border-right:1px solid #e4e7ed}.mgd{flex:1;text-align:center;font-size:9px;font-weight:600;padding:2px 0;border-right:1px solid #e4e7ed}.mgd:last-child{border-right:none}.mgd.we{color:#e9a824}.mr{display:flex}.mrl{width:24px;display:flex;align-items:center;justify-content:center;font-size:8px;border-right:1px solid #e4e7ed;border-bottom:1px solid #eee}.zm .mrl{color:#d97706;background:#fffbeb}.za .mrl{color:#059669;background:#f0fdf4}.ze .mrl{color:#2563eb;background:#eff6ff}.mc{flex:1;min-height:24px;border-right:1px solid #f5f5f5;border-bottom:1px solid #f5f5f5;position:relative}.mc.we{background:#fffef8}.mb{position:absolute;top:0;left:0;right:0;z-index:1;background:#3B82F6;color:#fff;font-size:7px;padding:1px 2px;border-radius:2px;overflow:hidden}.mbn{line-height:1.2}
</style>
