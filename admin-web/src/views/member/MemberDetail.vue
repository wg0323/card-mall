<script setup lang="ts">
import { ref, watch } from 'vue';
import { getMemberDetail } from '@/api/member';
import { dictOf, MEMBER_CARD_STATUS, PAY_STATUS } from '@/constants/dict';
import type { MemberDetailVO } from '@/types/api';

const visible = defineModel<boolean>({ required: true });
const props = defineProps<{ memberId: number }>();

const loading = ref(false);
const detail = ref<MemberDetailVO | null>(null);

watch(visible, async (open) => {
  if (!open || !props.memberId) {
    return;
  }
  loading.value = true;
  try {
    detail.value = await getMemberDetail(props.memberId);
  } catch {
    detail.value = null;
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <el-drawer v-model="visible" title="会员详情" size="760px">
    <div v-loading="loading">
      <el-descriptions v-if="detail?.member" :column="2" border>
        <el-descriptions-item label="昵称">
          <div class="member-name">
            <el-avatar :size="32" :src="detail.member.avatar || undefined" class="member-avatar">
              {{ (detail.member.nickname || '微').slice(0, 1) }}
            </el-avatar>
            <span>{{ detail.member.nickname || '微信用户' }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.member.phone || '未绑定' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ detail.member.createTime }}</el-descriptions-item>
        <el-descriptions-item label="最近登录">{{ detail.member.lastLoginTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else-if="!loading" description="暂无数据" :image-size="72" />

      <h3 class="panel-title section">持卡</h3>
      <el-table :data="detail?.cards ?? []" empty-text="该会员还没有卡">
        <el-table-column prop="cardName" label="卡名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="余次" width="90" align="right">
          <template #default="{ row }">
            {{ row.cardType === 1 ? `${row.remainingTimes} / ${row.totalTimes}` : '不限次' }}
          </template>
        </el-table-column>
        <el-table-column prop="validEnd" label="有效期至" min-width="170" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="dictOf(MEMBER_CARD_STATUS, row.status).type" size="small">
              {{ dictOf(MEMBER_CARD_STATUS, row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <h3 class="panel-title section">核销记录</h3>
      <el-table :data="detail?.verifies ?? []" empty-text="该会员还没有核销记录">
        <el-table-column prop="recordNo" label="核销单号" width="180" />
        <el-table-column prop="cardName" label="卡名称" min-width="170" show-overflow-tooltip />
        <el-table-column prop="verifyTimes" label="次数" width="70" align="right" />
        <el-table-column label="余次变化" width="110" align="right">
          <template #default="{ row }">
            <span v-if="row.remainingAfter === null">-</span>
            <span v-else class="num">{{ row.remainingBefore }} → {{ row.remainingAfter }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="staffName" label="核销员工" width="100" />
        <el-table-column prop="verifyTime" label="核销时间" min-width="170" />
      </el-table>

      <h3 class="panel-title section">订单</h3>
      <el-table :data="detail?.orders ?? []" empty-text="该会员还没有订单">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="productName" label="卡名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="金额" width="100" align="right">
          <template #default="{ row }">
            <span class="num">¥{{ Number(row.amount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="dictOf(PAY_STATUS, row.payStatus).type" size="small">
              {{ dictOf(PAY_STATUS, row.payStatus).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" min-width="170" />
      </el-table>
    </div>
  </el-drawer>
</template>

<style scoped>
.section {
  margin: 24px 0 12px;
}

/* 头像与昵称同行，没有头像时用昵称首字占位 */
.member-name {
  display: flex;
  gap: 8px;
  align-items: center;
}

.member-avatar {
  font-size: 13px;
  color: var(--brand);
  background: var(--brand-light);
}
</style>
