<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import {
  ArrowDown,
  Avatar,
  ChatDotRound,
  Checked,
  Document,
  Expand,
  Fold,
  Odometer,
  Postcard,
  Setting,
  User,
} from '@element-plus/icons-vue';
import brandLogo from '@/assets/brand-logo.svg';
import { changePassword, logout } from '@/api/auth';
import { BRAND_NAME } from '@/config';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const user = useUserStore();

// 菜单按设计文档第 10 节排
const menus = [
  { path: '/dashboard', label: '工作台', icon: Odometer },
  { path: '/product', label: '卡产品', icon: Postcard },
  { path: '/member', label: '会员', icon: User },
  { path: '/order', label: '订单', icon: Document },
  { path: '/comment', label: '评论管理', icon: ChatDotRound },
  { path: '/verify', label: '核销记录', icon: Checked },
  { path: '/staff', label: '员工', icon: Avatar },
  { path: '/store', label: '门店设置', icon: Setting },
];

const brandName = BRAND_NAME;
const title = computed(() => route.meta.title ?? '');

// 侧边栏收起后只留图标，刷新不保持，避免多人共用电脑时状态不明
const collapsed = ref(false);

const pwdVisible = ref(false);
const pwdFormRef = ref<FormInstance>();
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' });
const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '新密码长度需在 6 到 32 位之间', trigger: 'blur' },
  ],
  confirmPassword: [{ required: true, message: '请再次输入新密码', trigger: 'blur' }],
};

function openPassword(): void {
  pwdForm.oldPassword = '';
  pwdForm.newPassword = '';
  pwdForm.confirmPassword = '';
  pwdVisible.value = true;
}

async function submitPassword(): Promise<void> {
  if (!pwdFormRef.value) {
    return;
  }
  const valid = await pwdFormRef.value.validate().catch(() => false);
  if (!valid) {
    return;
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致');
    return;
  }
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword });
  } catch {
    return;
  }
  pwdVisible.value = false;
  ElMessage.success('密码已修改，请用新密码重新登录');
  user.clear();
  router.replace('/login');
}

async function handleCommand(command: string): Promise<void> {
  if (command === 'password') {
    openPassword();
    return;
  }
  try {
    await ElMessageBox.confirm('退出后需要重新登录', '确认退出？', {
      type: 'warning',
      confirmButtonText: '退出',
      cancelButtonText: '取消',
    });
  } catch {
    return;
  }
  try {
    await logout(user.refreshToken);
  } catch {
    // 撤销刷新令牌失败不影响本地退出
  }
  user.clear();
  router.replace('/login');
}
</script>

<template>
  <div class="layout">
    <aside class="sidebar" :class="{ collapsed }">
      <div class="brand">
        <img :src="brandLogo" :alt="brandName" width="24" height="24" />
        <span v-if="!collapsed">{{ brandName }}</span>
      </div>
      <nav>
        <router-link
          v-for="item in menus"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          :title="collapsed ? item.label : ''"
        >
          <el-icon class="menu-icon"><component :is="item.icon" /></el-icon>
          <span v-if="!collapsed">{{ item.label }}</span>
        </router-link>
      </nav>
    </aside>

    <div class="main">
      <header class="topbar">
        <div class="crumb">
          <button
            class="collapse-btn"
            type="button"
            :title="collapsed ? '展开菜单' : '收起菜单'"
            @click="collapsed = !collapsed"
          >
            <el-icon><component :is="collapsed ? Expand : Fold" /></el-icon>
          </button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <el-dropdown @command="handleCommand">
          <span class="user">{{ user.name || '未命名' }}<el-icon class="el-icon--right"><arrow-down /></el-icon></span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <section class="content">
        <router-view />
      </section>
    </div>

    <el-dialog v-model="pwdVisible" title="修改密码" width="420px">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password autocomplete="off" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password autocomplete="off" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPassword">保存并重新登录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  height: 100%;
}

.sidebar {
  display: flex;
  flex: 0 0 200px;
  flex-direction: column;
  background: var(--dark-sidebar);
  transition: flex-basis 0.2s;
}

.sidebar.collapsed {
  flex-basis: 64px;
}

.brand {
  display: flex;
  gap: 10px;
  align-items: center;
  height: 56px;
  padding: 0 20px;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
}

.sidebar.collapsed .brand {
  justify-content: center;
  padding: 0;
}

.brand img {
  border-radius: 4px;
  background: #fff;
}

.menu-item {
  position: relative;
  display: flex;
  gap: 10px;
  align-items: center;
  height: 44px;
  padding: 0 20px;
  font-size: 14px;
  color: rgb(255 255 255 / 72%);
  white-space: nowrap;
  text-decoration: none;
  overflow: hidden;
}

.sidebar.collapsed .menu-item {
  justify-content: center;
  padding: 0;
}

.menu-icon {
  flex: none;
  font-size: 18px;
}

.menu-item:hover {
  color: #fff;
}

.menu-item.router-link-active {
  color: #fff;
  background: var(--brand);
}

.menu-item.router-link-active::before {
  position: absolute;
  top: 8px;
  bottom: 8px;
  left: 0;
  width: 3px;
  content: '';
  background: var(--gold);
}

.main {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
}

.topbar {
  display: flex;
  flex: 0 0 56px;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid var(--line);
}

.crumb {
  display: flex;
  gap: 12px;
  align-items: center;
}

.collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  padding: 0;
  font-size: 18px;
  color: var(--text-2);
  cursor: pointer;
  background: none;
  border: none;
  border-radius: 4px;
}

.collapse-btn:hover {
  color: var(--text-1);
  background: var(--fill);
}

.user {
  font-size: 14px;
  color: var(--text-2);
  cursor: pointer;
  outline: none;
}

.content {
  flex: 1;
  padding: 20px;
  overflow: auto;
}
</style>
