<template>
  <section class="office-page">
    <header class="office-page__header"><div><h1>AI 公文摘要与审批助手</h1><p>仅分析当前流转至本人的待审批公文，生成内容不替代人工决定。</p></div><el-tag effect="dark" type="warning">AI 结果仅供参考</el-tag></header>
    <el-result v-if="!canUse" class="office-page__empty" icon="warning" title="无 AI 审批权限" />
    <el-row v-else :gutter="18">
      <el-col :xs="24" :lg="9">
        <el-card class="office-page__section" shadow="never">
          <template #header><strong>选择待审批公文</strong></template>
          <el-form label-position="top"><el-form-item label="待审批公文"><el-select v-model="docId" filterable placeholder="请选择"><el-option v-for="document in documents" :key="document.docId" :label="document.title" :value="document.docId" /></el-select></el-form-item><el-form-item label="本次适用的学校规章（可选）"><el-input v-model="schoolRules" type="textarea" :rows="5" maxlength="4000" show-word-limit placeholder="仅粘贴真实适用的制度条款" /></el-form-item></el-form>
          <el-empty v-if="!documents.length" description="当前没有待审批公文" />
          <template v-if="selected"><el-descriptions :column="1" border><el-descriptions-item label="类型">{{ selected.docType }}</el-descriptions-item><el-descriptions-item label="发起人">{{ selected.initiatorId }}</el-descriptions-item></el-descriptions><p class="document-content">{{ selected.content }}</p></template>
          <div class="office-page__actions ai-actions"><el-button type="primary" :disabled="!docId" :loading="loading==='summary'" @click="generate('summary')">生成要点摘要</el-button><el-button :disabled="!docId" :loading="loading==='recommend'" @click="generate('recommend')">生成审批建议</el-button></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="15">
        <el-card class="office-page__section result-card" shadow="never"><template #header><div class="result-title"><strong>AI 生成结果</strong><el-button link :disabled="!result" @click="copy">复制结果</el-button></div></template><el-skeleton v-if="loading" :rows="8" animated /><el-input v-else-if="result" v-model="result" type="textarea" :rows="24" resize="vertical" /><el-empty v-else description="选择公文后生成摘要或审批建议" /></el-card>
      </el-col>
    </el-row>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { aiApprovalAPI, documentAPI } from '@/api/office.js'
import { useOfficeAccess } from '@/composables/useOfficeAccess.js'

const { hasPermission } = useOfficeAccess()
const canUse = computed(() => hasPermission('ai-approval:use') && hasPermission('document:approve'))
const documents = ref([])
const docId = ref()
const result = ref('')
const loading = ref('')
const schoolRules = ref('')
const selected = computed(() => documents.value.find(item=>item.docId===docId.value))
const load = async () => { if(!canUse.value)return; documents.value=(await documentAPI.pending()).data||[]; docId.value=documents.value[0]?.docId }
const generate = async (type) => { loading.value=type; try { const response=type==='summary'?await aiApprovalAPI.summary(docId.value):await aiApprovalAPI.recommend(docId.value,{schoolRules:schoolRules.value}); result.value=response.data||'' } finally { loading.value='' } }
const copy = async () => { await navigator.clipboard.writeText(result.value); ElMessage.success('已复制') }
onMounted(load)
</script>

<style scoped>
@import '@/assets/office-workspace.css';
.document-content{max-height:180px;overflow:auto;line-height:1.65;white-space:pre-wrap}.ai-actions{justify-content:flex-start;margin-top:18px}.result-title{display:flex;align-items:center;justify-content:space-between}.result-card{min-height:540px}@media(max-width:1199px){.result-card{margin-top:18px}}
</style>
