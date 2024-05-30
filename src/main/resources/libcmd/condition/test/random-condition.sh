#!/bin/bash

# Generate a random number, then decide to return 0 or 1 based on the parity of this random number.
# 0 represents true, 1 represents false.
# This condition is for testing purposes only.

echo "Hello from random-condition.sh"
echo "Execute id: $1"
echo "Condition command id: $2"

randBool=$((RANDOM % 2))
echo $randBool
if [ $randBool -eq 1 ]
then
    echo "Random boolean is true, condition passed"
    exit 0
else
    echo "Random boolean is false, condition failed"
    exit 1
fi
