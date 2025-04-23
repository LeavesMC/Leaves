@echo off
setlocal enabledelayedexpansion

set /p increment=Enter a value to add on file number: 

for %%f in (*-*) do (
    set "filename=%%~nf"
    set "ext=%%~xf"

    :: Extract prefix and suffix
    for /f "tokens=1* delims=-" %%a in ("!filename!") do (
        set /a newnum=1%%a + %increment%
        set "newprefix=!newnum:~1!"
        ren "%%f" "!newprefix!-%%b!ext!"
    )
)

echo Done renaming.
pause
