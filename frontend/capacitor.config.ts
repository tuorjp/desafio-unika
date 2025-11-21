import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'jp.tuor.frontend',
  appName: 'clientes',
  webDir: 'dist/frontend/browser',
  server: {
    url: "http://10.0.2.2:4200",
    cleartext: true,
    "hostname": "localhost"
  }
};

export default config;
