import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface CreateResp { id: string; readUrl: string; expireAt: string; viewsLeft: number; }
export interface MetaResp   { kind: string; expiresAt: string; remaining: number; protectedByPassword: boolean; }
export interface OpenResp   { kind: string; content: string; remaining: number; }

@Injectable({ providedIn: 'root' })
export class PasteService {
  constructor(private http: HttpClient) {}

  createText(
    content: string,
    opts: { expiresIn: string; views: number; burnAfterRead: boolean; password?: string }
  ) {
    return this.http.post<CreateResp>('/api/pastes', { kind: 'TEXT', content, ...opts });
  }

  meta(id: string) {
    return this.http.get<MetaResp>(`/api/pastes/${id}`);
  }

  open(id: string, password?: string) {
    return this.http.post<OpenResp>(`/api/pastes/${id}/open`, { password });
  }
}