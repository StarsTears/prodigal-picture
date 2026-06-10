<template>
  <div id="userManagerView">
    <a-flex justify="space-between">
      <h2>用户管理</h2>
      <a-button type="primary" @click="doAdd">+ 新增用户</a-button>
    </a-flex>
    <div style="margin-bottom: 16px"/>
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" allow-clear/>
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" allow-clear/>
      </a-form-item>
      <a-form-item label="角色">
        <a-select v-model:value="searchParams.userRole"
                  :options="USER_ROLE_OPTIONS"
                  placeholder="选择角色"
                  style="min-width: 140px"
                  allow-clear/>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <div style="margin-bottom: 16px"/>
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      :loading="loading"
      :scroll="{ x: 'max-content' }"
      @change="doTableChange">

      <template #bodyCell="{ column, record, index, text }">
        <template v-if="column.title === '序号'">
          {{ (pagination.current - 1) * pagination.pageSize + parseInt(index) + 1 }}
        </template>
        <template v-else-if="column.dataIndex === 'userInfo'">
          <a-space>
            <a-avatar v-if="record.userAvatar" :src="record.userAvatar" :size="32"/>
            <a-avatar v-else :style="{ color: '#f56a00', backgroundColor: '#fde3cf' }">
              {{ record.userName?.charAt(0) || '?' }}
            </a-avatar>
            <span>{{ record.userName || '-' }}</span>
          </a-space>
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <a-tag v-if="record.userRole === 'administrator'" color="purple">{{ USER_ROLE_MAP[record.userRole] }}</a-tag>
          <a-tag v-else-if="record.userRole === 'admin'" color="green">{{ USER_ROLE_MAP[record.userRole] }}</a-tag>
          <a-tag v-else color="blue">{{ USER_ROLE_MAP[record.userRole] }}</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'userEmail'">
          <span>{{ record.userEmail || '-' }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button size="small" :icon="h(EyeOutlined)" @click="doView(record)">查看</a-button>
            <a-button size="small" type="primary" :icon="h(EditOutlined)" @click="doEdit(record)">编辑</a-button>
            <a-popconfirm v-if="record.userRole !== 'administrator'" okText="确定" cancelText="取消" title="确定删除？" @confirm="doDelete(record.id)">
              <a-button size="small" danger :icon="h(DeleteOutlined)">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-drawer
      title="用户详情"
      :open="drawerOpen"
      :width="480"
      @close="drawerOpen = false">
      <template v-if="viewingUser">
        <a-flex justify="center" style="margin-bottom: 16px">
          <a-avatar v-if="viewingUser.userAvatar" :src="viewingUser.userAvatar" :size="80"/>
          <a-avatar v-else :size="80" :style="{ color: '#f56a00', backgroundColor: '#fde3cf', fontSize: '32px' }">
            {{ viewingUser.userName?.charAt(0) || '?' }}
          </a-avatar>
        </a-flex>
        <a-descriptions :column="1" bordered size="small">
          <a-descriptions-item label="ID">{{ viewingUser.id }}</a-descriptions-item>
          <a-descriptions-item label="账号">{{ viewingUser.userAccount || '-' }}</a-descriptions-item>
          <a-descriptions-item label="用户名">{{ viewingUser.userName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="邮箱">{{ viewingUser.userEmail || '-' }}</a-descriptions-item>
          <a-descriptions-item label="角色">
            <a-tag v-if="viewingUser.userRole === 'administrator'" color="purple">{{ USER_ROLE_MAP[viewingUser.userRole] }}</a-tag>
            <a-tag v-else-if="viewingUser.userRole === 'admin'" color="green">{{ USER_ROLE_MAP[viewingUser.userRole] }}</a-tag>
            <a-tag v-else color="blue">{{ USER_ROLE_MAP[viewingUser.userRole] }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="简介">{{ viewingUser.userProfile || '-' }}</a-descriptions-item>
          <a-descriptions-item label="会员编码">{{ viewingUser.vipNumber ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="邀请用户ID">{{ viewingUser.inviteUser ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="分享码">{{ viewingUser.shareCode ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ viewingUser.createTime ? dayjs(viewingUser.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}</a-descriptions-item>
          <a-descriptions-item label="更新时间">{{ viewingUser.updateTime ? dayjs(viewingUser.updateTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}</a-descriptions-item>
        </a-descriptions>
      </template>
    </a-drawer>

    <UserFormModal ref="userFormModalRef" :onReload="fetchData"/>
  </div>
</template>

<script lang="ts" setup>
import { h, computed, onMounted, reactive, ref } from "vue";
import { EyeOutlined, DeleteOutlined, EditOutlined } from '@ant-design/icons-vue';
import type { TableColumnsType } from 'ant-design-vue';
import { message } from "ant-design-vue";
import dayjs from "dayjs";
import { USER_ROLE_OPTIONS, USER_ROLE_MAP } from '@/constants/user';
import { deleteUserUsingDelete, listUserVoByPageUsingPost } from "@/api/userController";
import UserFormModal from "@/views/admin/UserFormModal.vue";

const columns: TableColumnsType = [
  {
    title: '序号',
    width: 60,
  },
  {
    title: '用户',
    dataIndex: 'userInfo',
    width: 160,
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
    width: 140,
    ellipsis: true,
  },
  {
    title: '邮箱',
    dataIndex: 'userEmail',
    width: 200,
    ellipsis: true,
  },
  {
    title: '角色',
    dataIndex: 'userRole',
    width: 110,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 160,
  },
  {
    title: '操作',
    key: 'action',
    width: 240,
  },
]

const dataList = ref<API.UserVO[]>([])
const total = ref(0)
const loading = ref<boolean>(true)

const searchParams = reactive<API.UserQueryDTO>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend'
})

const fetchData = async () => {
  loading.value = true
  const res = await listUserVoByPageUsingPost({ ...searchParams })
  if (res.code === 0 && res.data) {
    dataList.value = res.data.records ?? []
    total.value = res.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.msg)
  }
  loading.value = false
}

onMounted(() => {
  fetchData()
})

const locale = {
  items_per_page: '/页',
  jump_to: '跳至',
  page: '页',
};

const pagination = computed(() => ({
  current: searchParams.current ?? 1,
  pageSize: searchParams.pageSize ?? 10,
  total: total.value,
  showSizeChanger: true,
  locale,
  showTotal: (total: number) => `共 ${total} 条`,
  pageSizeOptions: ['5', '10', '20', '30'],
}))

const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

const doSearch = () => {
  searchParams.current = 1
  fetchData()
}

// 新增/编辑用户
const userFormModalRef = ref()

const doAdd = () => {
  userFormModalRef.value?.openModal()
}

const doEdit = (record: API.UserVO) => {
  userFormModalRef.value?.openModal(record)
}

// 查看详情
const drawerOpen = ref(false)
const viewingUser = ref<API.UserVO>()

const doView = (record: API.UserVO) => {
  viewingUser.value = record
  drawerOpen.value = true
}

// 删除
const doDelete = async (id: string) => {
  if (!id) return
  const res = await deleteUserUsingDelete({ id })
  if (res.code === 0) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败，' + res.msg)
  }
}
</script>

<style scoped>
</style>
