# Coding Conventions

## Coding Standards
In order to keep the same coding standards, this project uses KtLint. You can find a full list of the coding standards
[here](https://ktlint.github.io/#rules).

## Kotlin Extensions
1. Extension functions need to be extracted in specific file with the name pattern {Type}Ext, e.g BooleanExt.
2. Functions (not class methods) need to be created in file with name {MainType}Ext, in example DateFunctions.
3. Separate annotated class properties by a line break.

## Tests
The building process includes coverage report which is available at the `build` folder on each module.

## Branch Names
New branches should be named using the following convention: `{fix/feature/chore}/{Pivotal ID #}-{branch-name}`

Example: `feature/168426599-submit-permission`

* **fix** should be used for code that solves a bug
* **feature** should be used for code that releases a new feature
* **chore** should be used for branches with code that is neither a fix nor a feature like refactors, renames, CI, etc

## Pull Requests
The project's default branch is `master`. All code to be included in this branch should be merged through a PR that
should pass its corresponding build and have at least one approval review.

The PR name should use the following convention: `{Fix/Feature/Chore} / Pivotal ID #{Ticket ID #} - {Ticket Title}`

Example: `Feature / Pivotal ID #168426599 - Submit Privilege`

The type of PR should match the branch.
