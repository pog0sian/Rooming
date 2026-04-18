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
│   └── bookings
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
