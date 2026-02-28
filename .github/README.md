# GitHub Actions Workflows

## Build and Test Workflow

### Overview
This workflow automatically compiles the Java code and runs all unit tests on every push to any branch.

### Triggers
- **Push**: Runs on every push to any branch
- **Pull Request**: Runs on pull requests to any branch

### Workflow Steps

1. **Checkout code**: Retrieves the repository code
2. **Set up JDK 17**: Configures Java Development Kit 17 (Temurin distribution) with Maven caching
3. **Grant execute permission**: Ensures the Maven wrapper (`mvnw`) is executable
4. **Build with Maven**: Compiles the project using `./mvnw clean compile`
5. **Run unit tests**: Executes all unit tests using `./mvnw test`
6. **Generate test report**: Creates a detailed test report (runs even if tests fail)
7. **Upload test results**: Archives test results and coverage reports as artifacts
8. **Test Summary**: Provides a summary in the GitHub Actions UI

### Artifacts
After each run, the following artifacts are available for download:
- **test-results**: Contains test reports and JaCoCo coverage reports
  - `target/surefire-reports/` - Maven Surefire test reports (XML and TXT formats)
  - `target/jacoco-report/` - JaCoCo code coverage reports (HTML)

### Viewing Results
- Test results are visible in the "Actions" tab of your GitHub repository
- Click on any workflow run to see detailed logs
- Download artifacts from the workflow run summary page

### Local Testing
To run the same checks locally before pushing:
```bash
# Compile the project
./mvnw clean compile

# Run all tests
./mvnw test

# View coverage report (after running tests)
open target/jacoco-report/index.html  # On macOS
xdg-open target/jacoco-report/index.html  # On Linux
```

### Configuration
The workflow uses:
- **Java Version**: 17 (Temurin distribution)
- **Build Tool**: Maven (using Maven wrapper)
- **Test Framework**: JUnit with RestAssured
- **Coverage Tool**: JaCoCo

