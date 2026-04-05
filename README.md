# ChatAssistant

Android-приложение AI-ассистента с авторизацией через Firebase Authentication, текстовым чатом на GigaChat, генерацией изображений, локальным хранением истории и профилем пользователя.

Проект построен на `Compose + MVVM + Hilt + Room + Paging + DataStore + Firebase + Retrofit/OkHttp/Moshi` с упором на современный Android-стек и аккуратную пользовательскую часть.

## Что реализовано

### Авторизация
- вход по `email / password`
- регистрация нового пользователя
- вход через Google
- валидация полей до отправки
- обработка ошибок и loader states
- автологин через проверку текущего пользователя
- системный `SplashScreen API`
- анимация появления форм логина и регистрации
- переход фокуса между полями клавиатурой

### Список чатов
- локальное хранение чатов в `Room`
- `Paging 3` для списка чатов
- поиск по названию чатов
- создание нового чата
- drawer-меню с поиском, новым чатом, переходом в изображения, главную и профиль

### Чат с GigaChat
- история сообщений хранится локально
- отправка сообщения пользователем
- запрос реального ответа в `GigaChat`
- состояния `GENERATING / SENT / ERROR`
- retry для assistant message
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
- обработка ошибок через snackbar
- loader states на долгих операциях

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

Проект остаётся одномодульным, но код разделён по feature-подходу.

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

Основные принципы:
- один источник истины для UI через `StateFlow`
- локальные данные через `Room`
- сетевой слой изолирован в `core.data.remote`
- корневая навигация разделена на `auth flow` и `main flow`
- минимальное число сущностей и абстракций, только там, где они реально помогают

## Локальное хранение

Используется `Room`:
- `ChatEntity`
- `MessageEntity`

Локально хранятся:
- список чатов
- история сообщений
- статусы сообщений ассистента
- token usage

Это даёт:
- историю между открытиями экрана
- быстрый список чатов
- локальный retry/share flow
- базу для профиля и количества токенов

## Навигация

Используются два уровня навигации:

1. `AppNavHost`
- `Login`
- `Register`
- `Main`
- `Chat/{chatId}`

2. `MainScreen`
- `Chats`
- `Images`
- `Profile`

## GigaChat

### Текстовый чат
Текстовые ответы получаются через:
- `POST /chat/completions`

### Изображения
Генерация изображения тоже идёт через:
- `POST /chat/completions`

Из ответа извлекается тег вида:

```text
<img src="file_id" fuse="true"/>
```

После этого изображение скачивается через:
- `GET /files/{file_id}/content`

### Токен
OAuth-токен получается через:
- `POST /oauth`

Токен кэшируется в памяти и переиспользуется до истечения срока действия.

## Network Security

Для доступа к GigaChat на Android добавлен `network_security_config`, потому что на ряде устройств/эмуляторов без этого TLS-цепочка GigaChat может не проходить проверку.

Используется:
- `russian_trusted_root_ca.cer`
- `res/xml/network_security_config.xml`

`AndroidManifest.xml` уже содержит:

```xml
<application
    android:name="io.valneva.chatassistant.app.ChatAssistantApp"
    android:networkSecurityConfig="@xml/network_security_config"
    android:theme="@style/Theme.ChatAssistant" />
```

## Что нужно для запуска

### 1. Firebase
Нужен собственный Firebase-проект с включёнными:
- Firebase Authentication
- Email/Password provider
- Google provider
- Firebase Storage

В репозитории есть шаблон:
- `app/google-services.json.example`

Перед запуском его нужно заменить на реальный файл:
- `app/google-services.json`

Реальный `google-services.json` скачивается из Firebase Console для вашего проекта.

### 2. GigaChat
В `local.properties` нужно добавить:

```properties
GIGACHAT_AUTH_KEY=your_key_here
```

Если ключа нет, сборка завершится ошибкой на этапе Gradle-конфигурации.

## Сборка и запуск

### Debug APK
```bash
./gradlew.bat :app:assembleDebug
```

### Unit tests
```bash
./gradlew.bat :app:testDebugUnitTest
```

### Полная сборка release
```bash
./gradlew.bat :app:assembleRelease
```

## Тесты

Сейчас в проекте есть unit tests для ключевых бизнес-сценариев:
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

### Локальные данные после logout
Локальные данные чатов user-scoped по `uid`, поэтому они не смешиваются между разными пользователями, даже если logout/login происходят на одном устройстве.

### Количество токенов
В профиле используется суммарный локально сохранённый `tokenUsage` по сообщениям.

## Что сделано из optional части

- анимация появления auth-форм
- вход через Google
- splash screen
- переход фокуса между полями клавиатурой
- генерация изображений
