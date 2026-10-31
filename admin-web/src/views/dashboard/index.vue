<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import * as echarts from 'echarts';
import { loadDashboard } from '@/api/dashboard';
import { dictOf, VERIFY_STATUS } from '@/constants/dict';
import type { DashboardVO } from '@/types/api';

const loading = ref(false);
const data = ref<DashboardVO | null>(null);
const router = useRouter();
const chartRef = ref<HTMLDivElement>();
let chart: echarts.ECharts | null = null;

const weekTotal = computed(() => (data.value?.last7Days ?? []).reduce((sum, item) => sum + item.count, 0));
const hasChartData = computed(() => (data.value?.last7Days ?? []).length > 0);

function money(value: number | undefined): string {
  return (value ?? 0).toFixed(2);
}

async function load(): Promise<void> {
  loading.value = true;
  try {
    data.value = await loadDashboard();
    // 等 v-show 把容器撑开再初始化，否则 echarts 拿到 0 宽高画不出来
    await nextTick();
    renderChart();
  } catch {
    // 拦截器已提示，这里保留空数据让页面显示占位
  } finally {
    loading.value = false;
  }
}

function renderChart(): void {
  const days = data.value?.last7Days ?? [];
  if (!chartRef.value || days.length === 0) {
    return;
  }
  chart ??= echarts.init(chartRef.value);
  chart.setOption({
    grid: { left: 40, right: 16, top: 24, bottom: 32 },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: days.map((item) => item.date),
      axisLine: { lineStyle: { color: '#e7e9ec' } },
      axisLabel: { color: '#8a9199' },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#f5f6f7' } },
      axisLabel: { color: '#8a9199' },
    },
    series: [
      {
        name: '核销次数',
        type: 'line',
        smooth: true,
        symbolSize: 6,
        data: days.map((item) => item.count),
        itemStyle: { color: '#16705a' },
        areaStyle: { color: 'rgba(22, 112, 90, 0.08)' },
      },
    ],
  });
}

function resize(): void {
  chart?.resize();
}

// 快捷入口直接落到「已打开新建表单」的目标页，少一次点击
function goCreate(target: 'product' | 'staff'): void {
  router.push({ path: `/${target}`, query: { create: '1' } });
}

onMounted(() => {
  load();
  window.addEventListener('resize', resize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize);
  chart?.dispose();
  chart = null;
});
</script>

<template>
  <div v-loading="loading">
    <div class="page-header">
      <div class="filters">
        <el-button @click="load">刷新</el-button>
        <el-button type="primary" @click="goCreate('product')">新建卡产品</el-button>
        <el-button @click="goCreate('staff')">新增员工</el-button>
      </div>
    </div>

    <el-row class="stat-row" :gutter="16">
      <el-col :span="6">
        <div class="panel stat">
          <span class="stat-label">今日核销次数</span>
          <strong class="stat-value num">{{ data?.todayVerifyCount ?? 0 }}</strong>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="panel stat">
          <span class="stat-label">今日新增会员</span>
          <strong class="stat-value num">{{ data?.todayNewMemberCount ?? 0 }}</strong>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="panel stat">
          <span class="stat-label">今日售卡金额</span>
          <strong class="stat-value stat-gold num">¥{{ money(data?.todaySaleAmount) }}</strong>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="panel stat">
          <span class="stat-label">近 7 日核销合计</span>
          <strong class="stat-value num">{{ weekTotal }}</strong>
        </div>
      </el-col>
    </el-row>

    <div class="panel">
      <h3 class="panel-title">近 7 日核销趋势</h3>
      <div v-show="hasChartData" ref="chartRef" class="chart"></div>
      <el-empty v-if="!hasChartData" description="暂无数据" :image-size="72" />
    </div>

    <div class="panel">
      <h3 class="panel-title">最近核销</h3>
      <el-table :data="data?.recentVerifies ?? []" empty-text="暂无数据">
        <el-table-column prop="cardName" label="卡名称" width="200" show-overflow-tooltip />
        <el-table-column label="会员" width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.memberNickname || row.memberPhone || '-' }}</template>
        </el-table-column>
        <el-table-column prop="verifyTimes" label="次数" width="80" align="right" />
        <el-table-column prop="staffName" label="核销员工" width="110" />
        <el-table-column prop="verifyTime" label="核销时间" width="170" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="dictOf(VERIFY_STATUS, row.status).type" size="small">
              {{ dictOf(VERIFY_STATUS, row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 宽屏下由这一列吸收剩余宽度，避免上面几列被均匀拉伸；min-width=1 去掉默认 80px 下限 -->
        <el-table-column min-width="1" />
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.filters {
  display: flex;
  gap: 8px;
}

.stat {
  display: flex;
  flex-direction: column;
  gap: 8px;
  height: 100%;
}

/* el-row 不受 .panel + .panel 约束，单独补上它与下方卡片的间距 */
.stat-row {
  margin-bottom: 16px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-3);
}

.stat-value {
  font-size: 26px;
  font-weight: 600;
  color: var(--text-1);
}

.stat-gold {
  color: var(--gold);
}

.chart {
  height: 260px;
}
</style>
