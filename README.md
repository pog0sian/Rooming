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
- Firebase Remote Config, Cloud Firestore, FCM и Crashlytics
- фоновые задачи через WorkManager

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
- Firebase Cloud Messaging / Remote Config / Firestore / Crashlytics
- WorkManager

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
./gradlew :app:assembleDemoDebug
./gradlew :app:assembleProdRelease
./gradlew :feature:auth:impl:testDebugUnitTest
./gradlew :quality:architecture-test:test
```

Debug APK для демонстрации находится в `app/build/outputs/apk/demo/debug/app-demo-debug.apk`.

## Критерии семестровой работы

Без AI-бонуса проект закрывает максимум 18/20: обязательные 15 баллов, Firebase-бонус 2 балла и 1 балл за внешние сервисы/сбор crash-аналитики.

| Критерий | Где реализовано |
| --- | --- |
| Clean Architecture, MVVM, многомодульность | `domain/*`, `data/*`, `feature/*/api`, `feature/*/impl`, `core:navigation`, `app/src/main/java/com/example/rooming/di` |
| Фоновая работа | `app/src/main/java/com/example/rooming/sync/RoomingSyncWorker.kt` периодически синхронизирует FCM-токен и профиль с Firestore; запуск в `RoomingApplication.scheduleBackgroundSync()` |
| Service / BroadcastReceiver / ContentProvider | `app/src/main/java/com/example/rooming/firebase/PushMessagingService.kt` принимает FCM push-уведомления |
| Compose-анимации | `feature/rooms/impl/.../RoomsScreen.kt`: `animateContentSize`, `AnimatedVisibility`, `Crossfade` |
| XML/View и Compose | `feature/about/impl/.../AboutScreen.kt`: `AndroidView` встраивает нативный `MapView` Yandex MapKit в Compose-экран |
| Debug/release и product flavors | `app/build.gradle.kts`: `debug`, `release`, flavors `demo` и `prod`; в `demo` включены лабораторные crash tools, в `prod` они скрыты |
| Firebase-бонус | `FirebaseRemoteConfigService`, `FirebaseUserProfileService`, `PushMessagingService`, `google-services.json` |
| Внешний сервис / crash analytics | Yandex ID, VK ID, Yandex MapKit, AppMetrica, Firebase Crashlytics |
| Качество кода | Hilt DI, Version Catalog, Konsist-правила в `quality/architecture-test`, отдельные интерфейсы для SDK |

Для демонстрации критериев лучше запускать `demoDebug`: на экране «О нас» будет виден вариант сборки `variant=demo`, блок Remote Config, данные профиля из Firestore и кнопка тестового Crashlytics/AppMetrica crash. В `prod` flavor этот тестовый блок скрыт, что показывает реальное отличие product flavors.

## Что приложить в отчет

- главный экран со списком аудиторий и экран деталей после бронирования, где кнопка меняется на «Забронировано»
- экран «О нас» в `demoDebug`: `variant=demo`, Remote Config, профиль из Firestore, карта через Yandex MapKit
- Firebase Console: Remote Config parameter, документ пользователя в Firestore, отправленное FCM-уведомление и Crashlytics issue после тестового crash
- AppMetrica: событие входа/бронирования или crash report
- скрин/лог сборки `./gradlew :app:assembleDemoDebug`
