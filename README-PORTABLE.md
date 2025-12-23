# Secured Properties - Windows Portable Edition

This is a standalone Windows executable version of Secured Properties that includes a bundled Java runtime. **No separate Java installation required.**

## What's Included

- `secured-properties.exe` - Native Windows executable
- `runtime/` - Bundled Java 22 runtime
- `app/` - Application JARs and dependencies

## Requirements

- Windows 10 or later
- No Java installation needed (runtime is included)

## How to Use

### GUI Mode

Double-click `secured-properties.exe` or run from command line:
```cmd
secured-properties.exe -gui
```

### Console Mode

Open Command Prompt or PowerShell in this folder and use:

**Get help:**
```cmd
secured-properties.exe -help
```

**Create a secure storage file:**
```cmd
secured-properties.exe -create myStorage.properties -pass mySecurePassword123
```

**Add encrypted property:**
```cmd
secured-properties.exe -addSecured myStorage.properties -key server@@prod@@password -value mySecret
```

**Add unencrypted property:**
```cmd
secured-properties.exe -addUnsecured myStorage.properties -key server@@prod@@user -value admin
```

**Get property value:**
```cmd
secured-properties.exe -getValue myStorage.properties -key server@@prod@@password
```

**Print all properties:**
```cmd
secured-properties.exe -print myStorage.properties
```

## Features

- **Windows DPAPI Integration**: On Windows, uses DPAPI for user-specific encryption
- **Master Password**: Cross-platform encryption with password
- **Hierarchical Properties**: Organize properties with labels using `@@` separator
- **GUI & CLI**: Both graphical and command-line interfaces
- **CSV Import**: Import properties from CSV files

## Property Format

Properties use a hierarchical structure with `@@` as separator:
```
label@@sublabel@@key=value
```

Example:
```
server@@integration@@user=myUser
server@@integration@@password={encrypted}
server@@production@@user=prodUser
server@@production@@password={encrypted}
```

## Size

This portable version is approximately 75 MB (includes full Java runtime).

For a smaller distribution that requires Java to be installed separately, download the standard distribution ZIP.

## Documentation

For complete documentation, visit: https://github.com/veilluka/secured-properties
