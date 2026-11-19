/* 
* All of BurnAfter's cryptography. It runs in the browser (WebCrypto API):
* the server only ever receives the ciphertext and the IV, never the key.
*
* The key travels in the URL fragment (the part after '#'), which browsers
* never send to the server. The fragment is either:
*   - the AES key itself (base64), or
*   - 'pwd:<salt>' when a password is used; the key is then derived from
*     password + salt, so the link alone is not enough to read the message.
*/

const PWD_PREFIX = 'pwd:';
const PBKDF2_ITERATIONS = 150_000; // makes each password guess slow

// Creates the key for a new message and the URL fragment the reader needs to rebuild it.
export async function newMessageKey(password: string): Promise<{ key: CryptoKey; fragment: string }> {
  if (password.trim()) {
    const salt = toB64(crypto.getRandomValues(new Uint8Array(16)));
    return { key: await keyFromPassword(password, salt), fragment: PWD_PREFIX + salt };
  }
  const key = await crypto.subtle.generateKey({ name: 'AES-GCM', length: 256 }, true, ['encrypt', 'decrypt']);
  const rawKey = new Uint8Array(await crypto.subtle.exportKey('raw', key));
  return { key, fragment: toB64(rawKey) };
}

export function isPasswordFragment(fragment: string): boolean {
  return fragment.startsWith(PWD_PREFIX);
}

// Rebuilds the key on the reader's side from the URL fragment (and the password, if any).
export function keyFromFragment(fragment: string, password: string): Promise<CryptoKey> {
  if (isPasswordFragment(fragment)) {
    return keyFromPassword(password, fragment.slice(PWD_PREFIX.length));
  }
  return crypto.subtle.importKey('raw', fromB64(fragment), 'AES-GCM', false, ['decrypt']);
}

// AES-256-GCM: encrypts and authenticates, so tampered data fails to decrypt.
export async function encrypt(plaintext: string, key: CryptoKey): Promise<{ ciphertext: string; iv: string }> {
  const iv = crypto.getRandomValues(new Uint8Array(12)); // must never repeat for the same key
  const ciphertext = await crypto.subtle.encrypt({ name: 'AES-GCM', iv }, key, new TextEncoder().encode(plaintext));
  return { ciphertext: toB64(new Uint8Array(ciphertext)), iv: toB64(iv) };
}

// Throws if the key is wrong or the data was modified.
export async function decrypt(ciphertext: string, iv: string, key: CryptoKey): Promise<string> {
  const plaintext = await crypto.subtle.decrypt({ name: 'AES-GCM', iv: fromB64(iv) }, key, fromB64(ciphertext));
  return new TextDecoder().decode(plaintext);
}

// PBKDF2-SHA256: turns a password + random salt into an AES key.
async function keyFromPassword(password: string, saltB64: string): Promise<CryptoKey> {
  const passwordKey = await crypto.subtle.importKey('raw', new TextEncoder().encode(password), 'PBKDF2', false, ['deriveKey']);
  return crypto.subtle.deriveKey(
    { name: 'PBKDF2', salt: fromB64(saltB64), iterations: PBKDF2_ITERATIONS, hash: 'SHA-256' },
    passwordKey,
    { name: 'AES-GCM', length: 256 },
    false,
    ['encrypt', 'decrypt']
  );
}

function toB64(bytes: Uint8Array): string {
  let binary = '';
  for (const b of bytes) binary += String.fromCharCode(b);
  return btoa(binary);
}

function fromB64(b64: string) {
  return Uint8Array.from(atob(b64), c => c.charCodeAt(0));
}
