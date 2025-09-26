import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type ExpiresKey = '10min' | '1h' | '24h' | '7d';

/** Back-end payloads */
export interface CreatePlainRequest {
  kind: 'TEXT';
  content: string;
  expiresIn: ExpiresKey;
  views: number;
  burnAfterRead: boolean;
  password?: string;
}

export interface CreateEncryptedRequest {
  kind: 'TEXT';
  ciphertext: string;  // base64
  iv: string;          // base64 (12 bytes for AES-GCM)
  expiresIn: ExpiresKey;
  views: number;
  burnAfterRead: boolean;
}

/** Back-end responses (match your Spring records) */
export interface CreateResponse {
  id: string;
  readUrl?: string;
  /** NOTE: Spring returns `expireAt` & `viewsLeft` in your codebase */
  expireAt: string;
  viewsLeft: number;
}

export interface PasteMeta {
  expiresAt: string;
  remaining: number;
  protectedByPassword?: boolean;
  hasCiphertext?: boolean; // optional; some backends include this
}

export interface PasteData {
  iv: string;
  ciphertext: string;
}

/** Optional: legacy plaintext open response */
export interface OpenResponse {
  kind: string;
  content: string;
  remaining: number;
}

@Injectable({ providedIn: 'root' })
export class PasteService {
  /** Use absolute base so the SPA on Cloudflare Pages can call your API anywhere */
  private readonly base = `${environment.apiBase}/api/pastes`;

  constructor(private http: HttpClient) {}

  /**
   * Create a paste.
   * - If opts.iv is present -> ciphertext mode (E2EE)
   * - Otherwise -> plaintext mode (legacy/password)
   */
  createText(
    data: string,
    opts: {
      expiresIn: ExpiresKey;
      views: number;
      burnAfterRead: boolean;
      password?: string;
      iv?: string; // presence => ciphertext mode
    }
  ): Observable<CreateResponse & { readUrl: string }> {
    const body: CreatePlainRequest | CreateEncryptedRequest = opts.iv
      ? {
          kind: 'TEXT',
          ciphertext: data,
          iv: opts.iv!,
          expiresIn: opts.expiresIn,
          views: opts.views,
          burnAfterRead: opts.burnAfterRead,
        }
      : {
          kind: 'TEXT',
          content: data,
          expiresIn: opts.expiresIn,
          views: opts.views,
          burnAfterRead: opts.burnAfterRead,
          password: opts.password,
        };

    return this.http.post<CreateResponse>(this.base, body).pipe(
      // normalize readUrl fallback for convenience
      map((res) => ({
        ...res,
        readUrl: res.readUrl || `${location.origin}/p/${res.id}`,
      }))
    );
  }

  /** Metadata endpoint: GET /api/pastes/{id}  (not /meta) */
  getMeta(id: string): Observable<PasteMeta> {
    return this.http.get<PasteMeta>(`${this.base}/${encodeURIComponent(id)}`);
  }

  /** Encrypted data endpoint: GET /api/pastes/{id}/data */
  getData(id: string): Observable<PasteData> {
    return this.http.get<PasteData>(`${this.base}/${encodeURIComponent(id)}/data`);
  }

  /** Legacy plaintext open: POST /api/pastes/{id}/open  */
  open(id: string): Observable<OpenResponse> {
    return this.http.post<OpenResponse>(`${this.base}/${encodeURIComponent(id)}/open`, {});
  }
}
