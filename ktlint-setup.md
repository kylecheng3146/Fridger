# KtLint Setup for KMM Project

## Overview
This project is configured with ktlint for code style checking and formatting, following Kotlin official coding conventions. The setup is customized for KMM (Kotlin Multiplatform Mobile) projects to only check Android and Common source sets.

## Available Tasks

### Check Code Style (Android & Common only)
```bash
./gradlew ktlintCheckKMM
```
This command checks Android and Common Kotlin files for style violations without modifying them.

### Format Code (Android & Common only)
```bash
./gradlew ktlintFormatKMM
```
This command automatically formats your Android and Common Kotlin code according to the defined rules.

### Module-specific Tasks
You can also run ktlint for specific modules:
```bash
# Check only composeApp module (Android & Common)
./gradlew :composeApp:ktlintCheckAndroidAndCommon

# Format only composeApp module (Android & Common)
./gradlew :composeApp:ktlintFormatAndroidAndCommon
```

### All platforms (if needed)
```bash
# Check all platforms (including iOS, Desktop, Web)
./gradlew ktlintCheck

# Format all platforms
./gradlew ktlintFormat
```

## Configuration

### .editorconfig
The project uses `.editorconfig` which follows Kotlin official coding conventions:
- Indentation: 4 spaces
- Max line length: 120 characters
- Kotlin official code style
- Trailing commas allowed
- Star imports configuration

### Excluded Source Sets
The following source sets are excluded from the KMM-specific tasks:
- `iosMain`, `iosX64Main`, `iosArm64Main`, `iosSimulatorArm64Main` - iOS platform code
- `desktopMain` - Desktop platform code
- `wasmJsMain` - Web platform code
- `**/build/**` - Build directories
- `**/generated/**` - Generated code

### Included Source Sets
Only the following source sets are checked and formatted:
- `androidMain` - Android specific code
- `commonMain` - Shared code
- `androidTest` - Android test code
- `commonTest` - Common test code

## Reports
After running ktlint checks, reports are generated in:
- HTML Report: `build/reports/ktlint/ktlint[Check|Format][SourceSet].html`
- Checkstyle Report: `build/reports/ktlint/ktlint[Check|Format][SourceSet].xml`

## IDE Integration
If you're using IntelliJ IDEA or Android Studio:
1. The IDE will automatically pick up the `.editorconfig` settings
2. You can configure the IDE to run ktlintFormat on save:
   - Go to `Settings > Tools > Actions on Save`
   - Enable "Reformat code" option
   - The IDE will use the `.editorconfig` rules

## Pre-commit Hook (Optional)
To ensure code style before committing, you can add a Git pre-commit hook:

```bash
#!/bin/sh
./gradlew ktlintCheckKMM
```

Save this in `.git/hooks/pre-commit` and make it executable:
```bash
chmod +x .git/hooks/pre-commit
```

## Continuous Integration
You can add ktlint checks to your CI/CD pipeline:
```yaml
- name: Check Code Style
  run: ./gradlew ktlintCheckKMM
```

## Troubleshooting

### If you see errors in generated code
Generated code is excluded from ktlint checks. If you still see errors, try:
```bash
./gradlew clean
./gradlew ktlintCheckKMM
```

### If you want to check specific files
You can use the standard ktlint tasks with file filters:
```bash
./gradlew ktlintCheck --include="**/commonMain/**/*.kt"
```
