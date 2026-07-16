<template>
  <div class="page-container">
    <div class="page-header"><div><h2>学杂费交纳与流水查询</h2><p>费用账单、在线支付与一卡通最近五笔明细</p></div>
      <div><el-input-number v-model="studentId" :min="1" controls-position="right"/><el-button type="primary" @click="load">查询学生</el-button><el-button @click="dialogVisible=true">导入账单</el-button></div>
    </div>
    <el-card shadow="never"><template #header>个人费用账单</template>
      <el-table :data="fees" v-loading="loading" stripe><el-table-column prop="feeType" label="费用类型"/><el-table-column prop="semester" label="学期"/><el-table-column prop="amount" label="金额"><template #default="s">¥ {{ s.row.amount }}</template></el-table-column><el-table-column prop="dueDate" label="截止日期"/><el-table-column label="状态"><template #default="s"><el-tag :type="s.row.status===1?'success':'warning'">{{ s.row.status===1?'已支付':'未支付' }}</el-tag></template></el-table-column><el-table-column label="操作"><template #default="s"><el-button type="primary" link :disabled="s.row.status===1" @click="pay(s.row)">立即支付</el-button></template></el-table-column></el-table>
    </el-card>
    <el-card shadow="never"><template #header>一卡通最近五笔明细</template>
      <el-table :data="payments" stripe><el-table-column prop="paymentTime" label="时间"/><el-table-column prop="paymentType" label="类型"/><el-table-column prop="description" label="说明"/><el-table-column prop="amount" label="金额"/></el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" title="财务人员导入账单" width="480px"><el-form label-width="90px"><el-form-item label="学生ID"><el-input-number v-model="form.studentId" :min="1"/></el-form-item><el-form-item label="费用类型"><el-select v-model="form.feeType"><el-option v-for="v in ['学费','报考费','住宿费']" :key="v" :value="v"/></el-select></el-form-item><el-form-item label="金额"><el-input-number v-model="form.amount" :min="0.01" :precision="2"/></el-form-item><el-form-item label="学期"><el-input v-model="form.semester"/></el-form-item><el-form-item label="截止日期"><el-date-picker v-model="form.dueDate" value-format="YYYY-MM-DD"/></el-form-item></el-form><template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="importFee">导入</el-button></template></el-dialog>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'; import { ElMessage, ElMessageBox } from 'element-plus'; import { feeAPI } from '@/api/getData.js';
const studentId=ref(1), fees=ref([]), payments=ref([]), loading=ref(false), dialogVisible=ref(false); const form=reactive({studentId:1,feeType:'学费',amount:0.01,semester:'2025-2026-1',dueDate:''});
const load=async()=>{loading.value=true;try{const [a,b]=await Promise.all([feeAPI.getStudentFees(studentId.value),feeAPI.getRecentPayments(studentId.value)]);if(a&&a!==-1)fees.value=a.data||[];if(b&&b!==-1)payments.value=b.data||[]}finally{loading.value=false}};
const pay=async row=>{await ElMessageBox.confirm(`确认支付 ¥${row.amount}？`,'支付确认');const r=await feeAPI.pay(row.feeId);if(r&&r!==-1){ElMessage.success('支付成功');load()}};
const importFee=async()=>{const r=await feeAPI.importFees([{...form,status:0}]);if(r&&r!==-1){ElMessage.success('账单导入成功');dialogVisible.value=false;if(form.studentId===studentId.value)load()}}; onMounted(load);
</script>
<style scoped>.page-container{padding:24px;background:#f5f7fa;min-height:100vh}.page-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:18px}.page-header h2{margin:0}.page-header p{color:#909399;margin:8px 0 0}.page-header>div:last-child{display:flex;gap:10px}.el-card+.el-card{margin-top:18px}</style>
