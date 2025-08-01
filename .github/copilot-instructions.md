# Fitness Project

This project contains both API project and Android client for a fitness application.

---

## API Project
The API is built using TypeScript with Drizzle ORM, and Express.js. It provides endpoints for managing users, workout plans, nutrition plans, and more.

---

## Android Project

This project follows the MVVM architecture using Java, XML layouts, Retrofit and Moshi for networking, Dagger Hilt for dependency injection, and ViewBinding for UI components.

---

### Project Structure

- `data/remote/retrofit`

    - Contains Retrofit interfaces for API endpoints.

- `data/repository/`

    - Houses repository classes that interact with Retrofit services.

- `di/`

    - Includes Dagger Hilt modules for providing dependencies like Retrofit instances.

- `model/`

    - Defines data models corresponding to API responses.

- `ui/`

    - `activity/`

        - Activities utilizing ViewBinding and observing ViewModels.

    - `fragment/`

        - Fragments with ViewBinding and ViewModel integration.

    - `adapter/`

        - Adapters for RecyclerViews or other UI components.

    - `viewmodel/`
        - ViewModels that interact with repositories and expose LiveData or StateFlow.

---

### Development Guidelines

#### 1. Networking with Retrofit

- Define API endpoints in interfaces within `data/remote/retrofit`.
- Use Retrofit annotations to specify HTTP methods and endpoints.

#### 2. Dependency Injection with Dagger Hilt

- Create modules in `di/` to provide Retrofit instances and other dependencies.
- Annotate modules with `@Module` and `@InstallIn(SingletonComponent.class)`.
- Provide dependencies using `@Provides` and annotate them with `@Singleton` if needed.

#### 3. Repositories

- Implement repositories in `data/repository/` that utilize Retrofit services.
- Repositories should handle data operations and expose results to ViewModels.

#### 4. ViewModels

- Place ViewModels in `ui/viewmodel/`.
- Inject repositories using Hilt's `@Inject` annotation.
- Expose data to the UI via LiveData.

#### 5. UI Components

- Use ViewBinding in activities and fragments.

#### 6. Lombok

- Use lombok's annotations for getters, setters, constructors and builders.

---

### Restrictions

- Do **not** use Retrofit services directly in activities, fragments, or ViewModels.
- All network operations must go through repositories.

## Agent Mode Environment

- Environment: Windows 11 using msys2 and zsh shell.
- Always use valid msys2 and zsh commands when trying to run commands.
- Do not use cmd or PowerShell commands.