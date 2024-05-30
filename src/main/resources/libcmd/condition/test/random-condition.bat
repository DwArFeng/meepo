@echo off

rem Generate a random number, then decide to return 0 or 1 based on the parity of this random number.
rem 0 represents true, 1 represents false.
rem This condition is for testing purposes only.

echo "Hello from random-condition.bat"
echo "Execute id: %1"
echo "Condition command id: %2"

set /a randBool=%random% %% 2
echo %randBool%
if %randBool% equ 1 (
    echo "Random boolean is true, condition passed"
    exit /b 0
) else (
    echo "Random boolean is false, condition failed"
    exit /b 1
)
