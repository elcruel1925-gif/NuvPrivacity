# NuvPrivacity — AAB firmado con GitHub Actions

Este proyecto genera `app-release.aab` firmado con una clave de subida (upload key).

## Secrets obligatorios en GitHub

Crea estos Repository secrets:

- `NUV_KEYSTORE_B64` — contenido Base64 del archivo `.jks`
- `NUV_KEYSTORE_PASSWORD` — contraseña del keystore
- `NUV_KEY_ALIAS` — alias de la clave
- `NUV_KEY_PASSWORD` — contraseña de la clave

El workflow usa esos secretos únicamente durante la compilación y no los escribe en el repositorio.

## Datos de la clave generada para este paquete

Alias:
`nuvprivacity-upload`

Guarda la contraseña entregada junto con el archivo `.jks` en un lugar seguro. No publiques la clave privada ni los secretos en GitHub.

## Flujo

1. Sube el proyecto a GitHub.
2. Configura los cuatro Repository secrets.
3. Ejecuta Actions → Build NuvPrivacity AAB → Run workflow.
4. Descarga el artefacto `NuvPrivacity-release-AAB`.
5. Sube `app-release.aab` a Google Play Console.

Application ID:
`com.nuvprivacity.app`

Version code:
`2`

Version name:
`1.0.1`
