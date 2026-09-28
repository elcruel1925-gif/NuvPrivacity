# NuvPrivacity Android

Proyecto Android nativo que empaqueta la interfaz web local de NuvPrivacity dentro de WebView y mantiene la conexión con Supabase.

## Abrir
1. Abrir esta carpeta en Android Studio.
2. Esperar a que Gradle sincronice.
3. Ejecutar `app` en un teléfono Android.
4. Para distribución, usar Build > Generate Signed Bundle / APK y generar un Android App Bundle (AAB).

## Application ID
`com.nuvprivacity.app`

## Versión
1.0.1 (versionCode 2)

## Permisos
Internet, cámara, micrófono y notificaciones.

## Nota
Esta conversión es una base Android funcional con WebView. Antes de producción deben validarse en dispositivos reales las llamadas, permisos multimedia, compras de Google Play y E2EE.


## Premium / Google Play Billing
La app incluye una integración nativa de Google Play Billing 9.1.0 preparada para la suscripción `nuvprivacity_premium` con planes básicos `monthly` y `annual`. Los productos y precios deben crearse en Play Console antes de que las compras reales estén disponibles.
