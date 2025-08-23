This is a Kotlin Multiplatform project targeting Android, iOS, Web, Desktop.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).

You can open the web application by running the `:composeApp:wasmJsBrowserDevelopmentRun` Gradle task.

## Run iOS from Android Studio (no Xcode app required)

From Android Studio, you can now build and run the iOS app on the Simulator via a Gradle task:

- Gradle task: `:iosRunDebugOnSimulator`
- Default simulator: iPhone 15
- Choose a different simulator: add a project property `-PiosSimulator="<Device Name>"`, for example:
  - `./gradlew iosRunDebugOnSimulator -PiosSimulator="iPhone 15 Pro Max"`

This task will:
- Boot the requested Simulator if needed
- Build the `iosApp` Xcode project in Debug configuration
- Install and launch the app on the booted Simulator

No need to open Xcode. Ensure you have Xcode command line tools installed (`xcode-select --install`).
