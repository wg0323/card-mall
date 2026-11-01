<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { deleteComment, pageComments, replyComment, updateCommentStatus } from '@/api/comment';
import { pageProducts } from '@/api/product';
import { COMMENT_STATUS, dictOf } from '@/constants/dict';
import type { AdminCommentVO, CardProductVO } from '@/types/api';

const loading = ref(false);
const list = ref<AdminCommentVO[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const productId = ref<number | undefined>();
const status = ref<number | undefined>();
const products = ref<CardProductVO[]>([]);

const replyVisible = ref(false);
const replySaving = ref(false);
const replyTarget = ref<AdminCommentVO | null>(null);
const replyForm = reactive({ replyContent: '' });

async function loadProducts(): Promise<void> {
  try {
    // M1 卡产品数量很少，一次取前 100 条做筛选下拉
    const page = await pageProducts(1, 100);
    products.value = page.list;
  } catch {
    // 拦截器已提示
  }
}

async function load(): Promise<void> {
  loading.value = true;
  try {
    const page = await pageComments({
      productId: productId.value || undefined,
      status: status.value || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    });
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
  productId.value = undefined;
  status.value = undefined;
  search();
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

function openReply(row: AdminCommentVO): void {
  replyTarget.value = row;
  replyForm.replyContent = row.replyContent ?? '';
  replyVisible.value = true;
}

async function submitReply(): Promise<void> {
  if (!replyTarget.value || replySaving.value) {
    return;
  }
  const content = replyForm.replyContent.trim();
  replySaving.value = true;
  try {
    await replyComment(replyTarget.value.id, content);
  } catch {
    return;
  } finally {
    replySaving.value = false;
  }
  replyVisible.value = false;
  ElMessage.success(content ? '回复已保存' : '回复已清除');
  load();
}

async function toggleStatus(row: AdminCommentVO): Promise<void> {
  const next = row.status === 1 ? 2 : 1;
  const action = next === 2 ? '屏蔽' : '恢复';
  try {
    await ElMessageBox.confirm(`确认${action}这条评论？`, `${action}确认`, {
      type: 'warning',
      confirmButtonText: action,
      cancelButtonText: '取消',
    });
  } catch {
    return;
  }
  try {
    await updateCommentStatus(row.id, next);
  } catch {
    return;
  }
  ElMessage.success(`已${action}`);
  load();
}

async function remove(row: AdminCommentVO): Promise<void> {
  try {
    await ElMessageBox.confirm(
      '删除后客户端不再展示，且该订单不能再次评论。确认删除？',
      '删除评论',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消',
      },
    );
  } catch {
    return;
  }
  try {
    await deleteComment(row.id);
  } catch {
    return;
  }
  ElMessage.success('已删除');
  load();
}

onMounted(() => {
  loadProducts();
  load();
});
</script>

<template>
  <div>
    <div class="page-header">
      <div class="filters">
        <el-select
          v-model="productId"
          class="product"
          placeholder="全部卡产品"
          clearable
          filterable
          @change="search"
        >
          <el-option v-for="item in products" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-select v-model="status" class="status" placeholder="全部状态" clearable @change="search">
          <el-option
            v-for="(item, key) in COMMENT_STATUS"
            :key="key"
            :label="item.label"
            :value="Number(key)"
          />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="list" empty-text="暂无评论">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="productName" label="卡产品" width="130" show-overflow-tooltip />
        <el-table-column label="会员" width="120">
          <template #default="{ row }">
            <div>{{ row.memberNickname || '微信用户' }}</div>
            <div class="sub">{{ row.memberPhone || '未绑定' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" width="205" show-overflow-tooltip />
        <el-table-column label="图片" width="170">
          <template #default="{ row }">
            <div v-if="row.images.length" class="thumbs">
              <el-image
                v-for="(url, index) in row.images"
                :key="url"
                :src="url"
                :preview-src-list="row.images"
                :initial-index="index"
                fit="cover"
                class="thumb"
                preview-teleported
              />
            </div>
            <span v-else class="sub">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="dictOf(COMMENT_STATUS, row.status).type" size="small">
              {{ dictOf(COMMENT_STATUS, row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="回复" width="70">
          <template #default="{ row }">
            <el-tag v-if="row.replyContent" type="success" size="small">已回复</el-tag>
            <span v-else class="sub">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评论时间" width="175" />
        <!-- 宽屏下由这一列吸收剩余宽度，避免上面几列被均匀拉伸；min-width=1 去掉默认 80px 下限 -->
        <el-table-column min-width="1" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="row.status === 3" @click="openReply(row)">
              回复
            </el-button>
            <el-button
              link
              :type="row.status === 1 ? 'warning' : 'success'"
              :disabled="row.status === 3"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '屏蔽' : '恢复' }}
            </el-button>
            <el-button link type="danger" :disabled="row.status === 3" @click="remove(row)">
              删除
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

    <el-dialog v-model="replyVisible" title="回复评论" width="520px">
      <div v-if="replyTarget" class="reply-quote">
        <div class="quote-meta">
          {{ replyTarget.memberNickname || '微信用户' }} · {{ replyTarget.productName }}
        </div>
        <div class="quote-content">{{ replyTarget.content }}</div>
      </div>
      <el-input
        v-model="replyForm.replyContent"
        type="textarea"
        :rows="4"
        maxlength="500"
        show-word-limit
        placeholder="填写回复内容；清空后保存即删除回复"
      />
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="replySaving" @click="submitReply">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.filters {
  display: flex;
  gap: 8px;
  align-items: center;
}

.product {
  width: 200px;
}

.status {
  width: 140px;
}

.pager {
  justify-content: flex-end;
  margin-top: 16px;
}

.thumbs {
  display: flex;
  gap: 4px;
}

.thumb {
  width: 36px;
  height: 36px;
  border-radius: 4px;
}

.sub {
  font-size: 13px;
  color: var(--text-3);
}

.reply-quote {
  padding: 12px;
  margin-bottom: 12px;
  background: var(--fill);
  border-radius: 8px;
}

.quote-meta {
  margin-bottom: 6px;
  font-size: 13px;
  color: var(--text-3);
}

.quote-content {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-1);
  white-space: pre-wrap;
}
</style>
