<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { pageProducts, updateProductStatus } from '@/api/product';
import { CARD_STATUS, CARD_TYPE, dictOf } from '@/constants/dict';
import type { CardProductVO } from '@/types/api';
import ProductForm from '@/views/product/ProductForm.vue';

const loading = ref(false);
const list = ref<CardProductVO[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

const formVisible = ref(false);
const editing = ref<CardProductVO | null>(null);

const route = useRoute();
const router = useRouter();

async function load(): Promise<void> {
  loading.value = true;
  try {
    const page = await pageProducts(pageNum.value, pageSize.value);
    list.value = page.list;
    total.value = page.total;
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false;
  }
}

function openCreate(): void {
  editing.value = null;
  formVisible.value = true;
}

function openEdit(row: CardProductVO): void {
  editing.value = row;
  formVisible.value = true;
}

function onSaved(): void {
  formVisible.value = false;
  load();
}

async function toggleStatus(row: CardProductVO): Promise<void> {
  const next = row.status === 1 ? 0 : 1;
  const action = next === 1 ? '上架' : '下架';
  try {
    await ElMessageBox.confirm(`确认${action}「${row.name}」？`, `${action}确认`, {
      type: 'warning',
      confirmButtonText: action,
      cancelButtonText: '取消',
    });
  } catch {
    return;
  }
  try {
    await updateProductStatus(row.id, next);
  } catch {
    return;
  }
  ElMessage.success(`已${action}`);
  load();
}

function changePage(page: number): void {
  pageNum.value = page;
  load();
}

function changeSize(size: number): void {
  pageSize.value = size;
  pageNum.value = 1;
  load();
}

onMounted(() => {
  load();
  // 工作台快捷入口带 create=1 进来，直接弹新建；随即清掉 query，刷新不会重复弹
  if (route.query.create === '1') {
    openCreate();
    router.replace({ query: {} });
  }
});
</script>

<template>
  <div>
    <div class="page-header">
      <div>
        <el-button @click="load">刷新</el-button>
        <el-button type="primary" @click="openCreate">新建卡产品</el-button>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="list" empty-text="还没有卡产品，先创建一个">
        <el-table-column prop="name" label="卡名称" width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="dictOf(CARD_TYPE, row.cardType).type" size="small">
              {{ dictOf(CARD_TYPE, row.cardType).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bizTag" label="标签" width="90" />
        <el-table-column label="售价" width="90" align="right">
          <template #default="{ row }">
            <span class="num">¥{{ Number(row.price).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="次数 / 有效期" width="140">
          <template #default="{ row }">
            {{ row.cardType === 1 ? `${row.totalTimes} 次` : '不限次' }} / {{ row.validDays }} 天
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" align="right" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="dictOf(CARD_STATUS, row.status).type" size="small">
              {{ dictOf(CARD_STATUS, row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <!-- 宽屏下由这一列吸收剩余宽度，避免上面几列被均匀拉伸；min-width=1 去掉默认 80px 下限 -->
        <el-table-column min-width="1" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        layout="total, sizes, prev, pager, next"
        :total="total"
        :current-page="pageNum"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        @current-change="changePage"
        @size-change="changeSize"
      />
    </div>

    <ProductForm v-model="formVisible" :product="editing" @saved="onSaved" />
  </div>
</template>

<style scoped>
.pager {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
