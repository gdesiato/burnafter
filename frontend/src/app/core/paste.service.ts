import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type ExpiresKey = '10min' | '1h' | '24h' | '7d';

/** Requests */
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

/** Raw responses from Spring */
interface CreateResponseRaw {
  id: string;
  readUrl?: string;
  expireAt: string;   // <- raw
  viewsLeft: number;  // <- raw
}
interface MetaResponseRaw {
  kind: string;
  expireAt: string;        // <- raw
  viewsLeft: number;       // <- raw
  hasPassword: boolean;    // <- raw
}
export interface PasteData { iv: string; ciphertext: string; }

/** Normalized responses used by the UI */
export interface CreateResponse {
  id: string;
  readUrl: string;
  expiresAt: string;
  remaining: number;
}
export interface PasteMeta {
  kind: string;
  expiresAt: string;
  remaining: number;
  protectedByPassword: boolean;
}
export interface OpenResponse {
  kind: string;
  content: string;
  remaining: number;
}

@Injectable({ providedIn: 'root' })
export class PasteService {
  private readonly base = `${environment.apiBase}/api/pastes`;

  constructor(private http: HttpClient) {}

  /** Create paste (plaintext or encrypted if iv provided) */
  createText(
    data: string,
    opts: { expiresIn: ExpiresKey; views: number; burnAfterRead: boolean; password?: string; iv?: string }
  ): Observable<CreateResponse> {
    const body: CreatePlainRequest | CreateEncryptedRequest = opts.iv
      ? { kind: 'TEXT', ciphertext: data, iv: opts.iv, expiresIn: opts.expiresIn, views: opts.views, burnAfterRead: opts.burnAfterRead }
      : { kind: 'TEXT', content: data,        expiresIn: opts.expiresIn, views: opts.views, burnAfterRead: opts.burnAfterRead, password: opts.password };

    return this.http.post<CreateResponseRaw>(this.base, body).pipe(
      map((raw) => ({
        id: raw.id,
        // If backend gives a readUrl use it; otherwise build from <base href> so /REPO_NAME/ works on GitHub Pages
        readUrl: raw.readUrl || `${this.baseHref()}/p/${raw.id}`,
        expiresAt: raw.expireAt,
        remaining: raw.viewsLeft,
      }))
    );
  }

  /** GET /api/pastes/{id} */
  getMeta(id: string): Observable<PasteMeta> {
    return this.http.get<MetaResponseRaw>(`${this.base}/${encodeURIComponent(id)}`).pipe(
      map((raw) => ({
        kind: raw.kind,
        expiresAt: raw.expireAt,
        remaining: raw.viewsLeft,
        protectedByPassword: raw.hasPassword,
      }))
    );
  }

  /** GET /api/pastes/{id}/data (for encrypted mode) */
  getData(id: string): Observable<PasteData> {
    return this.http.get<PasteData>(`${this.base}/${encodeURIComponent(id)}/data`);
  }

  /** POST /api/pastes/{id}/open  (send password if required) */
  open(id: string, password?: string): Observable<OpenResponse> {
    return this.http.post<OpenResponse>(`${this.base}/${encodeURIComponent(id)}/open`, password ? { password } : {});
  }

  /** Build absolute base from the document’s <base href> (works on GitHub Pages subpaths) */
  private baseHref(): string {
    const base = document.querySelector('base')?.href || location.origin;
    // strip trailing slash
    return base.endsWith('/') ? base.slice(0, -1) : base;
  }
}
