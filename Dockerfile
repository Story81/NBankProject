
FROM maven:3.9.12-eclipse-temurin-21

# Дефолтные значения аргументов
ARG TEST_PROFILE=api
ARG APIBASEURL=http://localhost:4111
ARG UIBASEURL=http://localhost:3000

# Переменные окружения для контейнера
ENV TEST_PROFILE=${TEST_PROFILE}
ENV APIBASEURL=${APIBASEURL}
ENV UIBASEURL=${UIBASEURL}

# работаем из папки /app
WORKDIR /app

# копируем помник
COPY pom.xml .

# загружаем зависимости и кешируем
RUN mvn dependency:go-offline

# копируем весь проект в текущую папку
COPY . .

# теперь внутри есть зависимости, есть весь проект и мы готовы запускать тесты (под админом)
USER root

# mvn test -P api
# mvn -DskipTests=true surfire-report:report-only
# лог выводился не в консоль, а в файл
# bash file
CMD ["/bin/bash","-lc", "\
mkdir -p /app/logs ; \
{ \
  echo \">>> Running tests with profile: ${TEST_PROFILE}\" ; \
  mvn test -P ${TEST_PROFILE} ; \
  \
  echo \">>> Running surefire-report:report\" ; \
  mvn -DskipTests=true surefire-report:report ; \
  echo \">>> Done\" ; \
} > /app/logs/run.log 2>&1" ]