import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { PasteService } from '../../core/paste'; // or '../../core/paste.service'

@Component({
  selector: 'app-view',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './view.html',
  styleUrls: ['./view.css']
})
export class ViewComponent implements OnInit {
  id = '';
  meta: any = null;
  password = '';
  content = '';
  opened = false;
  error: string | null = null;

  constructor(private route: ActivatedRoute, private api: PasteService) {}

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id')!;
    this.api.meta(this.id).subscribe({
      next: m => this.meta = m,
      error: _ => this.error = 'Not found or expired'
    });
  }

  open() {
    this.api.open(this.id, this.password || undefined).subscribe({
      next: r => { this.content = r.content; this.opened = true; },
      error: err => this.error = err.status === 403 ? 'Wrong password' : 'Not found or expired'
    });
  }
}
