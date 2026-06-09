
//---------------------------邮件类型---------------------------------------------
export const EMAIL_TYPE_ENUM = {
  NOTICE: 0,
  ALERT: 1,
}

export const EMAIL_TYPE_MAP = {
  0: '公告',
  1: '告警',
}

export const EMAIL_TYPE_OPTIONS = [
  { label: '公告', value: 0 },
  { label: '告警', value: 1 },
]



//---------------------------邮件状态---------------------------------------------
export const EMAIL_STATUS_ENUM = {
  DRAFT: 0,
  PASS: 1,
  SENT: 2,
}

export const EMAIL_STATUS_MAP = {
  0: '草稿',
  1: '提交',
  2: '已发',
}

export const EMAIL_STATUS_OPTIONS = [
  { label: '草稿', value: 0 },
  { label: '提交', value: 1 },
  { label: '已发', value: 2 },
]
