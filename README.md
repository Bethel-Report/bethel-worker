# Bethel Worker

Spring Boot worker service for processing messages via RabbitMQ.

## Prerequisites

- **Java 21+** (JDK)
- **Maven 3.9+** (included via Maven Wrapper)
- **Node.js 18+** and **Yarn** (only for Git hooks and commit validation)
- **Docker** (for running tests with Testcontainers)

## Getting Started

```bash
# Install Git hooks and commit validation tools
yarn install

# Build the project
./mvnw verify -DskipTests
```

## Code Quality Tools

| Tool | Purpose | Version |
|------|---------|---------|
| [Spotless](https://github.com/diffplug/spotless) + Eclipse JDT Formatter | Automatic code formatting | 3.10.4 |
| [Checkstyle](https://checkstyle.org/) | Style conventions and naming rules | 10.17.0 |
| [PMD](https://pmd.github.io/) | Static analysis (bad practices, complexity, error-prone patterns) | 7.17.0 |
| [Commitlint](https://commitlint.js.org/) | Commit message validation (Conventional Commits) | 19.6.1 |
| [Husky](https://typicode.github.io/husky/) | Git hooks automation | 9.1.7 |

## Commands

### Formatting

```bash
# Auto-format all Java code
./mvnw spotless:apply

# Check formatting without modifying files
./mvnw spotless:check
```

### Style and Static Analysis

```bash
# Run Checkstyle
./mvnw checkstyle:check

# Run PMD
./mvnw pmd:check
```

### Full Verification

```bash
# Run all checks (format + style + analysis + compile + test)
./mvnw verify

# Run all checks without tests
./mvnw verify -DskipTests
```

### Commit Message Validation

```bash
# Test a commit message
echo "feat: add user authentication" | yarn commitlint
```

## Conventional Commits

All commit messages must follow the [Conventional Commits](https://www.conventionalcommits.org/) format:

```
type(optional scope): description
```

### Allowed Types

| Type | Description |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation |
| `style` | Code style (no behavior change) |
| `refactor` | Refactoring |
| `perf` | Performance improvement |
| `test` | Tests |
| `build` | Build system or dependencies |
| `ci` | Continuous integration |
| `chore` | Maintenance tasks |
| `revert` | Reverts a previous commit |

### Examples

Valid:

```
feat: add user authentication
feat(auth): implement JWT authentication
fix: handle null pointer exception
refactor(user): extract user service
test: add unit tests for user service
docs: update API documentation
```

Invalid:

```
added authentication          # missing type and colon
Fixed a bug                   # missing type and colon
update code                   # missing type and colon
feat add authentication       # missing colon after type
```

## Git Hooks

Husky runs the following hooks automatically:

- **commit-msg**: Validates commit messages with Commitlint. Commits with invalid messages are blocked.

To set up hooks after cloning:

```bash
yarn install
```

## Configuration Files

| File | Purpose |
|------|---------|
| `config/checkstyle/checkstyle.xml` | Checkstyle rules |
| `config/checkstyle/suppressions.xml` | Checkstyle suppressions |
| `config/pmd/pmd-ruleset.xml` | PMD rules |
| `.commitlintrc.json` | Commitlint configuration |
| `.husky/commit-msg` | Husky commit-msg hook |

## Customizing Rules

### Adding Checkstyle Rules

Edit `config/checkstyle/checkstyle.xml` to add modules inside the `<TreeWalker>` block. See the [Checkstyle documentation](https://checkstyle.org/checks.html) for available checks.

To suppress a rule for a specific file, add an entry to `config/checkstyle/suppressions.xml`.

### Adding PMD Rules

Edit `config/pmd/pmd-ruleset.xml` to include or exclude rules. See the [PMD rule reference](https://docs.pmd-code.org/latest/pmd_rules_java.html) for available rules.

### Changing Commit Message Rules

Edit `.commitlintrc.json` to modify allowed types, header length, or other rules. See the [Commitlint reference](https://commitlint.js.org/reference/rules.html).

## Troubleshooting

### `spotless:check` fails

Run `./mvnw spotless:apply` to auto-fix formatting, then commit the changes.

### Checkstyle or PMD violations

Read the error messages in the console output. They include the file, line number, and rule name. Fix the code according to the rule, or add a suppression if it's a false positive.

### Commit rejected by Commitlint

Check the error output for which rule was violated. Rewrite the commit message following the format `type(scope): description`.

To amend the last commit message:

```bash
git commit --amend -m "feat: corrected message"
```

### `yarn install` fails

Ensure Node.js 18+ and Yarn are installed. Node.js is only needed for Git hooks — it is not required at runtime.

## CI/CD

The project includes a GitHub Actions workflow (`.github/workflows/ci.yml`) that:

1. Validates commit messages on pull requests.
2. Runs `./mvnw verify` (formatting + style + analysis + compile + tests).

The CI pipeline uses Java 21 (Temurin) and caches Maven dependencies.
