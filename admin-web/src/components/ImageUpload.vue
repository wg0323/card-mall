<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { uploadImage, type ImageBizType } from '@/api/file';
import { IMAGE_ACCEPT, IMAGE_ALLOWED_TYPES, IMAGE_MAX_SIZE_MB } from '@/config';

const props = withDefaults(
  defineProps<{
    bizType: ImageBizType;
    shape?: 'square' | 'round';
    size?: number;
    /** 单张大小上限（MB），与后端 card-mall.file.max-size-mb 保持一致 */
    maxSizeMb?: number;
  }>(),
  { shape: 'square', size: 96, maxSizeMb: IMAGE_MAX_SIZE_MB },
);

const model = defineModel<string>({ required: true });
const uploading = ref(false);
const inputRef = ref<HTMLInputElement>();

function pick(): void {
  if (!uploading.value) {
    inputRef.value?.click();
  }
}

// 选完图先本地拦一道，不合格的不用跑一趟服务端
function checkFile(file: File): string {
  if (!IMAGE_ALLOWED_TYPES.includes(file.type)) {
    return '只支持 JPG、PNG、WebP 格式的图片';
  }
  if (file.size > props.maxSizeMb * 1024 * 1024) {
    return `图片不能超过 ${props.maxSizeMb}MB`;
  }
  return '';
}

async function onChange(event: Event): Promise<void> {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  // 清空 value：否则连续选同一张图不会再触发 change
  input.value = '';
  if (!file) {
    return;
  }
  const invalidReason = checkFile(file);
  if (invalidReason) {
    ElMessage.error(invalidReason);
    return;
  }
  uploading.value = true;
  try {
    model.value = await uploadImage(file, props.bizType);
    ElMessage.success('图片已上传');
  } catch {
    // 拦截器已提示
  } finally {
    uploading.value = false;
  }
}
</script>

<template>
  <div class="image-upload">
    <div class="row">
      <div class="preview" :class="shape" :style="{ width: `${size}px`, height: `${size}px` }" @click="pick">
        <img v-if="model" :src="model" alt="已上传图片" />
        <span v-else class="placeholder">{{ uploading ? '上传中' : '点击上传' }}</span>
      </div>
      <el-button v-if="model" link type="danger" @click="model = ''">移除</el-button>
    </div>
    <p class="hint">支持 JPG / PNG / WebP，单张不超过 {{ maxSizeMb }}MB</p>
    <input ref="inputRef" class="file-input" type="file" :accept="IMAGE_ACCEPT" @change="onChange" />
  </div>
</template>

<style scoped>
.image-upload {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.preview {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  cursor: pointer;
  background: var(--fill);
  border: 1px dashed var(--line);
  border-radius: var(--el-border-radius-base);
}

.preview.round {
  border-radius: 50%;
}

.preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder {
  font-size: 12px;
  color: var(--text-3);
}

.file-input {
  display: none;
}

.hint {
  margin: 0;
  font-size: 12px;
  line-height: 1.5;
  color: var(--text-3);
}
</style>
