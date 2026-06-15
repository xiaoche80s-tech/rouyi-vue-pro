<template>
  <el-table
    :data="list"
    @selection-change="(rows: any[]) => emit('selectionChange', rows)"
  >
    <el-table-column type="selection" width="55" />
    <el-table-column align="center" label="经销商" prop="dealerName" min-width="120" show-overflow-tooltip />
    <el-table-column align="center" label="合同类型" prop="contractTypeName" width="100" />
    <el-table-column align="center" label="合同编码" prop="contractCode" width="150" />
    <el-table-column align="center" label="合同名称" min-width="200">
      <template #default="{ row }">
        <el-popover trigger="hover" placement="right" :width="360">
          <template #reference>
            <span class="cursor-pointer text-blue-600" @click="emit('viewDetail', row)">{{ row.contractName }}</span>
          </template>
          <ContractHoverCard :contract="row" />
        </el-popover>
      </template>
    </el-table-column>
    <el-table-column align="center" label="状态" width="100">
      <template #default="{ row }">
        <el-tag :type="row.status === 'signed' ? 'success' : 'warning'">
          {{ row.status === 'signed' ? '已签署' : '未签署' }}
        </el-tag>
        <div v-if="row.status === 'unsigned' && row.subStatus" class="text-11px text-gray-400 mt-2px">
          {{ row.subStatus === 'signing' ? '签署中' : '待签署' }}
        </div>
      </template>
    </el-table-column>
    <el-table-column align="center" label="下发日期" prop="issuedDate" width="110" />
    <el-table-column align="center" label="签署日期" prop="signDate" width="110" />
    <el-table-column align="center" label="操作" width="240" fixed="right">
      <template #default="{ row }">
        <!-- 管理员：编辑 -->
        <el-button v-hasPermi="['dealer:signing:update']" link type="primary" @click="emit('edit', row)">编辑</el-button>
        <!-- 经销商：去签署 -->
        <el-button
          v-hasPermi="['dealer:signing:sign']"
          v-if="row.status === 'unsigned' && row.subStatus === 'pending'"
          link type="success"
          @click="emit('sign', row)"
        >去签署</el-button>
        <!-- 执行员：上传盖章文件 -->
        <el-button
          v-hasPermi="['dealer:signing:upload-proof']"
          v-if="row.status === 'unsigned' && row.subStatus === 'signing'"
          link type="warning"
          @click="emit('uploadProof', row)"
        >上传盖章文件</el-button>
        <!-- 附件 -->
        <el-popover v-if="row.fileIds" trigger="hover" placement="left" :width="280">
          <template #reference>
            <el-button link type="info">附件</el-button>
          </template>
          <AttachmentPopover :file-ids="row.fileIds" />
        </el-popover>
        <!-- 咨询 -->
        <el-button v-hasPermi="['dealer:signing:consult']" link>咨询</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script lang="ts" setup>
import type { SigningContractVO } from '@/api/opshub/signing'
import ContractHoverCard from './ContractHoverCard.vue'
import AttachmentPopover from './AttachmentPopover.vue'

defineProps<{
  list: SigningContractVO[]
}>()

const emit = defineEmits<{
  (e: 'selectionChange', rows: SigningContractVO[]): void
  (e: 'edit', row: SigningContractVO): void
  (e: 'sign', row: SigningContractVO): void
  (e: 'uploadProof', row: SigningContractVO): void
  (e: 'viewDetail', row: SigningContractVO): void
}>()
</script>
