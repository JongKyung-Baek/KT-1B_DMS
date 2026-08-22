# TDMS PDF conversion release protocol v2

`Invoke-PdfConversionRelease.ps1` is the only server-side process allowed to
change the TDMS PDF conversion runtime. It owns preflight, apply, automatic
runtime rollback, and explicitly approved data recovery. Do not coordinate an
application stop through another deployment process.

## Fixed server layout

- Immutable release: `D:\KT1B-DMS\staging\<releaseId>`
- Live overlay: `D:\KT1B-DMS\runtime\pdf-conversion\compose.pdf-conversion.yaml`
- Live secret: `D:\KT1B-DMS\runtime\secrets\pdf-conversion.env`
- State and result records: `D:\KT1B-DMS\run-logs`

The live secret has exactly `KT1B_FILE_API_KEY`,
`TDMS_PDF_CONVERSION_CLIENT_ID`, and
`TDMS_PDF_CONVERSION_SHARED_SECRET`. Protect it with inherited ACLs disabled
and read access limited to the deployment account, SYSTEM, and Administrators.
Never print the file or run `docker compose config` without `--quiet`.

## Immutable upload

Before the first protocol-v2 release, perform a separately approved, backed-up
one-time ACL hardening of `D:\KT1B-DMS` and `D:\KT1B-DMS\staging`. The runner
never changes these ACLs. Both directories must have inheritance disabled, be
owned by SYSTEM or BUILTIN\Administrators, and grant FullControl only to SYSTEM
and BUILTIN\Administrators. An optional deployment or service SID may have
read/execute only; it must have no create, write, delete-child, delete,
permission-change, or owner-change right. In particular, an Authenticated Users
Modify grant is forbidden. Back up and independently verify the ACLs and the
application/container access path before publishing a release; keep the
approved ACL rollback procedure outside this runner.

1. Assemble all files named in `deployment-request.json`, plus the runner and
   common module, in a new local release directory. Replace every example hash
   and set `exampleOnly` to `false`.
2. Upload to a new `D:\KT1B-DMS\staging\<releaseId>.partial`. Before rename,
   disable inheritance on that directory, set its owner and rights to the same
   protected contract, and verify all direct-child file ACLs grant no untrusted
   write or delete access.
3. Protect the complete ancestry: `D:\KT1B-DMS`, its `staging` child, and the
   final release directory must each have inheritance disabled and be owned by
   SYSTEM or BUILTIN\Administrators. Only SYSTEM and BUILTIN\Administrators may
   have create, write, delete-child, delete, ACL-change, or owner-change rights;
   the elevated deployment process therefore runs as SYSTEM or an Administrator.
   Verify every SHA-256 on the server, then rename the fully assembled `.partial`
   directory once to `D:\KT1B-DMS\staging\<releaseId>`. Never overwrite a final
   release path or relax any ancestor ACL while the runner is active.
4. Invoke the uploaded runner with the absolute 64-bit Windows PowerShell 5.1
   path shown below. The runner rejects a 32-bit process. The remote command
   may contain only the validated release id, mode, and 64-hex hashes. Do not
   inline a script body or pass a path read from the request.

The production default omits `artifacts.publicProbeCa`; the release directory
then contains exactly the existing nine immutable files. If the public HTTPS
probe is served by a private PKI, `artifacts.publicProbeCa` may declare one
additional PEM X509 CA certificate using the same `file` and 64-hex `sha256`
fields as other artifacts. Its `file` must be a safe basename located directly
under the immutable release directory. The runner rejects certificate chains,
non-CA certificates, path traversal, unknown artifact declarations, hash
mismatches, and every undeclared stage file. When present, the pinned file is
passed to the fixed Windows curl executable with `--cacert`; ambient
`CURL_CA_BUNDLE`, `SSL_CERT_FILE`, and `SSL_CERT_DIR` values are ignored. Curl
configuration files are disabled by the first `--disable` argument, every
proxy is bypassed with `--noproxy '*'`, and HOME/CURL_HOME/proxy environment
values are removed for the probe and restored afterward.

All release artifacts are direct children of the final stage. Recursive
inventory must contain exactly nine files without the optional CA or ten files
with it; any subdirectory, reparse point, or undeclared file rejects the
release. Runner, common module, and request are ACL-checked, opened read-only,
SHA-256 verified, and held with write/delete sharing denied before the common
module is imported or the request is parsed. The request JSON is decoded from
those locked bytes, not reopened from its path.

Optional request fragment (add it only for an approved private CA):

```json
"publicProbeCa": {
  "file": "demo-public-probe-ca.pem",
  "sha256": "<64HEX>"
}
```

Example command shape (placeholders are not executable):

```powershell
C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File D:\KT1B-DMS\staging\pdfconv-YYYYMMDD-HHMMSS\Invoke-PdfConversionRelease.ps1 -Mode Preflight -ReleaseId pdfconv-YYYYMMDD-HHMMSS -ExpectedRunnerSha256 <64HEX> -ExpectedCommonSha256 <64HEX> -ExpectedRequestSha256 <64HEX>
```

Run `Preflight`, review its output, then run the same pinned file in `Apply`
mode. Apply performs backup, isolated PostgreSQL 17 restore and two migration
passes, image load/build, and production additive DDL while the old service is
still online. The archive hash, catalog validation, archive schema, restored
full-data, and restored schema fingerprints are retained as evidence. Normal online writes are
not treated as a full-database equality gate; protected business data and the
approved additive schema remain gated. Only then does it enter the 180-second
joint replacement window.

The gateway is never recreated: the runner stops and starts its captured
container identity. The application, File API, and converter are jointly
recreated; both sidecars must share the exact application network namespace,
must expose no host binding for ports 18080 or 9001, and must return the
expected private probe statuses.

## Rollback boundary

An Apply failure after quiesce automatically performs `RuntimeOnly` rollback.
It restores the old environment, compose files, secret ACL, WAR, checksums,
image identity, application, and any pre-existing private sidecars. Sidecars
introduced only by the failed release are removed. The runner then verifies
the protected business-data, schema, and document-storage fingerprints are
unchanged and starts the same gateway container. It never drops/restores the
database and never replaces document storage.

Data restoration is a separate explicit Rollback invocation with
`-RestoreData`, the approved state SHA-256, and a separately hashed evidence
JSON. Evidence must name the release and state, contain the protected data,
schema, and storage baseline fingerprints, match newly observed live
fingerprints, and prove at least one actual mismatch. Without all conditions,
data restoration is refused.

## Local checks

Run both scripts with Windows PowerShell 5.1 before approving a release:

```powershell
C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\Test-PdfConversionDeployment.Common.ps1
C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\Test-PdfConversionRuntimeRollback.Rehearsal.ps1
```

The model rehearsal creates a unique directory below the Windows temporary
directory, forces a failure after app replacement, and verifies runtime
recovery while database and storage sentinels remain byte-identical. It does
not create or reuse a `D:` drive mapping. A separate disposable Docker rehearsal
must exercise the official runner from the exact `D:\KT1B-DMS` layout before a
production release is approved.
