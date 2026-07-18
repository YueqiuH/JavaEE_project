<template>
  <section class="competition-workspace">
    <header class="workspace-header">
      <div>
        <p class="eyebrow">学生事务</p>
        <h1>{{ isTeacher ? '学科竞赛管理' : '学科竞赛与组队' }}</h1>
        <p>{{ isTeacher ? '发布竞赛、管理报名阶段并审核参赛队伍' : '发现竞赛、邀请队员并提交团队报名材料' }}</p>
      </div>
      <el-button v-if="isTeacher && activeTab === 'competitions' && canPublish" type="primary" :icon="Plus" @click="openCompetitionDialog()">发布竞赛</el-button>
    </header>

    <el-result v-if="roleResolved && !isStudent && !isTeacher" icon="warning" title="当前账号无竞赛业务权限" />

    <div v-else class="surface-panel data-panel">
      <el-tabs v-model="activeTab" class="workspace-tabs" @tab-change="resetAndLoad">
        <template v-if="isStudent">
          <el-tab-pane name="discover"><template #label><span class="tab-label"><el-icon><Trophy /></el-icon>竞赛广场</span></template></el-tab-pane>
          <el-tab-pane name="teams"><template #label><span class="tab-label"><el-icon><User /></el-icon>我的队伍</span></template></el-tab-pane>
          <el-tab-pane name="invitations"><template #label><span class="tab-label"><el-icon><Bell /></el-icon>组队邀请</span></template></el-tab-pane>
        </template>
        <template v-else-if="isTeacher">
          <el-tab-pane name="competitions"><template #label><span class="tab-label"><el-icon><Trophy /></el-icon>我的竞赛</span></template></el-tab-pane>
          <el-tab-pane name="reviews"><template #label><span class="tab-label"><el-icon><List /></el-icon>队伍审核</span></template></el-tab-pane>
        </template>
      </el-tabs>

      <div class="panel-toolbar">
        <div><h2>{{ panelTitle }}</h2><span>共 {{ total }} 项</span></div>
        <div class="filter-actions">
          <el-select v-if="activeTab === 'competitions' || activeTab === 'teams' || activeTab === 'reviews'" v-model="statusFilter" placeholder="全部状态" clearable @change="resetPageAndLoad">
            <el-option v-for="item in currentStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-tooltip content="刷新列表" placement="top">
            <el-button :icon="Refresh" circle aria-label="刷新列表" @click="loadData" />
          </el-tooltip>
        </div>
      </div>

      <el-result v-if="loadError" icon="error" title="数据加载失败" sub-title="请检查网络连接后重试">
        <template #extra><el-button type="primary" @click="loadData">重新加载</el-button></template>
      </el-result>

      <template v-else-if="activeTab === 'discover' || activeTab === 'competitions'">
        <el-table v-if="loading || records.length" v-loading="loading" :data="records" row-key="competitionId" class="desktop-table" @row-click="openCompetitionDetail">
          <el-table-column label="竞赛" min-width="250">
            <template #default="{ row }"><strong>{{ row.title }}</strong><small class="cell-subtitle">{{ row.competitionNo }}</small></template>
          </el-table-column>
          <el-table-column v-if="isStudent" label="发布教师" min-width="130"><template #default="{ row }">{{ row.publisherName }}</template></el-table-column>
          <el-table-column label="报名截止" min-width="135"><template #default="{ row }">{{ formatDate(row.deadline) }}</template></el-table-column>
          <el-table-column label="组队要求" min-width="140"><template #default="{ row }">{{ row.minMembers }}-{{ row.maxMembers }} 人 / {{ row.teamCount }} 队</template></el-table-column>
          <el-table-column label="状态" width="105"><template #default="{ row }"><el-tag :type="competitionStatusMeta(row.status).type">{{ competitionStatusMeta(row.status).label }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <div class="row-actions" @click.stop>
                <el-button link :icon="View" @click="openCompetitionDetail(row)">查看</el-button>
                <template v-if="isStudent">
                  <el-button v-if="row.myTeamId" link type="primary" :icon="User" @click="openTeamDetailById(row.myTeamId)">我的队伍</el-button>
                  <el-button v-else-if="canCreateTeam" link type="primary" :icon="Plus" @click="openTeamDialog(null, row)">发起组队</el-button>
                </template>
                <template v-else>
                  <el-button v-if="row.status === 'DRAFT' && canManageCompetition" link type="primary" :icon="EditPen" @click="openCompetitionDialog(row)">修改</el-button>
                  <el-button v-if="row.status === 'DRAFT' && canManageCompetition" link type="primary" :icon="Promotion" @click="publish(row)">发布</el-button>
                  <el-button v-if="row.status === 'OPEN' && canManageCompetition" link type="danger" :icon="Close" @click="closeRegistration(row)">关闭</el-button>
                </template>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <div v-loading="loading" class="mobile-records">
          <button v-for="row in records" :key="row.competitionId" type="button" class="mobile-record" @click="openCompetitionDetail(row)">
            <span class="record-top"><strong>{{ row.title }}</strong><el-tag :type="competitionStatusMeta(row.status).type" size="small">{{ competitionStatusMeta(row.status).label }}</el-tag></span>
            <span>{{ isStudent ? `${row.publisherName} · ` : '' }}截止 {{ formatDate(row.deadline) }}</span>
            <small>{{ row.minMembers }}-{{ row.maxMembers }} 人组队 · 已有 {{ row.teamCount }} 队</small>
          </button>
          <el-empty v-if="!loading && records.length === 0" class="mobile-empty" :description="emptyText" :image-size="72" />
        </div>
        <el-empty v-if="!loading && records.length === 0" class="desktop-empty" :description="emptyText" :image-size="88" />
      </template>

      <template v-else-if="activeTab === 'teams' || activeTab === 'reviews'">
        <el-table v-if="loading || records.length" v-loading="loading" :data="records" row-key="teamId" class="desktop-table" @row-click="openTeamDetail">
          <el-table-column label="队伍" min-width="210"><template #default="{ row }"><strong>{{ row.teamName }}</strong><small class="cell-subtitle">{{ row.registrationNo }}</small></template></el-table-column>
          <el-table-column label="参赛竞赛" min-width="220"><template #default="{ row }">{{ row.competitionTitle }}</template></el-table-column>
          <el-table-column v-if="isTeacher" label="队长" min-width="140"><template #default="{ row }">{{ row.leaderName }}<small class="cell-subtitle">{{ row.leaderNo }}</small></template></el-table-column>
          <el-table-column label="成员" width="105"><template #default="{ row }">{{ row.acceptedMemberCount }} / {{ row.maxMembers }} 人</template></el-table-column>
          <el-table-column label="提交时间" min-width="150"><template #default="{ row }">{{ formatDateTime(row.submittedAt) }}</template></el-table-column>
          <el-table-column label="状态" width="105"><template #default="{ row }"><el-tag :type="teamStatusMeta(row.status).type">{{ teamStatusMeta(row.status).label }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="205" fixed="right">
            <template #default="{ row }">
              <div class="row-actions" @click.stop>
                <el-button link :icon="View" @click="openTeamDetail(row)">查看</el-button>
                <template v-if="isStudent && isLeader(row) && isTeamManageable(row)">
                  <el-button v-if="canManageTeam" link type="primary" :icon="EditPen" @click="openTeamDialog(row)">编辑</el-button>
                  <el-button v-if="canSubmitTeam" link type="primary" :icon="Promotion" @click="submitTeam(row)">提交</el-button>
                </template>
                <el-button v-if="isTeacher && row.status === 'SUBMITTED' && canReview" link type="primary" :icon="CircleCheck" @click="openReviewDialog(row)">审核</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <div v-loading="loading" class="mobile-records">
          <button v-for="row in records" :key="row.teamId" type="button" class="mobile-record" @click="openTeamDetail(row)">
            <span class="record-top"><strong>{{ row.teamName }}</strong><el-tag :type="teamStatusMeta(row.status).type" size="small">{{ teamStatusMeta(row.status).label }}</el-tag></span>
            <span>{{ row.competitionTitle }}</span>
            <small>{{ isTeacher ? `队长 ${row.leaderName} · ` : '' }}{{ row.acceptedMemberCount }} 人已加入</small>
          </button>
          <el-empty v-if="!loading && records.length === 0" class="mobile-empty" :description="emptyText" :image-size="72" />
        </div>
        <el-empty v-if="!loading && records.length === 0" class="desktop-empty" :description="emptyText" :image-size="88" />
      </template>

      <template v-else-if="activeTab === 'invitations'">
        <el-table v-if="loading || records.length" v-loading="loading" :data="records" row-key="memberId" class="desktop-table">
          <el-table-column label="竞赛" min-width="220"><template #default="{ row }"><strong>{{ row.competitionTitle }}</strong><small class="cell-subtitle">截止 {{ formatDate(row.deadline) }}</small></template></el-table-column>
          <el-table-column label="邀请队伍" min-width="180"><template #default="{ row }">{{ row.teamName }}</template></el-table-column>
          <el-table-column label="队长" min-width="150"><template #default="{ row }">{{ row.leaderName }}（{{ row.leaderNo }}）</template></el-table-column>
          <el-table-column label="邀请时间" min-width="150"><template #default="{ row }">{{ formatDateTime(row.invitedAt) }}</template></el-table-column>
          <el-table-column label="状态" width="105"><template #default="{ row }"><el-tag :type="invitationStatusMeta(row.invitationStatus).type">{{ invitationStatusMeta(row.invitationStatus).label }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }"><div v-if="row.invitationStatus === 'INVITED'" class="row-actions"><el-button link type="primary" @click="respondInvitation(row, 'ACCEPT')">接受</el-button><el-button link type="danger" @click="respondInvitation(row, 'DECLINE')">拒绝</el-button></div></template>
          </el-table-column>
        </el-table>
        <div v-loading="loading" class="mobile-records">
          <article v-for="row in records" :key="row.memberId" class="mobile-record">
            <span class="record-top"><strong>{{ row.teamName }}</strong><el-tag :type="invitationStatusMeta(row.invitationStatus).type" size="small">{{ invitationStatusMeta(row.invitationStatus).label }}</el-tag></span>
            <span>{{ row.competitionTitle }} · 队长 {{ row.leaderName }}</span>
            <div v-if="row.invitationStatus === 'INVITED'" class="mobile-actions"><el-button size="small" type="primary" @click="respondInvitation(row, 'ACCEPT')">接受</el-button><el-button size="small" @click="respondInvitation(row, 'DECLINE')">拒绝</el-button></div>
          </article>
          <el-empty v-if="!loading && records.length === 0" class="mobile-empty" :description="emptyText" :image-size="72" />
        </div>
        <el-empty v-if="!loading && records.length === 0" class="desktop-empty" :description="emptyText" :image-size="88" />
      </template>

      <el-pagination v-if="total > pageSize" v-model:current-page="page" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="loadData" />
    </div>

    <el-dialog v-model="competitionDialogOpen" :title="editingCompetition ? '修改竞赛草稿' : '新建竞赛'" width="min(620px, calc(100vw - 32px))" destroy-on-close>
      <el-form ref="competitionFormRef" :model="competitionForm" :rules="competitionRules" label-position="top">
        <el-form-item label="竞赛名称" prop="title"><el-input v-model="competitionForm.title" maxlength="128" show-word-limit placeholder="例如：大学生程序设计竞赛" /></el-form-item>
        <div class="form-grid"><el-form-item label="报名截止日期" prop="deadline"><el-date-picker v-model="competitionForm.deadline" type="date" value-format="YYYY-MM-DD" :disabled-date="disablePastDate" class="full-width" /></el-form-item><el-form-item label="最大入选队伍数" prop="maxTeamCount"><el-input-number v-model="competitionForm.maxTeamCount" :min="1" :max="200" class="full-width" /></el-form-item></div>
        <div class="form-grid"><el-form-item label="每队最少人数" prop="minMembers"><el-input-number v-model="competitionForm.minMembers" :min="1" :max="10" class="full-width" /></el-form-item><el-form-item label="每队最多人数" prop="maxMembers"><el-input-number v-model="competitionForm.maxMembers" :min="1" :max="10" class="full-width" /></el-form-item></div>
        <el-form-item label="竞赛介绍" prop="description"><el-input v-model="competitionForm.description" type="textarea" :rows="4" maxlength="4000" show-word-limit placeholder="说明竞赛方向、赛制与时间安排" /></el-form-item>
        <el-form-item label="参赛要求" prop="requirements"><el-input v-model="competitionForm.requirements" type="textarea" :rows="4" maxlength="4000" show-word-limit placeholder="说明成员要求、材料清单和资格条件" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="competitionDialogOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveCompetition">保存草稿</el-button></template>
    </el-dialog>

    <el-dialog v-model="teamDialogOpen" :title="editingTeam ? '编辑队伍与材料' : '发起组队'" width="min(560px, calc(100vw - 32px))" destroy-on-close>
      <div v-if="teamCompetition" class="dialog-target"><strong>{{ teamCompetition.title || teamCompetition.competitionTitle }}</strong><span>{{ teamCompetition.minMembers }}-{{ teamCompetition.maxMembers }} 人组队</span></div>
      <el-form ref="teamFormRef" :model="teamForm" :rules="teamRules" label-position="top">
        <el-form-item label="队伍名称" prop="teamName"><el-input v-model="teamForm.teamName" maxlength="64" show-word-limit placeholder="请输入队伍名称" /></el-form-item>
        <el-form-item label="报名材料链接" prop="materialUrl"><el-input v-model="teamForm.materialUrl" placeholder="https://..." clearable /></el-form-item>
        <el-form-item label="材料说明" prop="materialDescription"><el-input v-model="teamForm.materialDescription" type="textarea" :rows="4" maxlength="1000" show-word-limit placeholder="说明项目方案、成员分工或材料内容" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="teamDialogOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveTeam">{{ editingTeam ? '保存修改' : '创建队伍' }}</el-button></template>
    </el-dialog>

    <el-dialog v-model="inviteDialogOpen" title="邀请队员" width="min(440px, calc(100vw - 32px))" destroy-on-close>
      <el-form ref="inviteFormRef" :model="inviteForm" :rules="inviteRules" label-position="top"><el-form-item label="学生学号" prop="studentNo"><el-input v-model="inviteForm.studentNo" inputmode="numeric" maxlength="20" placeholder="请输入受邀学生学号" /></el-form-item></el-form>
      <template #footer><el-button @click="inviteDialogOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveInvitation">发送邀请</el-button></template>
    </el-dialog>

    <el-dialog v-model="reviewDialogOpen" title="队伍资格审核" width="min(520px, calc(100vw - 32px))" destroy-on-close>
      <div v-if="reviewingTeam" class="dialog-target"><strong>{{ reviewingTeam.teamName }}</strong><span>{{ reviewingTeam.competitionTitle }} · {{ reviewingTeam.acceptedMemberCount }} 人</span></div>
      <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-position="top">
        <el-form-item label="审核结论" prop="decision"><el-radio-group v-model="reviewForm.decision"><el-radio-button value="APPROVE">通过</el-radio-button><el-radio-button value="RETURN">退回修改</el-radio-button><el-radio-button value="REJECT">拒绝</el-radio-button></el-radio-group></el-form-item>
        <el-form-item label="审核意见" prop="opinion"><el-input v-model="reviewForm.opinion" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="退回或拒绝时必须填写" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="reviewDialogOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveReview">提交结论</el-button></template>
    </el-dialog>

    <el-drawer v-model="competitionDetailOpen" title="竞赛详情" size="min(540px, 100vw)" destroy-on-close>
      <div v-if="competitionDetail" class="detail-content">
        <div class="detail-title"><div><small>{{ competitionDetail.competitionNo }}</small><h2>{{ competitionDetail.title }}</h2><span>{{ competitionDetail.publisherName }}</span></div><el-tag :type="competitionStatusMeta(competitionDetail.status).type">{{ competitionStatusMeta(competitionDetail.status).label }}</el-tag></div>
        <dl><dt>报名截止</dt><dd>{{ formatDate(competitionDetail.deadline) }}</dd><dt>组队人数</dt><dd>{{ competitionDetail.minMembers }}-{{ competitionDetail.maxMembers }} 人</dd><dt>入选名额</dt><dd>{{ competitionDetail.approvedTeamCount }} / {{ competitionDetail.maxTeamCount }} 队</dd><dt>竞赛介绍</dt><dd class="pre-wrap">{{ competitionDetail.description }}</dd><dt>参赛要求</dt><dd class="pre-wrap">{{ competitionDetail.requirements }}</dd></dl>
        <div class="drawer-actions"><el-button v-if="isStudent && competitionDetail.myTeamId" :icon="User" @click="openTeamDetailById(competitionDetail.myTeamId)">查看我的队伍</el-button><el-button v-else-if="isStudent && canCreateTeam" type="primary" :icon="Plus" @click="openTeamDialog(null, competitionDetail)">发起组队</el-button></div>
      </div>
    </el-drawer>

    <el-drawer v-model="teamDetailOpen" title="队伍详情" size="min(580px, 100vw)" destroy-on-close>
      <div v-if="teamDetail" class="detail-content">
        <div class="detail-title"><div><small>{{ teamDetail.registrationNo }}</small><h2>{{ teamDetail.teamName }}</h2><span>{{ teamDetail.competitionTitle }}</span></div><el-tag :type="teamStatusMeta(teamDetail.status).type">{{ teamStatusMeta(teamDetail.status).label }}</el-tag></div>
        <section class="member-section"><div class="section-heading"><h3>队伍成员</h3><el-button v-if="isStudent && isLeader(teamDetail) && isTeamManageable(teamDetail) && canManageTeam" link type="primary" :icon="CirclePlus" @click="openInviteDialog(teamDetail)">邀请队员</el-button></div><div class="member-list"><div v-for="member in teamDetail.members" :key="member.memberId"><span><strong>{{ member.studentName }}</strong><small>{{ member.studentNo }} · {{ member.role }}</small></span><el-tag :type="invitationStatusMeta(member.invitationStatus).type" size="small">{{ invitationStatusMeta(member.invitationStatus).label }}</el-tag><el-button v-if="isStudent && member.role !== '队长' && isLeader(teamDetail) && isTeamManageable(teamDetail) && canManageTeam" link type="danger" :icon="Close" aria-label="移除成员" @click="removeMember(teamDetail, member)" /></div></div></section>
        <dl><dt>报名材料</dt><dd><a v-if="teamDetail.materialUrl" :href="teamDetail.materialUrl" target="_blank" rel="noopener">打开材料链接</a><span v-else>尚未提交</span></dd><dt>材料说明</dt><dd class="pre-wrap">{{ teamDetail.materialDescription || '暂无说明' }}</dd><template v-if="teamDetail.reviewOpinion"><dt>审核意见</dt><dd class="review-opinion">{{ teamDetail.reviewOpinion }}</dd></template></dl>
        <div class="drawer-actions"><el-button v-if="isStudent && isLeader(teamDetail) && isTeamManageable(teamDetail) && canManageTeam" :icon="EditPen" @click="openTeamDialog(teamDetail)">编辑材料</el-button><el-button v-if="isStudent && isLeader(teamDetail) && isTeamManageable(teamDetail) && canSubmitTeam" type="primary" :icon="Promotion" @click="submitTeam(teamDetail)">提交报名</el-button><el-button v-if="isTeacher && teamDetail.status === 'SUBMITTED' && canReview" type="primary" :icon="CircleCheck" @click="openReviewDialog(teamDetail)">开始审核</el-button></div>
      </div>
    </el-drawer>
  </section>
</template>

<script setup>
import { computed, inject, reactive, ref, watch } from 'vue'
import { Bell, CircleCheck, CirclePlus, Close, EditPen, List, Plus, Promotion, Refresh, Trophy, User, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  closeCompetition, createCompetition, createCompetitionTeam, getCompetition, getCompetitionTeam,
  inviteCompetitionMember, listCompetitionReviews, listCompetitions, listMyCompetitionInvitations,
  listMyCompetitionTeams, publishCompetition, removeCompetitionMember, respondCompetitionInvitation,
  reviewCompetitionTeam, submitCompetitionTeam, updateCompetition, updateCompetitionTeam,
} from '@/api/student.js'

const currentUser = inject('currentUser', ref(null))
const roles = computed(() => new Set(currentUser.value?.roles || []))
const permissions = computed(() => new Set(currentUser.value?.permissions || []))
const roleResolved = computed(() => Boolean(currentUser.value))
const isStudent = computed(() => roles.value.has('STUDENT') && permissions.value.has('competition:read'))
const isTeacher = computed(() => roles.value.has('TEACHER') && permissions.value.has('competition:read'))
const canPublish = computed(() => permissions.value.has('competition:publish'))
const canManageCompetition = computed(() => permissions.value.has('competition:manage-self'))
const canCreateTeam = computed(() => permissions.value.has('competition:team:create'))
const canManageTeam = computed(() => permissions.value.has('competition:team:manage-self'))
const canSubmitTeam = computed(() => permissions.value.has('competition:team:submit-self'))
const canReview = computed(() => permissions.value.has('competition:review:submit-self'))

const competitionStatuses = [{ value: 'DRAFT', label: '草稿' }, { value: 'OPEN', label: '报名中' }, { value: 'CLOSED', label: '已关闭' }]
const teamStatuses = [{ value: 'FORMING', label: '组队中' }, { value: 'SUBMITTED', label: '待审核' }, { value: 'APPROVED', label: '已通过' }, { value: 'RETURNED', label: '已退回' }, { value: 'REJECTED', label: '未通过' }]
const competitionStatusMap = { DRAFT: { label: '草稿', type: 'info' }, OPEN: { label: '报名中', type: 'success' }, CLOSED: { label: '已关闭', type: 'info' } }
const teamStatusMap = { FORMING: { label: '组队中', type: 'info' }, SUBMITTED: { label: '待审核', type: 'warning' }, APPROVED: { label: '已通过', type: 'success' }, RETURNED: { label: '已退回', type: 'danger' }, REJECTED: { label: '未通过', type: 'danger' } }
const invitationStatusMap = { INVITED: { label: '待确认', type: 'warning' }, ACCEPTED: { label: '已加入', type: 'success' }, DECLINED: { label: '已拒绝', type: 'info' }, REMOVED: { label: '已移除', type: 'info' } }

const activeTab = ref('discover')
const records = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 10
const loading = ref(false)
const saving = ref(false)
const loadError = ref(false)
const statusFilter = ref('')
const panelTitle = computed(() => ({ discover: '报名中的竞赛', teams: '我参与的队伍', invitations: '我的组队邀请', competitions: '我发布的竞赛', reviews: '参赛资格审核' }[activeTab.value]))
const emptyText = computed(() => ({ discover: '暂无开放报名的竞赛', teams: '暂未加入竞赛队伍', invitations: '暂无组队邀请', competitions: '暂未发布竞赛', reviews: '暂无待审核队伍' }[activeTab.value]))
const currentStatusOptions = computed(() => activeTab.value === 'competitions' ? competitionStatuses : teamStatuses)

const competitionStatusMeta = (status) => competitionStatusMap[status] || { label: status || '未知', type: 'info' }
const teamStatusMeta = (status) => teamStatusMap[status] || { label: status || '未知', type: 'info' }
const invitationStatusMeta = (status) => invitationStatusMap[status] || { label: status || '未知', type: 'info' }
const formatDate = (value) => value ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium' }).format(new Date(`${value}T00:00:00`)) : '--'
const formatDateTime = (value) => value ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '未提交'
const isLeader = (team) => String(team.leaderNo) === String(currentUser.value?.user?.username)
const isTeamManageable = (team) => ['FORMING', 'RETURNED'].includes(team.status)
const disablePastDate = (date) => date.getTime() < new Date().setHours(0, 0, 0, 0)

const loadData = async () => {
  if (!isStudent.value && !isTeacher.value) return
  loading.value = true
  loadError.value = false
  try {
    const params = { page: page.value, size: pageSize, status: statusFilter.value || undefined }
    let result
    if (activeTab.value === 'discover' || activeTab.value === 'competitions') result = await listCompetitions(params)
    else if (activeTab.value === 'teams') result = await listMyCompetitionTeams(params)
    else if (activeTab.value === 'invitations') result = await listMyCompetitionInvitations({ page: page.value, size: pageSize })
    else result = await listCompetitionReviews(params)
    records.value = result.data.records || []
    total.value = result.data.total || 0
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
  }
}

const resetPageAndLoad = () => { page.value = 1; loadData() }
const resetAndLoad = () => { statusFilter.value = ''; resetPageAndLoad() }

const competitionDialogOpen = ref(false)
const competitionFormRef = ref()
const editingCompetition = ref(null)
const competitionForm = reactive({ title: '', description: '', requirements: '', deadline: '', minMembers: 1, maxMembers: 5, maxTeamCount: 10 })
const competitionRules = {
  title: [{ required: true, message: '请填写竞赛名称', trigger: 'blur' }], description: [{ required: true, message: '请填写竞赛介绍', trigger: 'blur' }],
  requirements: [{ required: true, message: '请填写参赛要求', trigger: 'blur' }], deadline: [{ required: true, message: '请选择截止日期', trigger: 'change' }],
  minMembers: [{ required: true, message: '请填写最少人数', trigger: 'change' }], maxMembers: [{ required: true, message: '请填写最多人数', trigger: 'change' }], maxTeamCount: [{ required: true, message: '请填写入选队伍数', trigger: 'change' }],
}
const openCompetitionDialog = (row = null) => { editingCompetition.value = row; Object.assign(competitionForm, row ? { title: row.title, description: row.description, requirements: row.requirements, deadline: row.deadline, minMembers: row.minMembers, maxMembers: row.maxMembers, maxTeamCount: row.maxTeamCount } : { title: '', description: '', requirements: '', deadline: '', minMembers: 1, maxMembers: 5, maxTeamCount: 10 }); competitionDialogOpen.value = true }
const saveCompetition = async () => { await competitionFormRef.value.validate(); if (competitionForm.minMembers > competitionForm.maxMembers) return ElMessage.warning('最少人数不能大于最多人数'); saving.value = true; try { editingCompetition.value ? await updateCompetition(editingCompetition.value.competitionId, { ...competitionForm }) : await createCompetition({ ...competitionForm }); ElMessage.success(editingCompetition.value ? '竞赛草稿已更新' : '竞赛草稿已创建'); competitionDialogOpen.value = false; await loadData() } finally { saving.value = false } }
const publish = async (row) => { await ElMessageBox.confirm('发布后竞赛将对学生开放报名，且不能再修改，确认发布？', '发布竞赛', { type: 'warning', confirmButtonText: '确认发布' }); await publishCompetition(row.competitionId); ElMessage.success('竞赛已发布'); await loadData() }
const closeRegistration = async (row) => { await ElMessageBox.confirm('关闭后学生不能再组队或提交报名，确认关闭？', '关闭报名', { type: 'warning', confirmButtonText: '确认关闭' }); await closeCompetition(row.competitionId); ElMessage.success('竞赛报名已关闭'); await loadData() }

const teamDialogOpen = ref(false)
const teamFormRef = ref()
const editingTeam = ref(null)
const teamCompetition = ref(null)
const teamForm = reactive({ teamName: '', materialUrl: '', materialDescription: '' })
const teamRules = { teamName: [{ required: true, message: '请填写队伍名称', trigger: 'blur' }], materialUrl: [{ type: 'url', message: '请输入完整的 http(s) 链接', trigger: 'blur' }] }
const openTeamDialog = (team = null, competition = null) => { editingTeam.value = team; teamCompetition.value = competition || team; Object.assign(teamForm, team ? { teamName: team.teamName, materialUrl: team.materialUrl || '', materialDescription: team.materialDescription || '' } : { teamName: '', materialUrl: '', materialDescription: '' }); teamDialogOpen.value = true }
const saveTeam = async () => { await teamFormRef.value.validate(); saving.value = true; try { const payload = { ...teamForm, materialUrl: teamForm.materialUrl || null, materialDescription: teamForm.materialDescription || null }; const result = editingTeam.value ? await updateCompetitionTeam(editingTeam.value.teamId, payload) : await createCompetitionTeam(teamCompetition.value.competitionId, payload); ElMessage.success(editingTeam.value ? '队伍信息已更新' : '队伍已创建'); teamDialogOpen.value = false; await loadData(); if (editingTeam.value && teamDetailOpen.value) teamDetail.value = result.data } finally { saving.value = false } }

const competitionDetailOpen = ref(false)
const competitionDetail = ref(null)
const openCompetitionDetail = async (row) => { const result = await getCompetition(row.competitionId); competitionDetail.value = result.data; competitionDetailOpen.value = true }
const teamDetailOpen = ref(false)
const teamDetail = ref(null)
const openTeamDetail = async (row) => { const result = await getCompetitionTeam(row.teamId); teamDetail.value = result.data; teamDetailOpen.value = true }
const openTeamDetailById = async (id) => { competitionDetailOpen.value = false; await openTeamDetail({ teamId: id }) }

const inviteDialogOpen = ref(false)
const inviteFormRef = ref()
const invitingTeam = ref(null)
const inviteForm = reactive({ studentNo: '' })
const inviteRules = { studentNo: [{ required: true, message: '请填写学生学号', trigger: 'blur' }, { pattern: /^\d+$/, message: '学号只能包含数字', trigger: 'blur' }] }
const openInviteDialog = (team) => { invitingTeam.value = team; inviteForm.studentNo = ''; inviteDialogOpen.value = true }
const saveInvitation = async () => { await inviteFormRef.value.validate(); saving.value = true; try { const result = await inviteCompetitionMember(invitingTeam.value.teamId, inviteForm.studentNo); teamDetail.value = result.data; ElMessage.success('组队邀请已发送'); inviteDialogOpen.value = false; await loadData() } finally { saving.value = false } }
const removeMember = async (team, member) => { await ElMessageBox.confirm(`确认将 ${member.studentName} 移出队伍？`, '移除成员', { type: 'warning' }); const result = await removeCompetitionMember(team.teamId, member.memberId); teamDetail.value = result.data; ElMessage.success('成员已移除'); await loadData() }
const submitTeam = async (team) => { await ElMessageBox.confirm('提交后队伍和材料将进入教师审核，确认提交？', '提交报名', { type: 'warning', confirmButtonText: '确认提交' }); await submitCompetitionTeam(team.teamId); ElMessage.success('竞赛报名已提交'); teamDetailOpen.value = false; await loadData() }
const respondInvitation = async (row, decision) => { await respondCompetitionInvitation(row.memberId, decision); ElMessage.success(decision === 'ACCEPT' ? '已加入队伍' : '已拒绝邀请'); await loadData() }

const reviewDialogOpen = ref(false)
const reviewFormRef = ref()
const reviewingTeam = ref(null)
const reviewForm = reactive({ decision: 'APPROVE', opinion: '' })
const reviewRules = computed(() => ({ decision: [{ required: true, message: '请选择审核结论', trigger: 'change' }], opinion: reviewForm.decision === 'APPROVE' ? [] : [{ required: true, message: '退回或拒绝时必须填写审核意见', trigger: 'blur' }] }))
const openReviewDialog = (team) => { reviewingTeam.value = team; Object.assign(reviewForm, { decision: 'APPROVE', opinion: '' }); reviewDialogOpen.value = true }
const saveReview = async () => { await reviewFormRef.value.validate(); saving.value = true; try { await reviewCompetitionTeam(reviewingTeam.value.teamId, { ...reviewForm, opinion: reviewForm.opinion || null }); ElMessage.success('审核结论已提交'); reviewDialogOpen.value = false; teamDetailOpen.value = false; await loadData() } finally { saving.value = false } }

watch([isStudent, isTeacher], ([student, teacher]) => { if (student) activeTab.value = 'discover'; else if (teacher) activeTab.value = 'competitions'; if (student || teacher) resetAndLoad() }, { immediate: true })
</script>

<style scoped>
.competition-workspace{width:min(1180px,calc(100% - 56px));margin:0 auto;padding:32px 0 48px}.workspace-header{display:flex;align-items:flex-end;justify-content:space-between;gap:24px;margin-bottom:24px}.workspace-header h1{margin:2px 0 4px;font-size:26px;font-weight:650;letter-spacing:0}.workspace-header p{margin:0;color:var(--color-text-secondary)}.workspace-header .eyebrow{color:var(--color-brand-600);font-size:12px;font-weight:700}.data-panel{min-height:430px;padding:0 22px 20px}.workspace-tabs{margin:0}.workspace-tabs :deep(.el-tabs__header){margin:0}.workspace-tabs :deep(.el-tabs__content){display:none}.workspace-tabs :deep(.el-tabs__nav-wrap::after){height:1px;background:var(--color-border-light)}.tab-label{display:inline-flex;align-items:center;gap:6px}.panel-toolbar{display:flex;min-height:70px;align-items:center;justify-content:space-between;gap:20px}.panel-toolbar>div:first-child{display:flex;align-items:baseline;gap:10px}.panel-toolbar h2{margin:0;font-size:17px;font-weight:600}.panel-toolbar span{color:var(--color-text-tertiary);font-size:13px}.filter-actions{display:flex;align-items:center;gap:8px}.filter-actions .el-select{width:145px}.desktop-table :deep(.el-table__row){cursor:pointer}.desktop-table :deep(th.el-table__cell){color:var(--color-text-secondary);background:#f8fafb;font-weight:600}.cell-subtitle{display:block;margin-top:2px;color:var(--color-text-tertiary)}.row-actions{display:flex;align-items:center;white-space:nowrap}.desktop-empty{padding:48px 0}.mobile-records{display:none}.el-pagination{justify-content:flex-end;margin-top:20px}.full-width{width:100%!important}.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:16px}.dialog-target{display:flex;flex-direction:column;gap:4px;margin:-4px 0 18px;padding:12px 14px;background:var(--color-brand-50);border-left:3px solid var(--color-brand-600)}.dialog-target span{color:var(--color-text-secondary);font-size:13px}.detail-content{padding:0 4px 24px}.detail-title{display:flex;align-items:flex-start;justify-content:space-between;gap:18px;padding-bottom:20px;border-bottom:1px solid var(--color-border-light)}.detail-title h2{margin:4px 0;font-size:20px;letter-spacing:0}.detail-title span,.detail-title small{color:var(--color-text-tertiary)}.detail-content dl{display:grid;grid-template-columns:88px 1fr;gap:18px 12px;margin:24px 0}.detail-content dt{color:var(--color-text-tertiary)}.detail-content dd{min-width:0;margin:0}.detail-content a{color:var(--color-brand-600)}.pre-wrap{white-space:pre-wrap}.review-opinion{padding:10px 12px;background:#fff8e8;border-left:3px solid var(--color-warning)}.drawer-actions{display:flex;justify-content:flex-end;gap:8px;padding-top:18px;border-top:1px solid var(--color-border-light)}.member-section{margin:22px 0}.section-heading{display:flex;align-items:center;justify-content:space-between}.section-heading h3{margin:0;font-size:16px}.member-list{display:grid;gap:8px;margin-top:12px}.member-list>div{display:grid;grid-template-columns:minmax(0,1fr) auto 32px;align-items:center;gap:10px;padding:10px 12px;background:#f8fafb;border:1px solid var(--color-border-light);border-radius:6px}.member-list span{display:flex;min-width:0;flex-direction:column}.member-list small{color:var(--color-text-tertiary)}.mobile-actions{display:flex;gap:8px;margin-top:8px}
@media(max-width:991px){.competition-workspace{width:calc(100% - 40px)}}
@media(max-width:767px){.competition-workspace{width:calc(100% - 24px);padding:22px 0 36px}.workspace-header{align-items:flex-start;margin-bottom:18px}.workspace-header h1{font-size:21px}.workspace-header>div>p:last-child{font-size:13px}.workspace-header .el-button{flex:0 0 auto}.data-panel{padding:0;background:transparent;border:0}.workspace-tabs{padding:0 4px}.workspace-tabs :deep(.el-tabs__nav){display:flex;width:100%}.workspace-tabs :deep(.el-tabs__item){min-width:0;flex:1;justify-content:center;padding:0 5px}.tab-label{gap:4px;font-size:13px}.panel-toolbar{min-height:64px}.panel-toolbar>div:first-child{display:block}.filter-actions .el-select{width:122px}.desktop-table,.desktop-empty{display:none}.mobile-records{display:flex;min-height:170px;flex-direction:column;gap:8px}.mobile-record{display:flex;width:100%;flex-direction:column;gap:4px;padding:14px;color:var(--color-text-secondary);text-align:left;background:#fff;border:1px solid var(--color-border-light);border-radius:6px}.record-top{display:flex;align-items:flex-start;justify-content:space-between;gap:10px;color:var(--color-text-primary)}.record-top strong{min-width:0;flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.record-top .el-tag{flex:0 0 auto}.mobile-record small{color:var(--color-text-tertiary)}.mobile-empty{width:100%;background:#fff;border:1px solid var(--color-border-light);border-radius:6px}.el-pagination{justify-content:center}.form-grid{grid-template-columns:1fr}.detail-content dl{grid-template-columns:76px 1fr}.drawer-actions{flex-wrap:wrap}.member-list>div{grid-template-columns:minmax(0,1fr) auto 28px}}
</style>
