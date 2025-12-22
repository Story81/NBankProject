#!/bin/bash

set -e  # остановка при любой ошибке

# Настройка конфигурации
IMAGE_NAME=nbank-tests
TEST_PROFILE=${1:-api} # аргумент запуска (api / ui)
TIMESTAMP=$(date +"%Y_%m-%d_%H_%M")
DOCKER_COMPOSE_FILE=./infra/docker_compose/docker-compose.yml
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

echo ">>> Поднятие тестового окружения через Docker Compose"
docker compose -f "$DOCKER_COMPOSE_FILE" up -d backend frontend selenoid selenoid-ui

echo ">>> Ожидание доступности сервисов"
sleep 10

echo ">>> Тесты запущены..."
docker run --rm \
  --network nbank-network \
  -v "$TEST_OUTPUT_DIR/logs:/app/logs" \
  -v "$TEST_OUTPUT_DIR/results:/app/target/surefire-reports" \
  -v "$TEST_OUTPUT_DIR/report:/app/target/site" \
  -e TEST_PROFILE="$TEST_PROFILE" \
  -e APIBASEURL=http://backend:4111 \
  -e UIBASEURL=http://frontend:80 \
  -e SELENOID_URL=http://selenoid:4444 \
  -e SELENOID_UI_URL=http://selenoid-ui:8080 \
  $IMAGE_NAME

echo ">>> Останавливаем тестовое окружение"
docker compose -f "$DOCKER_COMPOSE_FILE" down

echo
echo "✅  Тесты завершены успешно!"
echo "Лог файл:   $TEST_OUTPUT_DIR/logs/run.log"
echo "Результаты: $TEST_OUTPUT_DIR/results"
echo "Отчёт:      $TEST_OUTPUT_DIR/report"