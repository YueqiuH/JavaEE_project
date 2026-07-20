<template>
  <section class="teams-page">
    <PageBreadcrumb domain="student" title="学科竞赛" />
    <header class="page-header">
      <el-button :icon="ArrowLeft" circle aria-label="返回学科竞赛" @click="router.push({ name: 'competition' })" />
      <div>
        <p class="eyebrow">学科竞赛管理</p>
        <h1>{{ competition?.title || '参赛队伍' }}</h1>
        <p>查询该竞赛的报名队伍、成员、材料与审核结果</p>
      </div>
    </header>

    <el-result v-if="roleResolved && !canRead" icon="warning" title="当前账号无权查询竞赛队伍" />
    <template v-else>
      <section v-if="competition" class="summary-band">
        <div><span>竞赛编号</span><strong>{{ competition.competitionNo }}</strong></div>
        <div><span>报名队伍</span><strong>{{ competition.teamCount }}</strong></div>
        <div><span>已通过</span><strong>{{ competition.approvedTeamCount }} / {{ competition.maxTeamCount }}</strong></div>
        <div><span>报名截止</span><strong>{{ formatDate(competition.deadline) }}</strong></div>
      </section>

      <div class="surface-panel teams-panel">
        <div class="panel-toolbar">
          <div><h2>参赛队伍</h2><span>共 {{ total }} 支</span></div>
          <div class="filter-actions">
            <el-select v-model="status" placeholder="全部状态" clearable @change="resetAndLoad">
              <el-option v-for="item in statuses" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-tooltip content="刷新列表"><el-button :icon="Refresh" circle aria-label="刷新列表" @click="loadTeams" /></el-tooltip>
          </div>
        </div>

        <el-result v-if="loadError" icon="error" title="队伍数据加载失败" sub-title="请检查网络连接后重试">
          <template #extra><el-button type="primary" @click="loadAll">重新加载</el-button></template>
        </el-result>
        <template v-else>
          <el-table v-if="loading || teams.length" v-loading="loading" :data="teams" row-key="teamId" class="desktop-table" highlight-current-row @row-click="selectTeam">
            <el-table-column label="队伍" min-width="190"><template #default="{ row }"><strong>{{ row.teamName }}</strong><small>{{ row.registrationNo }}</small></template></el-table-column>
            <el-table-column label="队长" min-width="150"><template #default="{ row }">{{ row.leaderName }}<small>{{ row.leaderNo }}</small></template></el-table-column>
            <el-table-column label="成员" width="105"><template #default="{ row }">{{ row.acceptedMemberCount }} / {{ row.maxMembers }} 人</template></el-table-column>
            <el-table-column label="报名材料" min-width="200"><template #default="{ row }"><span class="file-name">{{ row.materialOriginalName || '未上传' }}</span></template></el-table-column>
            <el-table-column label="提交时间" min-width="155"><template #default="{ row }">{{ formatDateTime(row.submittedAt) }}</template></el-table-column>
            <el-table-column label="状态" width="105"><template #default="{ row }"><el-tag :type="statusMeta(row.status).type">{{ statusMeta(row.status).label }}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="150" fixed="right"><template #default="{ row }"><div @click.stop><el-button link :icon="View" @click="selectTeam(row)">详情</el-button><el-button v-if="row.status === 'SUBMITTED' && canReview" link type="primary" :icon="CircleCheck" @click="openReview(row)">审核</el-button></div></template></el-table-column>
          </el-table>

          <div v-loading="loading" class="mobile-list">
            <button v-for="row in teams" :key="row.teamId" type="button" class="mobile-team" @click="selectTeam(row)">
              <span><strong>{{ row.teamName }}</strong><el-tag :type="statusMeta(row.status).type" size="small">{{ statusMeta(row.status).label }}</el-tag></span>
              <em>队长 {{ row.leaderName }} · {{ row.acceptedMemberCount }} 人</em>
              <small>{{ row.materialOriginalName || '未上传报名材料' }}</small>
            </button>
          </div>
          <el-empty v-if="!loading && teams.length === 0" description="该竞赛下暂无符合条件的队伍" :image-size="84" />
          <el-pagination v-if="total > pageSize" v-model:current-page="page" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="loadTeams" />
        </template>
      </div>
    </template>

    <el-drawer v-model="detailOpen" title="队伍详情" size="min(580px, 100vw)" destroy-on-close @closed="selected = null">
      <div v-if="selected" class="team-detail">
        <div class="detail-heading">
          <div><span>{{ selected.registrationNo }}</span><h2>{{ selected.teamName }}</h2><p>队长 {{ selected.leaderName }}（{{ selected.leaderNo }}）</p></div>
          <el-tag :type="statusMeta(selected.status).type">{{ statusMeta(selected.status).label }}</el-tag>
        </div>
        <section class="detail-section">
          <h3>队伍成员</h3>
          <div class="members"><div v-for="member in selected.members" :key="member.memberId"><span><strong>{{ member.studentName }}</strong><small>{{ member.studentNo }}</small></span><el-tag size="small" type="info">{{ member.role }}</el-tag></div></div>
        </section>
        <section class="detail-section">
          <h3>报名与审核</h3>
          <dl><dt>报名材料</dt><dd><el-button v-if="selected.materialOriginalName" link type="primary" :icon="Download" @click="downloadMaterial(selected)">{{ selected.materialOriginalName }}</el-button><span v-else>未上传</span></dd><dt>材料说明</dt><dd>{{ selected.materialDescription || '暂无说明' }}</dd><dt>提交时间</dt><dd>{{ formatDateTime(selected.submittedAt) }}</dd><dt>审核时间</dt><dd>{{ formatDateTime(selected.reviewedAt) }}</dd><dt>审核意见</dt><dd>{{ selected.reviewOpinion || '暂无意见' }}</dd></dl>
        </section>
        <div v-if="selected.status === 'SUBMITTED' && canReview" class="detail-actions"><el-button type="primary" :icon="CircleCheck" @click="openReview(selected)">审核该队伍</el-button></div>
      </div>
    </el-drawer>

    <el-dialog v-model="reviewOpen" title="队伍资格审核" width="min(520px, calc(100vw - 32px))" destroy-on-close>
      <div v-if="reviewing" class="review-target"><strong>{{ reviewing.teamName }}</strong><span>{{ reviewing.registrationNo }} · {{ reviewing.acceptedMemberCount }} 人</span></div>
      <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-position="top">
        <el-form-item label="审核结论" prop="decision"><el-radio-group v-model="reviewForm.decision"><el-radio-button value="APPROVE">通过</el-radio-button><el-radio-button value="RETURN">退回修改</el-radio-button><el-radio-button value="REJECT">拒绝</el-radio-button></el-radio-group></el-form-item>
        <el-form-item label="审核意见" prop="opinion"><el-input v-model="reviewForm.opinion" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="退回或拒绝时必须填写" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="reviewOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="submitReview">提交结论</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, inject, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, CircleCheck, Download, Refresh, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { downloadCompetitionMaterial, getCompetition, getCompetitionTeam, listCompetitionTeams, reviewCompetitionTeam } from '@/api/student.js'
import PageBreadcrumb from '@/components/business/PageBreadcrumb.vue'

const route = useRoute()
const router = useRouter()
const currentUser = inject('currentUser', ref(null))
const permissions = computed(() => new Set(currentUser.value?.permissions || []))
const roles = computed(() => new Set(currentUser.value?.roles || []))
const roleResolved = computed(() => Boolean(currentUser.value))
const canRead = computed(() => (roles.value.has('TEACHER') && permissions.value.has('competition:review:read-self'))
  || ((roles.value.has('COUNSELOR') || roles.value.has('ADMIN')) && permissions.value.has('competition:oversight:read')))
const canReview = computed(() => permissions.value.has('competition:review:submit-self'))
const competitionId = computed(() => Number(route.params.competitionId))
const competition = ref(null)
const teams = ref([])
const selected = ref(null)
const detailOpen = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = 10
const status = ref('')
const loading = ref(false)
const loadError = ref(false)
const saving = ref(false)
const statuses = [{ value: 'FORMING', label: '组队中' }, { value: 'SUBMITTED', label: '待审核' }, { value: 'APPROVED', label: '已通过' }, { value: 'RETURNED', label: '已退回' }, { value: 'REJECTED', label: '未通过' }]
const statusMap = { FORMING: { label: '组队中', type: 'info' }, SUBMITTED: { label: '待审核', type: 'warning' }, APPROVED: { label: '已通过', type: 'success' }, RETURNED: { label: '已退回', type: 'danger' }, REJECTED: { label: '未通过', type: 'danger' } }
const statusMeta = (value) => statusMap[value] || { label: value || '未知', type: 'info' }
const formatDate = (value) => value ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium' }).format(new Date(`${value}T00:00:00`)) : '--'
const formatDateTime = (value) => value ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '--'

const loadTeams = async () => {
  loading.value = true
  loadError.value = false
  try {
    const result = await listCompetitionTeams(competitionId.value, { page: page.value, size: pageSize, status: status.value || undefined })
    teams.value = result.data.records || []
    total.value = result.data.total || 0
    if (selected.value && !teams.value.some((team) => team.teamId === selected.value.teamId)) {
      detailOpen.value = false
      selected.value = null
    }
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
  }
}
const loadAll = async () => {
  try {
    const result = await getCompetition(competitionId.value)
    competition.value = result.data
    await loadTeams()
  } catch {
    loadError.value = true
  }
}
const resetAndLoad = () => { page.value = 1; detailOpen.value = false; selected.value = null; loadTeams() }
const selectTeam = async (row) => { try { const result = await getCompetitionTeam(row.teamId); selected.value = result.data; detailOpen.value = true } catch { detailOpen.value = false; selected.value = null } }
const downloadMaterial = async (team) => { try { const result = await downloadCompetitionMaterial(team.teamId); const url = URL.createObjectURL(result.blob); const link = document.createElement('a'); link.href = url; link.download = result.fileName || team.materialOriginalName; link.click(); URL.revokeObjectURL(url) } catch (error) { ElMessage.error(error.message || '报名材料下载失败') } }

const reviewOpen = ref(false)
const reviewFormRef = ref()
const reviewing = ref(null)
const reviewForm = reactive({ decision: 'APPROVE', opinion: '' })
const reviewRules = computed(() => ({ decision: [{ required: true, message: '请选择审核结论', trigger: 'change' }], opinion: reviewForm.decision === 'APPROVE' ? [] : [{ required: true, message: '退回或拒绝时必须填写审核意见', trigger: 'blur' }] }))
const openReview = (team) => { reviewing.value = team; Object.assign(reviewForm, { decision: 'APPROVE', opinion: '' }); reviewOpen.value = true }
const submitReview = async () => { await reviewFormRef.value.validate(); saving.value = true; try { const result = await reviewCompetitionTeam(reviewing.value.teamId, { decision: reviewForm.decision, opinion: reviewForm.opinion || null }); selected.value = result.data; reviewOpen.value = false; ElMessage.success('审核结论已提交'); await loadAll() } finally { saving.value = false } }

watch(canRead, (allowed) => { if (allowed) loadAll() }, { immediate: true })
</script>

<style scoped>
.teams-page{width:min(1180px,calc(100% - 56px));margin:0 auto;padding:32px 0 48px}.page-header{display:flex;align-items:flex-start;gap:16px;margin-bottom:22px}.page-header>.el-button{margin-top:6px}.page-header p{margin:0;color:var(--color-text-secondary)}.page-header .eyebrow{color:var(--color-brand-600);font-size:12px;font-weight:700}.page-header h1{margin:2px 0 4px;font-size:26px;letter-spacing:0}.summary-band{display:grid;grid-template-columns:repeat(4,1fr);margin-bottom:18px;background:#fff;border:1px solid var(--color-border-light);border-radius:6px}.summary-band div{display:flex;min-width:0;flex-direction:column;gap:6px;padding:18px 20px;border-right:1px solid var(--color-border-light)}.summary-band div:last-child{border-right:0}.summary-band span{color:var(--color-text-tertiary);font-size:13px}.summary-band strong{overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:17px}.teams-panel{min-height:430px;padding:0 22px 24px}.panel-toolbar{display:flex;min-height:72px;align-items:center;justify-content:space-between;gap:16px}.panel-toolbar>div:first-child{display:flex;align-items:baseline;gap:10px}.panel-toolbar h2{margin:0;font-size:17px}.panel-toolbar span{color:var(--color-text-tertiary);font-size:13px}.filter-actions{display:flex;gap:8px}.filter-actions .el-select{width:145px}.desktop-table :deep(th.el-table__cell){color:var(--color-text-secondary);background:#f8fafb}.desktop-table :deep(.el-table__row){cursor:pointer}.desktop-table small{display:block;color:var(--color-text-tertiary)}.file-name{display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.mobile-list{display:none}.el-pagination{justify-content:flex-end;margin-top:20px}.team-detail{padding:0 4px 24px}.detail-heading{display:flex;align-items:flex-start;justify-content:space-between;gap:16px;padding-bottom:20px;border-bottom:1px solid var(--color-border-light)}.detail-heading span,.detail-heading p{margin:0;color:var(--color-text-tertiary)}.detail-heading h2{margin:3px 0;font-size:20px}.detail-section{margin-top:24px}.detail-section h3{margin:0 0 12px;font-size:16px}.members{display:grid;gap:8px}.members>div{display:flex;align-items:center;justify-content:space-between;gap:12px;padding:10px 12px;background:#f8fafb;border:1px solid var(--color-border-light);border-radius:6px}.members span{display:flex;min-width:0;flex-direction:column}.members small{color:var(--color-text-tertiary)}.detail-section dl{display:grid;grid-template-columns:76px minmax(0,1fr);gap:14px 12px;margin:0}.detail-section dt{color:var(--color-text-tertiary)}.detail-section dd{min-width:0;margin:0;white-space:pre-wrap}.detail-actions{display:flex;justify-content:flex-end;margin-top:22px;padding-top:16px;border-top:1px solid var(--color-border-light)}.review-target{display:flex;flex-direction:column;margin:-4px 0 18px;padding:12px 14px;background:var(--color-brand-50);border-left:3px solid var(--color-brand-600)}.review-target span{color:var(--color-text-secondary);font-size:13px}
@media(max-width:991px){.teams-page{width:calc(100% - 40px)}.summary-band{grid-template-columns:1fr 1fr}.summary-band div:nth-child(2){border-right:0}.summary-band div:nth-child(-n+2){border-bottom:1px solid var(--color-border-light)}}
@media(max-width:767px){.teams-page{width:calc(100% - 24px);padding:22px 0 36px}.page-header{gap:10px}.page-header h1{font-size:21px}.summary-band div{padding:14px}.teams-panel{padding:0;background:transparent;border:0}.panel-toolbar{min-height:64px}.panel-toolbar>div:first-child{display:block}.filter-actions .el-select{width:120px}.desktop-table{display:none}.mobile-list{display:grid;gap:8px}.mobile-team{display:flex;width:100%;flex-direction:column;gap:5px;padding:14px;text-align:left;background:#fff;border:1px solid var(--color-border-light);border-radius:6px}.mobile-team>span{display:flex;align-items:flex-start;justify-content:space-between;gap:10px}.mobile-team strong{min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.mobile-team em{color:var(--color-text-secondary);font-style:normal}.mobile-team small{overflow:hidden;color:var(--color-text-tertiary);text-overflow:ellipsis;white-space:nowrap}.team-detail{padding:0 2px 20px}.detail-section dl{grid-template-columns:72px minmax(0,1fr)}.el-pagination{justify-content:center}}
</style>
