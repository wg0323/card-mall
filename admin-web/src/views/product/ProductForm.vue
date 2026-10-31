<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { createProduct, updateProduct } from '@/api/product';
import ImageUpload from '@/components/ImageUpload.vue';
import type { CardProductSaveRequest, CardProductVO } from '@/types/api';

const visible = defineModel<boolean>({ required: true });
const props = defineProps<{ product: CardProductVO | null }>();
const emit = defineEmits<{ saved: [] }>();

const BIZ_TAGS = ['服务卡', '体验卡', '套餐卡', '月卡'];

const formRef = ref<FormInstance>();
const saving = ref(false);
const isCountType = computed(() => form.cardType === 1);

const form = reactive<CardProductSaveRequest>(emptyForm());

const rules = computed<FormRules>(() => ({
  name: [{ required: true, message: '请输入卡名称', trigger: 'blur' }],
  cardType: [{ required: true, message: '请选择计量方式', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }],
  validDays: [{ required: true, message: '请输入有效天数', trigger: 'blur' }],
  totalTimes: isCountType.value ? [{ required: true, message: '请输入总次数', trigger: 'blur' }] : [],
  unitPrice: isCountType.value ? [{ required: true, message: '请输入单次原价', trigger: 'blur' }] : [],
}));

function emptyForm(): CardProductSaveRequest {
  return {
    name: '',
    cardType: 1,
    bizTag: '服务卡',
    price: 0,
    unitPrice: 0,
    totalTimes: 1,
    validDays: 365,
    coverImage: '',
    applicableService: '',
    purchaseNotice: '',
    status: 1,
    sort: 0,
  };
}

// 编辑回填逐字段赋值：不能 Object.assign 整行，否则 id / createTime 会被当成入参再提交一遍
function fill(source: CardProductVO | null): void {
  if (!source) {
    Object.assign(form, emptyForm());
    return;
  }
  form.name = source.name;
  form.cardType = source.cardType;
  form.bizTag = source.bizTag;
  form.price = source.price;
  form.unitPrice = source.unitPrice;
  form.totalTimes = source.totalTimes;
  form.validDays = source.validDays;
  form.coverImage = source.coverImage;
  form.applicableService = source.applicableService;
  form.purchaseNotice = source.purchaseNotice;
  form.status = source.status;
  form.sort = source.sort;
}

// 期限型不按次计费，切过去时把次数与单次原价清零，避免脏值入库
watch(
  () => form.cardType,
  (type) => {
    if (type === 2) {
      form.totalTimes = 0;
      form.unitPrice = 0;
    } else if (form.totalTimes === 0) {
      form.totalTimes = 1;
    }
  },
);

watch(visible, (open) => {
  if (!open) {
    return;
  }
  fill(props.product);
  formRef.value?.clearValidate();
});

async function submit(): Promise<void> {
  if (!formRef.value) {
    return;
  }
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) {
    return;
  }
  const editing = props.product !== null;
  saving.value = true;
  try {
    if (props.product) {
      await updateProduct(props.product.id, { ...form });
    } else {
      await createProduct({ ...form });
    }
  } catch {
    saving.value = false;
    return;
  }
  saving.value = false;
  ElMessage.success(editing ? '已保存' : '已创建');
  emit('saved');
}
</script>

<template>
  <el-drawer v-model="visible" :title="product ? '编辑卡产品' : '新建卡产品'" size="520px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
      <el-form-item label="卡名称" prop="name">
        <el-input v-model="form.name" maxlength="64" placeholder="如 洗车5次卡" />
      </el-form-item>

      <el-form-item label="计量方式" prop="cardType">
        <el-radio-group v-model="form.cardType">
          <el-radio :value="1">计次型</el-radio>
          <el-radio :value="2">期限型</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="营销标签">
        <el-select v-model="form.bizTag" class="full">
          <el-option v-for="tag in BIZ_TAGS" :key="tag" :label="tag" :value="tag" />
        </el-select>
      </el-form-item>

      <el-form-item label="售价" prop="price">
        <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="10" class="full" />
      </el-form-item>

      <template v-if="isCountType">
        <el-form-item label="总次数" prop="totalTimes">
          <el-input-number v-model="form.totalTimes" :min="1" :precision="0" class="full" />
        </el-form-item>
        <el-form-item label="单次原价" prop="unitPrice">
          <el-input-number v-model="form.unitPrice" :min="0.01" :precision="2" :step="10" class="full" />
        </el-form-item>
      </template>

      <el-form-item label="有效天数" prop="validDays">
        <el-input-number v-model="form.validDays" :min="1" :precision="0" class="full" />
      </el-form-item>

      <el-form-item label="封面图">
        <ImageUpload v-model="form.coverImage" biz-type="card" :size="120" />
      </el-form-item>

      <el-form-item label="适用服务">
        <el-input v-model="form.applicableService" maxlength="255" placeholder="纯文案，如 全场洗车通用" />
      </el-form-item>

      <el-form-item label="购买须知">
        <el-input
          v-model="form.purchaseNotice"
          type="textarea"
          :rows="3"
          maxlength="1000"
          show-word-limit
          placeholder="退款口径等须在此写明"
        />
      </el-form-item>

      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :precision="0" class="full" />
      </el-form-item>

      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">上架</el-radio>
          <el-radio :value="0">下架</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
    </template>
  </el-drawer>
</template>

<style scoped>
.full {
  width: 100%;
}
</style>
