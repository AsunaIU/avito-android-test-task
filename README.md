# ChatAssistant

Тестовое задание, Android-приложение на основе GigaChat для выполнения повседневных задач. Приложение поддерживает авторизацию через Firebase Authentication, текстовый чат, генерацию изображений, локальное хранение истории переписки и отображение профиля пользователя.

Приложение реализовано с использованием:`Compose + MVVM + Hilt + Room + Paging + DataStore + Firebase + Retrofit/OkHttp/Moshi`.

## Основной функционал

### Авторизация
- вход через `email/password` или `Google Sign-In`
- регистрация нового аккаунта
- автологин при повторном запуске
- валидация полей, обработка ошибок, loader states
- Splash Screen API

### Список чатов
- локальное хранение чатов в `Room`
- `Paging 3` для списка чатов
- поиск по названию чатов
- создание нового чата
- `NavigationDrawer` с поиском и переходом между разделами

### Чат с GigaChat
- история сообщений хранится локально между сессиями
- запрос реального ответа в `GigaChat`
- состояния `GENERATING / SENT / ERROR`
- `retry` при ошибке ответа ассистента
- share ответа через системный Android Share Sheet
- long press по сообщению ассистента для share

### Генерация изображений
- отдельный экран `Images`
- генерация изображения через `GigaChat chat/completions`
- скачивание результата через `GET /files/{fileId}/content`
- loader, retry и показ ошибки

### Профиль
- данные пользователя из `FirebaseAuth`
- отображение имени, email, телефона
- количество токенов по локальной истории сообщений
- смена имени
- загрузка фото в `Firebase Storage`
- ручное переключение темы приложения
- logout с возвратом в auth-flow

### Общее
- светлая и тёмная тема с сохранением выбора в `DataStore`
- обработка ошибок через `Snackbar`
- индикатор загрузки на всех долгих операциях

## Стек
- Kotlin
- Jetpack Compose
- MVVM
- Hilt
- Room
- Paging 3
- DataStore Preferences
- Firebase Authentication
- Firebase Storage
- Retrofit
- OkHttp
- Moshi
- Coil

## Архитектура

```text
app/
  ChatAssistantApp.kt
  MainActivity.kt
  di/
  navigation/
  startup/
  theme/

core/
  common/
  data/
    datastore/
    local/
    remote/

designsystem/
  component/
  theme/

feature/
  auth/
  chats/
  chat/
  images/
  profile/
```

## GigaChat

- текст: `POST /chat/completions`
- изображения: тот же endpoint, из ответа извлекается `<img src="file_id" fuse="true"/>`, затем `GET /files/{file_id}/content`
- токен: `POST /oauth`, кэшируется в памяти до истечения

TLS-цепочка GigaChat не проходит без явного доверенного сертификата — добавлен `network_security_config` с `russian_trusted_root_ca.cer`.

## Что нужно для запуска

### 1. Firebase
Нужен Firebase-проект с Authentication (Email/Password + Google) и Storage.

В репозитории есть шаблон:
- `app/google-services.json.example`

Перед запуском его нужно заменить на реальный файл (скачивается из Firebase Console):
- `app/google-services.json`

### 2. GigaChat
В `local.properties` нужно добавить:

```properties
GIGACHAT_AUTH_KEY=your_key_here
```

### Сборка 

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

## Тесты

Сейчас в проекте есть unit тесты для ключевых бизнес-сценариев:
- `AuthFormValidatorTest`
- `ChatListViewModelTest`
- `ConversationViewModelTest`
- `ProfileViewModelTest`
- `ImagesViewModelTest`

Тесты покрывают:
- валидацию auth-полей
- создание нового чата и эффекты навигации
- отправку сообщения и запуск assistant reply flow
- сохранение имени и logout в профиле
- обработку успеха/ошибки генерации изображений

## Примечания

### Генерация изображений
Генерация изображения зависит от ответа GigaChat и может быть медленнее текстового ответа. Для этого в `OkHttp` увеличены таймауты.

### Количество токенов
В профиле используется суммарный локально сохранённый `tokenUsage` по сообщениям.

### Из optional части реализованы:
- анимация появления auth-форм
- вход через Google
- splash screen
- переход фокуса между полями клавиатурой
- генерация изображений
