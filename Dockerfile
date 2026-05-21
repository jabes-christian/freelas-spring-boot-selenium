FROM eclipse-temurin:21-jre-alpine

# Diretório de trabalho dentro do container
WORKDIR /app

# Copia o JAR gerado pelo Maven
COPY target/*.jar app.jar

# Porta da aplicação
EXPOSE 8080

# Variável para ativar o perfil de produção
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]