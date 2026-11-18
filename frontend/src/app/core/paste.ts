import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { PasteService } from '../core/paste.service';

export interface CreateResp {
  id: string;
  readUrl: string;
  expireAt: string | null;
  viewsLeft: number;
}

export interface MetaResp {
  kind: string;
  expiresAt: string | null;
  remaining: number;
}

// Encrypted payload returned by GET /data
export interface PasteData {
  iv: string;
  ciphertext: string;
}