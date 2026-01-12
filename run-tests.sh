#!/bin/bash

# Настройка
IMAGE_NAME=nbank-tests
TEST_PROFILE=${1:-api} # аргумент запуска
TIMESTAMP=$(date +"%Y_%m-%d_%H_%M")
# Абсолютный путь к папке проекта
TEST_OUTPUT_DIR="$PWD/test-output/$TIMESTAMP"

case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*) TEST_OUTPUT_DIR="$(cygpath -m "$TEST_OUTPUT_DIR")" ;;
esac

# Собираем Docker образ
echo ">>> Сборка тестов запущена"
docker build -t $IMAGE_NAME .

mkdir -p "$TEST_OUTPUT_DIR/logs"
mkdir -p "$TEST_OUTPUT_DIR/results"
mkdir -p "$TEST_OUTPUT_DIR/report"

# Запуск Docker контейнера
echo ">>> Тесты запущены"
echo "TEST_OUTPUT_DIR=$TEST_OUTPUT_DIR"
docker run --rm \
  -v "$TEST_OUTPUT_DIR/logs:/app/logs" \
  -v "$TEST_OUTPUT_DIR/results:/app/target/surefire-reports" \
  -v "$TEST_OUTPUT_DIR/report:/app/target/site" \
  -e TEST_PROFILE="$TEST_PROFILE" \
  -e APIBASEURL=http://host.docker.internal:4111 \
  -e UIBASEURL=http://host.docker.internal:3000 \
  "$IMAGE_NAME"

# Вывод итогов
echo "✅ >>> Тесты завершены успешно! "
echo "Лог файл:          $TEST_OUTPUT_DIR/logs/run.log"
echo "Результаты тестов: $TEST_OUTPUT_DIR/results"
echo "Отчет:             $TEST_OUTPUT_DIR/report"