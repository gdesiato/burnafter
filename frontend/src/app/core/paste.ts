import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { PasteService } from '../core/paste.service';

export interface CreateResp { id: string; readUrl: string; expireAt: string; viewsLeft: number; }
export interface MetaResp   { kind: string; expiresAt: string; remaining: number; protectedByPassword: boolean; }
export interface OpenResp   { kind: string; content: string; remaining: number; }
