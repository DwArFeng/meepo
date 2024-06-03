#!/bin/bash

# Echo the event message.
# This observer is for testing purposes only.

echo "Hello from echo-observer.sh"
echo "Execute id: $1"
echo "Observer id: $2"

case $3 in
    "CONDITION_PASSED")
        echo "Event: condition passed"
        ;;
    "CONDITION_NOT_PASSED")
        echo "Event: condition not passed"
        echo "Failed condition: $4"
        ;;
    "MODULES_STARTED")
        echo "Event: modules started"
        ;;
    "MODULES_FINISHED")
        echo "Event: modules finished"
        echo "Executed modules: $4"
        echo "Finished modules: $5"
        echo "Failed modules: $6"
        ;;
    "MODULE_STARTED")
        echo "Event: module started"
        echo "Module id: $4"
        ;;
    "MODULE_FINISHED")
        echo "Event: module finished"
        echo "Module id: $4"
        ;;
    "MODULE_FAILED")
        echo "Event: module failed"
        echo "Module id: $4"
        ;;
esac
