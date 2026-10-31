<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { getStore, saveStore } from '@/api/store';
import ImageUpload from '@/components/ImageUpload.vue';
import MapPicker from '@/components/MapPicker.vue';

const formRef = ref<FormInstance>();
const loading = ref(false);
const saving = ref(false);

const DEFAULT_OPEN_TIME = '09:00';
const DEFAULT_CLOSE_TIME = '21:00';

// 营业时间拆成开始/结束两个下拉，存库仍拼成 HH:mm-HH:mm，小程序按这个格式判断营业中
const form = reactive({
  name: '',
  logo: '',
  phone: '',
  address: '',
  longitude: undefined as number | undefined,
  latitude: undefined as number | undefined,
  openTime: DEFAULT_OPEN_TIME,
  closeTime: DEFAULT_CLOSE_TIME,
});

const rules: FormRules = {
  name: [{ required: true, message: '请输入门店名称', trigger: 'blur' }],
};

async function load(): Promise<void> {
  loading.value = true;
  try {
    const store = await getStore();
    if (store) {
      form.name = store.name;
      form.logo = store.logo;
      form.phone = store.phone;
      form.address = store.address;
      form.longitude = store.longitude ?? undefined;
      form.latitude = store.latitude ?? undefined;
      const [openTime, closeTime] = (store.businessHours || '').split('-');
      form.openTime = openTime?.trim() || DEFAULT_OPEN_TIME;
      form.closeTime = closeTime?.trim() || DEFAULT_CLOSE_TIME;
    }
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false;
  }
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
    await saveStore({
      name: form.name,
      logo: form.logo,
      phone: form.phone,
      address: form.address,
      longitude: form.longitude ?? null,
      latitude: form.latitude ?? null,
      businessHours: `${form.openTime}-${form.closeTime}`,
    });
  } catch {
    saving.value = false;
    return;
  }
  saving.value = false;
  ElMessage.success('已保存');
}

onMounted(load);
</script>

<template>
  <div>
    <div v-loading="loading" class="panel">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px" class="form">
        <el-form-item label="门店名称" prop="name">
          <el-input v-model="form.name" maxlength="64" />
        </el-form-item>
        <el-form-item label="门店 Logo">
          <ImageUpload v-model="form.logo" biz-type="store" shape="round" :size="72" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" maxlength="20" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.address" maxlength="200" />
        </el-form-item>
        <el-form-item label="门店位置">
          <MapPicker
            v-model:longitude="form.longitude"
            v-model:latitude="form.latitude"
            v-model:address="form.address"
          />
          <p class="hint">在地图上选点，经纬度与详细地址会自动回填；门店无定位时留空即可</p>
        </el-form-item>
        <el-form-item label="营业时间">
          <div class="hours">
            <el-time-select
              v-model="form.openTime"
              start="00:00"
              step="00:30"
              end="23:30"
              placeholder="开始时间"
              class="hours-select"
            />
            <span class="hours-sep">至</span>
            <el-time-select
              v-model="form.closeTime"
              start="00:30"
              step="00:30"
              end="23:30"
              placeholder="结束时间"
              class="hours-select"
            />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
        </el-form-item>
      </el-form>
      <p class="hint">M1 单店，门店 id 固定为 1；多门店在 M4 引入。</p>
    </div>
  </div>
</template>

<style scoped>
.form {
  max-width: 560px;
}

.hint {
  margin: 0;
  font-size: 12px;
  color: var(--text-3);
}

.hours {
  display: flex;
  gap: 8px;
  align-items: center;
}

.hours-select {
  width: 120px;
}

.hours-sep {
  color: var(--text-3);
}
</style>
