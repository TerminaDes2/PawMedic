@echo off
setlocal
cd /d "%~dp0"
"%~dp0runtime\bin\java.exe" --enable-native-access=ALL-UNNAMED -cp "%~dp0lib\*" com.pawsmedic.desktop.veterinary.MainKt
if errorlevel 1 (
    echo.
    echo PawMedic no pudo iniciarse. Revise el mensaje anterior.
    pause
)
endlocal
