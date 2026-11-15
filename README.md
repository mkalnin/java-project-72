# Анализатор страниц

[![Actions Status](https://github.com/mkalnin/java-project-72/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/mkalnin/java-project-72/actions)
[![Java CI](https://github.com/mkalnin/java-project-72/actions/workflows/ci.yaml/badge.svg)](https://github.com/mkalnin/java-project-72/actions/workflows/ci.yaml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mkalnin_java-project-72&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=mkalnin_java-project-72)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=mkalnin_java-project-72&metric=coverage)](https://sonarcloud.io/summary/new_code?id=mkalnin_java-project-72)

**Анализатор страниц** — веб-приложение, которое проверяет сайты на SEO-пригодность.
Пользователь добавляет адрес сайта, приложение сохраняет его в базу данных и показывает список добавленных сайтов.

## Возможности

- Добавление сайта по адресу: в базу попадает только домен с протоколом и портом
- Проверка адреса и защита от дубликатов
- Список всех добавленных сайтов, новые — первыми
- Страница с информацией о конкретном сайте
- Flash-сообщения об успехе и ошибках

## Требования

- Java 21+
- Gradle 8+
- Node.js 20+

## Установка

```bash
# Клонирование репозитория
git clone https://github.com/mkalnin/java-project-72.git
cd java-project-72

# Сборка проекта (стили + приложение)
make -C app setup
```

## Использование

Запуск приложения локально:

```bash
PORT=7070 ./app/build/install/app/bin/app
```

Приложение будет доступно по адресу http://localhost:7070. Порт задаётся переменной окружения `PORT` (по умолчанию 7070).

По умолчанию используется база данных H2 в памяти. Для подключения к другой базе (например, PostgreSQL) задайте переменную окружения `JDBC_DATABASE_URL`:

```bash
export JDBC_DATABASE_URL=jdbc:postgresql://db:5432/postgres?password=password&user=postgres
```

## Разработка

```bash
make -C app setup        # установка зависимостей и сборка стилей
make -C app start        # режим разработки (вотчер стилей + приложение)
make -C app build        # сборка проекта
```

Тесты и покрытие:

```bash
cd app
./gradlew test           # запуск тестов
./gradlew jacocoTestReport  # отчёт о покрытии
```

---

<details>
<summary>Автоматические тесты Хекслета</summary>

Тесты запускаются на каждый коммит. За запуск отвечает файл `.github/workflows/hexlet-check.yml` — не удаляйте и не переименовывайте ни его, ни репозиторий.

</details>

## О Хекслете

[Хекслет](https://ru.hexlet.io/) — школа программирования: авторские программы обучения с практикой, поддержкой наставников и реальными проектами, которые остаются в резюме. Этот репозиторий — один из таких проектов.
