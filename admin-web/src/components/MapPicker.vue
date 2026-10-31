<script setup lang="ts">
import { computed, ref } from 'vue';
import { ElMessage } from 'element-plus';

declare global {
  interface Window {
    TMap?: TMapNamespace;
  }
}

interface TMapLatLng {
  lat: number;
  lng: number;
  getLat?: () => number;
  getLng?: () => number;
}

interface TMapMap {
  setCenter(center: TMapLatLng): void;
  on(event: 'click', handler: (event: { latLng: TMapLatLng }) => void): void;
  destroy(): void;
}

interface TMapMultiMarker {
  setGeometries(geometries: Array<{ id: string; position: TMapLatLng }>): void;
}

interface TMapSuggestionItem {
  title: string;
  address: string;
  location: TMapLatLng;
}

interface TMapSuggestionResult {
  status: number;
  message?: string;
  data?: TMapSuggestionItem[];
}

interface TMapAddressResult {
  status: number;
  message?: string;
  result?: { address?: string; formatted_addresses?: { recommend?: string } };
}

interface TMapNamespace {
  Map: new (container: HTMLElement, options: { center: TMapLatLng; zoom: number }) => TMapMap;
  MultiMarker: new (options: {
    map: TMapMap;
    geometries: Array<{ id: string; position: TMapLatLng }>;
  }) => TMapMultiMarker;
  LatLng: new (lat: number, lng: number) => TMapLatLng;
  service?: {
    Suggestion: new (options?: { pageSize?: number }) => {
      getSuggestions(params: { keyword: string }): Promise<TMapSuggestionResult>;
    };
    Geocoder: new () => {
      getAddress(params: { location: TMapLatLng }): Promise<TMapAddressResult>;
    };
  };
}

const props = defineProps<{
  longitude?: number;
  latitude?: number;
  address?: string;
}>();

const emit = defineEmits<{
  (e: 'update:longitude', value: number | undefined): void;
  (e: 'update:latitude', value: number | undefined): void;
  (e: 'update:address', value: string): void;
}>();

// 腾讯位置服务 JavaScript API GL 的 key，必须有 service 附加库才能搜索和逆解析
const TMAP_KEY = import.meta.env.VITE_TMAP_KEY;

// 没有历史坐标时的兜底中心，注意 TMap.LatLng 是纬度在前、经度在后
const DEFAULT_CENTER: [number, number] = [39.90923, 116.397428];

const visible = ref(false);
const loadingMap = ref(false);
const searching = ref(false);
const keyword = ref('');
const mapContainer = ref<HTMLDivElement>();
const picked = ref<{ longitude: number; latitude: number; address: string } | null>(null);

let map: TMapMap | null = null;
let marker: TMapMultiMarker | null = null;
let scriptLoading: Promise<TMapNamespace> | null = null;

const hasPosition = computed(
  () => typeof props.longitude === 'number' && typeof props.latitude === 'number',
);

const coordinateText = computed(() => {
  if (!hasPosition.value) {
    return '';
  }
  return `${props.longitude}, ${props.latitude}`;
});

const pickedText = computed(() => {
  if (!picked.value) {
    return '在地图上点击选择门店位置，或先按地址搜索';
  }
  const { longitude, latitude, address } = picked.value;
  return address ? `${longitude}, ${latitude} · ${address}` : `${longitude}, ${latitude}`;
});

function readLatLng(value: TMapLatLng): { lat: number; lng: number } {
  return {
    lat: typeof value.getLat === 'function' ? value.getLat() : value.lat,
    lng: typeof value.getLng === 'function' ? value.getLng() : value.lng,
  };
}

function loadTMap(): Promise<TMapNamespace> {
  if (window.TMap) {
    return Promise.resolve(window.TMap);
  }
  if (!TMAP_KEY) {
    return Promise.reject(new Error('未配置 VITE_TMAP_KEY，请在 admin-web/.env.local 中填写腾讯地图 key'));
  }
  if (!scriptLoading) {
    scriptLoading = new Promise<TMapNamespace>((resolve, reject) => {
      // 用 callback 参数等 API 真正就绪，直接听 script.onload 时 TMap 可能还没挂上
      const callbackName = `__tmapReady${Date.now()}`;
      const scope = window as unknown as Record<string, unknown>;
      scope[callbackName] = () => {
        delete scope[callbackName];
        if (window.TMap) {
          resolve(window.TMap);
        } else {
          reject(new Error('腾讯地图加载失败'));
        }
      };
      const script = document.createElement('script');
      script.src = `https://map.qq.com/api/gljs?v=1.exp&key=${TMAP_KEY}`
        + `&libraries=service&callback=${callbackName}`;
      script.onerror = () => reject(new Error('腾讯地图加载失败，请检查 key 与网络'));
      document.head.appendChild(script);
    });
  }
  return scriptLoading;
}

// 逆地址解析：把坐标换成人能看懂的地址，回填到详细地址
async function reverseGeocode(position: TMapLatLng, longitude: number, latitude: number): Promise<void> {
  const TMap = window.TMap;
  if (!TMap?.service) {
    return;
  }
  try {
    const result = await new TMap.service.Geocoder().getAddress({ location: position });
    const address = result?.result?.formatted_addresses?.recommend || result?.result?.address || '';
    const current = picked.value;
    if (!address || !current || current.longitude !== longitude || current.latitude !== latitude) {
      return;
    }
    picked.value = { ...current, address };
  } catch (error) {
    // 逆解析失败不影响选点，地址可以手填
  }
}

function setPoint(lat: number, lng: number): void {
  const next = {
    longitude: Number(lng.toFixed(6)),
    latitude: Number(lat.toFixed(6)),
    address: '',
  };
  picked.value = next;
  const TMap = window.TMap;
  if (!TMap) {
    return;
  }
  const position = new TMap.LatLng(lat, lng);
  marker?.setGeometries([{ id: 'picked', position }]);
  map?.setCenter(position);
  reverseGeocode(position, next.longitude, next.latitude);
}

async function search(): Promise<void> {
  const text = keyword.value.trim() || props.address?.trim() || '';
  if (!text) {
    return;
  }
  const TMap = window.TMap;
  if (!TMap?.service) {
    ElMessage.error('地图服务未就绪，请关闭弹窗重试');
    return;
  }
  searching.value = true;
  try {
    const result = await new TMap.service.Suggestion({ pageSize: 10 })
      .getSuggestions({ keyword: text });
    const poi = result?.data?.find((item) => item.location);
    if (result?.status !== 0 || !poi) {
      ElMessage.warning('没有搜到匹配位置，请直接在地图上点选');
      return;
    }
    const { lat, lng } = readLatLng(poi.location);
    setPoint(lat, lng);
  } catch (error) {
    // 把腾讯返回的原因带出来（如配额超限），比只说“搜索失败”好排查
    const message = error instanceof Error && error.message ? error.message : '';
    ElMessage.warning(message || '搜索失败，请直接在地图上点选');
  } finally {
    searching.value = false;
  }
}

function open(): void {
  keyword.value = props.address ?? '';
  picked.value = hasPosition.value
    ? {
        longitude: props.longitude as number,
        latitude: props.latitude as number,
        address: props.address ?? '',
      }
    : null;
  visible.value = true;
}

async function initMap(): Promise<void> {
  loadingMap.value = true;
  try {
    const TMap = await loadTMap();
    if (!mapContainer.value) {
      return;
    }
    const position = hasPosition.value
      ? new TMap.LatLng(props.latitude as number, props.longitude as number)
      : new TMap.LatLng(DEFAULT_CENTER[0], DEFAULT_CENTER[1]);
    map = new TMap.Map(mapContainer.value, { center: position, zoom: hasPosition.value ? 16 : 11 });
    marker = new TMap.MultiMarker({ map, geometries: [{ id: 'picked', position }] });
    map.on('click', (event) => {
      const { lat, lng } = readLatLng(event.latLng);
      setPoint(lat, lng);
    });
    // 首次设置：门店已有坐标就直接显示，没有就先按地址搜一次
    if (!hasPosition.value && props.address?.trim()) {
      await search();
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '地图加载失败');
    visible.value = false;
  } finally {
    loadingMap.value = false;
  }
}

function destroyMap(): void {
  map?.destroy();
  map = null;
  marker = null;
}

function confirm(): void {
  if (!picked.value) {
    return;
  }
  emit('update:longitude', picked.value.longitude);
  emit('update:latitude', picked.value.latitude);
  if (picked.value.address) {
    emit('update:address', picked.value.address);
  }
  visible.value = false;
}
</script>

<template>
  <div class="map-picker">
    <el-input :model-value="coordinateText" readonly placeholder="尚未选择位置">
      <template #append>
        <el-button @click="open">地图选点</el-button>
      </template>
    </el-input>

    <el-dialog
      v-model="visible"
      title="选择门店位置"
      width="760px"
      append-to-body
      @opened="initMap"
      @closed="destroyMap"
    >
      <div class="search">
        <el-input
          v-model="keyword"
          placeholder="输入地址或门店名称，搜索后取第一个结果"
          clearable
          @keyup.enter="search"
        />
        <el-button type="primary" :loading="searching" @click="search">搜索</el-button>
      </div>
      <div v-loading="loadingMap" ref="mapContainer" class="map" />
      <p class="tip">{{ pickedText }}</p>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :disabled="!picked" @click="confirm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.map-picker {
  width: 100%;
}

.search {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.search :deep(.el-input) {
  flex: 1;
}

.search :deep(.el-button) {
  flex-shrink: 0;
}

.map {
  /* TMap 的缩放、比例尺、logo 都是绝对定位，容器没有定位上下文会跑到弹窗角落 */
  position: relative;
  width: 100%;
  height: 420px;
  border-radius: 6px;
  overflow: hidden;
}

/* 腾讯左下角的“比例尺 + logo”组用行内 inset 定位，只能 !important 覆盖，往里挪 12px 留点呼吸 */
.map :deep(div:has(> div > .logo-text)) {
  left: 12px !important;
  bottom: 8px !important;
}

.tip {
  margin: 10px 0 0;
  font-size: 12px;
  color: var(--text-3);
}
</style>
