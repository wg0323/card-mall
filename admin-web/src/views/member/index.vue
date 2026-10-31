<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { pageMembers } from '@/api/member';
import type { AdminMemberVO } from '@/types/api';
import MemberDetail from '@/views/member/MemberDetail.vue';

const loading = ref(false);
const list = ref<AdminMemberVO[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const keyword = ref('');

const detailVisible = ref(false);
const activeId = ref(0);

async function load(): Promise<void> {
  loading.value = true;
  try {
    // 后端约定：手机号精确匹配、昵称右模糊，空串按不过滤处理
    const page = await pageMembers(keyword.value.trim(), pageNum.value, pageSize.value);
    list.value = page.list;
    total.value = page.total;
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false;
  }
}

function search(): void {
  pageNum.value = 1;
  load();
}

function reset(): void {
  keyword.value = '';
  search();
}

function openDetail(row: AdminMemberVO): void {
  activeId.value = row.id;
  detailVisible.value = true;
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

onMounted(load);
</script>

<template>
  <div>
    <div class="page-header">
      <div class="filters">
        <el-input
          v-model="keyword"
          class="keyword"
          placeholder="手机号精确 / 昵称模糊"
          clearable
          @keyup.enter="search"
          @clear="search"
        />
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="list" empty-text="暂无会员">
        <el-table-column label="昵称" width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ row.nickname || '微信用户' }}</template>
        </el-table-column>
        <el-table-column label="手机号" width="140">
          <template #default="{ row }">{{ row.phone || '未绑定' }}</template>
        </el-table-column>
        <el-table-column label="持卡数" width="90" align="right">
          <template #default="{ row }">{{ row.cardCount }}</template>
        </el-table-column>
        <el-table-column label="累计核销" width="100" align="right">
          <template #default="{ row }"><span class="num">{{ row.totalVerifyTimes }}</span></template>
        </el-table-column>
        <el-table-column label="最近登录" width="180">
          <template #default="{ row }">{{ row.lastLoginTime || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <!-- 宽屏下由这一列吸收剩余宽度，避免上面几列被均匀拉伸；min-width=1 去掉默认 80px 下限 -->
        <el-table-column min-width="1" />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
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

    <MemberDetail v-model="detailVisible" :member-id="activeId" />
  </div>
</template>

<style scoped>
.filters {
  display: flex;
  gap: 8px;
  align-items: center;
}

.keyword {
  width: 220px;
}

.pager {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
