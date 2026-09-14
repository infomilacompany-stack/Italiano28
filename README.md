# Italiano 28 v2 🇮🇹

Aplicación Android de aprendizaje práctico de italiano en 28 días.

## Incluye
- 28 días organizados en 4 etapas.
- XP, nivel, racha diaria y corazones.
- Ejercicios de comprensión, vocabulario, gramática práctica y recuperación activa.
- Texto a voz en italiano.
- Reconocimiento de voz en italiano para práctica oral.
- Mini conversación diaria.
- Evaluación por habilidades.
- Detección de errores y repaso inteligente.
- Logros y evaluación final del día 28.
- Persistencia local del progreso.

## Crear APK sin Android Studio
El repositorio incluye `.github/workflows/build-apk.yml`. En GitHub:
1. Sube todo el contenido del proyecto a un repositorio.
2. Ve a Actions.
3. Ejecuta `Build Italiano 28 APK` con `Run workflow`, o haz push a `main`.
4. Abre la ejecución terminada.
5. En Artifacts descarga `Italiano28-v2-apk`.
6. Descomprime el ZIP del artifact y abre `app-debug.apk` en un Android.

## Desde una terminal con Gradle
`gradle assembleDebug`

El APK aparecerá en:
`app/build/outputs/apk/debug/app-debug.apk`
