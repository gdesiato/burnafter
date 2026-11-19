import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { PasteService, PasteData } from '../../core/paste.service';
import { isPasswordFragment, keyFromFragment, decrypt } from '../../shared/crypto.util';

@Component({
  selector: 'app-view',
  imports: [FormsModule],
  templateUrl: './view.html',
  styleUrl: './view.css'
})
export class ViewComponent {
  private api = inject(PasteService);
  private id = inject(ActivatedRoute).snapshot.paramMap.get('id') ?? '';

  // Everything after '#': the key, or 'pwd:<salt>'. It never leaves the browser.
  private fragment = location.hash.slice(1);
  readonly needsPassword = isPasswordFragment(this.fragment);

  password = '';
  loading = false;
  error: string | null = null;
  content: string | null = null;

  async open() {
    this.error = null;

    // Check everything we can BEFORE fetching: fetching uses up a view.
    if (!this.fragment) {
      this.error = 'This link is missing its key (the part after #)';
      return;
    }
    if (this.needsPassword && !this.password.trim()) {
      this.error = 'Please enter the password';
      return;
    }

    this.loading = true;
    try {
      let data: PasteData;
      try {
        data = await this.api.getData(this.id);
      } catch {
        this.error = 'Unable to load the message';
        return;
      }

      if (!data.ok || !data.ciphertext || !data.iv) {
        this.error = 'This message has already been read or has expired';
        return;
      }

      try {
        const key = await keyFromFragment(this.fragment, this.password);
        this.content = await decrypt(data.ciphertext, data.iv, key);
      } catch {
        this.error = 'Could not decrypt (wrong password or damaged link)';
      }
    } finally {
      this.loading = false;
    }
  }
}
