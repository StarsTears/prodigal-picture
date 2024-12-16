export const USER_ROLE_ENUM = {
  ADMINISTRATOR:'administrator',
  ADMIN: 'admin',
  USER: 'user',
}

export const USER_ROLE_MAP = {
  'administrator': '超级管理员',
  'admin': '管理员',
  'user': '普通用户',
}

export const USER_ROLE_OPTIONS = Object.keys(USER_ROLE_MAP).map((key) => {
  return {
    label: USER_ROLE_MAP[key],
    value: key,
  }
})
