<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { pageVerifies } from '@/api/order';
import { dictOf, VERIFY_STATUS } from '@/constants/dict';
import type { VerifyRecordVO } from '@/types/api';

const loading = ref(false);
const list = ref<VerifyRecordVO[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const status = ref<number | undefined>(undefined);
const dateRange = ref<[string, string] | null>(null);

const detailVisible = ref(false);
const active = ref<VerifyRecordVO | null>(null);

async function load(): Promise<void> {
  loading.value = true;
  try {
    const page = await pageVerifies(
      status.value,
      dateRange.value?.[0],
      dateRange.value?.[1],
      pageNum.value,
      pageSize.value,
    );
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

function openDetail(row: VerifyRecordVO): void {
  active.value = row;
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
        <el-date-picker
          v-model="dateRange"
          class="date-range"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="search"
        />
        <el-select v-model="status" placeholder="全部状态" clearable class="status" @change="search">
          <el-option label="已核销" :value="1" />
          <el-option label="已撤销" :value="2" />
        </el-select>
        <el-button @click="load">刷新</el-button>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="list" empty-text="暂无核销记录">
        <el-table-column prop="recordNo" label="核销单号" width="180" />
        <el-table-column prop="cardName" label="卡名称" width="180" show-overflow-tooltip />
        <el-table-column label="会员" width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.memberNickname || row.memberPhone || '-' }}</template>
        </el-table-column>
        <el-table-column prop="verifyTimes" label="次数" width="80" align="right" />
        <el-table-column label="余次变化" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.remainingAfter === null">-</span>
            <span v-else class="num">{{ row.remainingBefore }} → {{ row.remainingAfter }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="staffName" label="核销员工" width="100" />
        <el-table-column prop="verifyTime" label="核销时间" width="180" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="dictOf(VERIFY_STATUS, row.status).type" size="small">
              {{ dictOf(VERIFY_STATUS, row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
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

    <el-drawer v-model="detailVisible" title="核销详情" size="520px">
      <el-descriptions v-if="active" :column="1" border>
        <el-descriptions-item label="核销单号">{{ active.recordNo }}</el-descriptions-item>
        <el-descriptions-item label="卡名称">{{ active.cardName }}</el-descriptions-item>
        <el-descriptions-item label="会员">
          {{ active.memberNickname || '-' }}（{{ active.memberPhone || '未绑定手机号' }}）
        </el-descriptions-item>
        <el-descriptions-item label="本次扣减">{{ active.verifyTimes }} 次</el-descriptions-item>
        <el-descriptions-item label="余次变化">
          <span v-if="active.remainingAfter === null">期限型不扣次</span>
          <span v-else class="num">{{ active.remainingBefore }} → {{ active.remainingAfter }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="核销员工">{{ active.staffName }}</el-descriptions-item>
        <el-descriptions-item label="核销时间">{{ active.verifyTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="dictOf(VERIFY_STATUS, active.status).type" size="small">
            {{ dictOf(VERIFY_STATUS, active.status).label }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注">{{ active.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<style scoped>
.filters {
  display: flex;
  gap: 8px;
  align-items: center;
}

.status {
  width: 150px;
}

.date-range {
  width: 260px;
}

.pager {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
