## BurnAfter 
is a zero-knowledge, self-destructing paste service where secrets are encrypted client-side and never exposed to the server.
It features read-once endpoints hardened against enumeration, timing analysis, and other side-channel attacks through uniform responses, randomized delays, and payload padding.

## Live demo:  
https://gdesiato.github.io/burnafter/

## ⚠️ Cold start notice

This application is deployed on **Render free tier**.

Because of this, the backend may **take a few seconds to respond on the first request**:
- when the service is accessed for the first time
- or after **~15 minutes of inactivity**

This is expected behavior on free-tier infrastructure.  
Once the service wakes up, subsequent requests are fast and responsive.

