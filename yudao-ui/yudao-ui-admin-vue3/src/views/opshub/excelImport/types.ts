export interface EnumField {
  field: string
  values: string[]
  tagType?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
}

export interface ImportCardItem {
  type: string
  name: string
  nameEn: string
  fieldCount: number
  requiredFields: string
  fileName: string
  enumFields?: EnumField[]
  level: 'L0' | 'L1' | 'L2' | 'L3' | 'L4'
}

export interface ImportResult {
  successCount: number
  insertCount: number
  updateCount: number
  failureCount: number
  failureRows: Record<number, string>
}

export const levelTagMap: Record<
  string,
  { type: 'primary' | 'success' | 'warning' | 'danger' | 'info'; label: string }
> = {
  L0: { type: 'primary', label: '基础' },
  L1: { type: 'success', label: '关联' },
  L2: { type: 'warning', label: '业务' },
  L3: { type: 'danger', label: '子表' },
  L4: { type: 'info', label: '补充' }
}

export const enumTagTypes: ('primary' | 'success' | 'warning' | 'danger' | 'info')[] = [
  'info',
  'success',
  'warning',
  'danger'
]
