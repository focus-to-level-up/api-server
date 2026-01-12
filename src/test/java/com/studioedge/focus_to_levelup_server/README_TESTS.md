# CodeRabbit Configuration Tests

## Overview
This test suite provides comprehensive validation for the `.coderabbit.yaml` configuration file, ensuring that the CodeRabbit code review automation tool is properly configured for this project.

## Test File
- **Location**: `src/test/java/com/studioedge/focus_to_levelup_server/CodeRabbitConfigTest.java`
- **Test Framework**: JUnit 5
- **YAML Parser**: SnakeYAML 2.2

## Test Coverage

### 1. Basic Configuration Structure Tests (4 tests)
- ✅ Validates version field is set to "2"
- ✅ Verifies language configuration (ko-KR or en-US)
- ✅ Confirms early_access is enabled
- ✅ Checks enable_free_tier is configured

### 2. Reviews Configuration Tests (9 tests)
- ✅ Validates profile setting (chill/assertive)
- ✅ Confirms request_changes_workflow is disabled for solo development
- ✅ Verifies summary features are enabled
- ✅ Checks sequence diagrams are enabled
- ✅ Confirms poem feature is disabled
- ✅ Validates labeling automation
- ✅ Verifies path filters exclude build/test directories
- ✅ Checks JPA Entity conventions in path instructions
- ✅ Validates Controller conventions in path instructions
- ✅ Confirms timezone and logging guidance for Java files

### 3. Chat Configuration Tests (1 test)
- ✅ Verifies auto_reply is enabled

### 4. Code Generation Configuration Tests (3 tests)
- ✅ Validates docstrings configuration
- ✅ Confirms unit_tests configuration
- ✅ Checks Service class test instructions mention JUnit

### 5. Pre-merge Checks Configuration Tests (2 tests)
- ✅ Verifies all checks are in warning mode
- ✅ Validates docstrings threshold is configured

### 6. Tools Configuration Tests (5 tests)
- ✅ Confirms security tools (gitleaks, osv-scanner) are enabled
- ✅ Validates Java static analysis tools (PMD) are enabled
- ✅ Checks infrastructure tools (hadolint, checkov) are enabled
- ✅ Verifies linting tools (markdownlint, actionlint, yamllint) are enabled
- ✅ Confirms GitHub checks integration is enabled

### 7. Edge Cases and Robustness Tests (4 tests)
- ✅ Validates no null values in critical paths
- ✅ Confirms valid boolean values where expected
- ✅ Checks path instructions are non-empty
- ✅ Verifies minimum number of path instructions exist

## Total Test Count
**27 comprehensive test methods** organized into 7 nested test classes

## Running the Tests

### Using Gradle
```bash
./gradlew test --tests CodeRabbitConfigTest
```

### Using IDE
Run the test class directly from your IDE (IntelliJ IDEA, Eclipse, VS Code)

## Why These Tests Matter

### For Configuration Files
Configuration files like `.coderabbit.yaml` are critical for project automation but are often overlooked in testing. These tests ensure that:

1. **Schema Validity**: The YAML file is parseable and well-formed
2. **Required Keys**: All necessary configuration keys are present
3. **Value Correctness**: Critical values are set to expected states
4. **Convention Compliance**: Project-specific conventions (JPA, REST API, timezone handling) are properly configured
5. **Tool Integration**: All code quality and security tools are enabled

### Project-Specific Benefits
For this Spring Boot project, the tests validate:
- **JPA Entity Conventions**: Ensures Long type for IDs, FK constraint preferences
- **REST API Standards**: Validates kebab-case URIs, proper HTTP methods, response formats
- **Timezone Awareness**: Confirms guidance for LocalDateTime usage with Flutter frontend
- **Sentry Integration**: Ensures proper logging and error tracking configuration

## Maintenance

### When to Update Tests
Update these tests when:
1. Adding new path_instructions to `.coderabbit.yaml`
2. Enabling/disabling tools in the configuration
3. Changing review profile or check modes
4. Modifying language or version settings

### Test Philosophy
These tests follow the principle that **configuration is code** and should be validated just like any other code in the project. They provide:
- **Regression Prevention**: Catches accidental configuration changes
- **Documentation**: Serves as executable documentation of expected configuration
- **Confidence**: Ensures code review automation works as intended

## Dependencies
The test requires SnakeYAML for YAML parsing:
```gradle
testImplementation 'org.yaml:snakeyaml:2.2'
```

This dependency has been added to `build.gradle` under the test dependencies section.

## Future Enhancements
Potential improvements to consider:
1. Add tests for specific instruction text validation
2. Validate path glob patterns are syntactically correct
3. Test configuration against CodeRabbit's JSON schema
4. Add performance tests for configuration loading
5. Validate tool versions against latest available