# Rooming

Многомодульный Android-проект на Kotlin для лабораторной по `Clean Architecture`, `MVVM` и проверке архитектурных ограничений через `Konsist`.

## Архитектурный план

Базовая реализация в текущем workspace построена как `combined`-вариант:

- `domain` хранит бизнес-модели, интерфейсы репозиториев и use case'ы.
- `data` хранит fake-реализации репозиториев и общее in-memory состояние.
- `feature/*/api` публикует контракты маршрутов и точки интеграции.
- `feature/*/impl` хранит `ViewModel`, `UiState`, Compose-экраны и регистрацию навигации.
- `core:navigation` задаёт общий `FeatureEntry`-контракт, поэтому feature-impl не зависят друг от друга напрямую.
- `app` выступает composition root: собирает Hilt-граф, внедряет репозитории и подключает feature-entry через multibinding.

## Дерево модулей

```text
Rooming
├── app
├── core
│   ├── common
│   ├── analytics
│   ├── navigation
│   └── ui
├── domain
│   ├── model
│   ├── repository
│   └── usecase
├── data
│   ├── rooms
│   ├── favorites
│   └── bookings
├── feature
│   ├── rooms
│   │   ├── api
│   │   └── impl
│   ├── favorites
│   │   ├── api
│   │   └── impl
│   ├── bookings
│   │   ├── api
│   │   └── impl
│   ├── auth
│   │   ├── api
│   │   └── impl
│   └── about
│       ├── api
│       └── impl
└── quality
    └── architecture-test
```

## Реализованные сценарии

- список аудиторий
- детали аудитории
- избранные аудитории
- бронирование аудитории
- мои бронирования
- отмена бронирования
- авторизация через Яндекс ID и VK
- отправка событий в AppMetrica через общий `AnalyticsService`
- раздел «О нас» с картой офиса и построением маршрута

## Технологии

- Kotlin
- Jetpack Compose
- MVVM
- Coroutines + Flow
- Hilt
- Navigation Compose
- Gradle Kotlin DSL
- Version Catalog
- JUnit
- Konsist
- AppMetrica SDK
- Yandex ID LoginSDK
- VK Android SDK
- EncryptedSharedPreferences
- Yandex MapKit / Yandex Maps

## Лабораторная работа 6

SDK-зависимости спрятаны за интерфейсами и facade-классами:

- `core:analytics` содержит `AnalyticsService`, `AppMetricaAnalyticsService` и `FakeAnalyticsService`
- `feature:auth:api` содержит `AuthService`, `AuthProvider`, `AuthResult`, `AuthSession`
- `feature:auth:impl` содержит экран входа, `LoginViewModel`, LoginSDK-интеграцию и `SecureAuthStorage`
- `feature:about:impl` содержит экран «О нас», карту офиса и кнопку маршрута

Секреты не хранятся в исходниках. Добавьте ключи в `local.properties`:

```properties
APPMETRICA_API_KEY=your_appmetrica_key
YANDEX_CLIENT_ID=your_yandex_client_id
VK_CLIENT_ID=your_vk_app_id
VK_CLIENT_SECRET=your_vk_protected_key
YANDEX_MAPKIT_API_KEY=your_yandex_mapkit_key
```

`app/build.gradle.kts` передаёт значения в код через `buildConfigField`, а `local.properties` уже добавлен в `.gitignore`.
Из-за актуального Yandex MapKit `4.33.1-lite` приложение собирается с `minSdk = 26`.

Для карты нужен ключ `YANDEX_MAPKIT_API_KEY`. Экран «О нас» использует нативный `MapView` из Yandex MapKit прямо внутри приложения, а не WebView или браузер.

Для VK в кабинете приложения нужно указать:

- пакет: `com.example.rooming`
- Activity: `com.example.rooming.MainActivity`
- debug fingerprint SHA-1 без двоеточий: `119703882D4429916220D6F4667A2546884180D9`

VK-вход реализован через актуальный VK ID SDK. В `VK_CLIENT_SECRET` нужно вставить именно **Защищённый ключ** из кабинета VK. **Сервисный ключ доступа** для Android-входа не нужен: он используется для серверных запросов к API от имени приложения.

События аналитики:

- `screen_viewed` с параметром `screen_name=rooms` при открытии главного экрана
- `screen_viewed` с параметром `screen_name=login` при открытии экрана авторизации
- `user_logged_in` с параметром `provider=yandex` или `provider=vk` после успешного входа
- `room_booked` при успешном бронировании аудитории

Токен и имя пользователя сохраняются только в `EncryptedSharedPreferences`; обычный `SharedPreferences` для токенов не используется. При повторном запуске приложение проверяет сохранённую сессию и пропускает экран авторизации.

## Архитектурные варианты для веток

После `git init` можно зафиксировать три архитектурных варианта в отдельных ветках:

### `layer-based`

```text
app
core/*
domain/*
data/*
ui/*
quality/architecture-test
```

Идея:
- разбиение по слоям
- проще для первых лабораторных
- слабее масштабируется при росте числа фич

### `feature-based`

```text
app
core/*
feature/rooms/{domain,data,ui}
feature/favorites/{domain,data,ui}
feature/bookings/{domain,data,ui}
quality/architecture-test
```

Идея:
- каждая фича максимально автономна
- удобно масштабировать команды
- труднее переиспользовать общие domain-правила без выноса в core/domain

### `combined`

```text
app
core/*
domain/*
data/*
feature/*/api
feature/*/impl
quality/architecture-test
```

Идея:
- общий domain и data
- feature-level UI и navigation contracts
- лучший компромисс для лабораторной по Clean Architecture и многомодульности

## Навигация

- `core:navigation` содержит `FeatureEntry` и `TopLevelDestination`
- `app` получает `Set<FeatureEntry>` через Hilt multibinding
- каждый feature-impl сам регистрирует свои destinations
- переходы между фичами выполняются через контракты из `feature/*/api`

## Hilt

- `RoomingApplication` помечен `@HiltAndroidApp`
- `MainActivity` помечена `@AndroidEntryPoint`
- репозитории связываются в [RepositoryModule](/Users/pog0sian/AndroidStudioProjects/Rooming/app/src/main/java/com/example/rooming/di/RepositoryModule.kt)
- внешние SDK-конфиги и `AnalyticsService` связываются в [ExternalServicesModule](/Users/pog0sian/AndroidStudioProjects/Rooming/app/src/main/java/com/example/rooming/di/ExternalServicesModule.kt)
- feature-entry подключаются в собственных Hilt-модулях фич

## Konsist и архитектурные правила

Модуль [quality/architecture-test](/Users/pog0sian/AndroidStudioProjects/Rooming/quality/architecture-test/build.gradle.kts) проверяет:

- `domain` не импортирует Android framework
- `data` не зависит от UI
- feature-impl не импортируют другие feature-impl напрямую
- `UseCase`-классы лежат в `domain/usecase`
- интерфейсы репозиториев лежат в `domain/repository`, реализации в `data`

## Сборка

```bash
./gradlew :app:assembleDebug
./gradlew :feature:auth:impl:testDebugUnitTest
./gradlew :quality:architecture-test:test
```

## Как подготовить ветки

Если репозиторий ещё не инициализирован:

```bash
git init
git checkout -b combined
git checkout -b layer-based
git checkout -b feature-based
git checkout combined
```

Дальше в каждой ветке можно разнести модули по соответствующей архитектуре, а `quality/architecture-test` использовать как регрессионный набор правил.
