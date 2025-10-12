import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type ExpiresKey = '10min' | '1h' | '24h' | '7d';

/** Raw responses from Spring (as the backend sends them) */
interface CreateResponseRaw { id: string; readUrl?: string; expireAt: string; viewsLeft: number; }
interface MetaResponseRaw   { kind: string; expireAt: string; viewsLeft: number; hasPassword: boolean; }
export interface PasteData  { iv: string; ciphertext: string; }

/** Normalized responses for the UI */
export interface CreateResponse { id: string; readUrl: string; expiresAt: string; remaining: number; }
export interface PasteMeta { kind: string; expiresAt: string; remaining: number; protectedByPassword: boolean; }
export interface OpenResponse { kind: string; content: string; remaining: number; }

@Injectable({ providedIn: 'root' })
export class PasteService {
  private readonly base = `${environment.apiBase}/api/pastes`;

  constructor(private http: HttpClient) {}

  createText(
    data: string,
    opts: { expiresIn: ExpiresKey; views: number; burnAfterRead: boolean; password?: string; iv?: string }
  ): Observable<CreateResponse> {
    const body = opts.iv
      ? { kind: 'TEXT', ciphertext: data, iv: opts.iv, expiresIn: opts.expiresIn, views: opts.views, burnAfterRead: opts.burnAfterRead }
      : { kind: 'TEXT', content:    data,              expiresIn: opts.expiresIn, views: opts.views, burnAfterRead: opts.burnAfterRead, password: opts.password };

    return this.http.post<CreateResponseRaw>(this.base, body).pipe(
      map(raw => ({
        id: raw.id,
        readUrl: raw.readUrl || `${this.baseHref()}/p/${raw.id}`,
        // map the backend’s expireAt → expiresAt and viewsLeft → remaining
        expiresAt: raw.expireAt,
        remaining: raw.viewsLeft
      }))
    );
  }

  getMeta(id: string): Observable<PasteMeta> {
    return this.http.get<MetaResponseRaw>(`${this.base}/${encodeURIComponent(id)}`).pipe(
      map(raw => ({
        kind: raw.kind,
        expiresAt: raw.expireAt,
        remaining: raw.viewsLeft,
        protectedByPassword: raw.hasPassword
      }))
    );
  }

  getData(id: string): Observable<PasteData> {
    return this.http.get<PasteData>(`${this.base}/${encodeURIComponent(id)}/data`);
  }

  open(id: string, password?: string): Observable<OpenResponse> {
    return this.http.post<OpenResponse>(`${this.base}/${encodeURIComponent(id)}/open`, password ? { password } : {});
  }

  private baseHref(): string {
    const base = document.querySelector('base')?.href || location.origin;
    return base.endsWith('/') ? base.slice(0, -1) : base;
  }
}
