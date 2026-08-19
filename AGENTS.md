# AGENTS.md

## Commands
- build-test: ./gradlew clean build test -x itest -x jacocoTestCoverageVerification
- itest-nfs: unset ITEST_FAIL_FACTOR && ./gradlew clean :submission:submission-webapp:itest -PenableFire=false --rerun-tasks
- itest-fire: unset ITEST_FAIL_FACTOR && ./gradlew clean :submission:submission-webapp:itest -PenableFire=true --rerun-tasks
- itest-chaos: export ITEST_FAIL_FACTOR=8 && ./gradlew clean :submission:submission-webapp:itest -PenableFire=true --rerun-tasks

## Skills Used
- grill-with-docs: used to clarify and document the user-space cleanup design.
- domain-modeling: requested through grill-with-docs; not installed locally, so the design was modeled from repository code and captured in ADR/glossary docs.

## Guidelines
- When a new integration test is added to any class in the `ac.uk.ebi.biostd.itest.test.` package, a new entry should be
 added to `itestsinventory.md`

## Definition of Done
Before marking the work as complete:

- Run:
  - build-test
  - itest-nfs
  - itest-fire
  - itest-chaos

- Ensure:
  - Compilation is successful.
  - All unit tests are passing.
  - All integration tests defined at ac.uk.ebi.biostd.itest.test are passing with enableFire=true
  - All integration tests defined at ac.uk.ebi.biostd.itest.test are passing with enableFire=false

- Provide:
  - Summary of changes
  - List of modified files
  - Test results
