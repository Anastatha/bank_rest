# Система управления банковскими картами

1. Сборка Docker образа и запуск контейнеров
   docker compose up --build
   Приложение доступно на порту 8080, база — на порту 5432
2. Проверка работы
   Swagger UI: http://localhost:8080/swagger-ui.html
   Тестовые учётные записи:
   ADMIN	email: admin@gmail.com password: admin123
   USER	    email: nastya1152ty@gmail.com password: nastya1152ty
3. Юнит-тесты
   mvn test
