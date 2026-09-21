# Development

Use JDK 17+, Android SDK API 35, and the Gradle wrapper:

```text
gradlew.bat test
gradlew.bat :shared:features:auth:jvmTest
gradlew.bat :apps:desktop-veterinary:run
```

Configure Supabase URL and public anon key only through deployment
configuration. Service-role keys are Edge Function secrets and never belong in
Kotlin source, SQL seed data, or client configuration.
