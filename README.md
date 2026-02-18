# BurnAfter

BurnAfter is a zero-knowledge, self-destructing paste service implemented as a set of backend microservices.

Secrets are encrypted client-side and are never exposed to the server. 
The server stores only ciphertext and cannot decrypt user content.
Each paste supports burn-after-read semantics or time-based expiration.

The system is designed with a focus on security, transactional integrity, and reliable event delivery.

---

## Overview

BurnAfter consists of two primary services:

- **Message Service**  
  Handles paste creation, retrieval, expiration, and lifecycle management.

- **Audit Service**  
  Receives domain events asynchronously for auditing and traceability.

Services communicate through HTTP and rely on a database-backed event delivery mechanism.

---

## Transactional Outbox

To ensure reliable event publishing without distributed transactions, the system implements the Transactional Outbox pattern.

When a paste operation occurs:

1. The business entity and its corresponding domain event are persisted within the same database transaction.
2. A background processor claims pending events using `FOR UPDATE SKIP LOCKED`.
3. Events are delivered outside the database lock.
4. Each event is finalized in its own transaction.
5. Failures trigger exponential backoff retries.
6. Events transition to a `DEAD` state after repeated failures or timeout thresholds.

This approach provides at-least-once delivery semantics while remaining horizontally scalable.

---

## Reliability Characteristics

- No HTTP calls inside open database transactions
- Short-lived locking during event claiming
- Exponential backoff with capped delay
- Dead-letter state for persistent failures
- Safe multi-instance processing using row-level locking

---

## Security Model

- Client-side AES encryption
- Server never processes plaintext secrets
- Read-once access pattern
- Uniform API responses to reduce enumeration vectors
- Optional API key support with rate limiting
