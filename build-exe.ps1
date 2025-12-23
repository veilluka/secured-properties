# Build Windows Executable using jpackage
# This script creates a native Windows application (app-image)

Write-Host "Building secured-properties Windows executable..." -ForegroundColor Green

# Step 1: Build the distribution
Write-Host "`n[1/3] Building distribution..." -ForegroundColor Cyan
.\gradlew.bat installDist --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}

# Step 2: Get version
$version = (Get-Content gradle.properties | Select-String "^version\s*=\s*(.+)").Matches.Groups[1].Value.Trim()
Write-Host "Version: $version" -ForegroundColor Yellow

# Step 3: Create jpackage parameters
$appName = "secured-properties"
$mainJar = "secured-properties-$version.jar"
$installDir = "build\install\secured-properties"
$inputDir = "$installDir\lib"
$mainClass = "ch.vilki.secured.Console"
$modulePath = "$installDir\lib"

Write-Host "`n[2/3] Creating Windows executable with jpackage..." -ForegroundColor Cyan

# Clean previous output
if (Test-Path "build\jpackage-output") {
    Remove-Item -Recurse -Force "build\jpackage-output"
}

# Run jpackage to create app-image (standalone app folder)
jpackage `
    --input "$inputDir" `
    --name "$appName" `
    --main-jar "$mainJar" `
    --main-class "$mainClass" `
    --type app-image `
    --app-version "$version" `
    --description "Secure storage for properties and credentials" `
    --vendor "Vedran Bauer" `
    --win-console `
    --dest "build\jpackage-output" `
    --verbose

if ($LASTEXITCODE -ne 0) {
    Write-Host "`njpackage failed!" -ForegroundColor Red
    exit 1
}

Write-Host "\n[3/3] Application created successfully!" -ForegroundColor Green
Write-Host "\nOutput location: build\jpackage-output\$appName\" -ForegroundColor Yellow
Write-Host "\nTo run the app, use: build\jpackage-output\$appName\$appName.exe" -ForegroundColor White

# Create ZIP for distribution
Write-Host "\n[4/4] Creating ZIP archive for distribution..." -ForegroundColor Cyan
$zipFile = "build\jpackage-output\$appName-$version-windows-portable.zip"
if (Test-Path $zipFile) {
    Remove-Item $zipFile -Force
}
Compress-Archive -Path "build\jpackage-output\$appName" -DestinationPath $zipFile -CompressionLevel Optimal

Write-Host "\nZIP created: $zipFile" -ForegroundColor Green

# List created files
Get-ChildItem "build\jpackage-output" -File | Format-Table Name, @{Label="Size (MB)"; Expression={[math]::Round($_.Length/1MB, 2)}}, LastWriteTime
