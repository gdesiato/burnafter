import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PasteService, ExpiresKey } from '../../core/paste.service';
import { encryptGCM, deriveKey, genKey, exportKeyRawB64 } from '../../shared/crypto.util';

@Component({
  selector: 'app-create',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create.html',
  styleUrls: ['./create.css']
})
export class CreateComponent {
  text = '';
  expiresIn: ExpiresKey = '10min';
  views = 1;
  burnAfterRead = false;
  password = '';
  loading = false;
  error: string | null = null;
  resultUrl: string | null = null;

  constructor(private api: PasteService) {}

  async submit() {
    if (!this.text.trim() || this.overCharLimit) return;
    this.loading = true; this.error = null;

    try {
      let key: CryptoKey;
      let fragment: string;

      if (this.password.trim()) {
        // --- password-protected mode ---
        const salt = crypto.getRandomValues(new Uint8Array(16));
        const saltB64 = btoa(String.fromCharCode(...salt));
        key = await deriveKey(this.password, saltB64);
        fragment = `pwd:${saltB64}`; // marker so receiver knows it's pwd-based
      } else {
        // --- link-only mode ---
        key = await genKey();
        fragment = await exportKeyRawB64(key);
      }

      const { ivB64, ctB64 } = await encryptGCM(this.text.trim(), key);

      this.api.createText(ctB64, {
        iv: ivB64,
        expiresIn: this.expiresIn,
        views: Math.min(Math.max(this.views || 1, 1), 10),
        burnAfterRead: this.burnAfterRead,
      }).subscribe({
        next: r => {
          this.resultUrl = `${r.readUrl}#${fragment}`;  // always trust backend URL
          this.loading = false;
        },
        error: err => {
          this.error = err?.error?.message || 'Failed to create link';
          this.loading = false;
        }
      });
    } catch (e: any) {
      this.error = e?.message || 'Encryption failed';
      this.loading = false;
    }
  }

  copy() {
    if (this.resultUrl) navigator.clipboard.writeText(this.resultUrl);
  }
  maxChars = 12000; // or whatever limit you want

  get charCount(): number {
    return this.text.length;
  }

  get overCharLimit(): boolean {
    return this.charCount > this.maxChars;
  }

  get charPercent(): number {
    return Math.min(100, Math.round((this.charCount / this.maxChars) * 100));
  }

  get counterColor(): string {
    if (this.overCharLimit) return '#d32f2f';       // red
    if (this.charPercent >= 90) return '#ed6c02';   // orange near limit
    return '#64748b';                                // neutral
  }

}
