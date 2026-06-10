<template>
  <div id="dictManageView">
    <a-flex justify="space-between">
      <h2>字典管理</h2>
      <a-button type="primary" @click="doAdd">+ 新增字典</a-button>
    </a-flex>
    <div style="margin-bottom: 16px"/>
    <a-tabs v-model:activeKey="activeType" @change="onTabChange">
      <a-tab-pane v-for="type in dictTypes" :key="type" :tab="DICT_TYPE_MAP[type] || type"/>
    </a-tabs>
    <a-table
      :columns="columns"
      :data-source="dataList"
      :loading="loading"
      :pagination="false">
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.title === '序号'">
          {{ index + 1 }}
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button size="small" type="primary" :icon="h(EditOutlined)" @click="doEdit(record)">编辑</a-button>
            <a-popconfirm okText="确定" cancelText="取消" title="确定删除？" @confirm="doDelete(record.id)">
              <a-button size="small" danger :icon="h(DeleteOutlined)">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal
      :title="editingDict.id ? '编辑字典' : '新增字典'"
      :open="modalOpen"
      :confirm-loading="modalLoading"
      @ok="doSubmit"
      @cancel="modalOpen = false">
      <a-form :model="editingDict" layout="vertical">
        <a-form-item label="字典类型">
          <a-input v-model:value="editingDict.dictType" disabled/>
        </a-form-item>
        <a-form-item label="字典键">
          <a-input v-model:value="editingDict.dictKey" placeholder="输入字典键"/>
        </a-form-item>
        <a-form-item label="字典值">
          <a-input v-model:value="editingDict.dictValue" placeholder="输入字典值"/>
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="editingDict.sortOrder" :min="0" style="width: 100%"/>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { h, onMounted, ref } from "vue";
import { EditOutlined, DeleteOutlined } from '@ant-design/icons-vue';
import type { TableColumnsType } from 'ant-design-vue';
import { message } from "ant-design-vue";
import dayjs from "dayjs";
import { listDictTypesUsingGet, listDictByTypeUsingGet, addDictUsingPost, updateDictUsingPost, deleteDictUsingPost } from "@/api/dictController";

const DICT_TYPE_MAP: Record<string, string> = {
  PIC_CATEGORY: '图片分类',
  PIC_TAG: '图片标签',
};

const columns: TableColumnsType = [
  { title: '序号', width: 60 },
  { title: '字典键', dataIndex: 'dictKey', ellipsis: true },
  { title: '字典值', dataIndex: 'dictValue', ellipsis: true },
  { title: '排序', dataIndex: 'sortOrder', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 160 },
];

const dictTypes = ref<string[]>([]);
const activeType = ref('');
const dataList = ref<API.Dict[]>([]);
const loading = ref(false);

const fetchTypes = async () => {
  const res = await listDictTypesUsingGet();
  if (res.code === 0 && res.data) {
    dictTypes.value = res.data;
    if (dictTypes.value.length > 0 && !activeType.value) {
      activeType.value = dictTypes.value[0];
      fetchData();
    }
  }
};

const fetchData = async () => {
  if (!activeType.value) return;
  loading.value = true;
  const res = await listDictByTypeUsingGet(activeType.value);
  if (res.code === 0 && res.data) {
    dataList.value = res.data;
  } else {
    message.error('获取数据失败，' + res.msg);
  }
  loading.value = false;
};

const onTabChange = () => {
  fetchData();
};

onMounted(() => {
  fetchTypes();
});

const modalOpen = ref(false);
const modalLoading = ref(false);
const editingDict = ref<API.DictDTO>({});

const resetForm = () => {
  editingDict.value = {};
};

const doAdd = () => {
  resetForm();
  editingDict.value.dictType = activeType.value;
  modalOpen.value = true;
};

const doEdit = (record: API.Dict) => {
  editingDict.value = {
    id: record.id,
    dictType: record.dictType,
    dictKey: record.dictKey,
    dictValue: record.dictValue,
    sortOrder: record.sortOrder,
  };
  modalOpen.value = true;
};

const doSubmit = async () => {
  modalLoading.value = true;
  try {
    if (editingDict.value.id) {
      const res = await updateDictUsingPost(editingDict.value);
      if (res.code === 0) {
        message.success('更新成功');
        modalOpen.value = false;
        fetchData();
      } else {
        message.error('更新失败，' + res.msg);
      }
    } else {
      const res = await addDictUsingPost(editingDict.value);
      if (res.code === 0) {
        message.success('添加成功');
        modalOpen.value = false;
        fetchData();
      } else {
        message.error('添加失败，' + res.msg);
      }
    }
  } finally {
    modalLoading.value = false;
  }
};

const doDelete = async (id: number | undefined) => {
  if (!id) return;
  const res = await deleteDictUsingPost({ id });
  if (res.code === 0) {
    message.success('删除成功');
    fetchData();
  } else {
    message.error('删除失败，' + res.msg);
  }
};
</script>

<style scoped>
</style>
