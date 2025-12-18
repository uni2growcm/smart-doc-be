# Contributor Guidelines

## IDE Setup

Intellij Ultimate or Community

### Code Formatter

1. In Intellij settings, enable EditorConfig under `Editor > Code Style`
2. Set hard wrap to 120 (not 100)
3. Perform a test to ensure everything works correctly:
    1. Open a class containing some code
    2. Force a Reformat Code + Rearrange Code + Optimize Imports
    3. Verify that the class has not changed by even a comma
4. Enable the following three options during commit (others are not needed):
    1. Reformat Code
    2. Rearrange Code
    3. Optimize Imports

### SonarLint

- Install the SonarLint plugin on Intellij https://www.sonarsource.com/products/sonarlint/
- Enable sonar analysis in the Intellij commit window

## Git & PRs

### Commit message

Comment in English prefixed by the referenced issue code and the type of
activity:

```issue-key: [feat|fix|refactor|chore] - description```

Example:

```YCP-123: feat - add new api for spid privacy```

- `feat` is for adding a new feature
- `fix` is for fixing a bug
- `refactor` is for changing code for performance or convenience purpose (e.g. readability)
- `chore` is for everything else (writing documentation, formatting, adding tests, cleaning useless code, etc.)

### Branching model

- `main` for production-ready branch
- `develop` for development features

No direct commits allowed on these branches.

### Branch Naming

Direct commits on `main` and `develop` branches are disabled

- `hotfix/[main|develop]/SD-123`: for hotfixes
- `feature/SD-123`: for new features
- `bugfix/[main|develop]/SD-123`: for bugfixes
- `fix/[main|develop]/my-fix`: for quick fixes outside of an issue/ticket
- `test/my-test`: for experimenting outside of an issue/ticket

### Pull Requests

Files changed in a PR must be related only to the feature. Refactoring and other chore activities must be
done in separate PRs.

## Testing

- When possible, prefer unit tests over integration tests
- Classes in the root of the modules should have at least 80% coverage
- Code related to business rules should always be tested
- Use the `*Test.java` suffix for unit tests, `*IT.java` for integration tests

## Mock API

To start the OH API mocks (WireMock), move into the `wiremock` folder and run:

```sh
docker compose up -d
```

## Tools

- Error Handling
    - Doc: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html
    - Demo: https://dev.to/noelopez/spring-rest-exception-handling-problem-details-2hkj
