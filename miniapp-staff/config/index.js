/**
 * 环境与接口地址：换环境只改 ENV，换品牌只改 BRAND_NAME
 * 作者：card-mall 创建日期：2026-10-31
 */
const ENV = 'dev';

// 真机调试时 localhost 指向手机自己，必须换成电脑的局域网 IP（ipconfig 里的 WLAN IPv4）；
// 手机与电脑要在同一个 WiFi 下，换网络后 IP 会变，记得同步这里。
const ENV_MAP = {
  dev: { baseUrl: 'http://localhost:8080/api' },
  prod: { baseUrl: 'https://your-domain.com/api' },
};

export const BASE_URL = ENV_MAP[ENV].baseUrl;
export const BRAND_NAME = 'XX 门店';
