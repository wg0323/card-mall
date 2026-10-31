<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { createStaff, pageStaffs, updateStaffAvatar, updateStaffStatus } from '@/api/staff';
import ImageUpload from '@/components/ImageUpload.vue';
import { dictOf, STAFF_ROLE, STAFF_STATUS } from '@/constants/dict';
import type { StaffCreateRequest, StaffVO } from '@/types/api';

const loading = ref(false);
const list = ref<StaffVO[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

const route = useRoute();
const router = useRouter();

const dialogVisible = ref(false);
const saving = ref(false);
const avatarVisible = ref(false);
const avatarSaving = ref(false);
const avatarTarget = ref<StaffVO | null>(null);
const avatarValue = ref('');
const formRef = ref<FormInstance>();
const form = reactive<StaffCreateRequest>({ name: '', phone: '', role: 3 });
const rules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
};

async function load(): Promise<void> {
  loading.value = true;
  try {
    const page = await pageStaffs(pageNum.value, pageSize.value);
    list.value = page.list;
    total.value = page.total;
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false;
  }
}

function openCreate(): void {
  form.name = '';
  form.phone = '';
  form.role = 3;
  dialogVisible.value = true;
  formRef.value?.clearValidate();
}

async function submit(): Promise<void> {
  if (!formRef.value) {
    return;
  }
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) {
    return;
  }
  saving.value = true;
  try {
    await createStaff({ ...form });
  } catch {
    saving.value = false;
    return;
  }
  saving.value = false;
  dialogVisible.value = false;
  ElMessage.success('已创建，初始密码 123456，请提醒首次登录后修改');
  load();
}

async function toggleStatus(row: StaffVO): Promise<void> {
  const next = row.status === 1 ? 0 : 1;
  const action = next === 1 ? '启用' : '停用';
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
    await updateStaffStatus(row.id, next);
  } catch {
    return;
  }
  ElMessage.success(`已${action}`);
  load();
}

function openAvatar(row: StaffVO): void {
  avatarTarget.value = row;
  avatarValue.value = row.avatar ?? '';
  avatarVisible.value = true;
}

async function submitAvatar(): Promise<void> {
  if (!avatarTarget.value) {
    return;
  }
  avatarSaving.value = true;
  try {
    await updateStaffAvatar(avatarTarget.value.id, avatarValue.value);
  } catch {
    avatarSaving.value = false;
    return;
  }
  avatarSaving.value = false;
  avatarVisible.value = false;
  ElMessage.success('头像已更新');
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
  // 工作台快捷入口带 create=1 进来，直接弹新增；随即清掉 query，刷新不会重复弹
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
        <el-button type="primary" @click="openCreate">新增员工</el-button>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="list" empty-text="暂无员工">
        <el-table-column label="头像" width="72">
          <template #default="{ row }">
            <el-avatar v-if="row.avatar" :src="row.avatar" :size="32" />
            <el-avatar v-else :size="32" class="avatar-fallback">{{ row.name.slice(0, 1) }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="160" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">{{ STAFF_ROLE[row.role] ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="dictOf(STAFF_STATUS, row.status).type" size="small">
              {{ dictOf(STAFF_STATUS, row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <!-- 宽屏下由这一列吸收剩余宽度，避免上面几列被均匀拉伸；min-width=1 去掉默认 80px 下限 -->
        <el-table-column min-width="1" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openAvatar(row)">改头像</el-button>
            <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
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

    <el-dialog v-model="dialogVisible" title="新增员工" width="440px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" maxlength="32" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="20" placeholder="用于登录，全局唯一" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" class="full">
            <el-option label="老板" :value="1" />
            <el-option label="店长" :value="2" />
            <el-option label="店员" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <p class="hint">初始密码固定为 123456，仅老板与店长可登录后台。</p>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">创建</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="avatarVisible" :title="`设置「${avatarTarget?.name ?? ''}」的头像`" width="360px">
      <ImageUpload v-model="avatarValue" biz-type="staff" shape="round" :size="88" />
      <template #footer>
        <el-button @click="avatarVisible = false">取消</el-button>
        <el-button type="primary" :loading="avatarSaving" @click="submitAvatar">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.avatar-fallback {
  font-size: 13px;
  color: var(--brand);
  background: var(--brand-light);
}

.full {
  width: 100%;
}

.hint {
  margin: 0 0 0 80px;
  font-size: 12px;
  color: var(--text-3);
}

.pager {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
