import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export type Expiry = '10min' | '1h' | '24h' | '7d';

/** What the server receives: encrypted data only, never the key. */
export interface NewPaste {
  ciphertext: string;
  iv: string;
  expiresIn: Expiry;
  views: number;
}

/**
 * The server always answers 200, so nobody can probe whether a link exists.
 * ok=false means "nothing to decrypt" (already read, expired, or never existed).
 */
export interface PasteData {
  ok: boolean;
  iv: string | null;
  ciphertext: string | null;
}

// Relative URLs: Nginx serves the UI and proxies /api to the backend on the
// same origin (and `ng serve` does the same with proxy.conf.json).
@Injectable({ providedIn: 'root' })
export class PasteService {
  private http = inject(HttpClient);

  /** Stores the encrypted message and returns its id. */
  async create(paste: NewPaste): Promise<string> {
    const res = await firstValueFrom(this.http.post<{ id: string }>('/api/pastes', paste));
    return res.id;
  }

  /** Fetches the encrypted message. This uses up one view. */
  getData(id: string): Promise<PasteData> {
    return firstValueFrom(this.http.get<PasteData>(`/api/pastes/${encodeURIComponent(id)}/data`));
  }
}
