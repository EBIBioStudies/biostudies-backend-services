# Generated OpenAPI specs

`internal.yaml` and `public.yaml` in this directory are produced by:

```
./gradlew :submission:submission-webapp:generateOpenApiDocs
```

The task boots the application under the `openapi-gen` Spring profile
(`application-openapi-gen.yml`), hits `/v3/api-docs.yaml/internal` and
`/v3/api-docs.yaml/public`, and writes the resulting YAML here.

**Do not edit by hand** — the `verify-openapi` GitLab CI job regenerates them
and fails the pipeline if the committed copies drift from what the code
produces. Update by re-running the task and committing the result.

The pre-existing per-endpoint YAML files under `docs/api/<module>/` are the
*legacy* hand-maintained specs being migrated into code annotations. They will
be deleted module-by-module as each set of controllers is annotated.
