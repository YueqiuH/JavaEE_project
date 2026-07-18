<template>
  <section class="lab-workspace">
    <header class="workspace-header">
      <div>
        <p class="eyebrow">学生事务</p>
        <h1>{{ isTeacher ? '实验室开放管理' : '实验室与实践预约' }}</h1>
        <p>{{ isTeacher ? '维护实验室资源与开放时间，查看学生预约结果' : '按开放时段预约实验设备或工位并接收结果通知' }}</p>
      </div>
      <el-button v-if="headerAction.visible" type="primary" :icon="Plus" @click="headerAction.run">{{ headerAction.label }}</el-button>
    </header>

    <el-result v-if="roleResolved && !isStudent && !isTeacher" icon="warning" title="当前账号无实验室预约权限" />

    <div v-else class="surface-panel data-panel">
      <el-tabs v-model="activeTab" class="workspace-tabs" @tab-change="resetAndLoad">
        <template v-if="isStudent">
          <el-tab-pane name="resources"><template #label><span class="tab-label"><el-icon><OfficeBuilding /></el-icon>可预约资源</span></template></el-tab-pane>
          <el-tab-pane name="bookings"><template #label><span class="tab-label"><el-icon><Calendar /></el-icon>我的预约</span></template></el-tab-pane>
          <el-tab-pane name="notices"><template #label><span class="tab-label"><el-icon><Bell /></el-icon>预约通知</span></template></el-tab-pane>
        </template>
        <template v-else-if="isTeacher">
          <el-tab-pane name="labs"><template #label><span class="tab-label"><el-icon><OfficeBuilding /></el-icon>实验室资源</span></template></el-tab-pane>
          <el-tab-pane name="slots"><template #label><span class="tab-label"><el-icon><Clock /></el-icon>开放时段</span></template></el-tab-pane>
          <el-tab-pane name="managed"><template #label><span class="tab-label"><el-icon><Calendar /></el-icon>预约记录</span></template></el-tab-pane>
        </template>
      </el-tabs>

      <div class="panel-toolbar">
        <div><h2>{{ panelTitle }}</h2><span>共 {{ total }} 项</span></div>
        <div class="filter-actions">
          <el-select v-if="['bookings','managed'].includes(activeTab)" v-model="statusFilter" placeholder="全部状态" clearable @change="resetPageAndLoad">
            <el-option v-for="item in bookingStatuses" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-else-if="activeTab === 'labs'" v-model="statusFilter" placeholder="全部状态" clearable @change="resetPageAndLoad">
            <el-option label="开放" value="OPEN" /><el-option label="维护中" value="MAINTENANCE" />
          </el-select>
          <el-select v-else-if="activeTab === 'notices'" v-model="noticeFilter" placeholder="全部通知" clearable @change="resetPageAndLoad">
            <el-option label="未读" :value="0" /><el-option label="已读" :value="1" />
          </el-select>
          <el-tooltip content="刷新列表" placement="top"><el-button :icon="Refresh" circle aria-label="刷新列表" @click="loadData" /></el-tooltip>
        </div>
      </div>

      <el-alert v-if="loadError" title="数据加载失败，请稍后重试" type="error" show-icon :closable="false"><template #default><el-button link type="primary" @click="loadData">重新加载</el-button></template></el-alert>

      <template v-if="activeTab === 'resources' || activeTab === 'labs'">
        <el-table v-if="loading || records.length" v-loading="loading" :data="records" row-key="labId" class="desktop-table" @row-click="openLabDetail">
          <el-table-column label="实验室" min-width="230"><template #default="{ row }"><strong>{{ row.labName }}</strong><small class="cell-subtitle">{{ row.labNo }} · {{ row.location }}</small></template></el-table-column>
          <el-table-column label="资源" width="120"><template #default="{ row }">{{ row.resourceCount }} 项</template></el-table-column>
          <el-table-column label="近期开放" min-width="160"><template #default="{ row }">{{ row.nextOpenDate ? `${formatDate(row.nextOpenDate)}起` : '暂未设置' }}<small class="cell-subtitle">{{ row.upcomingSlotCount }} 个时段</small></template></el-table-column>
          <el-table-column label="容量" width="90"><template #default="{ row }">{{ row.capacity }} 人</template></el-table-column>
          <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="labStatusMeta(row.status).type">{{ labStatusMeta(row.status).label }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="190" fixed="right"><template #default="{ row }"><div class="row-actions" @click.stop><el-button link :icon="View" @click="openLabDetail(row)">查看</el-button><el-button v-if="isTeacher && canManageLab" link type="primary" :icon="EditPen" @click="openLabDialog(row)">修改</el-button></div></template></el-table-column>
        </el-table>
        <div class="mobile-records">
          <button v-for="row in records" :key="row.labId" type="button" class="mobile-record" @click="openLabDetail(row)"><span class="record-top"><strong>{{ row.labName }}</strong><el-tag :type="labStatusMeta(row.status).type" size="small">{{ labStatusMeta(row.status).label }}</el-tag></span><span>{{ row.location }}</span><small>{{ row.resourceCount }} 项资源 · {{ row.upcomingSlotCount }} 个开放时段</small></button>
          <el-empty v-if="!loading && !records.length" class="mobile-empty" :description="emptyText" />
        </div>
      </template>

      <template v-else-if="activeTab === 'slots'">
        <el-table v-if="loading || records.length" v-loading="loading" :data="records" row-key="slotId" class="desktop-table">
          <el-table-column prop="labName" label="实验室" min-width="220"><template #default="{ row }"><strong>{{ row.labName }}</strong><small class="cell-subtitle">{{ row.location }}</small></template></el-table-column>
          <el-table-column label="开放日期" width="160"><template #default="{ row }">{{ formatDate(row.openDate) }}</template></el-table-column>
          <el-table-column label="开放节次" min-width="150"><template #default="{ row }">第 {{ row.startPeriod }}-{{ row.endPeriod }} 节</template></el-table-column>
          <el-table-column label="操作" width="170"><template #default="{ row }"><div class="row-actions"><el-button link type="primary" :icon="EditPen" @click="openSlotDialog(row)">修改</el-button><el-button link type="danger" :icon="Close" @click="removeSlot(row)">删除</el-button></div></template></el-table-column>
        </el-table>
        <div class="mobile-records"><article v-for="row in records" :key="row.slotId" class="mobile-record"><span class="record-top"><strong>{{ row.labName }}</strong><span>{{ formatDate(row.openDate) }}</span></span><span>第 {{ row.startPeriod }}-{{ row.endPeriod }} 节</span><div class="mobile-actions"><el-button size="small" @click="openSlotDialog(row)">修改</el-button><el-button size="small" type="danger" plain @click="removeSlot(row)">删除</el-button></div></article><el-empty v-if="!loading && !records.length" class="mobile-empty" :description="emptyText" /></div>
      </template>

      <template v-else-if="activeTab === 'bookings' || activeTab === 'managed'">
        <el-table v-if="loading || records.length" v-loading="loading" :data="records" row-key="bookingId" class="desktop-table" @row-click="openBookingDetail">
          <el-table-column label="预约资源" min-width="230"><template #default="{ row }"><strong>{{ row.resourceName }}</strong><small class="cell-subtitle">{{ row.labName }} · {{ row.resourceNo }}</small></template></el-table-column>
          <el-table-column v-if="isTeacher" label="预约学生" width="150"><template #default="{ row }">{{ row.studentName }}<small class="cell-subtitle">{{ row.studentNo }}</small></template></el-table-column>
          <el-table-column label="预约时间" min-width="180"><template #default="{ row }">{{ formatDate(row.bookingDate) }}<small class="cell-subtitle">第 {{ row.startPeriod }}-{{ row.endPeriod }} 节</small></template></el-table-column>
          <el-table-column label="状态" width="105"><template #default="{ row }"><el-tag :type="bookingStatusMeta(row.status).type">{{ bookingStatusMeta(row.status).label }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="180" fixed="right"><template #default="{ row }"><div class="row-actions" @click.stop><el-button link :icon="View" @click="openBookingDetail(row)">详情</el-button><el-button v-if="isStudent && row.status === 'BOOKED' && canCancelBooking" link type="danger" :icon="Close" @click="cancelBooking(row)">取消</el-button><el-button v-if="isTeacher && row.status === 'BOOKED' && canComplete(row)" link type="primary" :icon="CircleCheck" @click="completeBooking(row)">完成</el-button></div></template></el-table-column>
        </el-table>
        <div class="mobile-records"><button v-for="row in records" :key="row.bookingId" type="button" class="mobile-record" @click="openBookingDetail(row)"><span class="record-top"><strong>{{ row.resourceName }}</strong><el-tag :type="bookingStatusMeta(row.status).type" size="small">{{ bookingStatusMeta(row.status).label }}</el-tag></span><span>{{ row.labName }}</span><small>{{ formatDate(row.bookingDate) }} · 第 {{ row.startPeriod }}-{{ row.endPeriod }} 节<span v-if="isTeacher"> · {{ row.studentName }}</span></small></button><el-empty v-if="!loading && !records.length" class="mobile-empty" :description="emptyText" /></div>
      </template>

      <template v-else>
        <el-table v-if="loading || records.length" v-loading="loading" :data="records" row-key="noticeId" class="desktop-table" @row-click="readNotice">
          <el-table-column label="通知" min-width="260"><template #default="{ row }"><strong :class="{ unread: row.isRead === 0 }">{{ row.title }}</strong><small class="cell-subtitle">{{ row.content }}</small></template></el-table-column>
          <el-table-column prop="bookingNo" label="预约编号" width="190" />
          <el-table-column label="发送时间" width="180"><template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template></el-table-column>
          <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="row.isRead ? 'info' : 'primary'">{{ row.isRead ? '已读' : '未读' }}</el-tag></template></el-table-column>
        </el-table>
        <div class="mobile-records"><button v-for="row in records" :key="row.noticeId" type="button" class="mobile-record" @click="readNotice(row)"><span class="record-top"><strong :class="{ unread: row.isRead === 0 }">{{ row.title }}</strong><el-tag :type="row.isRead ? 'info' : 'primary'" size="small">{{ row.isRead ? '已读' : '未读' }}</el-tag></span><span>{{ row.content }}</span><small>{{ formatDateTime(row.createdAt) }}</small></button><el-empty v-if="!loading && !records.length" class="mobile-empty" :description="emptyText" /></div>
      </template>

      <el-empty v-if="!loading && !records.length" class="desktop-empty" :description="emptyText" />
      <el-pagination v-if="total > pageSize" v-model:current-page="page" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="loadData" />
    </div>

    <el-dialog v-model="labDialogOpen" :title="editingLab ? '修改实验室' : '新建实验室'" width="min(580px, calc(100vw - 32px))" destroy-on-close>
      <el-form ref="labFormRef" :model="labForm" :rules="labRules" label-position="top"><el-form-item label="实验室名称" prop="labName"><el-input v-model="labForm.labName" maxlength="64" /></el-form-item><el-form-item label="位置" prop="location"><el-input v-model="labForm.location" maxlength="128" /></el-form-item><div class="form-grid"><el-form-item label="容量" prop="capacity"><el-input-number v-model="labForm.capacity" :min="1" :max="500" class="full-width" /></el-form-item><el-form-item label="状态" prop="status"><el-segmented v-model="labForm.status" :options="labStatusOptions" class="full-width" /></el-form-item></div><el-form-item label="使用说明"><el-input v-model="labForm.description" type="textarea" :rows="4" maxlength="1000" show-word-limit /></el-form-item></el-form>
      <template #footer><el-button @click="labDialogOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveLab">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="resourceDialogOpen" :title="editingResource ? '修改资源' : '新增设备或工位'" width="min(560px, calc(100vw - 32px))" destroy-on-close>
      <div v-if="detailLab" class="dialog-target"><strong>{{ detailLab.labName }}</strong><span>{{ detailLab.location }}</span></div>
      <el-form ref="resourceFormRef" :model="resourceForm" :rules="resourceRules" label-position="top"><div class="form-grid"><el-form-item label="资源编号" prop="resourceNo"><el-input v-model="resourceForm.resourceNo" maxlength="32" /></el-form-item><el-form-item label="资源类型" prop="resourceType"><el-select v-model="resourceForm.resourceType" class="full-width"><el-option label="实验设备" value="EQUIPMENT" /><el-option label="实验工位" value="WORKSTATION" /></el-select></el-form-item></div><el-form-item label="资源名称" prop="resourceName"><el-input v-model="resourceForm.resourceName" maxlength="64" /></el-form-item><el-form-item label="状态" prop="status"><el-segmented v-model="resourceForm.status" :options="resourceStatusOptions" /></el-form-item><el-form-item label="资源说明"><el-input v-model="resourceForm.description" type="textarea" :rows="3" maxlength="500" show-word-limit /></el-form-item></el-form>
      <template #footer><el-button @click="resourceDialogOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveResource">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="slotDialogOpen" :title="editingSlot ? '修改开放时段' : '新增开放时段'" width="min(520px, calc(100vw - 32px))" destroy-on-close>
      <el-form ref="slotFormRef" :model="slotForm" :rules="slotRules" label-position="top"><el-form-item label="实验室" prop="labId"><el-select v-model="slotForm.labId" :disabled="Boolean(editingSlot)" class="full-width"><el-option v-for="lab in teacherLabs" :key="lab.labId" :label="`${lab.labName} · ${lab.location}`" :value="lab.labId" /></el-select></el-form-item><el-form-item label="开放日期" prop="openDate"><el-date-picker v-model="slotForm.openDate" type="date" value-format="YYYY-MM-DD" :disabled-date="disablePastDate" class="full-width" /></el-form-item><div class="form-grid"><el-form-item label="开始节次" prop="startPeriod"><el-input-number v-model="slotForm.startPeriod" :min="1" :max="12" class="full-width" /></el-form-item><el-form-item label="结束节次" prop="endPeriod"><el-input-number v-model="slotForm.endPeriod" :min="1" :max="12" class="full-width" /></el-form-item></div></el-form>
      <template #footer><el-button @click="slotDialogOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveSlot">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="bookingDialogOpen" title="预约实验资源" width="min(520px, calc(100vw - 32px))" destroy-on-close>
      <div v-if="bookingResource" class="dialog-target"><strong>{{ bookingResource.resourceName }}</strong><span>{{ bookingResource.labName }} · {{ bookingResource.resourceNo }}</span></div>
      <el-form ref="bookingFormRef" :model="bookingForm" :rules="bookingRules" label-position="top"><el-form-item label="预约日期" prop="bookingDate"><el-date-picker v-model="bookingForm.bookingDate" type="date" value-format="YYYY-MM-DD" :disabled-date="disablePastDate" class="full-width" /></el-form-item><div class="form-grid"><el-form-item label="开始节次" prop="startPeriod"><el-input-number v-model="bookingForm.startPeriod" :min="1" :max="12" class="full-width" /></el-form-item><el-form-item label="结束节次" prop="endPeriod"><el-input-number v-model="bookingForm.endPeriod" :min="1" :max="12" class="full-width" /></el-form-item></div><el-form-item label="预约用途" prop="purpose"><el-input v-model="bookingForm.purpose" type="textarea" :rows="3" maxlength="256" show-word-limit /></el-form-item></el-form>
      <template #footer><el-button @click="bookingDialogOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveBooking">确认预约</el-button></template>
    </el-dialog>

    <el-drawer v-model="labDetailOpen" title="实验室详情" size="min(620px, 100vw)" destroy-on-close>
      <div v-if="detailLab" class="detail-content"><div class="detail-title"><div><small>{{ detailLab.labNo }}</small><h2>{{ detailLab.labName }}</h2><span>{{ detailLab.location }}</span></div><el-tag :type="labStatusMeta(detailLab.status).type">{{ labStatusMeta(detailLab.status).label }}</el-tag></div><dl><dt>容量</dt><dd>{{ detailLab.capacity }} 人</dd><dt>负责人</dt><dd>{{ detailLab.managerName || '--' }}</dd><dt>使用说明</dt><dd class="pre-wrap">{{ detailLab.description || '暂无说明' }}</dd></dl><section class="resource-section"><div class="section-heading"><h3>设备与工位</h3><el-button v-if="isTeacher && canManageResource" link type="primary" :icon="Plus" @click="openResourceDialog()">新增资源</el-button></div><div class="resource-list"><article v-for="resource in detailResources" :key="resource.resourceId"><div><strong>{{ resource.resourceName }}</strong><small>{{ resource.resourceNo }} · {{ resource.resourceTypeLabel }}</small></div><el-tag :type="resourceStatusMeta(resource.status).type" size="small">{{ resourceStatusMeta(resource.status).label }}</el-tag><div class="resource-actions"><el-button v-if="isStudent && canCreateBooking" size="small" type="primary" :disabled="resource.status !== 'AVAILABLE'" @click="openBookingDialog(resource)">预约</el-button><el-button v-if="isTeacher" size="small" @click="openResourceDialog(resource)">修改</el-button></div></article><el-empty v-if="!detailResources.length" description="暂无设备或工位" /></div></section><section class="slot-section"><div class="section-heading"><h3>近期开放时段</h3></div><div class="slot-list"><span v-for="slot in detailSlots" :key="slot.slotId"><strong>{{ formatDate(slot.openDate) }}</strong>第 {{ slot.startPeriod }}-{{ slot.endPeriod }} 节</span><el-empty v-if="!detailSlots.length" description="暂无开放时段" /></div></section></div>
    </el-drawer>

    <el-drawer v-model="bookingDetailOpen" title="预约详情" size="min(540px, 100vw)" destroy-on-close>
      <div v-if="bookingDetail" class="detail-content"><div class="detail-title"><div><small>{{ bookingDetail.bookingNo }}</small><h2>{{ bookingDetail.resourceName }}</h2><span>{{ bookingDetail.labName }}</span></div><el-tag :type="bookingStatusMeta(bookingDetail.status).type">{{ bookingStatusMeta(bookingDetail.status).label }}</el-tag></div><dl><dt>实验室</dt><dd>{{ bookingDetail.labName }} · {{ bookingDetail.location }}</dd><dt>资源编号</dt><dd>{{ bookingDetail.resourceNo }}</dd><template v-if="isTeacher"><dt>预约学生</dt><dd>{{ bookingDetail.studentName }} · {{ bookingDetail.studentNo }}</dd></template><dt>预约时间</dt><dd>{{ formatDate(bookingDetail.bookingDate) }} 第 {{ bookingDetail.startPeriod }}-{{ bookingDetail.endPeriod }} 节</dd><dt>预约用途</dt><dd class="pre-wrap">{{ bookingDetail.purpose }}</dd><dt>创建时间</dt><dd>{{ formatDateTime(bookingDetail.createTime) }}</dd></dl><div class="drawer-actions"><el-button v-if="isStudent && bookingDetail.status === 'BOOKED'" type="danger" plain @click="cancelBooking(bookingDetail)">取消预约</el-button><el-button v-if="isTeacher && bookingDetail.status === 'BOOKED' && canComplete(bookingDetail)" type="primary" @click="completeBooking(bookingDetail)">标记完成</el-button></div></div>
    </el-drawer>
  </section>
</template>

<script setup>
import { computed, inject, reactive, ref, watch } from 'vue'
import { Bell, Calendar, CircleCheck, Clock, Close, EditPen, OfficeBuilding, Plus, Refresh, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  cancelLabBooking, completeLabBooking, createLab, createLabBooking, createLabOpenSlot,
  createLabResource, deleteLabOpenSlot, getLab, getLabBooking, listLabOpenSlots,
  listLabResources, listLabs, listManagedLabBookings, listMyLabBookingNotices,
  listMyLabBookings, markLabBookingNoticeRead, updateLab, updateLabOpenSlot, updateLabResource,
} from '@/api/student.js'

const currentUser = inject('currentUser', ref(null))
const roles = computed(() => new Set(currentUser.value?.roles || []))
const permissions = computed(() => new Set(currentUser.value?.permissions || []))
const roleResolved = computed(() => Boolean(currentUser.value))
const isStudent = computed(() => roles.value.has('STUDENT') && permissions.value.has('lab:read'))
const isTeacher = computed(() => roles.value.has('TEACHER') && permissions.value.has('lab:read'))
const canManageLab = computed(() => permissions.value.has('lab:manage-self'))
const canManageResource = computed(() => permissions.value.has('lab:resource:manage-self'))
const canManageSlot = computed(() => permissions.value.has('lab:slot:manage-self'))
const canCreateBooking = computed(() => permissions.value.has('lab:booking:create'))
const canCancelBooking = computed(() => permissions.value.has('lab:booking:cancel-self'))

const activeTab = ref('resources'), records = ref([]), total = ref(0), page = ref(1), loading = ref(false), saving = ref(false), loadError = ref(false)
const pageSize = 10, statusFilter = ref(''), noticeFilter = ref(null), teacherLabs = ref([])
const bookingStatuses = [{ value: 'BOOKED', label: '已预约' }, { value: 'CANCELLED', label: '已取消' }, { value: 'COMPLETED', label: '已完成' }]
const labStatusOptions = [{ label: '开放', value: 1 }, { label: '维护中', value: 0 }]
const resourceStatusOptions = [{ label: '可预约', value: 1 }, { label: '维护中', value: 0 }]
const labStatusMap = { OPEN: { label: '开放', type: 'success' }, MAINTENANCE: { label: '维护中', type: 'warning' } }
const resourceStatusMap = { AVAILABLE: { label: '可预约', type: 'success' }, MAINTENANCE: { label: '维护中', type: 'warning' } }
const bookingStatusMap = { BOOKED: { label: '已预约', type: 'primary' }, CANCELLED: { label: '已取消', type: 'info' }, COMPLETED: { label: '已完成', type: 'success' } }
const labStatusMeta = (status) => labStatusMap[status] || { label: status || '未知', type: 'info' }
const resourceStatusMeta = (status) => resourceStatusMap[status] || { label: status || '未知', type: 'info' }
const bookingStatusMeta = (status) => bookingStatusMap[status] || { label: status || '未知', type: 'info' }
const panelTitle = computed(() => ({ resources: '开放中的实验室', bookings: '我的预约记录', notices: '预约结果通知', labs: '我负责的实验室', slots: '实验室开放时段', managed: '学生预约记录' }[activeTab.value]))
const emptyText = computed(() => ({ resources: '暂无可预约实验室', bookings: '暂未提交实验室预约', notices: '暂无预约通知', labs: '暂未维护实验室', slots: '暂未设置开放时段', managed: '暂无学生预约记录' }[activeTab.value]))
const headerAction = computed(() => {
  if (isTeacher.value && activeTab.value === 'labs' && canManageLab.value) return { visible: true, label: '新建实验室', run: () => openLabDialog() }
  if (isTeacher.value && activeTab.value === 'slots' && canManageSlot.value) return { visible: true, label: '新增开放时段', run: () => openSlotDialog() }
  return { visible: false, label: '', run: () => {} }
})

const formatDate = (value) => value ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium' }).format(new Date(`${value}T00:00:00`)) : '--'
const formatDateTime = (value) => value ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '--'
const disablePastDate = (date) => date.getTime() < new Date().setHours(0, 0, 0, 0)
const canComplete = (row) => row.bookingDate && row.bookingDate <= new Date().toLocaleDateString('sv-SE')

const loadTeacherLabs = async () => { if (!isTeacher.value) return; const result = await listLabs({ page: 1, size: 100 }); teacherLabs.value = result.data.records || [] }
const loadData = async () => {
  if (!isStudent.value && !isTeacher.value) return
  loading.value = true; loadError.value = false
  try {
    let result
    if (activeTab.value === 'resources' || activeTab.value === 'labs') result = await listLabs({ page: page.value, size: pageSize, status: statusFilter.value || undefined })
    else if (activeTab.value === 'slots') result = await listLabOpenSlots({ page: page.value, size: pageSize })
    else if (activeTab.value === 'bookings') result = await listMyLabBookings({ page: page.value, size: pageSize, status: statusFilter.value || undefined })
    else if (activeTab.value === 'managed') result = await listManagedLabBookings({ page: page.value, size: pageSize, status: statusFilter.value || undefined })
    else result = await listMyLabBookingNotices({ page: page.value, size: pageSize, isRead: noticeFilter.value ?? undefined })
    records.value = result.data.records || []; total.value = result.data.total || 0
  } catch { loadError.value = true; records.value = []; total.value = 0 } finally { loading.value = false }
}
const resetPageAndLoad = () => { page.value = 1; loadData() }
const resetAndLoad = () => { statusFilter.value = ''; noticeFilter.value = null; resetPageAndLoad() }

const labDialogOpen = ref(false), labFormRef = ref(), editingLab = ref(null), labForm = reactive({ labName: '', location: '', capacity: 30, description: '', status: 1 })
const labRules = { labName: [{ required: true, message: '请填写实验室名称', trigger: 'blur' }], location: [{ required: true, message: '请填写实验室位置', trigger: 'blur' }], capacity: [{ required: true, message: '请填写容量', trigger: 'change' }], status: [{ required: true, message: '请选择状态', trigger: 'change' }] }
const openLabDialog = (row = null) => { editingLab.value = row; Object.assign(labForm, row ? { labName: row.labName, location: row.location, capacity: row.capacity, description: row.description || '', status: row.statusCode } : { labName: '', location: '', capacity: 30, description: '', status: 1 }); labDialogOpen.value = true }
const saveLab = async () => { await labFormRef.value.validate(); saving.value = true; try { editingLab.value ? await updateLab(editingLab.value.labId, { ...labForm, description: labForm.description || null }) : await createLab({ ...labForm, description: labForm.description || null }); ElMessage.success(editingLab.value ? '实验室已更新' : '实验室已创建'); labDialogOpen.value = false; await Promise.all([loadData(), loadTeacherLabs()]) } finally { saving.value = false } }

const labDetailOpen = ref(false), detailLab = ref(null), detailResources = ref([]), detailSlots = ref([])
const openLabDetail = async (row) => { const [labResult, resourcesResult, slotsResult] = await Promise.all([getLab(row.labId), listLabResources({ labId: row.labId }), listLabOpenSlots({ page: 1, size: 100, labId: row.labId })]); detailLab.value = labResult.data; detailResources.value = resourcesResult.data || []; detailSlots.value = slotsResult.data.records || []; labDetailOpen.value = true }
const refreshLabDetail = async () => { if (detailLab.value) await openLabDetail(detailLab.value) }

const resourceDialogOpen = ref(false), resourceFormRef = ref(), editingResource = ref(null), resourceForm = reactive({ resourceNo: '', resourceName: '', resourceType: 'WORKSTATION', description: '', status: 1 })
const resourceRules = { resourceNo: [{ required: true, message: '请填写资源编号', trigger: 'blur' }], resourceName: [{ required: true, message: '请填写资源名称', trigger: 'blur' }], resourceType: [{ required: true, message: '请选择资源类型', trigger: 'change' }], status: [{ required: true, message: '请选择状态', trigger: 'change' }] }
const openResourceDialog = (row = null) => { editingResource.value = row; Object.assign(resourceForm, row ? { resourceNo: row.resourceNo, resourceName: row.resourceName, resourceType: row.resourceType, description: row.description || '', status: row.statusCode } : { resourceNo: '', resourceName: '', resourceType: 'WORKSTATION', description: '', status: 1 }); resourceDialogOpen.value = true }
const saveResource = async () => { await resourceFormRef.value.validate(); saving.value = true; try { const payload = { ...resourceForm, description: resourceForm.description || null }; editingResource.value ? await updateLabResource(editingResource.value.resourceId, payload) : await createLabResource(detailLab.value.labId, payload); ElMessage.success(editingResource.value ? '资源已更新' : '资源已新增'); resourceDialogOpen.value = false; await Promise.all([refreshLabDetail(), loadData()]) } finally { saving.value = false } }

const slotDialogOpen = ref(false), slotFormRef = ref(), editingSlot = ref(null), slotForm = reactive({ labId: null, openDate: '', startPeriod: 1, endPeriod: 2 })
const slotRules = { labId: [{ required: true, message: '请选择实验室', trigger: 'change' }], openDate: [{ required: true, message: '请选择开放日期', trigger: 'change' }], startPeriod: [{ required: true, message: '请选择开始节次', trigger: 'change' }], endPeriod: [{ required: true, message: '请选择结束节次', trigger: 'change' }] }
const openSlotDialog = async (row = null) => { if (!teacherLabs.value.length) await loadTeacherLabs(); editingSlot.value = row; Object.assign(slotForm, row ? { labId: row.labId, openDate: row.openDate, startPeriod: row.startPeriod, endPeriod: row.endPeriod } : { labId: teacherLabs.value[0]?.labId || null, openDate: '', startPeriod: 1, endPeriod: 2 }); slotDialogOpen.value = true }
const saveSlot = async () => { await slotFormRef.value.validate(); if (slotForm.startPeriod > slotForm.endPeriod) return ElMessage.warning('开始节次不能晚于结束节次'); saving.value = true; try { const payload = { openDate: slotForm.openDate, startPeriod: slotForm.startPeriod, endPeriod: slotForm.endPeriod }; editingSlot.value ? await updateLabOpenSlot(editingSlot.value.slotId, payload) : await createLabOpenSlot(slotForm.labId, payload); ElMessage.success(editingSlot.value ? '开放时段已更新' : '开放时段已新增'); slotDialogOpen.value = false; await loadData() } finally { saving.value = false } }
const removeSlot = async (row) => { await ElMessageBox.confirm('确认删除该开放时段？已有预约时系统会拒绝删除。', '删除开放时段', { type: 'warning' }); await deleteLabOpenSlot(row.slotId); ElMessage.success('开放时段已删除'); await loadData() }

const bookingDialogOpen = ref(false), bookingFormRef = ref(), bookingResource = ref(null), bookingForm = reactive({ bookingDate: '', startPeriod: 1, endPeriod: 2, purpose: '' })
const bookingRules = { bookingDate: [{ required: true, message: '请选择预约日期', trigger: 'change' }], startPeriod: [{ required: true, message: '请选择开始节次', trigger: 'change' }], endPeriod: [{ required: true, message: '请选择结束节次', trigger: 'change' }], purpose: [{ required: true, message: '请填写预约用途', trigger: 'blur' }] }
const openBookingDialog = (resource) => { bookingResource.value = resource; Object.assign(bookingForm, { bookingDate: '', startPeriod: 1, endPeriod: 2, purpose: '' }); bookingDialogOpen.value = true }
const saveBooking = async () => { await bookingFormRef.value.validate(); if (bookingForm.startPeriod > bookingForm.endPeriod) return ElMessage.warning('开始节次不能晚于结束节次'); saving.value = true; try { await createLabBooking({ resourceId: bookingResource.value.resourceId, ...bookingForm }); ElMessage.success('预约成功，通知已发送'); bookingDialogOpen.value = false; labDetailOpen.value = false; activeTab.value = 'bookings'; await loadData() } finally { saving.value = false } }
const cancelBooking = async (row) => { await ElMessageBox.confirm('确认取消该预约？取消后资源时段将重新开放。', '取消预约', { type: 'warning' }); await cancelLabBooking(row.bookingId); ElMessage.success('预约已取消'); bookingDetailOpen.value = false; await loadData() }
const completeBooking = async (row) => { await ElMessageBox.confirm('确认该预约已经完成？', '完成预约', { type: 'info' }); await completeLabBooking(row.bookingId); ElMessage.success('预约已标记完成'); bookingDetailOpen.value = false; await loadData() }

const bookingDetailOpen = ref(false), bookingDetail = ref(null)
const openBookingDetail = async (row) => { const result = await getLabBooking(row.bookingId); bookingDetail.value = result.data; bookingDetailOpen.value = true }
const readNotice = async (row) => { if (row.isRead === 0) { await markLabBookingNoticeRead(row.noticeId); row.isRead = 1 } ElMessage.info(row.content) }

watch([isStudent, isTeacher], async ([student, teacher]) => { if (student) activeTab.value = 'resources'; else if (teacher) { activeTab.value = 'labs'; await loadTeacherLabs() } if (student || teacher) resetAndLoad() }, { immediate: true })
</script>

<style scoped>
.lab-workspace{width:min(1180px,calc(100% - 56px));margin:0 auto;padding:32px 0 48px}.workspace-header{display:flex;align-items:flex-end;justify-content:space-between;gap:24px;margin-bottom:24px}.workspace-header h1{margin:2px 0 4px;font-size:26px;font-weight:650;letter-spacing:0}.workspace-header p{margin:0;color:var(--color-text-secondary)}.workspace-header .eyebrow{color:var(--color-brand-600);font-size:12px;font-weight:700}.data-panel{min-height:430px;padding:0 22px 20px}.workspace-tabs :deep(.el-tabs__header){margin:0}.workspace-tabs :deep(.el-tabs__content){display:none}.workspace-tabs :deep(.el-tabs__nav-wrap::after){height:1px;background:var(--color-border-light)}.tab-label{display:inline-flex;align-items:center;gap:6px}.panel-toolbar{display:flex;min-height:70px;align-items:center;justify-content:space-between;gap:20px}.panel-toolbar>div:first-child{display:flex;align-items:baseline;gap:10px}.panel-toolbar h2{margin:0;font-size:17px;font-weight:600}.panel-toolbar span{color:var(--color-text-tertiary);font-size:13px}.filter-actions{display:flex;align-items:center;gap:8px}.filter-actions .el-select{width:145px}.desktop-table :deep(.el-table__row){cursor:pointer}.desktop-table :deep(th.el-table__cell){color:var(--color-text-secondary);background:#f8fafb;font-weight:600}.cell-subtitle{display:block;margin-top:2px;color:var(--color-text-tertiary)}.row-actions{display:flex;align-items:center;white-space:nowrap}.desktop-empty{padding:48px 0}.mobile-records{display:none}.el-pagination{justify-content:flex-end;margin-top:20px}.full-width{width:100%!important}.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:16px}.dialog-target{display:flex;flex-direction:column;gap:4px;margin:-4px 0 18px;padding:12px 14px;background:var(--color-brand-50);border-left:3px solid var(--color-brand-600)}.dialog-target span{color:var(--color-text-secondary);font-size:13px}.detail-content{padding:0 4px 24px}.detail-title{display:flex;align-items:flex-start;justify-content:space-between;gap:18px;padding-bottom:20px;border-bottom:1px solid var(--color-border-light)}.detail-title h2{margin:4px 0;font-size:20px;letter-spacing:0}.detail-title span,.detail-title small{color:var(--color-text-tertiary)}.detail-content dl{display:grid;grid-template-columns:88px 1fr;gap:18px 12px;margin:24px 0}.detail-content dt{color:var(--color-text-tertiary)}.detail-content dd{min-width:0;margin:0}.pre-wrap{white-space:pre-wrap}.section-heading{display:flex;align-items:center;justify-content:space-between}.section-heading h3{margin:0;font-size:16px}.resource-section,.slot-section{margin-top:24px}.resource-list{display:grid;gap:8px;margin-top:12px}.resource-list article{display:grid;grid-template-columns:minmax(0,1fr) auto auto;align-items:center;gap:10px;padding:12px;background:#f8fafb;border:1px solid var(--color-border-light);border-radius:6px}.resource-list article>div:first-child{display:flex;min-width:0;flex-direction:column}.resource-list small{color:var(--color-text-tertiary)}.resource-actions{display:flex;gap:6px}.slot-list{display:flex;flex-wrap:wrap;gap:8px;margin-top:12px}.slot-list>span{display:flex;gap:8px;padding:9px 12px;background:#f8fafb;border:1px solid var(--color-border-light);border-radius:4px}.slot-list strong{color:var(--color-brand-700)}.drawer-actions{display:flex;justify-content:flex-end;gap:8px;padding-top:18px;border-top:1px solid var(--color-border-light)}.unread{font-weight:700}.mobile-actions{display:flex;gap:8px;margin-top:8px}
@media(max-width:991px){.lab-workspace{width:calc(100% - 40px)}}
@media(max-width:767px){.lab-workspace{width:calc(100% - 24px);padding:22px 0 36px}.workspace-header{align-items:flex-start;margin-bottom:18px}.workspace-header h1{font-size:21px}.workspace-header>div>p:last-child{font-size:13px}.workspace-header .el-button{flex:0 0 auto}.data-panel{padding:0;background:transparent;border:0}.workspace-tabs{padding:0 4px}.workspace-tabs :deep(.el-tabs__nav){display:flex;width:100%}.workspace-tabs :deep(.el-tabs__item){min-width:0;flex:1;justify-content:center;padding:0 4px}.tab-label{gap:4px;font-size:12px}.panel-toolbar{min-height:64px}.panel-toolbar>div:first-child{display:block}.filter-actions .el-select{width:118px}.desktop-table,.desktop-empty{display:none}.mobile-records{display:flex;min-height:170px;flex-direction:column;gap:8px}.mobile-record{display:flex;width:100%;flex-direction:column;gap:4px;padding:14px;color:var(--color-text-secondary);text-align:left;background:#fff;border:1px solid var(--color-border-light);border-radius:6px}.record-top{display:flex;align-items:flex-start;justify-content:space-between;gap:10px;color:var(--color-text-primary)}.record-top strong{min-width:0;flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.record-top .el-tag{flex:0 0 auto}.mobile-record small{color:var(--color-text-tertiary)}.mobile-empty{width:100%;background:#fff;border:1px solid var(--color-border-light);border-radius:6px}.el-pagination{justify-content:center}.form-grid{grid-template-columns:1fr}.detail-content dl{grid-template-columns:76px 1fr}.resource-list article{grid-template-columns:minmax(0,1fr) auto}.resource-actions{grid-column:1/3}.resource-actions .el-button{flex:1}.drawer-actions{flex-wrap:wrap}}
</style>
