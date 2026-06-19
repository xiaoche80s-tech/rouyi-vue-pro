<template>
  <div class="import-card">
    <!-- 卡片头部：Level 徽章 + 名称 + 表名代码 -->
    <div class="card-head">
      <div class="card-head-left">
        <el-tag size="small" :type="levelInfo.type" effect="dark" round class="level-pill">
          {{ item.level }}
        </el-tag>
        <span class="card-name">{{ item.name }}</span>
      </div>
      <code class="card-table-name">{{ item.nameEn }}</code>
    </div>

    <!-- 卡片信息区 -->
    <div class="card-body">
      <div class="info-row">
        <span class="info-label">字段</span>
        <span class="info-value">{{ item.fieldCount }} 个</span>
      </div>
      <div class="info-row">
        <span class="info-label">必填</span>
        <span class="info-value required">{{ item.requiredFields }}</span>
      </div>

      <!-- 枚举字段折叠面板 -->
      <div v-if="item.enumFields && item.enumFields.length > 0" class="enum-section">
        <el-collapse class="enum-collapse">
          <el-collapse-item title="枚举字段说明" name="enum-fields">
            <div class="enum-list">
              <div v-for="(ef, idx) in item.enumFields" :key="ef.field" class="enum-item">
                <div class="enum-field-name">{{ ef.field }}</div>
                <div class="enum-tags">
                  <el-tag
                    v-for="v in ef.values"
                    :key="v"
                    size="small"
                    :type="ef.tagType ?? enumTagTypes[idx % enumTagTypes.length]"
                    effect="light"
                    class="enum-tag"
                  >
                    {{ v }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>

    <!-- 操作按钮区 -->
    <div class="card-footer">
      <el-button type="primary" class="footer-btn footer-btn-primary" @click="handleDownloadTemplate">
        <svg class="btn-svg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
          <polyline points="7 10 12 15 17 10" />
          <line x1="12" y1="15" x2="12" y2="3" />
        </svg>
        下载模板
      </el-button>
      <el-upload
        accept=".xlsx,.xls"
        :show-file-list="false"
        :before-upload="beforeUpload"
        :on-change="handleUpload"
        :disabled="loading"
        class="upload-wrapper"
      >
        <el-button type="success" :loading="loading" class="footer-btn footer-btn-success">
          <svg v-if="!loading" class="btn-svg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
            <polyline points="17 8 12 3 7 8" />
            <line x1="12" y1="3" x2="12" y2="15" />
          </svg>
          上传导入
        </el-button>
      </el-upload>
    </div>

    <!-- 导入结果 -->
    <div v-if="result" class="import-result">
      <el-descriptions :column="4" size="small" border>
        <el-descriptions-item label="成功">
          <span style="color: #67c23a; font-weight: bold">{{ result.successCount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="新增">
          <span style="color: #409eff">{{ result.insertCount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="更新">
          <span style="color: #e6a23c">{{ result.updateCount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="失败">
          <span :style="`color: ${result.failureCount > 0 ? '#f56c6c' : '#67c23a'}; font-weight: bold`">
            {{ result.failureCount }}
          </span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 失败明细 -->
      <el-collapse v-if="result.failureCount > 0 && result.failureRows" style="margin-top: 8px">
        <el-collapse-item :title="`失败明细（${result.failureCount} 行）`" name="failure-detail">
          <div class="failure-list">
            <div v-for="(reason, row) in result.failureRows" :key="row" class="failure-item">
              <el-tag type="danger" size="small">第 {{ row }} 行</el-tag>
              <span class="failure-reason">{{ reason }}</span>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadFile } from 'element-plus'
import download from '@/utils/download'
import { getImportTemplate, importExcel } from '@/api/opshub/excelImport'
import { levelTagMap, enumTagTypes } from '../types'
import type { ImportCardItem, ImportResult } from '../types'

const props = defineProps<{
  item: ImportCardItem
}>()

const loading = ref(false)
const result = ref<ImportResult | null>(null)

const levelInfo = computed(() => levelTagMap[props.item.level] || { type: 'info', label: props.item.level })

// 下载模板
const handleDownloadTemplate = async () => {
  try {
    const res = await getImportTemplate(props.item.type)
    download.excel(res, props.item.fileName)
  } catch (e: any) {
    ElMessage.error('下载模板失败：' + (e.message || '未知错误'))
  }
}

// 上传导入
const handleUpload = async (uploadFile: UploadFile) => {
  if (!uploadFile.raw) return
  loading.value = true
  result.value = null
  try {
    const res = await importExcel(props.item.type, uploadFile.raw)
    result.value = res as unknown as ImportResult
    if (res && res.failureCount === 0) {
      ElMessage.success(`${props.item.name} 导入完成：成功 ${res.successCount} 行`)
    } else if (res && res.failureCount > 0) {
      ElMessage.warning(
        `${props.item.name} 导入完成：成功 ${res.successCount} 行，失败 ${res.failureCount} 行`
      )
    }
  } catch (e: any) {
    ElMessage.error('导入失败：' + (e.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// beforeUpload 钩子
const beforeUpload = (file: File) => {
  const isExcel = file.name.endsWith('.xlsx') || file.name.endsWith('.xls')
  if (!isExcel) {
    ElMessage.error('仅支持 .xlsx 或 .xls 格式文件')
    return false
  }
  return true
}
</script>

<style scoped lang="scss">
.import-card {
  margin-bottom: 16px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  background: #fff;
  overflow: hidden;
  transition: box-shadow 0.25s ease, transform 0.25s ease, border-color 0.25s ease;

  &:hover {
    border-color: #d4d7de;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06), 0 2px 6px rgba(0, 0, 0, 0.04);
    transform: translateY(-2px);
  }

  // 卡片头部：Level Pill + 名称 + 表名代码
  .card-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 14px;
    background: linear-gradient(135deg, #f8f9fb 0%, #fff 100%);
    border-bottom: 1px solid #f2f3f5;

    .card-head-left {
      display: flex;
      align-items: center;
      gap: 8px;
      min-width: 0;

      .level-pill {
        flex-shrink: 0;
        font-size: 11px;
        font-weight: 600;
        letter-spacing: 0.3px;
      }

      .card-name {
        font-size: 14px;
        font-weight: 600;
        color: #1d2129;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }

    .card-table-name {
      font-size: 11px;
      color: #86909c;
      background: #f2f3f5;
      padding: 2px 8px;
      border-radius: 4px;
      font-family: 'SF Mono', 'Menlo', 'Monaco', 'Consolas', monospace;
      max-width: 200px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      flex-shrink: 0;
      margin-left: 8px;
    }
  }

  // 信息区
  .card-body {
    padding: 10px 14px;

    .info-row {
      display: flex;
      align-items: baseline;
      gap: 8px;
      padding: 3px 0;
      font-size: 13px;

      .info-label {
        color: #86909c;
        min-width: 28px;
        flex-shrink: 0;
      }

      .info-value {
        color: #4e5969;
      }

      .info-value.required {
        color: #e6a23c;
        font-size: 12px;
      }
    }

    .enum-section {
      margin-top: 6px;

      .enum-collapse {
        border: none;
        --el-collapse-header-bg-color: transparent;
        --el-collapse-content-bg-color: transparent;

        :deep(.el-collapse-item__header) {
          font-size: 12px;
          color: #86909c;
          height: 28px;
          line-height: 28px;
          border: none;
          padding-left: 0;
        }

        :deep(.el-collapse-item__wrap) {
          border: none;
        }

        :deep(.el-collapse-item__content) {
          padding: 0;
        }
      }

      .enum-list {
        padding: 4px 0 0;

        .enum-item {
          padding: 5px 0;
          border-bottom: 1px solid #f7f8fa;

          &:last-child {
            border-bottom: none;
          }

          .enum-field-name {
            font-size: 12px;
            color: #606266;
            margin-bottom: 4px;
            font-weight: 500;
          }

          .enum-tags {
            display: flex;
            flex-wrap: wrap;
            gap: 4px;

            .enum-tag {
              font-size: 11px;
              border-radius: 4px;
              padding: 0 6px;
            }
          }
        }
      }
    }
  }

  // 操作按钮区
  .card-footer {
    display: flex;
    gap: 8px;
    padding: 4px 14px 14px;

    .footer-btn {
      flex: 1;
      font-size: 13px;
      border-radius: 6px;
      font-weight: 500;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      transition: all 0.2s ease;

      .btn-svg-icon {
        width: 14px;
        height: 14px;
        flex-shrink: 0;
      }
    }

    .footer-btn-primary {
      background: #409eff;
      border-color: #409eff;
      color: #fff;

      &:hover {
        background: #66b1ff;
        border-color: #66b1ff;
        transform: translateY(-1px);
        box-shadow: 0 3px 8px rgba(64, 158, 255, 0.35);
      }

      &:active {
        transform: translateY(0);
      }
    }

    .footer-btn-success {
      background: #67c23a;
      border-color: #67c23a;
      color: #fff;

      &:hover {
        background: #85ce61;
        border-color: #85ce61;
        transform: translateY(-1px);
        box-shadow: 0 3px 8px rgba(103, 194, 58, 0.35);
      }

      &:active {
        transform: translateY(0);
      }
    }

    .upload-wrapper {
      flex: 1;
      display: flex;

      :deep(> div) {
        width: 100%;
      }

      :deep(.el-upload) {
        width: 100%;
      }
    }
  }

  // 导入结果
  .import-result {
    margin: 0 14px 14px;
    padding: 12px;
    background: #fafbfc;
    border-radius: 6px;
    border: 1px solid #f2f3f5;

    .failure-list {
      max-height: 200px;
      overflow-y: auto;

      .failure-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 4px 0;
        font-size: 13px;

        .failure-reason {
          color: #4e5969;
        }
      }
    }
  }
}
</style>
