<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Platform Android" />
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Architecture-MVVM%20%2F%20Clean-FF6F00?style=for-the-badge" alt="Architecture" />
  <img src="https://img.shields.io/badge/Min%20SDK-24-brightgreen?style=for-the-badge" alt="Min SDK" />
  <img src="https://img.shields.io/badge/Target%20SDK-36-green?style=for-the-badge" alt="Target SDK" />
  <img src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge" alt="License MIT" />
</p>

---

## 📌 Оглавление
* [Описание](#-описание)
* [Ключевые возможности](#-ключевые-возможности)
* [Стек технологий](#-стек-технологий)
* [Архитектура проекта](#-архитектура-проекта)
* [Структура пакетов](#-структура-пакетов)
* [Требования и сборка](#-требования-и-сборка)
* [Безопасность и хранение данных](#-безопасность-и-хранение-данных)
* [Тестирование](#-тестирование)
* [Вклад в проект (Contributing)](#-вклад-в-проект-contributing)
* [Лицензия](#-лицензия)

---

## 📖 Описание

**DeadlineKiller** — современное мобильное приложение для операционной системы Android, разработанное для эффективного планирования задач, жесткого контроля дедлайнов и повышения персональной продуктивности.

Приложение построено с использованием декларативного UI-фреймворка **Jetpack Compose** и дизайн-системы **Material 3**, использует принципы чистой архитектуры (**Clean Architecture / MVVM**) и обеспечивает надежное локальное шифрование пользовательских данных.

---

## ✨ Ключевые возможности

* 📋 **Трекинг дедлайнов:** Создание, редактирование, категоризация и удаление задач с фиксацией точного времени сдачи.
* ⏰ **Фоновые напоминания:** Надежное расписание и отправка периодических уведомлений о приближающихся сроках с помощью `WorkManager`.
* 🔒 **Безопасность и приватность:** Хэширование учетных данных/паролей с помощью `jBCrypt` и шифрованное хранилище `AndroidX Security Crypto`.
* 💾 **Офлайн-режим (Offline-first):** Надежная локальная персистентность данных на базе `Room Database` и реактивное чтение через корутины и `Flow`.
* ⚙️ **Гибкие настройки:** Сохранение пользовательских предпочтений и параметров отображения с помощью `Jetpack DataStore Preferences`.
* 🖼️ **Медиа-вложения:** Асинхронная загрузка и кэширование изображений для задач благодаря `Coil`.
* 🎨 **Material Design 3:** Современный адаптивный интерфейс с поддержкой динамических цветов (Dynamic Color) и темной/светлой темы.

---

## 🛠 Стек технологий

### Ядро и UI
* **Язык:** Kotlin 
* **UI:** Jetpack Compose (BOM) + Material 3 + Material Icons Extended
* **Навигация:** Navigation Compose (`androidx.navigation:navigation-compose`)
* **Асинхронность:** Kotlin Coroutines & StateFlow

### Архитектура и DI
* **Архитектурный паттерн:** Clean Architecture + MVVM (Model-View-ViewModel)
* **Внедрение зависимостей:** Dagger Hilt (`hilt-android`, `hilt-compiler`)
* **Lifecycle:** ViewModel Compose, Lifecycle Runtime KTX

### Персистентность и фоновые процессы
* **Локальная БД:** Room Database (`room-runtime`, `room-compiler`)
* **Фоновые задачи:** WorkManager (`androidx.work:work-runtime-ktx`)
* **Настройки:** DataStore Preferences
* **Безопасность:** AndroidX Security Crypto (`EncryptedSharedPreferences`), jBCrypt

### Работа с медиа
* **Изображения:** Coil Compose

### Тестирование
* **Unit-тесты:** JUnit 4, MockK, Kotlinx Coroutines Test
* **UI/Инструментальные тесты:** Compose UI Test JUnit4, Espresso Core, AndroidX Test Runner

---

## 🏛 Архитектура проекта

Приложение следует принципам **Unidirectional Data Flow (UDF)** и многослойной архитектуры:

```
┌────────────────────────────────────────────────────────┐
│                   UI Layer (Compose)                   │
│   Screens / Components / Composables / Theme           │
└───────────────────────────▲────────────────────────────┘
                            │ StateFlow / UI Events
┌───────────────────────────┴────────────────────────────┐
│                    ViewModel Layer                     │
│   Управление состоянием UI (State) и обработка событий │
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────┴────────────────────────────┐
│                   Domain Layer (Use Cases)             │
│   Чистая бизнес-логика валидации дедлайнов и триггеров │
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────┴────────────────────────────┐
│                       Data Layer                       │
│  Repositories / Room DAO / DataStore / Encrypted Store │
└────────────────────────────────────────────────────────┘
```

---

## 📂 Структура пакетов

Базовый пакет приложения: `com.bignerdranch.android.deadlinetimer`

```text
app/src/main/java/com/bignerdranch/android/deadlinetimer/
├── data/
│   ├── local/
│   │   ├── dao/             # Room Data Access Objects
│   │   ├── entity/          # Сущности базы данных
│   │   └── AppDatabase.kt   # Конфигурация Room DB
│   ├── preferences/         # DataStore менеджер
│   ├── repository/          # Реализация репозиториев
│   └── security/            # Логика шифрования и хэширования (jBCrypt / Crypto)
├── di/                      # Модули Dagger Hilt (DatabaseModule, RepositoryModule и др.)
├── domain/
│   ├── model/               # Доменные модели данных
│   ├── repository/          # Интерфейсы репозиториев
│   └── usecase/             # Use Cases / Интеракторы
├── ui/
│   ├── navigation/          # Compose Navigation графы и маршруты
│   ├── screens/             # Экраны (детали дедлайна, список, создание, настройки)
│   ├── theme/               # Material 3 Color, Type, Shape, Theme
│   └── components/          # Переиспользуемые Compose-компоненты
├── workers/                 # Worker-классы для WorkManager (фоновые алерты)
└── DeadlineApp.kt           # Application класс с аннотацией @HiltAndroidApp
```

---

## 🚀 Требования и сборка

### Требования к окружению
* **Android Studio:** Ladybug / Meerkat (или новее)
* **JDK:** Java 11 (или Java 17 с targetCompatibility = 11)
* **Android SDK:** 
  * `minSdk`: 24 (Android 7.0 Nougat)
  * `targetSdk` / `compileSdk`: 36

### Клонирование и запуск

1. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/execorix/DeadlineKiller.git
   cd DeadlineKiller
   ```

2. Откройте проект в Android Studio (`File` -> `Open...` -> выберите папку проекта).

3. Дождитесь завершения Gradle Sync.

4. Соберите и запустите приложение через CLI:
   ```bash
   # Сборка debug APK
   ./gradlew assembleDebug

   # Установка и запуск на подключенном устройстве/эмуляторе
   ./gradlew installDebug
   ```

---

## 🔐 Безопасность и хранение данных

* **jBCrypt:** Используется для безопасного одностороннего хэширования паролей или PIN-кодов пользователя при входе в защищенные разделы приложения.
* **AndroidX Security Crypto:** Обеспечивает криптографическую защиту конфиденциальных ключей и настроек в аппаратном или защищенном хранилище Android Keystore.
* **Room Database:** Обеспечивает строгую типизацию, транзакционность и целостность сохраняемых задач без утечек памяти.

---

## 🧪 Тестирование

Запуск модульных тестов (Unit tests):
```bash
./gradlew testDebugUnitTest
```

Запуск инструментальных тестов на эмуляторе/устройстве:
```bash
./gradlew connectedDebugAndroidTest
```

---

## 🤝 Вклад в проект (Contributing)

Будем рады вашим Pull Request'ам и предложениям:

1. Сделайте **Fork** проекта
2. Создайте свою ветку (`git checkout -b feature/NewAwesomeFeature`)
3. Зафиксируйте изменения (`git commit -m "feat: Add NewAwesomeFeature"`)
4. Отправьте ветку на GitHub (`git push origin feature/NewAwesomeFeature`)
5. Создайте **Pull Request**

---

## 📄 Лицензия

Проект распространяется под лицензией [MIT](LICENSE).

---

<p align="center">
  Разработано с ❤️ автором <a href="https://github.com/execorix">@execorix</a>
</p>

🏷 Теги — группировка и фильтрация задач по категориям

📊 Статистика — наглядный обзор ваших дедлайнов и прогресса

🛠 Технологии
Технология	Назначение
Kotlin	Основной язык разработки
Android SDK	Платформа
Dagger	Внедрение зависимостей (DI)
Room	Локальная база данных (SQLite)
🚀 Установка и запуск
Требования
Android Studio (последняя версия)

JDK 17+

Android SDK с минимальной поддерживаемой версией 
