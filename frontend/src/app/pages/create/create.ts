import { Component, inject } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PasteService, Expiry } from '../../core/paste.service';
import { newMessageKey, encrypt } from '../../shared/crypto.util';

@Component({
  selector: 'app-create',
  imports: [FormsModule, DecimalPipe],
  templateUrl: './create.html',
  styleUrl: './create.css'
})
export class CreateComponent {
  private api = inject(PasteService);
  readonly maxChars = 12000;

  // Form fields
  text = '';
  expiresIn: Expiry = '10min';
  views = 1;
  password = '';

  // UI state
  loading = false;
  error: string | null = null;
  resultUrl: string | null = null;

  get tooLong(): boolean {
    return this.text.length > this.maxChars;
  }

  async submit() {
    if (!this.text.trim() || this.tooLong) return;
    this.loading = true;
    this.error = null;
    this.resultUrl = null;

    try {
      // 1. Create the key in the browser (random, or derived from the password).
      const { key, fragment } = await newMessageKey(this.password);
      // 2. Encrypt in the browser.
      const { ciphertext, iv } = await encrypt(this.text.trim(), key);
      // 3. Send only the encrypted data.
      const id = await this.api.create({ ciphertext, iv, expiresIn: this.expiresIn, views: this.views });
      // 4. Put the key after '#': browsers never send that part to the server.
      this.resultUrl = `${location.origin}/p/${id}#${fragment}`;
    } catch {
      this.error = 'Could not create the message';
    } finally {
      this.loading = false;
    }
  }

  copy() {
    if (this.resultUrl) navigator.clipboard.writeText(this.resultUrl);
  }
}
