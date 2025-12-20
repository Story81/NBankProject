#!/bin/bash

# Настройки
IMAGE_NAME=nbank-tests
TAG=latest

# token и username можно хранить в переменных окружения или так:
#DOCKERHUB_USERNAME=username
#DOCKERHUB_TOKEN=token

# Логин в Docker Hub с токеном
echo ">>> Логин в Docker Hub с токеном"
if [ -z "$DOCKERHUB_USERNAME" ]; then
  echo "Ошибка: не установлена переменная окружения DOCKERHUB_USERNAME"
  exit 1
fi
if [ -z "$DOCKERHUB_TOKEN" ]; then
  echo "Ошибка: не установлена переменная окружения DOCKERHUB_TOKEN"
  exit 1
fi
echo "$DOCKERHUB_TOKEN"| docker login --username $DOCKERHUB_USERNAME --password-stdin


# Тегирование образа
echo ">>> Тегирование образа"
docker tag  "$IMAGE_NAME" $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG
if [ $? -ne 0 ]; then
  echo "Ошибка: не удалось тегировать образ"
  exit 1
fi


# Отправка образа в Docker Hub
echo ">>> Отправка образа в Docker Hub"
docker push "$DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"
if [ $? -ne 0 ]; then
  echo "Ошибка: не удалось отправить образ в Docker Hub"
  exit 1
fi

echo ">>> Готово! Образ доступен как: docker pull $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"

#3. Выполните скрипт
#chmod +x push-tests.sh
#./push-tests.sh (если установленные переменные окружения)
# выполняю команду с передачей данных в терминале  DOCKERHUB_USERNAME="story81" DOCKERHUB_TOKEN="dckr_pat_lalala" ./push-tests.sh

#4. Проверьте, что образ появился в вашем аккаунте На hub.docker.com → ваш профиль → Repositories → nbank-tests.

#5. Проверьте, что образ можно скачать На другой машине:
#docker pull $DOCKERHUB_USERNAME/nbank-tests:latest