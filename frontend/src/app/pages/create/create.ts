import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  PasteService,
  ExpiresKey,
  CreateResponse
} from '../../core/paste.service';

import {
  encryptGCM,
  deriveKey,
  genKey,
  exportKeyRawB64
} from '../../shared/crypto.util';

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

  maxChars = 12000;

  constructor(private api: PasteService) {}

  async submit() {
    if (!this.text.trim() || this.overCharLimit) return;

    this.loading = true;
    this.error = null;

    try {
      let key: CryptoKey;
      let fragment: string;

      if (this.password.trim()) {
        // --- password-protected mode ---
        const salt = crypto.getRandomValues(new Uint8Array(16));
        const saltB64 = btoa(String.fromCharCode(...salt));
        key = await deriveKey(this.password, saltB64);
        fragment = `pwd:${saltB64}`;
      } else {
        // --- link-only mode ---
        key = await genKey();
        fragment = await exportKeyRawB64(key);
      }

      const { ivB64, ctB64 } = await encryptGCM(this.text.trim(), key);

      this.api.createEncrypted(ctB64, ivB64, {
        expiresIn: this.expiresIn,
        views: this.views,
        burnAfterRead: this.burnAfterRead
      }).subscribe({
        next: (r: CreateResponse) => {
          this.resultUrl = r.readUrl;
          this.loading = false;
        },
        error: (err: unknown) => {
          console.error(err);
          this.error = 'Failed to create paste';
          this.loading = false;
        }
      });

    } catch (e: any) {
      this.error = e?.message || 'Encryption failed';
      this.loading = false;
    }
  }

  copy() {
    if (this.resultUrl) {
      navigator.clipboard.writeText(this.resultUrl);
    }
  }

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
    if (this.overCharLimit) return '#d32f2f';
    if (this.charPercent >= 90) return '#ed6c02';
    return '#64748b';
  }
}
