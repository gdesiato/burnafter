import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type ExpiresKey = '10min' | '1h' | '24h' | '7d';

/** Raw responses from Spring (as the backend sends them) */
interface CreateResponseRaw {
  id: string;
  readUrl?: string;
  expireAt: string | null;
  viewsLeft: number;
}
interface MetaResponseRaw {
  kind: string;
  expireAt: string | null;
  viewsLeft: number;
  hasPassword: boolean;
}
export interface PasteData {
  iv: string;
  ciphertext: string;
}

/** Normalized responses for the UI */
export interface CreateResponse {
  id: string;
  readUrl: string;
  expiresAt: string | null;
  remaining: number;
}
export interface PasteMeta {
  kind: string;
  expiresAt: string | null;
  remaining: number;
}

@Injectable({ providedIn: 'root' })
export class PasteService {
  private readonly base = `${environment.apiBase}/api/pastes`;

  constructor(private http: HttpClient) {}

  // CREATE (ZK-only)
  createEncrypted(
    ciphertext: string,
    iv: string,
    opts: { expiresIn: ExpiresKey; views: number; burnAfterRead: boolean }
  ): Observable<CreateResponse> {

    const body = {
      kind: 'TEXT' as const,
      ciphertext,
      iv,
      views: opts.views,
      burnAfterRead: opts.burnAfterRead,
      expiresIn: opts.expiresIn
    };

    return this.http.post<CreateResponseRaw>(this.base, body).pipe(
      map(raw => ({
        id: raw.id,
        readUrl: raw.readUrl || `${this.baseHref()}/p/${raw.id}`,
        expiresAt: raw.expireAt ?? null,
        remaining: raw.viewsLeft
      }))
    );
  }

  /**
   * 🔹 ALIAS for UI compatibility
   * Keeps CreateComponent semantics intact
   */
  createText(
    ciphertext: string,
    iv: string,
    opts: { expiresIn: ExpiresKey; views: number; burnAfterRead: boolean }
  ): Observable<CreateResponse> {
    return this.createEncrypted(ciphertext, iv, opts);
  }

  // METADATA
  getMeta(id: string): Observable<PasteMeta> {
    return this.http.get<Omit<MetaResponseRaw, 'hasPassword'>>(
      `${this.base}/${encodeURIComponent(id)}`
    ).pipe(
      map(raw => ({
        kind: raw.kind,
        expiresAt: raw.expireAt ?? null,
        remaining: raw.viewsLeft
      }))
    );
  }

  // READ ENCRYPTED PAYLOAD
  getData(id: string): Observable<PasteData> {
    return this.http.get<PasteData>(
      `${this.base}/${encodeURIComponent(id)}/data`
    );
  }

  private baseHref(): string {
    const base = document.querySelector('base')?.href || location.origin;
    return base.endsWith('/') ? base.slice(0, -1) : base;
  }
}
