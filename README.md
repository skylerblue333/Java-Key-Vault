# Sky Key Vault (Java)

**Status: engineering beta.** This repository implements a focused Java 21 in-memory secret vault primitive. It encrypts stored values with AES-256-GCM under a process-supplied wrapping key, authenticates each envelope with the key ID as AAD, validates identifiers and secret sizes, and supports retrieval/deletion plus opaque envelope export for inspection.

## What is verified by the repository

- Java 21 Maven build and JUnit tests
- AES/GCM authenticated encryption with a fresh 96-bit IV per write
- 32-byte wrapping-key validation
- bounded key IDs (`[A-Za-z0-9_.-]{1,64}`)
- bounded secret payloads (1–4096 bytes)
- non-root container packaging
- dependency/security scanning in GitHub Actions
- startup configuration validation through `SKY_VAULT_MASTER_KEY_B64`

## What this is not

This is **not** HashiCorp Vault, AWS KMS, HSM-backed key management, a distributed secret store, or a production deployment. Secrets are held only in process memory; there is no durable encrypted datastore, replication, RBAC, audit-log durability, lease/rotation engine, PKI, TLS termination, HA, hardware-rooted protection, or independently verified security assessment.

## Build and test

```bash
mvn clean verify
```

## Container smoke run

```bash
docker build -t sky-key-vault .
docker run --rm \
  -e SKY_VAULT_MASTER_KEY_B64=AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8= \
  sky-key-vault
```

The example key is test material only. Never reuse it for real secrets.

## SKYCOIN4444 integration boundary

A future ecosystem adapter can place this primitive behind an authenticated service boundary for development/testing. Production integration should instead use an externally managed KMS/HSM-backed root key and add durable encrypted storage, authorization, audit logging, rotation, backup/restore, and operational monitoring before any production-readiness claim.

## License

See `LICENSE`.
