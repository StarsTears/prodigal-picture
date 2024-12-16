<template>
  <a-form layout="inline" :model="searchParams" @finish="doSearch">
    <a-form-item label="账号">
      <a-input v-model:value="searchParams.userAccount" placeholder="输入账号"/>
    </a-form-item>
    <a-form-item label="用户名">
      <a-input v-model:value="searchParams.userName" placeholder="输入用户名"/>
    </a-form-item>
    <a-form-item label="用户角色">
      <a-select v-model:value="searchParams.userRole"
                :options="USER_ROLE_OPTIONS"
                placeholder="输入审核状态"
                style="min-width: 180px"
                allow-clear/>
    </a-form-item>

<!--    <a-form-item label="用户角色">-->
<!--        <a-checkable-tag-->
<!--          v-for="(role, index) in roleList"-->
<!--          :key="role"-->
<!--          v-model:checked="selectRoleList[index]"-->
<!--        >-->
<!--          {{ role === 'administrator' ? '超级管理员' : role === 'admin' ? '管理员' : '普通用户' }}-->
<!--        </a-checkable-tag>-->
<!--    </a-form-item>-->
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
    :scroll="{ x: 1500, y: 1000 }"
    @change="doTableChange">

    <template #bodyCell="{ column, record,index,text }">
      <template v-if="column.title === '序号'">
        {{ (pagination.current - 1) * pagination.pageSize + parseInt(index) + 1 }}
      </template>
      <template v-if="column.dataIndex === 'userAvatar'">
        <a-image :src="record.userAvatar" :width="50"/>
      </template>
      <!--可编辑字段-->
      <teleplate v-if="['userProfile', 'userAccount', 'userName'].includes(column.dataIndex)">
        <div>
          <a-input
            v-if="editableData[record.id]"
            v-model:value="editableData[record.id][column.dataIndex]"
            style="margin: -5px 0"
          />
          <template v-else>
            {{ text }}
          </template>
        </div>
      </teleplate>
      <template v-if="column.dataIndex === 'userRole'">
        <div v-if="record.userRole === 'administrator'">
          <a-tag color="purple">{{ USER_ROLE_MAP[record.userRole] }}</a-tag>
        </div>
        <div v-else-if="record.userRole === 'admin'">
          <a-tag color="green">{{ USER_ROLE_MAP[record.userRole] }}</a-tag>
        </div>
        <div v-else>
          <a-tag color="blue">{{ USER_ROLE_MAP[record.userRole] }}</a-tag>
        </div>
      </template>
      <template v-else-if="column.dataIndex === 'createTime'">
        {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
      </template>
      <template v-else-if="column.dataIndex === 'updateTime'">
        {{ dayjs(record.updateTime).format('YYYY-MM-DD HH:mm:ss') }}
      </template>
      <template v-else-if="column.key === 'action'">
        <a-space wrap>
          <a-button v-if="editableData[record.id]"
                    default
                    :icon="h(SaveOutlined)"
                    @click="doSave(record.id)">
            保存
          </a-button>
          <a-button v-if="editableData[record.id]"
                    type="dashed"
                    @click="doCancel(record.id)">
            取消
            <template #icon>
              <UndoOutlined />
            </template>
          </a-button>
          <a-button v-if="!editableData[record.id]"
                    type="primary"
                    :icon="h(EditOutlined)"
                    @click="doEdit(record.id)">
            编辑
          </a-button>
          <a-popconfirm v-if="!editableData[record.id]"
                        okText="确定" cancelText="取消"
                        title="Sure to Confirm?"
                        @confirm="doDelete(record.id)">
            <a-button danger :icon="h(DeleteOutlined)">
              删除
            </a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </template>
  </a-table>
</template>
<script lang="ts" setup>
import {h , computed, onMounted, reactive, ref, UnwrapRef} from "vue";
import {UndoOutlined, DownOutlined, DeleteOutlined, EditOutlined,SaveOutlined} from '@ant-design/icons-vue';
import type {TableColumnsType} from 'ant-design-vue';
import {message} from "ant-design-vue";
import dayjs from "dayjs";
import {cloneDeep} from 'lodash-es';
import {USER_ROLE_OPTIONS,USER_ROLE_MAP} from '@/constants/user'
import {deleteUserUsingDelete, listUserVoByPageUsingPost, updateUserUsingPost} from "@/api/systemController";

const columns: TableColumnsType = [
  {
    title: '序号',
    width: 50,
    fixed: 'left'
  },
  {
    title: 'id',
    dataIndex: 'id',
    fixed: 'left',
    // ellipsis: true, // 宽度不够会自动折行，但是鼠标放上去会显示"ellipsis"
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
    fixed: 'left'
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
    ellipsis: true,
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '会员编码',
    dataIndex: 'vipNumber',
  },
  {
    title: '邀请用户ID',
    dataIndex: 'inviteUser',
    ellipsis: true,
  },
  {
    title: '分享码',
    dataIndex: 'shareCode',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
  },
]
// 数据
const dataList = ref<API.UserVO[]>([])
const total = ref(0)
const loading = ref<boolean>(true)
// 搜索条件
const roleList = ref<string[]>(["administrator","admin","user"])
const selectRoleList = ref<boolean[]>([])
const searchParams = reactive<API.UserQueryDto>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'ascend'
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  //转换搜索参数
  // const params = {
  //   ...searchParams,
  //   userRole: ''
  // }
  // selectRoleList.value.forEach((userRole,index)=>{
  //   if (userRole){
  //     params.userRole = params.userRole+","+roleList.value[index]
  //   }
  // })
  const res = await listUserVoByPageUsingPost({
    ...searchParams
  })
  if (res.data) {
    dataList.value = res.data.records ?? []
    total.value = res.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.msg)
  }
  loading.value = false
}
// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
// 设置分页的中文语言包
const locale = {
  items_per_page: '/页',
  jump_to: '跳至',
  page: '页',
};
// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    locale: locale,
    showTotal: (total) => `共 ${total} 条`,
    pageSizeOptions: ['5', '10', '20', '30'],//可选的页面显示条数
  }
})

// 表格变化处理
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 获取数据
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  fetchData()
}

//编辑数据

// const dataSource = ref(dataList);
const editableData: UnwrapRef<Record<string, dataList.value>> = reactive({});
const doEdit = (key: string) => {
  editableData[key] = cloneDeep(dataList.value.filter(item => key === item.id)[0]);
  console.log("用户管理-编辑：", editableData[key])
};

const doSave = async (key: string) => {
  console.log("用户管理-保存：", editableData[key])
  Object.assign(dataList.value.filter(item => key === item.id)[0], editableData[key]);
 const res = await updateUserUsingPost(editableData[key])
  if (res.code === 0 && res.data){
    message.success('用户修改成功')
  }else{
    message.error('用户修改失败,'+res.msg)
  }
  delete editableData[key];
};
const doCancel = (key: string) => {
  delete editableData[key];
};

// 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteUserUsingDelete({id})
  if (res.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}
</script>

<style scoped>

</style>
