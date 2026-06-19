<template>
  <ContentWrap>
    <div class="excel-import-page">
      <!-- 顶部标题 -->
      <div class="page-header">
        <div class="header-top">
          <div class="header-left">
            <h2>OpsHub 数据批量导入</h2>
            <el-tag type="info" size="small" effect="plain">13 张业务表</el-tag>
          </div>
          <el-button type="warning" :icon="Download" :loading="batchDownloading" @click="handleBatchDownload">
            批量打包下载全部模板
          </el-button>
        </div>
        <el-alert
          title="请按依赖层级顺序导入：L0 基础主数据 → L1 关联数据 → L2 业务主数据 → L3 业务子表 → L4 补充数据"
          type="info"
          :closable="false"
          show-icon
          style="margin-top: 12px"
        />
      </div>

      <!-- L0 基础主数据 -->
      <div class="level-group level-l0">
        <h3 class="level-title">
          <el-tag type="primary" size="small" effect="dark">L0</el-tag>
          基础主数据
          <span class="level-desc">经销商、产品线等基础主数据，请优先导入</span>
        </h3>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8" v-for="item in l0Cards" :key="item.type">
            <ImportCard :item="item" />
          </el-col>
        </el-row>
      </div>

      <!-- L1 关联数据 -->
      <div class="level-group level-l1">
        <h3 class="level-title">
          <el-tag type="success" size="small" effect="dark">L1</el-tag>
          关联数据
          <span class="level-desc">经销商与产品线的绑定关系</span>
        </h3>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8" v-for="item in l1Cards" :key="item.type">
            <ImportCard :item="item" />
          </el-col>
        </el-row>
      </div>

      <!-- L2 业务主数据 -->
      <div class="level-group level-l2">
        <h3 class="level-title">
          <el-tag type="warning" size="small" effect="dark">L2</el-tag>
          业务主数据
          <span class="level-desc">合同、订单、售后单等核心业务单据</span>
        </h3>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8" v-for="item in l2Cards" :key="item.type">
            <ImportCard :item="item" />
          </el-col>
        </el-row>
      </div>

      <!-- L3 业务子表 -->
      <div class="level-group level-l3">
        <h3 class="level-title">
          <el-tag type="danger" size="small" effect="dark">L3</el-tag>
          业务子表
          <span class="level-desc">订单明细、付款、发票、物流、时间线等子表数据</span>
        </h3>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8" v-for="item in l3Cards" :key="item.type">
            <ImportCard :item="item" />
          </el-col>
        </el-row>
      </div>

      <!-- L4 补充数据 -->
      <div class="level-group level-l4">
        <h3 class="level-title">
          <el-tag type="info" size="small" effect="dark">L4</el-tag>
          补充数据
          <span class="level-desc">资质文件、授权文件等基础数据文件</span>
        </h3>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8" v-for="item in l4Cards" :key="item.type">
            <ImportCard :item="item" />
          </el-col>
        </el-row>
      </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElButton, ElTag } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import download from '@/utils/download'
import { downloadAllTemplates } from '@/api/opshub/excelImport'
import ImportCard from './components/ImportCard.vue'
import type { ImportCardItem } from './types'

// 批量下载状态
const batchDownloading = ref(false)

// 批量打包下载
const handleBatchDownload = async () => {
  batchDownloading.value = true
  try {
    const res = await downloadAllTemplates()
    download.zip(res, 'OpsHub导入模板.zip')
    ElMessage.success('全部模板已打包下载')
  } catch (e: any) {
    ElMessage.error('批量下载失败：' + (e.message || '未知错误'))
  } finally {
    batchDownloading.value = false
  }
}

// ==================== 13 张卡片配置 ====================

const l0Cards: ImportCardItem[] = [
  { level: 'L0', type: 'dealer-info', name: '经销商信息', nameEn: 'dealer_info', fieldCount: 7, requiredFields: '经销商名称、经销商编码', fileName: '经销商导入模板.xlsx',
    enumFields: [{ field: '状态', values: ['正常', '停用'], tagType: 'success' }] },
  { level: 'L0', type: 'product-line', name: '产品线', nameEn: 'dealer_product_line', fieldCount: 5, requiredFields: '产品线名称、产品线编码', fileName: '产品线导入模板.xlsx',
    enumFields: [{ field: '状态', values: ['正常', '停用'], tagType: 'success' }] }
]

const l1Cards: ImportCardItem[] = [
  { level: 'L1', type: 'relation', name: '经销商产品线关联', nameEn: 'dealer_product_line_relation', fieldCount: 2, requiredFields: '经销商编码、产品线编码', fileName: '经销商产品线关联导入模板.xlsx' }
]

const l2Cards: ImportCardItem[] = [
  { level: 'L2', type: 'contract', name: '签约合同', nameEn: 'signing_contract', fieldCount: 9, requiredFields: '合同编码、经销商编码', fileName: '签约合同导入模板.xlsx',
    enumFields: [{ field: '合同类型', values: ['主合同', '政策合同', '补充协议', '终止协议'], tagType: 'primary' }] },
  { level: 'L2', type: 'order', name: '订单信息', nameEn: 'order_info', fieldCount: 9, requiredFields: '订单号、经销商编码', fileName: '订单导入模板.xlsx',
    enumFields: [
      { field: '进度状态', values: ['待确认', '已确认', '已发货', '已签收', '已完成'], tagType: 'info' },
      { field: '付款状态', values: ['未付款', '已付款'], tagType: 'success' },
      { field: '开票状态', values: ['未开票', '部分开票', '已开票'], tagType: 'warning' }
    ] },
  { level: 'L2', type: 'aftersale', name: '售后单', nameEn: 'aftersale_info', fieldCount: 15, requiredFields: '售后单号、经销商编码', fileName: '售后单导入模板.xlsx',
    enumFields: [
      { field: '处理方式', values: ['退货', '退换货', '退货退款'], tagType: 'danger' },
      { field: '售后原因', values: ['投诉', '召回', '破损'], tagType: 'warning' },
      { field: '进度状态', values: ['待处理', '进行中', '换货中', '已完成'], tagType: 'info' }
    ] }
]

const l3Cards: ImportCardItem[] = [
  { level: 'L3', type: 'order-product', name: '订单产品', nameEn: 'order_product', fieldCount: 7, requiredFields: '订单号、产品编码', fileName: '订单产品导入模板.xlsx' },
  { level: 'L3', type: 'order-payment', name: '订单付款', nameEn: 'order_payment', fieldCount: 7, requiredFields: '订单号、付款凭证号', fileName: '订单付款导入模板.xlsx',
    enumFields: [{ field: '状态', values: ['审批中', '已通过', '已拒绝'], tagType: 'warning' }] },
  { level: 'L3', type: 'order-invoice', name: '订单发票', nameEn: 'order_invoice', fieldCount: 10, requiredFields: '订单号、发票号', fileName: '订单发票导入模板.xlsx',
    enumFields: [{ field: '状态', values: ['待开票', '已开票'], tagType: 'info' }] },
  { level: 'L3', type: 'order-logistics', name: '订单物流', nameEn: 'order_logistics', fieldCount: 7, requiredFields: '订单号、物流单号', fileName: '订单物流导入模板.xlsx',
    enumFields: [{ field: '是否已完成', values: ['是', '否'], tagType: 'success' }] },
  { level: 'L3', type: 'order-timeline', name: '订单时间线', nameEn: 'order_timeline', fieldCount: 6, requiredFields: '订单号、节点编码', fileName: '订单时间线导入模板.xlsx',
    enumFields: [
      { field: '节点编码', values: ['下单', '已确认', '已发货', '已签收', '已完成'], tagType: 'info' },
      { field: '是否完成', values: ['是', '否'], tagType: 'success' }
    ] },
  { level: 'L3', type: 'aftersale-progress', name: '售后进度', nameEn: 'aftersale_progress', fieldCount: 7, requiredFields: '售后单号、节点编码', fileName: '售后进度导入模板.xlsx',
    enumFields: [
      { field: '节点编码', values: ['申请提交', '审核通过', '商品退回', '退款完成', '红字发票', '新商品发出', '确认收货'], tagType: 'info' },
      { field: '是否完成', values: ['是', '否'], tagType: 'success' }
    ] }
]

const l4Cards: ImportCardItem[] = [
  { level: 'L4', type: 'basedata-file', name: '基础数据文件', nameEn: 'basedata_file', fieldCount: 9, requiredFields: '经销商编码、文件名称、文件地址', fileName: '基础数据文件导入模板.xlsx',
    enumFields: [
      { field: '文件分类', values: ['资质文件', '授权文件', '合同文件', '产品文件'], tagType: 'primary' },
      { field: '状态', values: ['正常', '停用'], tagType: 'success' }
    ] }
]

</script>

<style scoped lang="scss">
.excel-import-page {
  .page-header {
    margin-bottom: 28px;

    .header-top {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .header-left {
        display: flex;
        align-items: center;
        gap: 10px;

        h2 {
          margin: 0;
          font-size: 20px;
          font-weight: 600;
          color: #1d2129;
        }
      }
    }
  }

  .level-group {
    margin-bottom: 28px;
    padding: 16px 20px 8px;
    border-radius: 8px;
    background: #fafbfc;
    border: 1px solid #f0f1f3;

    &.level-l0 { border-left: 3px solid #409EFF; }
    &.level-l1 { border-left: 3px solid #67C23A; }
    &.level-l2 { border-left: 3px solid #E6A23C; }
    &.level-l3 { border-left: 3px solid #F56C6C; }
    &.level-l4 { border-left: 3px solid #909399; }

    .level-title {
      margin: 0 0 16px 0;
      font-size: 15px;
      font-weight: 500;
      color: #1d2129;
      display: flex;
      align-items: center;
      gap: 8px;

      .level-desc {
        font-size: 12px;
        font-weight: 400;
        color: #a8abb2;
        margin-left: 4px;
      }
    }
  }
}
</style>
