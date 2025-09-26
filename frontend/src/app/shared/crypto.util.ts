// src/app/shared/crypto.util.ts

// --- base64 helpers (robust to URL-safe and missing padding) ---
const normalizeB64 = (s: string) => {
    s = s.replace(/-/g, '+').replace(/_/g, '/');
    const pad = s.length % 4;
    return pad ? s + '='.repeat(4 - pad) : s;
  };
  const ub = (b: string) => {
    const s = atob(normalizeB64(b));
    const u8 = new Uint8Array(s.length);
    for (let i = 0; i < s.length; i++) u8[i] = s.charCodeAt(i);
    return u8;
  };
  const b64 = (u8: Uint8Array) => btoa(String.fromCharCode(...u8));
  
  // --- password → key (PBKDF2) ---
  export async function deriveKey(password: string, saltB64: string, iterations = 150000): Promise<CryptoKey> {
    const enc = new TextEncoder();
    const pwKey = await crypto.subtle.importKey('raw', enc.encode(password), { name: 'PBKDF2' }, false, ['deriveKey']);
    const salt = ub(saltB64);
    return crypto.subtle.deriveKey(
      { name: 'PBKDF2', salt, iterations, hash: 'SHA-256' },
      pwKey,
      { name: 'AES-GCM', length: 256 },
      false,
      ['encrypt','decrypt']
    );
  }
  
  // --- symmetric encrypt/decrypt (AES-GCM) ---
  export async function encryptGCM(plaintext: string, key: CryptoKey) {
    const iv = crypto.getRandomValues(new Uint8Array(12));
    const ct = await crypto.subtle.encrypt({ name: 'AES-GCM', iv }, key, new TextEncoder().encode(plaintext));
    return { ivB64: b64(iv), ctB64: b64(new Uint8Array(ct)) };
  }
  
  export async function decryptGCMWithKey(ctB64: string, ivB64: string, key: CryptoKey) {
    const pt = await crypto.subtle.decrypt({ name: 'AES-GCM', iv: ub(ivB64) }, key, ub(ctB64));
    return new TextDecoder().decode(pt);
  }
  
  // --- optional: link-key mode helpers you may already have ---
  export async function genKey(): Promise<CryptoKey> {
    return crypto.subtle.generateKey({ name: 'AES-GCM', length: 256 }, true, ['encrypt','decrypt']);
  }
  export async function exportKeyRawB64(key: CryptoKey): Promise<string> {
    const raw = new Uint8Array(await crypto.subtle.exportKey('raw', key));
    return b64(raw);
  }
  // decrypt when you have a base64 raw key (link-only mode)
export async function decryptGCM(ctB64: string, ivB64: string, keyB64: string) {
    const normalizeB64 = (s: string) => {
      s = s.replace(/-/g, '+').replace(/_/g, '/');
      const pad = s.length % 4;
      return pad ? s + '='.repeat(4 - pad) : s;
    };
    const ub = (b: string) => {
      const s = atob(normalizeB64(b));
      const u8 = new Uint8Array(s.length);
      for (let i = 0; i < s.length; i++) u8[i] = s.charCodeAt(i);
      return u8;
    };
  
    const key = await crypto.subtle.importKey('raw', ub(keyB64), { name: 'AES-GCM' }, false, ['decrypt']);
    const pt = await crypto.subtle.decrypt({ name: 'AES-GCM', iv: ub(ivB64) }, key, ub(ctB64));
    return new TextDecoder().decode(pt);
  }
  