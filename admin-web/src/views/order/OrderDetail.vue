<script setup lang="ts">
import { ref, watch } from 'vue';
import { getOrder } from '@/api/order';
import { dictOf, PAY_STATUS, PAY_TRANSACTION_STATUS } from '@/constants/dict';
import type { CardOrderDetailVO } from '@/types/api';

const visible = defineModel<boolean>({ required: true });
const props = defineProps<{ orderNo: string }>();

const loading = ref(false);
const order = ref<CardOrderDetailVO | null>(null);

watch(visible, async (open) => {
  if (!open || !props.orderNo) {
    return;
  }
  loading.value = true;
  try {
    order.value = await getOrder(props.orderNo);
  } catch {
    order.value = null;
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <el-drawer v-model="visible" title="订单详情" size="560px">
    <div v-loading="loading">
      <el-descriptions v-if="order" :column="1" border>
        <el-descriptions-item label="订单号">{{ order.order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="卡名称">{{ order.order.productName }}</el-descriptions-item>
        <el-descriptions-item label="会员">
          {{ order.order.memberNickname || order.order.memberPhone || `会员 ${order.order.memberId}` }}
        </el-descriptions-item>
        <el-descriptions-item label="实付金额">
          <span class="num">¥{{ Number(order.order.amount).toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付渠道">{{ order.order.payChannel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="dictOf(PAY_STATUS, order.order.payStatus).type" size="small">
            {{ dictOf(PAY_STATUS, order.order.payStatus).label }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ order.order.paidTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="关闭时间">{{ order.order.closeTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ order.order.createTime }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else-if="!loading" description="暂无数据" :image-size="72" />

      <h3 class="panel-title section">支付流水</h3>
      <el-table :data="order?.payTransactions ?? []" empty-text="该订单还没有支付流水">
        <el-table-column prop="transactionId" label="渠道单号" min-width="200" show-overflow-tooltip />
        <el-table-column prop="channel" label="渠道" width="90" />
        <el-table-column label="金额" width="110" align="right">
          <template #default="{ row }">
            <span class="num">¥{{ Number(row.amount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="dictOf(PAY_TRANSACTION_STATUS, row.status).type" size="small">
              {{ dictOf(PAY_TRANSACTION_STATUS, row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="finishTime" label="完成时间" min-width="170" />
      </el-table>
    </div>
  </el-drawer>
</template>
