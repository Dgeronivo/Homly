#!/bin/bash
# PreToolUse hook: перевіряє білд перед `git commit`.
# Запускається з кореня проєкту ($CLAUDE_PROJECT_DIR).

echo "Перевіряю білд перед комітом..." >&2

if ./gradlew build; then
    echo "Білд успішний — коміт дозволено." >&2
    exit 0
else
    echo "Білд впав — коміт заблоковано. Виправ помилки збірки." >&2
    exit 2
fi
