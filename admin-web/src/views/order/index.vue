<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { pageOrders } from '@/api/order';
import { dictOf, PAY_STATUS } from '@/constants/dict';
import type { CardOrderVO } from '@/types/api';
import OrderDetail from '@/views/order/OrderDetail.vue';

const loading = ref(false);
const list = ref<CardOrderVO[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const payStatus = ref<number | undefined>(undefined);

const detailVisible = ref(false);
const activeOrderNo = ref('');

async function load(): Promise<void> {
  loading.value = true;
  try {
    const page = await pageOrders(payStatus.value, pageNum.value, pageSize.value);
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

function openDetail(row: CardOrderVO): void {
  activeOrderNo.value = row.orderNo;
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
        <el-select v-model="payStatus" placeholder="全部状态" clearable class="status" @change="search">
          <el-option label="待支付" :value="0" />
          <el-option label="已支付" :value="1" />
          <el-option label="已关闭" :value="2" />
          <el-option label="已退款" :value="3" />
        </el-select>
        <el-button @click="load">刷新</el-button>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="list" empty-text="暂无订单">
        <el-table-column prop="orderNo" label="订单号" width="170" />
        <el-table-column prop="productName" label="卡名称" width="190" show-overflow-tooltip />
        <el-table-column label="会员" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.memberNickname || row.memberPhone || `会员 ${row.memberId}` }}
          </template>
        </el-table-column>
        <el-table-column label="金额" width="100" align="right">
          <template #default="{ row }">
            <span class="num">¥{{ Number(row.amount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="支付渠道" width="100">
          <template #default="{ row }">{{ row.payChannel || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="dictOf(PAY_STATUS, row.payStatus).type" size="small">
              {{ dictOf(PAY_STATUS, row.payStatus).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" />
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

    <OrderDetail v-model="detailVisible" :order-no="activeOrderNo" />
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

.pager {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
