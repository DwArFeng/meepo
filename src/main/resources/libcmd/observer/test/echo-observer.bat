@echo off

rem Echo the event message.
rem This observer is for testing purpose only.

echo "Hello from echo-observer.bat"
echo "Execute id: %1"
echo "Observer id: %2"

if "%3" == "CONDITION_PASSED" (
    echo "Event: condition passed"
    goto end
)
if "%3" == "CONDITION_NOT_PASSED" (
    echo "Event: condition not passed"
    echo "Failed condition: %4"
    goto end
)
if "%3" == "MODULES_STARTED" (
    echo "Event: modules started"
    goto end
)
if "%3" == "MODULES_FINISHED" (
    echo "Event: modules finished"
    echo "Executed modules: %4"
    echo "Finished modules: %5"
    echo "Failed modules: %6"
    goto end
)
if "%3" == "MODULE_STARTED" (
    echo "Event: module started"
    echo "Module id: %4"
    goto end
)
if "%3" == "MODULE_FINISHED" (
    echo "Event: module finished"
    echo "Module id: %4"
    goto end
)
if "%3" == "MODULE_FAILED" (
    echo "Event: module failed"
    echo "Module id: %4"
    curl -X POST -H "Content-Type: application/json" -d "{\"message\":\"Module failed: %4\"}" http://localhost:8080/api/notify
    goto end
)

:end
