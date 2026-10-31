<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import brandLogo from '@/assets/brand-logo.svg';
import { login } from '@/api/auth';
import { BRAND_NAME } from '@/config';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const user = useUserStore();
const brandName = BRAND_NAME;

const formRef = ref<FormInstance>();
const loading = ref(false);
const form = reactive({ phone: '', password: '' });
const rules: FormRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

async function submit(): Promise<void> {
  if (!formRef.value) {
    return;
  }
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) {
    return;
  }
  loading.value = true;
  try {
    const result = await login({ phone: form.phone, password: form.password });
    user.setLogin(result);
    ElMessage.success(`欢迎回来，${result.name}`);
    router.replace('/dashboard');
  } catch {
    // 失败原因由请求拦截器统一提示，这里不重复弹
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="login">
    <div class="card">
      <div class="brand">
        <img :src="brandLogo" :alt="brandName" width="40" height="40" />
        <div>
          <h1>{{ brandName }}</h1>
          <p>商家后台</p>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="submit">
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="登录手机号" autocomplete="username" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="登录密码"
            show-password
            autocomplete="current-password"
            @keyup.enter="submit"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="submit" :loading="loading" @click="submit">
          登录
        </el-button>
      </el-form>

      <p class="hint">首次登录请用初始口令，登录后立即在右上角修改密码</p>
    </div>
  </div>
</template>

<style scoped>
.login {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: var(--dark-sidebar);
}

.card {
  width: 380px;
  padding: 36px 32px 28px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 32px rgb(0 0 0 / 24%);
}

.brand {
  display: flex;
  gap: 14px;
  align-items: center;
  margin-bottom: 28px;
}

.brand h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-1);
}

.brand p {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--text-3);
}

.submit {
  width: 100%;
  margin-top: 4px;
}

.hint {
  margin: 18px 0 0;
  font-size: 12px;
  line-height: 1.6;
  color: var(--text-3);
}
</style>
