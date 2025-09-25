import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PasteService } from '../../core/paste'; // or '../../core/paste.service'

@Component({
  selector: 'app-create',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create.html',
  styleUrls: ['./create.css']
})
export class CreateComponent {
  title = '';
  text = '';
  encrypt = true;     // just visual for now
  expiresIn = '10min';
  views = 1;
  burnAfterRead = false;
  password = '';
  showPwd = false;

  loading = false;
  error: string | null = null;
  resultUrl: string | null = null;

  constructor(private api: PasteService) {}

  submit() {
    if (!this.text.trim()) return;
    this.loading = true; this.error = null; this.resultUrl = null;

    this.api.createText(this.text, {
      expiresIn: this.expiresIn,
      views: Math.min(Math.max(this.views || 1, 1), 10),
      burnAfterRead: this.burnAfterRead,
      password: this.password || undefined
    }).subscribe({
      next: r => {
        // If backend returns readUrl pointing to 8080, rebuild on the frontend origin:
        this.resultUrl = (r.readUrl?.includes('localhost:8080'))
          ? `${window.location.origin}/p/${r.id}`
          : (r.readUrl || `${window.location.origin}/p/${r.id}`);
        this.loading = false;
      },
      error: err => {
        this.error = err?.error?.message || 'Failed to create link';
        this.loading = false;
      }
    });
  }

  copy() {
    if (this.resultUrl) navigator.clipboard.writeText(this.resultUrl);
  }
}
