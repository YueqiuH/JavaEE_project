<template>
  <div class="p">
    <div class="bar">
      <span>📋 共 {{ selected.length }} 门已选课程</span>
      <el-switch v-model="week" active-text="周课表" inactive-text="表格" size="small" />
    </div>
    <el-table v-if="!week" :data="selected" stripe size="small" style="width:100%">
      <el-table-column type="index" width="40" />
      <el-table-column prop="courseCode" label="课程代码" width="100" />
      <el-table-column prop="courseName" label="课程名称" width="140" />
      <el-table-column label="性质" width="70"><template #default="{row}"><el-tag :type="row.classification==='必修'?'danger':'success'" size="small">{{ row.classification }}</el-tag></template></el-table-column>
      <el-table-column prop="teacherName" label="教师" width="80" />
      <el-table-column label="上课时间" width="150"><template #default="{row}">{{ fmtTime(row) }}</template></el-table-column>
      <el-table-column prop="classroomName" label="地点" width="90" />
      <el-table-column label="容量" width="90"><template #default="{row}">{{ row.currentCount||0 }}/{{ row.maxCapacity||'-' }}</template></el-table-column>
      <el-table-column label="操作" width="80" fixed="right"><template #default="{row}"><el-button size="small" type="danger" @click="drop(row)">退选</el-button></template></el-table-column>
    </el-table>
    <div v-else class="wk">
      <div class="g"><div class="gl">节</div><div v-for="d in 7" :key="d" class="gd" :class="{we:d>=6}">{{ wl[d-1] }}</div></div>
      <div v-for="p in 13" :key="p" class="r" :class="zp(p)"><div class="rl">{{ p }}</div>
        <div v-for="d in 7" :key="d" class="c" :class="{we:d>=6}">
          <div v-if="gs(d,p)&&gs(d,p).startPeriod===p" class="b" :style="{height:(gs(d,p).endPeriod-gs(d,p).startPeriod+1)*40-2+'px'}">
            <div class="bn">{{ gs(d,p).courseName }}</div><div class="bi">{{ gs(d,p).teacherName||'-' }}</div></div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref,computed,onMounted } from 'vue'; import { ElMessageBox,ElMessage } from 'element-plus'
import { dropCourse,getMySelection } from '@/api/teaching.js'; import { getStoredCurrentUser } from '@/utils/authSession.js'
const wl=['一','二','三','四','五','六','日']; const sem=ref('2025-2026-1'); const week=ref(false); const selected=ref([])
const uid=computed(()=>{const u=getStoredCurrentUser();return u?.user?.userId||1})
const load=async()=>{try{const r=await getMySelection(uid.value,sem.value);selected.value=(r?.data||[]).map(s=>({...s,courseName:s.course_name,courseCode:s.course_code||'-',teacherName:s.teacher_name,classroomName:s.classroom_name,weekDay:s.week_day,startPeriod:s.start_period,endPeriod:s.end_period,classification:s.classification||'-',credit:s.credit||2,currentCount:s.currentCount||0,maxCapacity:s.maxCapacity||60}))}catch{selected.value=[]}}
const fmtTime=r=>r.weekDay?'周'+wl[r.weekDay-1]+' '+r.startPeriod+'-'+r.endPeriod+'节':'-'
const gs=(d,p)=>selected.value.find(s=>s.weekDay===d&&p>=s.startPeriod&&p<=s.endPeriod)||null
const zp=p=>p<=5?'zm':p<=10?'za':'ze'
const drop=s=>{ElMessageBox.confirm(`退选《${s.courseName}》？`,'退选',{type:'warning'}).then(async()=>{try{const r=await dropCourse({studentId:uid.value,courseId:s.courseId||s.course_id,semester:sem.value});if(r?.code===0){ElMessage.success('退选成功');load()}}catch(e){ElMessage.error(e?.message||'失败')}}).catch(()=>{})}
onMounted(load)
</script>
<style scoped>
.p{padding:12px;height:100%;overflow:auto}.bar{display:flex;justify-content:space-between;align-items:center;margin-bottom:8px}
.wk{border:1px solid #e4e7ed;border-radius:4px;overflow:hidden;background:#fff}.g{display:flex;background:#f8f9fb}.gl{width:36px;text-align:center;padding:4px 0;font-size:10px;color:#888;border-right:1px solid #e4e7ed}.gd{flex:1;text-align:center;padding:4px 0;font-size:11px;font-weight:600;border-right:1px solid #e4e7ed}.gd:last-child{border-right:none}.gd.we{color:#e9a824}.r{display:flex}.rl{width:36px;display:flex;align-items:center;justify-content:center;font-size:10px;font-weight:600;border-right:1px solid #e4e7ed;border-bottom:1px solid #eee}.zm .rl{color:#d97706;background:#fffbeb}.za .rl{color:#059669;background:#f0fdf4}.ze .rl{color:#2563eb;background:#eff6ff}.c{flex:1;min-height:36px;border-right:1px solid #f5f5f5;border-bottom:1px solid #f5f5f5;position:relative;background:#fff}.c:last-child{border-right:none}.c.we{background:#fffef8}.b{position:absolute;top:1px;left:1px;right:1px;z-index:1;border-radius:3px;padding:2px 4px;color:#fff;font-size:9px;overflow:hidden;background:linear-gradient(135deg,#3B82F6,#60A5FA)}.bn{font-weight:600;line-height:1.3}.bi{opacity:.85;font-size:8px}
</style>
