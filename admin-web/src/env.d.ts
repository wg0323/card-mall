/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_TMAP_KEY?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
