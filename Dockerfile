# back
# устанавливаем самую лёгкую версию JVM
FROM openjdk:17-jdk-alpine

ENV DATA_HOST=mysql

# указываем ярлык. Например, разработчика образа и проч. Необязательный пункт.
LABEL maintainer="re@recupe.ru"

# указываем точку монтирования для внешних данных внутри контейнера (как мы помним, это Линукс)
VOLUME /tmp

# внешний порт, по которому наше приложение будет доступно извне
EXPOSE 3000

# указываем, где в нашем приложении лежит джарник
ARG JAR_FILE=target/smartcity-0.0.1-SNAPSHOT.jar

# добавляем джарник в образ под именем rebounder-chain-backend.jar
ADD ${JAR_FILE} smartcity-0.0.1-SNAPSHOT.jar

# команда запуска джарника
ENTRYPOINT ["java","-jar","/smartcity-0.0.1-SNAPSHOT.jar"]