@echo off
echo Building Softcore Addon for all Minecraft versions...
echo.

REM Create release directory
if not exist "release" mkdir "release"

echo Building for Minecraft 1.21.11...
call gradlew clean build "-PmcVersion=1.21.11"
if %errorlevel% neq 0 (
    echo Build failed for 1.21.11
    pause
    exit /b 1
)
copy "build\libs\softcore-addon-1.0.1-1.21.11.jar" "release\softcore-addon-1.0.1-1.21.11.jar"
echo Build successful for 1.21.11 - Saved to release folder

echo.
echo Building for Minecraft 1.21.10...
call gradlew clean build "-PmcVersion=1.21.10"
if %errorlevel% neq 0 (
    echo Build failed for 1.21.10
    pause
    exit /b 1
)
copy "build\libs\softcore-addon-1.0.1-1.21.10.jar" "release\softcore-addon-1.0.1-1.21.10.jar"
echo Build successful for 1.21.10 - Saved to release folder

echo.
echo Building for Minecraft 1.21.4...
call gradlew clean build "-PmcVersion=1.21.4"
if %errorlevel% neq 0 (
    echo Build failed for 1.21.4
    pause
    exit /b 1
)
copy "build\libs\softcore-addon-1.0.1-1.21.4.jar" "release\softcore-addon-1.0.1-1.21.4.jar"
echo Build successful for 1.21.4 - Saved to release folder

echo.
echo All builds completed successfully!
echo.
echo JAR files saved in release\:
echo - softcore-addon-1.0.1-1.21.11.jar
echo - softcore-addon-1.0.1-1.21.10.jar
echo - softcore-addon-1.0.1-1.21.4.jar
echo.
echo Total files in release folder:
dir "release" /b
echo.
pause
