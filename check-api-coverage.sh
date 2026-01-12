#!/bin/bash

MIN_COVERAGE=${1:-50}
HTML_FILE="swagger-coverage-report.html"

[ ! -f "$HTML_FILE" ] && { echo "ERROR: $HTML_FILE not found"; exit 1; }

# Извлекаем процент из строки "Covered: N% "
COVERAGE=$(grep -o 'Covered:[^<]*' "$HTML_FILE" | grep -o '[0-9]*%' | head -1 | tr -d '%')

[ -z "$COVERAGE" ] && COVERAGE=0

echo "API Conditions Coverage: ${COVERAGE}%"
echo "Minimum Required: ${MIN_COVERAGE}%"

if [ "$COVERAGE" -lt "$MIN_COVERAGE" ]; then
    echo "❌ QUALITY GATE FAILED: API coverage ${COVERAGE}% is below minimum ${MIN_COVERAGE}%"
    exit 1
else
    echo "✅ QUALITY GATE PASSED: API coverage ${COVERAGE}% meets minimum ${MIN_COVERAGE}%"
    exit 0
fi