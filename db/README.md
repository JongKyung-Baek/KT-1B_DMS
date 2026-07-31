# KT-1B database recreation

`Recreate-Kt1bDatabase.ps1` creates a PostgreSQL 17 database from the tracked
`260701dumpdb3.backup`, applies every current first-party DDL in dependency
order, removes the retired portal schema/menu data, and loads the local sample
data.

The target database is dropped. The explicit confirmation switch and the
database/container parameters are therefore mandatory review points:

```powershell
powershell -ExecutionPolicy Bypass -File .\db\Recreate-Kt1bDatabase.ps1 `
  -Container kt1b-postgres `
  -Database kt1b `
  -DatabaseUser myuser `
  -DiscardExistingDatabase `
  -VerifyRepeatableMigrations
```

For a production-shaped database without local sample users/documents, add
`-SkipSampleData`. The schema, ACL, menu cleanup, and all other migrations are
still applied.

The ordered psql entry point is
`src/main/resources/sql/fresh_database_migration.psql`. Add every future
`*_ddl.sql` migration there exactly once; the PowerShell runner refuses to run
when a DDL is missing from that manifest.

For direct PostgreSQL 17 operation on AIX, perform the same steps with the
native client tools. Do not load sample data in production:

```sh
dropdb -U myuser --if-exists --force kt1b
createdb -U myuser --encoding=UTF8 --template=template0 kt1b
pg_restore -U myuser -d kt1b --no-owner --no-privileges \
  --exit-on-error --single-transaction db/260701dumpdb3.backup
psql -U myuser -d kt1b -v ON_ERROR_STOP=1 \
  -v include_sample_data=false \
  -f src/main/resources/sql/fresh_database_migration.psql
```

The local sample postcondition is 26 active database menu records, four
visible navigation roots, 25 assignable menu roles, no portal-wide wildcard
ACL, four security grades, six users, six departments, and 16 documents with
one main and one supplementary file each.
