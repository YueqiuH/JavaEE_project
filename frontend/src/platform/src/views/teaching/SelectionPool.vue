<template>
  <div class="p">
    <div class="bar">
      <span>📚 待选课程（教务处分配的教学班）</span>
      <el-input v-model="kw" placeholder="搜索课程/代码/教师" size="small" style="width:220px" clearable />
    </div>
    <div class="hint">💡 可在课程不冲突的情况下选择同课程代码其他教学班</div>
    <el-table :data="filtered" stripe size="small" style="width:100%" max-height="calc(100vh - 160px)">
      <el-table-column type="index" width="40" />
      <el-table-column prop="courseCode" label="课程代码" width="100" />
      <el-table-column prop="courseName" label="课程名称" width="140" />
      <el-table-column label="性质" width="70"><template #default="{row}"><el-tag :type="row.classification==='必修'?'danger':'success'" size="small">{{ row.classification }}</el-tag></template></el-table-column>
      <el-table-column prop="teacherName" label="教师" width="80" />
      <el-table-column label="时间" width="150"><template #default="{row}">{{ fmtTime(row) }}</template></el-table-column>
      <el-table-column prop="classroomName" label="地点" width="90" />
      <el-table-column label="容量" width="100"><template #default="{row}"><el-progress :percentage="pct(row)" :stroke-width="5" style="width:60px;display:inline-block"/><span style="font-size:11px;margin-left:4px">{{ row.currentCount||0 }}/{{ row.maxCapacity||60 }}</span></template></el-table-column>
      <el-table-column label="操作" width="140" fixed="right"><template #default="{row}">
        <el-button size="small" type="primary" :disabled="pct(row)>=100" @click="sel(row)">选此班</el-button>
        <el-button v-if="sameCodeOthers(row).length>0" size="small" @click="showOthers(row)">换班</el-button>
      </template></el-table-column>
    </el-table>
    <el-dialog v-model="dlg" title="同课程其他教学班" width="600px">
      <el-table :data="others" stripe size="small">
        <el-table-column prop="teacherName" label="教师" /><el-table-column label="时间"><template #default="{r}">{{ fmtTime(r) }}</template></el-table-column>
        <el-table-column prop="classroomName" label="地点" /><el-table-column label="容量"><template #default="{r}">{{ r.currentCount||0 }}/{{ r.maxCapacity||60 }}</template></el-table-column>
        <el-table-column label="操作"><template #default="{r}"><el-button size="small" type="primary" @click="sel(r);dlg=false">选此班</el-button></template></el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref,computed,onMounted } from 'vue'; import { ElMessage,ElMessageBox } from 'element-plus'
import { selectCourse,getMySelection,getCourseList } from '@/api/teaching.js'; import { getStoredCurrentUser } from '@/utils/authSession.js'
const wl=['一','二','三','四','五','六','日']; const sem=ref('2025-2026-1'); const kw=ref(''); const courses=ref([]); const selected=ref([])
const dlg=ref(false); const others=ref([])
const uid=computed(()=>{const u=getStoredCurrentUser();return u?.user?.userId||1})
const filtered=computed(()=>courses.value.filter(c=>!kw.value||(c.courseName||'').includes(kw.value)||(c.courseCode||'').includes(kw.value)))
const load=async()=>{try{const[cr,sr]=await Promise.all([getCourseList(sem.value),getMySelection(uid.value,sem.value)]);courses.value=(cr?.data||[]).filter(c=>c.courseId);selected.value=sr?.data||[]}catch{courses.value=[];selected.value=[]}}
const fmtTime=r=>r.weekDay?'周'+wl[r.weekDay-1]+' '+r.startPeriod+'-'+r.endPeriod+'节':'-'
const pct=r=>Math.round((r.currentCount||0)/(r.maxCapacity||60)*100)
const sameCodeOthers=r=>courses.value.filter(c=>c.courseId!==r.courseId&&c.courseCode===r.courseCode&&(c.currentCount||0)<(c.maxCapacity||99))
const showOthers=r=>{others.value=sameCodeOthers(r);dlg.value=true}
const sel=r=>{ElMessageBox.confirm(`选择《${r.courseName}》？`,'选课').then(async()=>{try{const res=await selectCourse({studentId:uid.value,courseId:r.courseId,semester:sem.value});if(res?.code===0){ElMessage.success('选课成功');load()}}catch(e){ElMessage.error(e?.message||'失败')}}).catch(()=>{})}
onMounted(load)
</script>
<style scoped>.p{padding:12px;height:100%;overflow:auto}.bar{display:flex;justify-content:space-between;align-items:center;margin-bottom:8px}.hint{font-size:12px;color:#888;margin-bottom:6px}</style>
