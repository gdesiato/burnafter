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

  // Ciphertext fetched by the first attempt, kept so password retries don't refetch.
  private data: { ciphertext: string; iv: string } | null = null;

  async open() {
    this.error = null;

    // Check BEFORE fetching: fetching uses up a view.
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
      // Fetch only once: the fetch is what uses up a view, so a mistyped
      // password must not cost another one. Retries decrypt the copy already
      // in memory. This doesn't help an attacker: whoever has the ciphertext
      // can already guess passwords offline.
      if (!this.data) {
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
        this.data = { ciphertext: data.ciphertext, iv: data.iv };
      }

      try {
        const key = await keyFromFragment(this.fragment, this.password);
        this.content = await decrypt(this.data.ciphertext, this.data.iv, key);
        this.data = null; // drop the ciphertext once it's been read
      } catch {
        this.error = this.needsPassword
          ? 'Wrong password, try again. The message stays open until you close this tab.'
          : 'Could not decrypt (the link is damaged or incomplete)';
      }
    } finally {
      this.loading = false;
    }
  }
}
