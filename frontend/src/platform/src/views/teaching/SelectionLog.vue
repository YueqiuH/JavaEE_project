<template>
  <div class="p"><h4>📝 选课日志</h4>
    <el-table :data="logs" stripe size="small" style="width:100%" max-height="calc(100vh - 100px)" empty-text="暂无记录">
      <el-table-column label="时间" width="160"><template #default="{row}">{{ row.time }}</template></el-table-column>
      <el-table-column label="操作" width="60"><template #default="{row}"><el-tag :type="row.action==='选课'?'success':'danger'" size="small">{{ row.action }}</el-tag></template></el-table-column>
      <el-table-column prop="courseName" label="课程" width="130" />
      <el-table-column prop="courseCode" label="代码" width="80" />
      <el-table-column prop="teacherName" label="教师" width="80" />
      <el-table-column label="结果" width="60"><template #default="{row}"><span :style="{color:row.ok?'#059669':'#EF4444'}">{{ row.ok?'成功':'失败' }}</span></template></el-table-column>
      <el-table-column prop="msg" label="备注" min-width="150" />
    </el-table>
  </div>
</template>
<script setup>
import { ref } from 'vue'
const logs = ref(JSON.parse(localStorage.getItem('selection_logs')||'[]'))
</script>
<style scoped>.p{padding:12px;height:100%;overflow:auto}</style>
