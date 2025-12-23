# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

Secured Properties is a JavaFX application for securely storing credentials and properties in encrypted files. It uses Windows Data Protection API (DPAPI) via the windpapi4j library for encryption when available, with fallback to PBKDF2-based AES encryption. Properties can be secured with a master password and organized hierarchically using labels.

## Build System

This project uses Gradle with Kotlin DSL and the Java Module System (JPMS). It requires Java 21+ and uses Kotlin 2.0.

### Common Commands

**Build the project:**
```powershell
.\gradlew.bat build
```

**Run the console application:**
```powershell
.\gradlew.bat run --args="-help"
```

**Run the GUI:**
```powershell
.\gradlew.bat run --args="-gui"
```

**Run tests:**
```powershell
.\gradlew.bat test
```

**Create distributable packages (Windows installer):**
```powershell
.\gradlew.bat jpackage
```

**Create runtime image with jlink:**
```powershell
.\gradlew.bat jlink
```

**Clean build artifacts:**
```powershell
.\gradlew.bat clean
```

**Note:** This project uses the Gradle wrapper (gradlew.bat). If gradle is installed globally, you can use `gradle` instead.

## Architecture

### Core Components

**SecStorage (SecStorage.java)**
- Central singleton managing secure storage operations
- Handles Windows DPAPI integration via windpapi4j
- Manages master password encryption/decryption using PBKDF2WithHmacSHA256
- Supports two security modes:
  - Windows-secured: Uses DPAPI to encrypt master password (user-specific)
  - Master password: Requires password for each access
- Key methods: `createNewSecureStorage()`, `open_SecuredStorage()`, `addSecuredProperty()`, `addUnsecuredProperty()`

**SecureProperties (SecureProperties.java)**
- Manages property storage using Guava's ArrayListMultimap
- Persists properties to `.properties` files with UTF-8 encoding
- Handles file I/O and property serialization
- Properties format: `label@@label@@...@@key=value` with `{ENC}value{ENC}` for encrypted values
- File structure: Header section (metadata) + Data section (user properties)

**Enc (Enc.kt)**
- Kotlin-based encryption utilities
- Implements AES/CBC/PKCS5Padding with PBKDF2WithHmacSHA256 key derivation
- 500,000 iterations, 256-bit keys, 64-byte salt
- Combines salt + IV + ciphertext in single Base64-encoded string
- Password generation with configurable character sets

**SecureString (SecureString.java)**
- Wrapper for sensitive strings that zeroes memory on destruction
- Use `.destroyValue()` to clear sensitive data from memory

**SecureProperty (SecureProperty.java)**
- Represents individual properties with hierarchical keys
- Keys stored as LinkedHashSet<String> maintaining insertion order
- Supports encrypted/unencrypted values
- Label separator: `@@`

### UI Components

**Gui (Gui.java)**
- JavaFX Application entry point
- Loads MainWindow.fxml for UI layout

**MainWindowController (MainWindowController.java)**
- FXML controller managing the main window
- Handles property CRUD operations, CSV import, label management

**Console (Console.java)**
- CLI entry point using Apache Commons CLI
- Supports file operations: create, print, add/get/delete properties
- Main class defined in module-info.java: `ch.vilki.secured.Console`

### Key Design Patterns

1. **Hierarchical Property Keys**: Properties use `@@` delimiter to create tree structure (e.g., `server@@integration@@password`)
2. **Dual Encryption Modes**: Windows DPAPI for single-user convenience or master password for portability
3. **Version Detection**: `ENC_VERSION` property tracks encryption format (current: version 2)
4. **Singleton Storage**: `SecStorage._storage` ensures single instance per operation

### File Format

Storage files use `.properties` extension with structure:
```
-------------------------------@@HEADER_START@@-------------------------------------------------------------
STORAGE@@MASTER_PASSWORD_HASH=<salt>$<hash>
STORAGE@@MASTER_PASSWORD_WINDOWS_SECURED=<base64-dpapi-encrypted>
STORAGE@@WINDOWS_SECURED=<test-value>
STORAGE@@ENC_VERSION=2
-------------------------------@@HEADER_END@@-------------------------------------------------------------
server@@integration@@user=myIntUser
server@@integration@@password={ENC}<base64-encrypted>{ENC}
```

## Testing

Tests are located in `src/test/java/` and use JUnit Jupiter. Note that `compileTestJava` is disabled in build.gradle.kts (line 102), so tests may not compile automatically. The test files serve as integration examples:

- **TestSecProperties.java**: Unit tests for SecureProperties class
- **Storage.java**: Integration tests for SecStorage operations
- **CommandLine.java**: CLI command testing
- **CSV.java**: CSV import functionality testing

To run tests, ensure test compilation is enabled first.

## Module System

This project uses Java modules (JPMS). The module descriptor is in `src/main/java/module-info.java`:
- Module name: `ch.vilki.secured`
- Requires: JavaFX, windpapi4j, SLF4J, BouncyCastle, Apache Commons CLI/Codec, Guava, Kotlin stdlib
- Exports package: `ch.vilki.secured`

## Version Management

Application version is defined in `gradle.properties` and auto-generated into `ApplicationVersion.java` by the `generateVersionClass` task before compilation.

## Property Key Format

When working with properties, remember:
- Labels and keys are **case-sensitive**
- Use `@@` as separator (e.g., `server@@integration@@user`)
- Properties without labels are valid (e.g., `mykey`)
- Special internal keys use `STORAGE@@` prefix (reserved)

## Security Notes

- Master passwords must be at least 12 characters for secured storage
- Windows DPAPI encryption is user-specific; files encrypted by one Windows user require master password when opened by another
- Encrypted values in memory should use SecureString and call `.destroyValue()` when done
- Avoid characters like `&`, `$`, `;`, `|` in passwords when using in scripts
