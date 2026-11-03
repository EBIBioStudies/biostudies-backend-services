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
- When writing pagetab for a test, prefer TSV format
- Don't create unit tests for the web resource classes (i.e. containing endpoints) unless specifically asked.
- When writing Kotlin unit tests with MockK:
  - Annotate the test class with `@ExtendWith(MockKExtension::class)` and inject mocks through constructor parameters using `@param:MockK`.
  - Put shared mock behavior and object mocks in `@BeforeEach`; clean them in `@AfterEach` with `clearAllMocks()` and the corresponding `unmockkObject(...)` calls.
  - Keep reusable immutable fixtures and configuration in a `private companion object`, using named arguments for configuration objects.
  - Keep test-specific stubbing and captured values inside the test that uses them.

## Definition of Done
Before marking the work as complete:

- Ensure:
  - Compilation is successful.
  - All integration tests defined at ac.uk.ebi.biostd.itest.test are passing (run `itest-fire`)

- Provide:
  - Summary of changes
  - List of modified files
  - Test results
