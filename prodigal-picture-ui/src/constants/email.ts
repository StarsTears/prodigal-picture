
//---------------------------邮件类型---------------------------------------------
export const EMAIL_TYPE_ENUM = {
  NOTICE: 0,
  ALERT: 1,
  NOTIFY: 2,
}

export const EMAIL_TYPE_MAP = {
  0: '公告',
  1: '告警',
  2: '通知',
}

export const EMAIL_TYPE_COLOR_MAP: Record<number, string> = {
  0: 'blue',
  1: 'orange',
  2: 'green',
}

export const EMAIL_TYPE_OPTIONS = [
  { label: '公告', value: 0 },
  { label: '告警', value: 1 },
  { label: '通知', value: 2 },
]



//---------------------------邮件状态---------------------------------------------
export const EMAIL_STATUS_ENUM = {
  DRAFT: 0,
  SUBMITTED: 1,
  SENT: 2,
}

export const EMAIL_STATUS_MAP = {
  0: '草稿',
  1: '发送中',
  2: '已发',
}

export const EMAIL_STATUS_OPTIONS = [
  { label: '草稿', value: 0 },
  { label: '发送中', value: 1 },
  { label: '已发', value: 2 },
]
