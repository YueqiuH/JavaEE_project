<template>
  <section class="lab-workspace">
    <header class="workspace-header">
      <div>
        <p class="eyebrow">学生事务</p>
        <h1>{{ isManager ? '实验室开放管理' : '实验室当日预约' }}</h1>
        <p>{{ isManager ? '维护实验室容量与开放状态，查看学生使用情况' : '预约今日使用名额并完成签到、签退' }}</p>
      </div>
      <el-button v-if="isManager && activeTab === 'labs' && canManageLab" type="primary" :icon="Plus" @click="openLabDialog()">新建实验室</el-button>
    </header>

    <el-result v-if="roleResolved && !isStudent && !isManager" icon="warning" title="当前账号无实验室预约权限" />

    <div v-else class="surface-panel data-panel">
      <el-tabs v-model="activeTab" class="workspace-tabs" @tab-change="resetAndLoad">
        <template v-if="isStudent">
          <el-tab-pane name="labs"><template #label><span class="tab-label"><el-icon><OfficeBuilding /></el-icon>开放实验室</span></template></el-tab-pane>
          <el-tab-pane name="bookings"><template #label><span class="tab-label"><el-icon><Calendar /></el-icon>我的预约</span></template></el-tab-pane>
          <el-tab-pane name="notices"><template #label><span class="tab-label"><el-icon><Bell /></el-icon>预约通知</span></template></el-tab-pane>
        </template>
        <template v-else-if="isManager">
          <el-tab-pane name="labs"><template #label><span class="tab-label"><el-icon><OfficeBuilding /></el-icon>实验室管理</span></template></el-tab-pane>
          <el-tab-pane name="managed"><template #label><span class="tab-label"><el-icon><Calendar /></el-icon>预约记录</span></template></el-tab-pane>
        </template>
      </el-tabs>

      <div class="panel-toolbar">
        <div><h2>{{ panelTitle }}</h2><span>共 {{ total }} 项</span></div>
        <div class="filter-actions">
          <el-select v-if="['bookings','managed'].includes(activeTab)" v-model="statusFilter" placeholder="全部状态" clearable @change="resetPageAndLoad">
            <el-option v-for="item in bookingStatuses" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-else-if="isManager && activeTab === 'labs'" v-model="statusFilter" placeholder="全部状态" clearable @change="resetPageAndLoad"><el-option label="开放" value="OPEN" /><el-option label="关闭" value="MAINTENANCE" /></el-select>
          <el-select v-else-if="activeTab === 'notices'" v-model="noticeFilter" placeholder="全部通知" clearable @change="resetPageAndLoad"><el-option label="未读" :value="0" /><el-option label="已读" :value="1" /></el-select>
          <el-tooltip content="刷新列表"><el-button :icon="Refresh" circle aria-label="刷新列表" @click="loadData" /></el-tooltip>
        </div>
      </div>

      <el-alert v-if="loadError" title="数据加载失败，请稍后重试" type="error" show-icon :closable="false"><template #default><el-button link type="primary" @click="loadData">重新加载</el-button></template></el-alert>

      <template v-if="activeTab === 'labs'">
        <el-table v-if="loading || records.length" v-loading="loading" :data="records" row-key="labId" class="desktop-table" @row-click="openLabDetail">
          <el-table-column label="实验室" min-width="230"><template #default="{ row }"><strong>{{ row.labName }}</strong><small>{{ row.labNo }} · {{ row.location }}</small></template></el-table-column>
          <el-table-column label="容量" width="100"><template #default="{ row }">{{ row.capacity }} 人</template></el-table-column>
          <el-table-column label="今日占用" width="120"><template #default="{ row }">{{ row.activeBookingCount || 0 }} 人</template></el-table-column>
          <el-table-column label="剩余名额" width="120"><template #default="{ row }"><strong :class="{ 'capacity-full': remaining(row) === 0 }">{{ remaining(row) }}</strong></template></el-table-column>
          <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="labStatusMeta(row.status).type">{{ labStatusMeta(row.status).label }}</el-tag></template></el-table-column>
          <el-table-column label="操作" :width="isManager ? 170 : 190" fixed="right"><template #default="{ row }"><div class="row-actions" @click.stop><el-button link :icon="View" @click="openLabDetail(row)">查看</el-button><el-button v-if="isStudent" link type="primary" :icon="Calendar" :disabled="!canReserve(row)" @click="openBookingDialog(row)">预约今日</el-button><el-button v-else-if="canManageLab" link type="primary" :icon="EditPen" @click="openLabDialog(row)">修改</el-button></div></template></el-table-column>
        </el-table>
        <div class="mobile-records">
          <article v-for="row in records" :key="row.labId" class="mobile-record"><span class="record-top"><strong>{{ row.labName }}</strong><el-tag :type="labStatusMeta(row.status).type" size="small">{{ labStatusMeta(row.status).label }}</el-tag></span><span>{{ row.location }}</span><small>容量 {{ row.capacity }} · 今日占用 {{ row.activeBookingCount || 0 }} · 剩余 {{ remaining(row) }}</small><div class="mobile-actions"><el-button size="small" :icon="View" @click="openLabDetail(row)">查看</el-button><el-button v-if="isStudent" size="small" type="primary" :icon="Calendar" :disabled="!canReserve(row)" @click="openBookingDialog(row)">预约今日</el-button><el-button v-else-if="canManageLab" size="small" :icon="EditPen" @click="openLabDialog(row)">修改</el-button></div></article>
          <el-empty v-if="!loading && !records.length" class="mobile-empty" :description="emptyText" />
        </div>
      </template>

      <template v-else-if="activeTab === 'bookings' || activeTab === 'managed'">
        <el-table v-if="loading || records.length" v-loading="loading" :data="records" row-key="bookingId" class="desktop-table" @row-click="openBookingDetail">
          <el-table-column label="实验室" min-width="220"><template #default="{ row }"><strong>{{ row.labName }}</strong><small>{{ row.location }} · {{ row.bookingNo }}</small></template></el-table-column>
          <el-table-column v-if="isManager" label="预约学生" width="150"><template #default="{ row }">{{ row.studentName }}<small>{{ row.studentNo }}</small></template></el-table-column>
          <el-table-column label="使用日期" width="135"><template #default="{ row }">{{ formatDate(row.bookingDate) }}</template></el-table-column>
          <el-table-column label="状态" width="105"><template #default="{ row }"><el-tag :type="bookingStatusMeta(row.status).type">{{ bookingStatusMeta(row.status).label }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="230" fixed="right"><template #default="{ row }"><div class="row-actions" @click.stop><el-button link :icon="View" @click="openBookingDetail(row)">详情</el-button><el-button v-if="isStudent && row.status === 'RESERVED' && canCheckIn" link type="primary" :icon="CircleCheck" @click="checkIn(row)">签到</el-button><el-button v-if="isStudent && row.status === 'RESERVED' && canCancel" link type="danger" :icon="Close" @click="cancel(row)">取消</el-button><el-button v-if="isStudent && row.status === 'CHECKED_IN' && canCheckOut" link type="primary" :icon="SwitchButton" @click="checkOut(row)">签退</el-button><el-button v-if="isManager && row.status === 'CHECKED_IN'" link type="danger" :icon="SwitchButton" @click="forceCheckOut(row)">强制签退</el-button></div></template></el-table-column>
        </el-table>
        <div class="mobile-records"><article v-for="row in records" :key="row.bookingId" class="mobile-record"><span class="record-top"><strong>{{ row.labName }}</strong><el-tag :type="bookingStatusMeta(row.status).type" size="small">{{ bookingStatusMeta(row.status).label }}</el-tag></span><span>{{ formatDate(row.bookingDate) }}<template v-if="isManager"> · {{ row.studentName }}</template></span><small>{{ row.bookingNo }}</small><div class="mobile-actions"><el-button size="small" @click="openBookingDetail(row)">详情</el-button><el-button v-if="isStudent && row.status === 'RESERVED'" size="small" type="primary" @click="checkIn(row)">签到</el-button><el-button v-if="isStudent && row.status === 'CHECKED_IN'" size="small" type="primary" @click="checkOut(row)">签退</el-button><el-button v-if="isManager && row.status === 'CHECKED_IN'" size="small" type="danger" plain @click="forceCheckOut(row)">强制签退</el-button></div></article><el-empty v-if="!loading && !records.length" class="mobile-empty" :description="emptyText" /></div>
      </template>

      <template v-else>
        <el-table v-if="loading || records.length" v-loading="loading" :data="records" row-key="noticeId" class="desktop-table" @row-click="readNotice"><el-table-column label="通知" min-width="280"><template #default="{ row }"><strong :class="{ unread: row.isRead === 0 }">{{ row.title }}</strong><small>{{ row.content }}</small></template></el-table-column><el-table-column prop="bookingNo" label="预约编号" width="190" /><el-table-column label="发送时间" width="180"><template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template></el-table-column><el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="row.isRead ? 'info' : 'primary'">{{ row.isRead ? '已读' : '未读' }}</el-tag></template></el-table-column></el-table>
        <div class="mobile-records"><button v-for="row in records" :key="row.noticeId" type="button" class="mobile-record" @click="readNotice(row)"><span class="record-top"><strong :class="{ unread: row.isRead === 0 }">{{ row.title }}</strong><el-tag :type="row.isRead ? 'info' : 'primary'" size="small">{{ row.isRead ? '已读' : '未读' }}</el-tag></span><span>{{ row.content }}</span><small>{{ formatDateTime(row.createdAt) }}</small></button><el-empty v-if="!loading && !records.length" class="mobile-empty" :description="emptyText" /></div>
      </template>

      <el-empty v-if="!loading && !records.length" class="desktop-empty" :description="emptyText" />
      <el-pagination v-if="total > pageSize" v-model:current-page="page" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="loadData" />
    </div>

    <el-dialog v-model="labDialogOpen" :title="editingLab ? '修改实验室' : '新建实验室'" width="min(580px, calc(100vw - 32px))" destroy-on-close><el-form ref="labFormRef" :model="labForm" :rules="labRules" label-position="top"><el-form-item label="实验室名称" prop="labName"><el-input v-model="labForm.labName" maxlength="64" /></el-form-item><el-form-item label="位置" prop="location"><el-input v-model="labForm.location" maxlength="128" /></el-form-item><div class="form-grid"><el-form-item label="最大容量" prop="capacity"><el-input-number v-model="labForm.capacity" :min="1" :max="500" class="full-width" /></el-form-item><el-form-item label="开放状态" prop="status"><el-segmented v-model="labForm.status" :options="labStatusOptions" class="full-width" /></el-form-item></div><el-form-item label="使用说明"><el-input v-model="labForm.description" type="textarea" :rows="4" maxlength="1000" show-word-limit /></el-form-item></el-form><template #footer><el-button @click="labDialogOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveLab">保存</el-button></template></el-dialog>

    <el-dialog v-model="bookingDialogOpen" title="预约今日实验室" width="min(500px, calc(100vw - 32px))" destroy-on-close><div v-if="bookingLab" class="booking-summary"><div><strong>{{ bookingLab.labName }}</strong><span>{{ bookingLab.location }}</span></div><div><span>剩余名额</span><strong>{{ remaining(bookingLab) }} / {{ bookingLab.capacity }}</strong></div></div><el-alert title="预约成功后请在30分钟内签到，签退后名额立即释放" type="info" show-icon :closable="false" /><el-form ref="bookingFormRef" :model="bookingForm" :rules="bookingRules" label-position="top" class="booking-form"><el-form-item label="使用用途" prop="purpose"><el-input v-model="bookingForm.purpose" type="textarea" :rows="4" maxlength="256" show-word-limit placeholder="请填写课程实践、项目开发等用途" /></el-form-item></el-form><template #footer><el-button @click="bookingDialogOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveBooking">确认预约</el-button></template></el-dialog>

    <el-drawer v-model="labDetailOpen" title="实验室详情" size="min(540px, 100vw)" destroy-on-close><div v-if="detailLab" class="detail-content"><div class="detail-title"><div><small>{{ detailLab.labNo }}</small><h2>{{ detailLab.labName }}</h2><span>{{ detailLab.location }}</span></div><el-tag :type="labStatusMeta(detailLab.status).type">{{ labStatusMeta(detailLab.status).label }}</el-tag></div><section class="capacity-band"><div><span>最大容量</span><strong>{{ detailLab.capacity }}</strong></div><div><span>今日占用</span><strong>{{ detailLab.activeBookingCount || 0 }}</strong></div><div><span>剩余名额</span><strong>{{ remaining(detailLab) }}</strong></div></section><dl><dt>负责人</dt><dd>{{ detailLab.managerName || '--' }}</dd><dt>使用说明</dt><dd class="pre-wrap">{{ detailLab.description || '暂无说明' }}</dd></dl><div class="drawer-actions"><el-button v-if="isStudent" type="primary" :icon="Calendar" :disabled="!canReserve(detailLab)" @click="openBookingDialog(detailLab)">预约今日</el-button><el-button v-if="isManager && canManageLab" :icon="EditPen" @click="openLabDialog(detailLab)">修改实验室</el-button></div></div></el-drawer>

    <el-drawer v-model="bookingDetailOpen" title="预约详情" size="min(540px, 100vw)" destroy-on-close><div v-if="bookingDetail" class="detail-content"><div class="detail-title"><div><small>{{ bookingDetail.bookingNo }}</small><h2>{{ bookingDetail.labName }}</h2><span>{{ bookingDetail.location }}</span></div><el-tag :type="bookingStatusMeta(bookingDetail.status).type">{{ bookingStatusMeta(bookingDetail.status).label }}</el-tag></div><dl><template v-if="isManager"><dt>预约学生</dt><dd>{{ bookingDetail.studentName }} · {{ bookingDetail.studentNo }}</dd></template><dt>使用日期</dt><dd>{{ formatDate(bookingDetail.bookingDate) }}</dd><dt>使用用途</dt><dd class="pre-wrap">{{ bookingDetail.purpose }}</dd><dt>预约时间</dt><dd>{{ formatDateTime(bookingDetail.createTime) }}</dd><template v-if="bookingDetail.status === 'RESERVED'"><dt>签到期限</dt><dd>{{ formatDateTime(bookingDetail.expiresAt) }}</dd></template><template v-if="bookingDetail.checkInAt"><dt>签到时间</dt><dd>{{ formatDateTime(bookingDetail.checkInAt) }}</dd></template><template v-if="bookingDetail.checkOutAt"><dt>签退时间</dt><dd>{{ formatDateTime(bookingDetail.checkOutAt) }}</dd></template></dl><div class="drawer-actions"><el-button v-if="isStudent && bookingDetail.status === 'RESERVED'" type="danger" plain @click="cancel(bookingDetail)">取消预约</el-button><el-button v-if="isStudent && bookingDetail.status === 'RESERVED'" type="primary" @click="checkIn(bookingDetail)">签到</el-button><el-button v-if="isStudent && bookingDetail.status === 'CHECKED_IN'" type="primary" @click="checkOut(bookingDetail)">签退</el-button><el-button v-if="isManager && bookingDetail.status === 'CHECKED_IN'" type="danger" plain @click="forceCheckOut(bookingDetail)">强制签退</el-button></div></div></el-drawer>
  </section>
</template>

<script setup>
import { computed, inject, reactive, ref, watch } from 'vue'
import { Bell, Calendar, CircleCheck, Close, EditPen, OfficeBuilding, Plus, Refresh, SwitchButton, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelLabBooking, checkInLabBooking, checkOutLabBooking, completeLabBooking, createLab, createLabBooking, getLab, getLabBooking, listLabs, listManagedLabBookings, listMyLabBookingNotices, listMyLabBookings, markLabBookingNoticeRead, updateLab } from '@/api/student.js'

const currentUser = inject('currentUser', ref(null))
const roles = computed(() => new Set(currentUser.value?.roles || []))
const permissions = computed(() => new Set(currentUser.value?.permissions || []))
const roleResolved = computed(() => Boolean(currentUser.value))
const isStudent = computed(() => roles.value.has('STUDENT') && permissions.value.has('lab:read'))
const isManager = computed(() => (roles.value.has('TEACHER') || roles.value.has('COUNSELOR') || roles.value.has('ADMIN')) && permissions.value.has('lab:read'))
const canManageLab = computed(() => permissions.value.has('lab:manage-self'))
const canCancel = computed(() => permissions.value.has('lab:booking:cancel-self'))
const canCheckIn = computed(() => permissions.value.has('lab:booking:check-in-self'))
const canCheckOut = computed(() => permissions.value.has('lab:booking:check-out-self'))

const activeTab = ref('labs'), records = ref([]), total = ref(0), page = ref(1), loading = ref(false), saving = ref(false), loadError = ref(false)
const pageSize = 10, statusFilter = ref(''), noticeFilter = ref(null)
const bookingStatuses = [{ value: 'RESERVED', label: '待签到' }, { value: 'CHECKED_IN', label: '使用中' }, { value: 'CHECKED_OUT', label: '已签退' }, { value: 'CANCELLED', label: '已取消' }, { value: 'EXPIRED', label: '已过期' }]
const labStatusOptions = [{ label: '开放', value: 1 }, { label: '关闭', value: 0 }]
const labStatusMap = { OPEN: { label: '开放', type: 'success' }, MAINTENANCE: { label: '关闭', type: 'info' } }
const bookingStatusMap = { RESERVED: { label: '待签到', type: 'warning' }, CHECKED_IN: { label: '使用中', type: 'primary' }, CHECKED_OUT: { label: '已签退', type: 'success' }, CANCELLED: { label: '已取消', type: 'info' }, EXPIRED: { label: '已过期', type: 'info' } }
const labStatusMeta = (status) => labStatusMap[status] || { label: status || '未知', type: 'info' }
const bookingStatusMeta = (status) => bookingStatusMap[status] || { label: status || '未知', type: 'info' }
const panelTitle = computed(() => ({ labs: isManager.value ? '我负责的实验室' : '今日开放实验室', bookings: '我的预约记录', notices: '预约通知', managed: '学生预约记录' }[activeTab.value]))
const emptyText = computed(() => ({ labs: isManager.value ? '暂未维护实验室' : '暂无开放实验室', bookings: '今日暂无实验室预约', notices: '暂无预约通知', managed: '暂无学生预约记录' }[activeTab.value]))
const remaining = (lab) => Math.max(Number(lab.capacity || 0) - Number(lab.activeBookingCount || 0), 0)
const canReserve = (lab) => isStudent.value && permissions.value.has('lab:booking:create') && lab.status === 'OPEN' && remaining(lab) > 0
const formatDate = (value) => value ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium' }).format(new Date(`${value}T00:00:00`)) : '--'
const formatDateTime = (value) => value ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '--'

const loadData = async () => { if (!isStudent.value && !isManager.value) return; loading.value = true; loadError.value = false; try { let result; if (activeTab.value === 'labs') result = await listLabs({ page: page.value, size: pageSize, status: statusFilter.value || undefined }); else if (activeTab.value === 'bookings') result = await listMyLabBookings({ page: page.value, size: pageSize, status: statusFilter.value || undefined }); else if (activeTab.value === 'managed') result = await listManagedLabBookings({ page: page.value, size: pageSize, status: statusFilter.value || undefined }); else result = await listMyLabBookingNotices({ page: page.value, size: pageSize, isRead: noticeFilter.value ?? undefined }); records.value = result.data.records || []; total.value = result.data.total || 0 } catch { loadError.value = true; records.value = []; total.value = 0 } finally { loading.value = false } }
const resetPageAndLoad = () => { page.value = 1; loadData() }
const resetAndLoad = () => { statusFilter.value = ''; noticeFilter.value = null; resetPageAndLoad() }

const labDialogOpen = ref(false), labFormRef = ref(), editingLab = ref(null), labForm = reactive({ labName: '', location: '', capacity: 30, description: '', status: 1 })
const labRules = { labName: [{ required: true, message: '请填写实验室名称', trigger: 'blur' }], location: [{ required: true, message: '请填写实验室位置', trigger: 'blur' }], capacity: [{ required: true, message: '请填写最大容量', trigger: 'change' }], status: [{ required: true, message: '请选择开放状态', trigger: 'change' }] }
const openLabDialog = (row = null) => { editingLab.value = row; Object.assign(labForm, row ? { labName: row.labName, location: row.location, capacity: row.capacity, description: row.description || '', status: row.statusCode } : { labName: '', location: '', capacity: 30, description: '', status: 1 }); labDialogOpen.value = true }
const saveLab = async () => { await labFormRef.value.validate(); saving.value = true; try { editingLab.value ? await updateLab(editingLab.value.labId, { ...labForm, description: labForm.description || null }) : await createLab({ ...labForm, description: labForm.description || null }); ElMessage.success(editingLab.value ? '实验室已更新' : '实验室已创建'); labDialogOpen.value = false; labDetailOpen.value = false; await loadData() } finally { saving.value = false } }

const labDetailOpen = ref(false), detailLab = ref(null)
const openLabDetail = async (row) => { const result = await getLab(row.labId); detailLab.value = result.data; labDetailOpen.value = true }
const bookingDialogOpen = ref(false), bookingFormRef = ref(), bookingLab = ref(null), bookingForm = reactive({ purpose: '' })
const bookingRules = { purpose: [{ required: true, message: '请填写使用用途', trigger: 'blur' }] }
const openBookingDialog = (lab) => { bookingLab.value = lab; bookingForm.purpose = ''; bookingDialogOpen.value = true }
const saveBooking = async () => { await bookingFormRef.value.validate(); saving.value = true; try { await createLabBooking({ labId: bookingLab.value.labId, purpose: bookingForm.purpose }); ElMessage.success('预约成功，请在30分钟内签到'); bookingDialogOpen.value = false; labDetailOpen.value = false; activeTab.value = 'bookings'; await loadData() } finally { saving.value = false } }

const bookingDetailOpen = ref(false), bookingDetail = ref(null)
const openBookingDetail = async (row) => { const result = await getLabBooking(row.bookingId); bookingDetail.value = result.data; bookingDetailOpen.value = true }
const cancel = async (row) => { await ElMessageBox.confirm('确认取消该预约并释放名额？', '取消预约', { type: 'warning' }); await cancelLabBooking(row.bookingId); ElMessage.success('预约已取消'); bookingDetailOpen.value = false; await loadData() }
const checkIn = async (row) => { await checkInLabBooking(row.bookingId); ElMessage.success('签到成功'); bookingDetailOpen.value = false; await loadData() }
const checkOut = async (row) => { await ElMessageBox.confirm('确认签退并释放实验室名额？', '实验室签退', { type: 'info' }); await checkOutLabBooking(row.bookingId); ElMessage.success('签退成功'); bookingDetailOpen.value = false; await loadData() }
const forceCheckOut = async (row) => { await ElMessageBox.confirm('确认强制签退该学生并释放名额？', '强制签退', { type: 'warning' }); await completeLabBooking(row.bookingId); ElMessage.success('已强制签退'); bookingDetailOpen.value = false; await loadData() }
const readNotice = async (row) => { if (row.isRead === 0) { await markLabBookingNoticeRead(row.noticeId); row.isRead = 1 } ElMessage.info(row.content) }

watch([isStudent, isManager], ([student, manager]) => { if (student || manager) { activeTab.value = 'labs'; resetAndLoad() } }, { immediate: true })
</script>

<style scoped>
.lab-workspace{width:min(1180px,calc(100% - 56px));margin:0 auto;padding:32px 0 48px}.workspace-header{display:flex;align-items:flex-end;justify-content:space-between;gap:24px;margin-bottom:24px}.workspace-header h1{margin:2px 0 4px;font-size:26px;font-weight:650;letter-spacing:0}.workspace-header p{margin:0;color:var(--color-text-secondary)}.eyebrow{color:var(--color-brand-600)!important;font-size:12px;font-weight:700}.data-panel{min-height:430px;padding:0 22px 20px}.workspace-tabs :deep(.el-tabs__header){margin:0}.workspace-tabs :deep(.el-tabs__content){display:none}.tab-label{display:inline-flex;align-items:center;gap:6px}.panel-toolbar{display:flex;min-height:70px;align-items:center;justify-content:space-between;gap:20px}.panel-toolbar>div:first-child{display:flex;align-items:baseline;gap:10px}.panel-toolbar h2{margin:0;font-size:17px}.panel-toolbar span{color:var(--color-text-tertiary);font-size:13px}.filter-actions{display:flex;align-items:center;gap:8px}.filter-actions .el-select{width:145px}.desktop-table :deep(.el-table__row){cursor:pointer}.desktop-table :deep(th.el-table__cell){color:var(--color-text-secondary);background:#f8fafb}.desktop-table small{display:block;color:var(--color-text-tertiary)}.row-actions{display:flex;align-items:center;white-space:nowrap}.capacity-full{color:var(--color-danger)}.desktop-empty{padding:48px 0}.mobile-records{display:none}.el-pagination{justify-content:flex-end;margin-top:20px}.full-width{width:100%!important}.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:16px}.booking-summary{display:flex;align-items:center;justify-content:space-between;gap:20px;margin:-4px 0 16px;padding:14px;background:var(--color-brand-50);border-left:3px solid var(--color-brand-600)}.booking-summary>div{display:flex;flex-direction:column}.booking-summary>div:last-child{text-align:right}.booking-summary span{color:var(--color-text-secondary);font-size:13px}.booking-form{margin-top:18px}.detail-content{padding:0 4px 24px}.detail-title{display:flex;align-items:flex-start;justify-content:space-between;gap:16px;padding-bottom:20px;border-bottom:1px solid var(--color-border-light)}.detail-title h2{margin:4px 0;font-size:20px}.detail-title span,.detail-title small{color:var(--color-text-tertiary)}.capacity-band{display:grid;grid-template-columns:repeat(3,1fr);margin:24px 0;border:1px solid var(--color-border-light);border-radius:6px}.capacity-band div{display:flex;flex-direction:column;gap:4px;padding:14px;border-right:1px solid var(--color-border-light)}.capacity-band div:last-child{border-right:0}.capacity-band span{color:var(--color-text-tertiary);font-size:12px}.capacity-band strong{font-size:18px}.detail-content dl{display:grid;grid-template-columns:88px 1fr;gap:18px 12px;margin:24px 0}.detail-content dt{color:var(--color-text-tertiary)}.detail-content dd{min-width:0;margin:0}.pre-wrap{white-space:pre-wrap}.drawer-actions{display:flex;justify-content:flex-end;gap:8px;padding-top:18px;border-top:1px solid var(--color-border-light)}.unread{font-weight:700}.mobile-actions{display:flex;gap:8px;margin-top:8px}
@media(max-width:991px){.lab-workspace{width:calc(100% - 40px)}}
@media(max-width:767px){.lab-workspace{width:calc(100% - 24px);padding:22px 0 36px}.workspace-header{align-items:flex-start;margin-bottom:18px}.workspace-header h1{font-size:21px}.workspace-header>div>p:last-child{font-size:13px}.data-panel{padding:0;background:transparent;border:0}.workspace-tabs{padding:0 4px}.workspace-tabs :deep(.el-tabs__nav){display:flex;width:100%}.workspace-tabs :deep(.el-tabs__item){min-width:0;flex:1;justify-content:center;padding:0 4px}.tab-label{gap:4px;font-size:12px}.panel-toolbar{min-height:64px}.panel-toolbar>div:first-child{display:block}.filter-actions .el-select{width:118px}.desktop-table,.desktop-empty{display:none}.mobile-records{display:flex;min-height:170px;flex-direction:column;gap:8px}.mobile-record{display:flex;width:100%;flex-direction:column;gap:4px;padding:14px;color:var(--color-text-secondary);text-align:left;background:#fff;border:1px solid var(--color-border-light);border-radius:6px}.record-top{display:flex;align-items:flex-start;justify-content:space-between;gap:10px;color:var(--color-text-primary)}.record-top strong{min-width:0;flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.mobile-record small{color:var(--color-text-tertiary)}.mobile-empty{width:100%;background:#fff;border:1px solid var(--color-border-light);border-radius:6px}.el-pagination{justify-content:center}.form-grid{grid-template-columns:1fr}.capacity-band div{padding:10px}.detail-content dl{grid-template-columns:76px 1fr}.drawer-actions{flex-wrap:wrap}}
</style>
