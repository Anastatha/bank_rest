# Система управления банковскими картами

1. Сборка приложения
   
   mvn clean package spring-boot:repackage
3. Сборка Docker образа и запуск контейнеров
   
   docker compose up --build
   
5. Проверка работы
   Swagger UI: http://localhost:8080/swagger-ui.html
   Тестовые учётные записи:
   
   ADMIN

   email: admin@gmail.com

   password: admin123
   
   USER
   
   email: nastya1152ty@gmail.com

   password: nastya1152ty
6. Юнит-тесты

   mvn test
