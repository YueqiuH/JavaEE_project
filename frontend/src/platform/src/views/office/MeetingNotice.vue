<template>
  <section class="office-page">
    <header class="office-page__header">
      <div><h1>校园会议与通知</h1><p>会议发布、本人参会反馈和通知已读管理。</p></div>
      <div class="office-page__actions"><el-badge :value="unread" :hidden="!unread"><el-button @click="load">刷新通知</el-button></el-badge><el-button v-if="canManage" type="primary" @click="visible=true">发布会议</el-button></div>
    </header>

    <el-result v-if="!canMeetingSelf&&!canNotify&&!canManage" class="office-page__empty" icon="warning" title="无会议访问权限" />
    <template v-else>
      <el-card v-if="canMeetingSelf||canManage" class="office-page__section" shadow="never">
        <template #header><strong>{{ canManage ? '会议列表' : '我的会议' }}</strong></template>
        <el-table :data="meetings" empty-text="暂无会议">
          <el-table-column prop="title" label="会议主题" min-width="170" /><el-table-column prop="meetingDate" label="日期" width="120" /><el-table-column prop="startTime" label="开始" width="100" /><el-table-column prop="location" label="地点" min-width="130" />
          <el-table-column label="操作" min-width="210"><template #default="scope"><el-button v-if="canMeetingSelf" link type="success" @click="reply(scope.row,'参会')">参会</el-button><el-button v-if="canMeetingSelf" link type="warning" @click="reply(scope.row,'请假')">请假</el-button><el-button v-if="canManage" link @click="showSummary(scope.row)">反馈汇总</el-button></template></el-table-column>
        </el-table>
      </el-card>

      <el-card v-if="canNotify" class="office-page__section" shadow="never">
        <template #header><strong>我的通知</strong></template>
        <el-table :data="notifications" empty-text="暂无通知">
          <el-table-column prop="title" label="标题" min-width="180" /><el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip /><el-table-column prop="createTime" label="时间" min-width="170" />
          <el-table-column label="状态" width="110"><template #default="scope"><el-tag :type="scope.row.isRead===1?'info':'danger'">{{ scope.row.isRead===1?'已读':'未读' }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="110"><template #default="scope"><el-button link :disabled="scope.row.isRead===1" @click="read(scope.row)">标记已读</el-button></template></el-table-column>
        </el-table>
      </el-card>
    </template>

    <el-dialog v-model="visible" title="发布会议" width="min(620px,92vw)">
      <el-form label-position="top"><el-form-item label="会议主题"><el-input v-model="form.title" /></el-form-item><el-form-item label="会议内容"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item><el-form-item label="会议日期"><el-date-picker v-model="form.meetingDate" value-format="YYYY-MM-DD" /></el-form-item><el-form-item label="起止时间"><el-time-picker v-model="timeRange" is-range value-format="HH:mm:ss" /></el-form-item><el-form-item label="会议地点"><el-input v-model="form.location" /></el-form-item><el-form-item label="参会用户 ID"><el-input v-model="attendeeText" placeholder="多个 ID 用逗号分隔，如：1,2,3" /></el-form-item></el-form>
      <template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="publish">发布</el-button></template>
    </el-dialog>
    <el-dialog v-model="summaryVisible" title="参会反馈汇总" width="420px"><el-descriptions :column="1" border><el-descriptions-item v-for="(count,status) in summaryData" :key="status" :label="status">{{ count }} 人</el-descriptions-item></el-descriptions></el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { meetingAPI } from '@/api/office.js'
import { useOfficeAccess } from '@/composables/useOfficeAccess.js'

const { hasPermission } = useOfficeAccess()
const canMeetingSelf = computed(() => hasPermission('meeting:self'))
const canManage = computed(() => hasPermission('meeting:manage'))
const canNotify = computed(() => hasPermission('notification:self:read'))
const meetings = ref([])
const notifications = ref([])
const visible = ref(false)
const summaryVisible = ref(false)
const summaryData = ref({})
const attendeeText = ref('2')
const timeRange = ref([])
const form = reactive({ title: '', content: '', meetingDate: '', location: '' })
const unread = computed(() => notifications.value.filter(item=>item.isRead===0).length)
const load = async () => { const tasks=[]; if(canManage.value)tasks.push(meetingAPI.list().then(r=>{meetings.value=r.data||[]})); else if(canMeetingSelf.value)tasks.push(meetingAPI.mine().then(r=>{meetings.value=r.data||[]})); if(canNotify.value)tasks.push(meetingAPI.notifications().then(r=>{notifications.value=r.data||[]})); await Promise.all(tasks) }
const publish = async () => { const ids=attendeeText.value.split(',').map(v=>Number(v.trim())).filter(Number.isInteger); await meetingAPI.publish({meeting:{...form,startTime:timeRange.value?.[0],endTime:timeRange.value?.[1]},attendeeIds:ids}); ElMessage.success('会议发布成功，通知已推送'); visible.value=false; await load() }
const reply = async (row,status) => { await meetingAPI.reply(row.meetingId,status); ElMessage.success(`已反馈：${status}`) }
const showSummary = async (row) => { summaryData.value=(await meetingAPI.summary(row.meetingId)).data||{}; summaryVisible.value=true }
const read = async (row) => { await meetingAPI.readNotification(row.notifyId); await load() }
onMounted(load)
</script>

<style scoped>@import '@/assets/office-workspace.css';</style>
