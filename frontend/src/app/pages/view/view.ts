import { Component, OnInit, OnDestroy, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { PasteService, PasteMeta } from '../../core/paste.service';
import { deriveKey, decryptGCMWithKey, decryptGCM } from '../../shared/crypto.util';

@Component({
  selector: 'app-view',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './view.html',
  styleUrls: ['./view.css']
})
export class ViewComponent implements OnInit, OnDestroy {
  id = '';
  meta: PasteMeta | null = null;
  loadingMeta = true;

  // UI state
  password = '';
  hasPwdFragment = false;
  opened = false;
  openClicked = false;
  openError: string | null = null;
  content = '';

  @ViewChild('noteEl') noteEl?: ElementRef<HTMLElement>;

  private onHashChange = () => this.updatePwdFlag();

  constructor(private route: ActivatedRoute, private api: PasteService) {}

  ngOnInit() {
    const rid = this.route.snapshot.paramMap.get('id');
    if (!rid) { this.openError = 'Invalid link: missing id'; this.loadingMeta = false; return; }
    this.id = rid;

    this.openClicked = false;
    this.openError = null;

    this.updatePwdFlag();
    window.addEventListener('hashchange', this.onHashChange);

    // Load meta quietly; if it fails, just hide the hint
    this.api.getMeta(this.id).subscribe({
      next: (m) => { this.meta = m; this.loadingMeta = false; },
      error: () => { this.meta = null; this.loadingMeta = false; }
    });
  }

  ngOnDestroy() {
    window.removeEventListener('hashchange', this.onHashChange);
  }

  private updatePwdFlag() {
    const h = (window.location.hash || '');
    this.hasPwdFragment = h.startsWith('#pwd:');
  }

  open() {
    this.openClicked = true;
    this.openError = null;

    const frag = (window.location.hash || '').slice(1);
    if (!frag) { this.openError = 'Missing decryption info in URL'; return; }

    this.api.getData(this.id).subscribe({
      next: async data => {
        try {
          if (frag.startsWith('pwd:')) {
            const saltB64 = frag.slice(4);
            if (!this.password.trim()) { this.openError = 'Please enter password'; return; }
            const key = await deriveKey(this.password, saltB64);
            this.content = await decryptGCMWithKey(data.ciphertext, data.iv, key);
          } else {
            this.content = await decryptGCM(data.ciphertext, data.iv, frag);
          }

          this.opened = true;
          queueMicrotask(() => this.noteEl?.nativeElement.focus());
        } catch {
          this.openError = 'Failed to decrypt (wrong password or corrupted data)';
        }
      },
      error: err => {
        this.openError = (err?.status === 404) ? 'Not found or expired' : 'Unable to load paste';
      }
    });
  }
}
