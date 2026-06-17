# IBI Products App

Android take-home assignment demonstrating modern Android development practices, clean architecture, and Kotlin proficiency.

---

## Setup Instructions

1. Clone the repository and open in **Android Studio Hedgehog** or later
2. Sync Gradle — all dependencies resolve via `gradle/libs.versions.toml`
3. Run on any device or emulator with **API 26+**
4. No API keys required

### Testing Credentials

| Field    | Value          |
|----------|----------------|
| Username | `emilys`       |
| Password | `emilyspass`   |

---

## Architecture

The app follows **Clean Architecture** with three strictly separated layers. Dependencies only point inward — the domain layer has zero knowledge of Android or any framework.

```
Presentation  →  Domain  ←  Data
```

### Domain Layer

Pure Kotlin — no Android imports. Contains:

- **Domain models:** `Product`, `AuthToken`, `LoginCredentials`, `SortOption`
- **Repository interfaces:** `ProductRepository`, `AuthRepository`
- **Use cases:** `LoginUseCase`, `GetProductsUseCase`, `GetProductDetailUseCase`, `GetCategoriesUseCase`, `AddProductUseCase`, `EditProductUseCase`, `DeleteProductUseCase`, `ResetProductUseCase`, `GetFavoritesUseCase`, `AddFavoriteUseCase`, `RemoveFavoriteUseCase`, `RefreshProductsUseCase`

### Data Layer

- **Remote:** Retrofit `ApiService` hitting `https://dummyjson.com` with sort, search, and category endpoints
- **Local:** Room database with `ProductEntity` (products table) and `FavoriteEntity` (favorites table kept separate to avoid `PagingSource` invalidation on favorite toggles)
- **Paging:** `ProductPagingSource` — fetches from network page by page as the user scrolls, caches each page to Room, falls back to Room cache when offline
- **Mappers:** Extension functions converting DTOs → Entities → Domain models
- **Repository implementations:** `AuthRepositoryImpl`, `ProductRepositoryImpl`

### Presentation Layer

- MVVM with `@HiltViewModel`
- UI state held in `MutableStateFlow(DataClassState())` — data classes, not sealed classes
- `collectAsStateWithLifecycle()` throughout — pauses collection when app is backgrounded
- `viewModelScope.launch` + `withContext(ioDispatcher)` for all async work

### Utils

- **`DataStoreHelper`** — DataStore-backed: auth token, login session, dark mode, language, biometric flag
- **`AppBiometricManager`** — abstracts `BiometricPrompt` behind a clean callback so ViewModels never reference `FragmentActivity` directly

---

## Features

| Feature | Details |
|---------|---------|
| Auth | Username/password login via DummyJSON `/auth/login` |
| Biometric | `BiometricPrompt` via `AppBiometricManager`; toggle in Settings; session persisted across app restarts |
| Persistent session | Token and login state stored in DataStore; restored on app open |
| Product list | Paginated via Paging 3 + `ProductPagingSource`; cached in Room for offline access |
| Search | Debounced search via API |
| Filter | Category chips populated from Room |
| Sort | Price asc/desc, rating, name — delegated to API via `sortBy` and `order` query params |
| Product detail | Full detail with image, rating, stock, tags, favorite toggle |
| Favorites | Separate Room table; `favoriteIds` observed as `Set<Int>` in ViewModel to avoid paging invalidation |
| Undo remove | Snackbar with 4s delay before actual Room deletion; multiple pending removals tracked as a `Set` |
| CRUD | Add/edit/delete products locally; reset single product from API; locally modified products preserved on network refresh |
| Dark mode | DataStore-backed; theme recomposes reactively in `MainActivity` |
| Language | English / Hebrew via `AppCompatDelegate.setApplicationLocales()`; RTL handled automatically |
| Logout | Clears DataStore session, navigates to Login clearing back stack |
| Offline | `ProductPagingSource` falls back to Room cache on network failure |
| Error handling | Every layer returns `NetworkResult<T>`; UI shows retry on failure |

---

## Screenshots

<p align="center">
  <img src="screenshots/3 Products screen.png" width="200"/>
  <img src="screenshots/5 Favorites screen.png" width="200"/>
  <img src="screenshots/8 Add Product.png" width="200"/>
  <img src="screenshots/13 Settings screen in Hebrew.png" width="200"/>
</p>

> More screenshots available in the [`screenshots/`](screenshots/) folder.

---

## Tech Stack

| Category | Library |
|----------|---------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| Async | Coroutines + Flow |
| DI | Hilt |
| Networking | Retrofit + OkHttp + Gson |
| Local Storage | Room + DataStore Preferences |
| Pagination | Paging 3 |
| Images | Coil |
| Animation | Lottie |
| Biometric | AndroidX Biometric |
| Navigation | Jetpack Navigation Compose |

---

## Project Structure

```
app/src/main/java/com/task/ibiproducts/
├── common/
│   ├── Constants.kt
│   ├── NetworkResult.kt
│   ├── ApiErrorResponse.kt
│   └── ParseErrorMessage.kt
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   │   ├── ProductDao.kt
│   │   │   └── FavoriteDao.kt
│   │   └── entity/
│   │       ├── ProductEntity.kt
│   │       └── FavoriteEntity.kt
│   ├── mapper/
│   │   └── Mappers.kt
│   ├── paging/
│   │   └── ProductPagingSource.kt
│   ├── remote/
│   │   ├── ApiService.kt
│   │   ├── AuthInterceptor.kt
│   │   └── dto/
│   └── repository/
│       ├── AuthRepositoryImpl.kt
│       └── ProductRepositoryImpl.kt
├── di/
│   ├── DatabaseModule.kt
│   ├── DispatcherModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
├── domain/
│   ├── model/
│   │   ├── Product.kt
│   │   ├── AuthToken.kt
│   │   └── SortOption.kt
│   ├── repository/
│   │   ├── ProductRepository.kt
│   │   └── AuthRepository.kt
│   └── usecase/
│       ├── LoginUseCase.kt
│       ├── GetProductsUseCase.kt
│       ├── GetProductDetailUseCase.kt
│       ├── GetCategoriesUseCase.kt
│       ├── AddProductUseCase.kt
│       ├── EditProductUseCase.kt
│       ├── DeleteProductUseCase.kt
│       ├── ResetProductUseCase.kt
│       ├── GetFavoritesUseCase.kt
│       ├── AddFavoriteUseCase.kt
│       ├── RemoveFavoriteUseCase.kt
│       └── RefreshProductsUseCase.kt
├── presentation/
│   ├── auth/
│   │   ├── LoginViewModel.kt
│   │   └── LoginScreen.kt
│   ├── product/
│   │   ├── ProductViewModel.kt
│   │   └── ProductScreen.kt
│   ├── detail/
│   │   ├── ProductDetailViewModel.kt
│   │   └── ProductDetailScreen.kt
│   ├── favorites/
│   │   ├── FavoritesViewModel.kt
│   │   └── FavoritesScreen.kt
│   ├── settings/
│   │   ├── SettingsViewModel.kt
│   │   └── SettingsScreen.kt
│   └── crud/
│       ├── AddEditProductViewModel.kt
│       └── AddEditProductScreen.kt
├── utils/
│   ├── AppBiometricManager.kt
│   ├── DispatcherQualifier.kt
│   └── DataStoreHelper.kt
├── ui/theme/
├── AppNavGraph.kt
├── IbiProductsApp.kt
└── MainActivity.kt
```

---

## Known Limitations

- **Sort consistency across pages:** Sorting is delegated to the DummyJSON API which sorts within each page. For globally consistent sorting across all pages a `RemoteMediator` implementation would be needed. This was evaluated and deferred given time constraints.
- **Favorite scroll position:** Toggling a favorite previously triggered a `PagingSource` refresh causing a scroll jump. Resolved by keeping favorites in a separate table so the product paging source is never invalidated on favorite changes.

---

## AI Usage Report

### Tools Used

**Claude (Anthropic)** was used as the primary AI assistant throughout development.

### What AI Assisted With

**Scaffolding and boilerplate**
AI generated the initial file structure, Hilt module wiring, Room entity definitions, Retrofit setup, and boilerplate patterns based on existing architecture conventions from my production projects. This covered predictable setup work while keeping all structural decisions mine.
These were strictly according to the file structure, architecture patterns, coding conventions, code style, and naming conventions I provided in the prompts.
Lastly, I reviewed all AI-generated code line by line before inclusion to ensure correctness, security, and maintainability.

### Meaningful Prompts Used During Development

**Prompt 1 — Architecture scaffolding**
> *"Build the full Clean Architecture scaffold for an Android product app using Hilt, Room, Retrofit, and Paging 3 — following my existing patterns: @Provides in RepositoryModule (not @Binds), data class UI state in ViewModels, MutableStateFlow(DataClassState()).asStateFlow(), viewModelScope.launch + withContext(ioDispatcher), NetworkResult sealed class for all API responses."*

**Prompt 2 — Biometric abstraction**
> *"AppBiometricManager should abstract BiometricPrompt behind a clean callback interface — the ViewModel should never reference FragmentActivity directly. Wrap authenticate() to return a sealed BiometricResult class covering Success, Cancelled, Error, NotAvailable, and NotEnrolled."*

**Prompt 3 — Favorites architecture decision**
> *"The favorites table should be kept separate from the products table to avoid PagingSource invalidation when a user toggles a favorite. The product list ViewModel should observe favoriteIds as a Flow<Set<Int>> and pass it into the Compose UI separately from PagingData, so the grid never refreshes on a favorite toggle."*

### What Was Implemented or Reviewed Manually

- All architectural decisions and layer boundaries
- Decision to keep `FavoriteEntity` as a separate table after evaluating the `PagingSource` invalidation problem hands-on
- `ProductPagingSource` logic — network-first, Room cache fallback, offline detection
- `DataStoreHelper` DataStore key design and synchronous `getAuthToken()` via `runBlocking` for `AuthInterceptor`
- Biometric session persistence fix — re-saving login session after biometric success so the app remembers on next open
- `getProductById` logic — serving locally modified products from Room without overwriting with network data
- Preserving locally modified products during network page fetches in `ProductPagingSource`
- Navigation graph structure and back stack management on login/logout
- All Compose UI layout, UX decisions, and string resources including Hebrew translations
- `DisposableEffect` + `LifecycleEventObserver` approach for reloading product detail on resume
- Debugging and resolving all runtime issues including biometric `FragmentActivity` cast crash, Room paging blank screen, and favorites cycling bug

### How Correctness and Code Quality Were Verified

- All AI-generated code was read line by line before inclusion
- Architecture boundaries enforced manually — domain layer verified to have no Android imports
- Tested on a physical device throughout development
- Verified auth, pagination, offline fallback, biometric, favorites undo, CRUD, dark mode, and language switching end to end
- Verified `collectAsStateWithLifecycle` used consistently over `collectAsState` throughout