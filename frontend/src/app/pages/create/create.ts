import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PasteService } from '../../core/paste'; // if you renamed to paste.service.ts, change to '../../core/paste.service'

@Component({
  selector: 'app-create',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create.html',
  styleUrls: ['./create.css']
})
export class CreateComponent {
  text = '';
  expiresIn = '24h';
  views = 1;
  burnAfterRead = false;
  password = '';

  loading = false;
  error: string | null = null;
  resultUrl: string | null = null;

  constructor(private api: PasteService) {}

  submit() {
    this.error = null;
    this.loading = true;
    this.resultUrl = null;

    this.api.createText(this.text, {
      expiresIn: this.expiresIn,
      views: this.views,
      burnAfterRead: this.burnAfterRead,
      password: this.password || undefined
    }).subscribe({
      next: r => { this.resultUrl = r.readUrl; this.loading = false; },
      error: err => { this.error = err?.error?.message || 'Failed'; this.loading = false; }
    });
  }

  copy() {
    if (this.resultUrl) navigator.clipboard.writeText(this.resultUrl);
  }
}
