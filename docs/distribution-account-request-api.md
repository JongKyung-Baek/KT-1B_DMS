# Distribution-system account-request API

This interface lets a registered first-party or third-party technical-data
distribution system request an administrator decision for an external account
operation. Approval is a signed contract result only: TDMS does not
create/unlock a `docs_user` account and does not generate or change a password.

## Runtime registration

Secrets are runtime-only and are not stored in PostgreSQL.

- `TDMS_DISTRIBUTION_INTEGRATION_ENABLED=true`
- `TDMS_DISTRIBUTION_INTEGRATION_CLIENT_ID`: HMAC client identifier
- `TDMS_DISTRIBUTION_INTEGRATION_SOURCE_SYSTEM_ID`: asserted source system
- `TDMS_DISTRIBUTION_INTEGRATION_SHARED_SECRET`: at least 32 UTF-8 bytes
- `TDMS_DISTRIBUTION_INTEGRATION_ADDITIONAL_CLIENTS`: optional semicolon-separated
  `clientId|sourceSystemId|secret` registrations

## HMAC headers

Every external request sends:

- `X-DIST-Client-Id`
- `X-DIST-Timestamp`: current Unix epoch seconds
- `X-DIST-Nonce`: a new UUID for every HTTP request
- `X-DIST-Content-SHA256`: lower-case SHA-256 hex of the exact request body
  (SHA-256 of zero bytes for GET)
- `X-DIST-Signature`: lower-case HMAC-SHA256 hex

The canonical signing value is:

```text
HTTP_METHOD\n
REQUEST_PATH\n
CLIENT_ID\n
TIMESTAMP\n
NONCE\n
CONTENT_SHA256
```

`REQUEST_PATH` excludes scheme, host, query string, and servlet context path.
For example, the status path includes its exact event UUID segment.

## Submit a request

`POST /api/integrations/distribution/v1/account-requests`

```json
{
  "eventId": "b591440e-3225-440d-a73e-04ac29f36d23",
  "correlationId": "DIST-ACCOUNT-2026-0001",
  "sourceSystemId": "DISTRIBUTION-DEMO",
  "requestType": "REGISTER_USER",
  "occurredAt": "2026-08-01T03:00:00Z",
  "representative": {
    "id": "representative-1",
    "name": "Representative",
    "email": "representative@example.com",
    "phone": "+62-21-555-0100"
  },
  "organization": {
    "code": "ORG-001",
    "name": "Example Aerospace",
    "businessNumber": "ID-12345"
  },
  "targetUser": {
    "id": "new.user",
    "name": "New User",
    "email": "new.user@example.com",
    "phone": "+62-21-555-0101",
    "position": "Engineer"
  },
  "reason": "New project participant",
  "metadata": {
    "partnerType": "SUPPLIER"
  }
}
```

`requestType` is one of `REGISTER_USER`, `UNLOCK_ACCOUNT`, or
`RESET_PASSWORD`. `organization` and `metadata` are optional. The authenticated
client's registered source system must equal `sourceSystemId`.

Field requirements are type-specific:

- `REGISTER_USER`: `targetUser.id`, `targetUser.name`, and `targetUser.email`
  are required.
- `UNLOCK_ACCOUNT` and `RESET_PASSWORD`: only `targetUser.id` is required;
  `name` and `email` are optional, but a supplied email must be valid.
- `representative.id`, `representative.name`, and `representative.email` remain
  required for every request so an administrator can identify the accountable
  representative.

Never send a password, reset code, access token, API key, private key, secret,
or credential in this payload. Metadata keys are checked recursively through
objects and arrays. Each key is normalized to lower-case ASCII letters and
digits; a key containing `password`, `passwd`, `pwd`, `secret`, `token`,
`credential`, `apikey`, or `privatekey` is rejected with HTTP 400.

The first receipt returns HTTP 201. An exact retry with a new nonce returns HTTP
200 and `"duplicate": true`. Within one source system, both `eventId` and
`correlationId` are idempotency keys. Reusing either key with different content
returns HTTP 409.

Every external response, including a duplicate receipt, omits internal TDMS
administrator codes, login IDs, and names.

## Read the decision

`GET /api/integrations/distribution/v1/account-requests/{eventId}`

Sign the exact GET path and the SHA-256 of an empty body. The original client
receives the immutable request snapshot, `status` (`PENDING`, `APPROVED`, or
`REJECTED`), decision comment/time, and `events` history. TDMS-user actors in
the external event history are also de-identified. The external distribution
system performs the approved operation in its own account store.

## TDMS administrator API

These browser-session APIs require `ROLE_MENU_231`, an administrator account,
and the normal CSRF token.

- `GET /general/distribution/account-requests/api/requests`
  - filters: `status`, `requestType`, `sourceSystemId`, `keyword`, `limit`, `offset`
- `GET /general/distribution/account-requests/api/requests/{requestId}`
- `POST /general/distribution/account-requests/api/requests/{requestId}/approve`
- `POST /general/distribution/account-requests/api/requests/{requestId}/reject`

Decision body:

```json
{"decisionComment":"Approved for the project period."}
```

A rejection comment is required. The first approval or rejection creates
immutable request history plus a canonical TDMS audit event. An exact retry by
the same administrator returns the existing decision without another event;
an opposite or changed decision returns HTTP 409.

All error responses use:

```json
{"success":false,"code":"ERROR_CODE","message":"Description"}
```
