import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

export type ExpiresKey = '10min' | '1h' | '24h' | '7d';

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

export interface CreateResponse {
  id: string;
  readUrl?: string;
  expiresAt: string;
  remaining: number;
}

export interface PasteMeta {
  expiresAt: string;
  remaining: number;
  protectedByPassword?: boolean;
  hasCiphertext?: boolean;
}

export interface PasteData {
  iv: string;
  ciphertext: string;
}

@Injectable({ providedIn: 'root' })
export class PasteService {
  private readonly base = '/api/pastes';
  constructor(private http: HttpClient) {}

  createText(
    data: string,
    opts: {
      expiresIn: ExpiresKey;
      views: number;
      burnAfterRead: boolean;
      password?: string;
      iv?: string; // if present => ciphertext mode
    }
  ): Observable<CreateResponse> {
    const body: CreatePlainRequest | CreateEncryptedRequest = opts.iv
      ? { kind: 'TEXT', ciphertext: data, iv: opts.iv, expiresIn: opts.expiresIn, views: opts.views, burnAfterRead: opts.burnAfterRead }
      : { kind: 'TEXT', content: data, expiresIn: opts.expiresIn, views: opts.views, burnAfterRead: opts.burnAfterRead, password: opts.password };

    return this.http.post<CreateResponse>(this.base, body).pipe(
      map(res => ({ ...res, readUrl: res.readUrl || `${location.origin}/p/${res.id}` }))
    );
  }

  getMeta(id: string): Observable<PasteMeta> {
    return this.http.get<PasteMeta>(`${this.base}/${encodeURIComponent(id)}/meta`);
  }

  getData(id: string): Observable<PasteData> {
    return this.http.get<PasteData>(`${this.base}/${encodeURIComponent(id)}/data`);
  }

  open(id: string): Observable<{ remaining: number }> {
    return this.http.post<{ remaining: number }>(`${this.base}/${encodeURIComponent(id)}/open`, {});
  }
}
