<template>
  <main class="workbench">
    <div class="container">
      <header class="workbench-header">
        <div><p>{{ formattedDate }}</p><h1>{{ greeting }}，{{ displayName }}</h1><span>这里汇总了与你相关的日程、待办和业务进度。</span></div>
        <el-button type="primary" :icon="Plus" @click="planDialog = true">新建计划</el-button>
      </header>

      <section class="metrics" aria-label="工作概览">
        <button v-for="metric in metrics" :key="metric.label" type="button" @click="metric.action()">
          <span class="metric-icon" :style="{ color: metric.color, background: metric.soft }"><el-icon><component :is="metric.icon" /></el-icon></span>
          <span><small>{{ metric.label }}</small><strong>{{ metric.value }}</strong><em>{{ metric.note }}</em></span>
          <el-icon class="metric-arrow"><Right /></el-icon>
        </button>
      </section>

      <div class="workbench-grid">
        <section class="todo-section surface-panel">
          <div class="panel-heading"><div><h2>我的待办</h2><p>按优先级处理当前事项</p></div><el-button text @click="router.push({ name: 'documentOA' })">查看全部</el-button></div>
          <div class="todo-tabs"><button v-for="tab in todoTabs" :key="tab.key" type="button" :class="{ active: todoTab === tab.key }" @click="todoTab = tab.key">{{ tab.label }} <span>{{ tab.count }}</span></button></div>
          <div class="todo-list">
            <button v-for="item in filteredTodos" :key="item.id" type="button" @click="router.push({ name: item.routeName })">
              <span class="todo-priority" :class="item.priority"></span>
              <span class="todo-copy"><strong>{{ item.title }}</strong><small>{{ item.source }} · {{ item.time }}</small></span>
              <el-tag size="small" effect="plain" :type="item.tagType">{{ item.status }}</el-tag>
              <el-icon><Right /></el-icon>
            </button>
          </div>
        </section>

        <section class="agenda-section surface-panel">
          <div class="panel-heading"><div><h2>今日日程</h2><p>7月16日 · 星期四</p></div><el-button text :icon="Calendar" @click="router.push({ name: 'courseSchedule' })">完整日历</el-button></div>
          <div class="timeline">
            <div v-for="event in agenda" :key="event.time" class="timeline-item">
              <time>{{ event.time }}</time><span :style="{ background: event.color }"></span><div><strong>{{ event.title }}</strong><small>{{ event.place }}</small></div>
            </div>
          </div>
        </section>
      </div>

      <section class="progress-section">
        <div class="section-heading"><div><h2>我的业务进度</h2><p>最近提交的申请与办理事项</p></div></div>
        <div class="progress-list">
          <article v-for="item in progressItems" :key="item.title">
            <div class="progress-top"><span class="progress-icon"><el-icon><component :is="item.icon" /></el-icon></span><div><strong>{{ item.title }}</strong><small>{{ item.number }}</small></div><el-tag :type="item.type" effect="plain">{{ item.status }}</el-tag></div>
            <el-progress :percentage="item.progress" :stroke-width="8" :show-text="false" />
            <div class="progress-meta"><span>{{ item.current }}</span><span>{{ item.updated }}</span></div>
          </article>
        </div>
      </section>
    </div>

    <el-dialog v-model="planDialog" title="新建工作计划" width="min(520px, calc(100vw - 32px))">
      <el-form label-position="top"><el-form-item label="计划标题"><el-input v-model="planForm.title" placeholder="输入计划标题" /></el-form-item><el-form-item label="完成日期"><el-date-picker v-model="planForm.date" type="date" style="width:100%" placeholder="选择日期" /></el-form-item><el-form-item label="优先级"><el-radio-group v-model="planForm.priority"><el-radio-button value="normal">普通</el-radio-button><el-radio-button value="important">重要</el-radio-button><el-radio-button value="urgent">紧急</el-radio-button></el-radio-group></el-form-item></el-form>
      <template #footer><el-button @click="planDialog=false">取消</el-button><el-button type="primary" @click="savePlan">保存计划</el-button></template>
    </el-dialog>
  </main>
</template>

<script setup>
import { computed, inject, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, Checked, Document, Plus, Reading, Right, Tickets } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const currentUser = inject('currentUser', ref(null))
const todoTab = ref('all')
const planDialog = ref(false)
const planForm = reactive({ title: '', date: '', priority: 'normal' })
const displayName = computed(() => currentUser.value?.user?.realName || currentUser.value?.user?.username || '同学')
const greeting = computed(() => new Date().getHours() < 12 ? '上午好' : new Date().getHours() < 18 ? '下午好' : '晚上好')
const formattedDate = new Intl.DateTimeFormat('zh-CN', { year:'numeric', month:'long', day:'numeric', weekday:'long' }).format(new Date())
const metrics = [
  { label:'待办事项', value:'6', note:'2 项即将超时', icon:'Checked', color:'#c2413b', soft:'#fcebea', action:()=>document.querySelector('.todo-section')?.scrollIntoView({behavior:'smooth'}) },
  { label:'今日课程', value:'4', note:'下一节 10:20', icon:'Reading', color:'#3973b7', soft:'#eaf2fb', action:()=>router.push({name:'courseSchedule'}) },
  { label:'未读通知', value:'3', note:'1 条重要通知', icon:'Bell', color:'#c77800', soft:'#fff4df', action:()=>router.push({name:'meetingNotice'}) },
  { label:'办理中', value:'2', note:'进度均正常', icon:'Loading', color:'#16865b', soft:'#e8f5ef', action:()=>document.querySelector('.progress-section')?.scrollIntoView({behavior:'smooth'}) },
]
const todoTabs = [{key:'all',label:'全部',count:6},{key:'approval',label:'审批',count:3},{key:'study',label:'教学',count:2},{key:'notice',label:'通知',count:1}]
const todos = [
  {id:1,title:'2026 年度设备购置申请待确认',source:'固定资产管理',time:'30分钟前',status:'即将超时',tagType:'danger',priority:'urgent',group:'approval',routeName:'assetManagement'},
  {id:2,title:'本科毕业设计中期材料审核',source:'毕业设计管理',time:'1小时前',status:'待审核',tagType:'warning',priority:'important',group:'approval',routeName:'graduationDesign'},
  {id:3,title:'《数据结构》课程评教',source:'评教反馈',time:'今天 18:00 截止',status:'待完成',tagType:'warning',priority:'important',group:'study',routeName:'teachingEvaluation'},
  {id:4,title:'第二轮选课结果确认',source:'选课与容量',time:'昨天',status:'待确认',tagType:'info',priority:'normal',group:'study',routeName:'courseSelection'},
  {id:5,title:'关于暑期实验室开放的通知',source:'会议与通知',time:'昨天',status:'未读',tagType:'info',priority:'normal',group:'notice',routeName:'meetingNotice'},
]
const filteredTodos = computed(()=>todoTab.value==='all'?todos:todos.filter(item=>item.group===todoTab.value))
const agenda = [
  {time:'08:30',title:'数据结构',place:'A教学楼 302',color:'#3973b7'},
  {time:'10:20',title:'Java Web 应用开发',place:'实验中心 405',color:'#16865b'},
  {time:'14:30',title:'教学工作协调会',place:'行政楼 210',color:'#c77800'},
  {time:'19:00',title:'竞赛项目组讨论',place:'线上会议',color:'#8a4d8f'},
]
const progressItems = [
  {title:'实验室预约申请',number:'LAB-20260716-018',status:'已通过',type:'success',progress:100,current:'预约成功',updated:'更新于 09:42',icon:'OfficeBuilding'},
  {title:'奖学金申请',number:'SCH-20260712-106',status:'院系审核',type:'warning',progress:62,current:'当前：院系审核',updated:'更新于昨天',icon:'Medal'},
  {title:'学籍信息修改',number:'STA-20260710-044',status:'教务复核',type:'info',progress:78,current:'当前：教务处复核',updated:'更新于 7月15日',icon:'Refresh'},
]
const savePlan=()=>{ if(!planForm.title.trim()){ElMessage.warning('请输入计划标题');return} ElMessage.success('计划已保存'); planDialog.value=false; planForm.title=''; planForm.date='' }
</script>

<style scoped>
.workbench { min-height: calc(100vh - var(--header-height)); padding: 34px 0 60px; background: var(--color-background); }
.workbench-header { display:flex; align-items:center; justify-content:space-between; gap:24px; margin-bottom:24px; }
.workbench-header p { margin:0; color:var(--color-brand-700); font-weight:600; }
.workbench-header h1 { margin:2px 0 0; font-size:28px; font-weight:600; }
.workbench-header span { color:var(--color-text-secondary); }
.metrics { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); overflow:hidden; margin-bottom:24px; background:#fff; border:1px solid var(--color-border); border-radius:8px; }
.metrics button { display:grid; min-height:112px; grid-template-columns:44px minmax(0,1fr) 16px; align-items:center; gap:12px; padding:18px; color:inherit; text-align:left; background:#fff; border:0; border-right:1px solid var(--color-border); cursor:pointer; }
.metrics button:last-child { border-right:0; }
.metrics button:hover { background:var(--color-brand-50); }
.metric-icon { display:inline-flex; width:44px; height:44px; align-items:center; justify-content:center; border-radius:8px; font-size:23px; }
.metrics button > span:nth-child(2) { display:flex; min-width:0; flex-direction:column; }
.metrics small { color:var(--color-text-secondary); }
.metrics strong { font-size:26px; line-height:1.25; font-weight:600; }
.metrics em { color:var(--color-text-tertiary); font-size:12px; font-style:normal; }
.metric-arrow { color:var(--color-text-tertiary); }
.workbench-grid { display:grid; grid-template-columns:minmax(0,1.7fr) minmax(300px,.8fr); gap:24px; }
.todo-section,.agenda-section { padding:22px 24px; }
.panel-heading { display:flex; align-items:flex-start; justify-content:space-between; gap:16px; }
.panel-heading h2 { margin:0; font-size:18px; font-weight:600; }
.panel-heading p { margin:2px 0 0; color:var(--color-text-secondary); font-size:13px; }
.todo-tabs { display:flex; gap:24px; margin:18px 0 4px; border-bottom:1px solid var(--color-border-light); }
.todo-tabs button { position:relative; min-height:38px; padding:0 0 10px; color:var(--color-text-secondary); background:transparent; border:0; cursor:pointer; }
.todo-tabs button.active { color:var(--color-brand-700); font-weight:600; }
.todo-tabs button.active::after { position:absolute; right:0; bottom:-1px; left:0; height:2px; content:''; background:var(--color-brand-700); }
.todo-tabs span { margin-left:3px; color:var(--color-text-tertiary); }
.todo-list button { display:grid; width:100%; min-height:64px; grid-template-columns:6px minmax(0,1fr) auto 18px; align-items:center; gap:12px; padding:10px 2px; color:inherit; text-align:left; background:#fff; border:0; border-bottom:1px solid var(--color-border-light); cursor:pointer; }
.todo-list button:hover { background:var(--color-brand-50); }
.todo-priority { width:6px; height:6px; background:#8a94a3; border-radius:50%; }
.todo-priority.urgent{background:var(--color-danger)} .todo-priority.important{background:var(--color-warning)}
.todo-copy { display:flex; min-width:0; flex-direction:column; }
.todo-copy strong { overflow:hidden; font-weight:500; text-overflow:ellipsis; white-space:nowrap; }
.todo-copy small { color:var(--color-text-tertiary); }
.timeline { margin-top:20px; }
.timeline-item { display:grid; min-height:72px; grid-template-columns:48px 10px minmax(0,1fr); gap:10px; }
.timeline-item time { color:var(--color-text-secondary); font-size:13px; }
.timeline-item > span { position:relative; width:8px; height:8px; margin-top:6px; border-radius:50%; }
.timeline-item > span::after { position:absolute; top:12px; bottom:-52px; left:3px; width:1px; content:''; background:var(--color-border); }
.timeline-item:last-child > span::after { display:none; }
.timeline-item div { display:flex; flex-direction:column; }
.timeline-item strong { font-weight:500; }
.timeline-item small { color:var(--color-text-tertiary); }
.progress-section { padding-top:34px; }
.progress-list { display:grid; grid-template-columns:repeat(3,minmax(0,1fr)); gap:16px; }
.progress-list article { padding:20px; background:#fff; border:1px solid var(--color-border); border-radius:8px; }
.progress-top { display:grid; grid-template-columns:40px minmax(0,1fr) auto; align-items:center; gap:10px; margin-bottom:16px; }
.progress-icon { display:inline-flex; width:40px; height:40px; align-items:center; justify-content:center; color:var(--color-brand-700); background:var(--color-brand-100); border-radius:8px; }
.progress-top div { display:flex; min-width:0; flex-direction:column; }
.progress-top strong { overflow:hidden; font-weight:600; text-overflow:ellipsis; white-space:nowrap; }
.progress-top small { color:var(--color-text-tertiary); }
.progress-meta { display:flex; justify-content:space-between; gap:12px; margin-top:8px; color:var(--color-text-tertiary); font-size:12px; }
@media(max-width:991px){.metrics{grid-template-columns:repeat(2,minmax(0,1fr))}.metrics button:nth-child(2){border-right:0}.metrics button:nth-child(-n+2){border-bottom:1px solid var(--color-border)}.workbench-grid{grid-template-columns:1fr}.progress-list{grid-template-columns:1fr}}
@media(max-width:767px){.workbench{padding-top:24px}.workbench-header{align-items:flex-start;flex-direction:column}.metrics{grid-template-columns:1fr}.metrics button{border-right:0;border-bottom:1px solid var(--color-border)}.metrics button:last-child{border-bottom:0}.todo-section,.agenda-section{padding:18px 14px}.todo-list button{grid-template-columns:6px minmax(0,1fr) 18px}.todo-list .el-tag{display:none}.todo-tabs{gap:16px;overflow-x:auto}.progress-top{grid-template-columns:40px minmax(0,1fr)}.progress-top .el-tag{grid-column:2}.progress-meta{align-items:flex-start;flex-direction:column}}
</style>
