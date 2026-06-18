# AGENTS.md

## Commands
- build-test: ./gradlew clean build test -x itest -x jacocoTestCoverageVerification
- itest-nfs: unset ITEST_FAIL_FACTOR && ./gradlew clean :submission:submission-webapp:itest -PenableFire=false --rerun-tasks
- itest-fire: unset ITEST_FAIL_FACTOR && ./gradlew clean :submission:submission-webapp:itest -PenableFire=true --rerun-tasks
- itest-chaos: export ITEST_FAIL_FACTOR=8 && ./gradlew clean :submission:submission-webapp:itest -PenableFire=true --rerun-tasks

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
