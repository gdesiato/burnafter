import { TestBed } from '@angular/core/testing';

import { Paste } from './paste';

describe('Paste', () => {
  let service: Paste;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Paste);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
