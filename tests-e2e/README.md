# Pruebas E2E y reportes PDF

Entorno Node.js separado del backend Java para automatizar el frontend con Playwright.

## Requisitos

- Node.js 20 o superior.
- Backend del instituto ejecutándose en `http://localhost:8080`.

## Instalación

```powershell
cd tests-e2e
npm install
npx playwright install chromium
```

## Ejecutar las pruebas

```powershell
npm test
```

Para ver el navegador durante la prueba:

```powershell
npm run test:headed
```

Para abrir el modo interactivo:

```powershell
npm run test:ui
```

El reporte HTML queda en `playwright-report/` y los artefactos de fallos en `test-results/`.

La URL base se puede cambiar con la variable `BASE_URL`:

```powershell
$env:BASE_URL = "http://localhost:8081"
npm test
```

## Generar un PDF

```powershell
npm run reporte -- --url http://localhost:8080
```

También se puede pasar la URL como argumento posicional:

```powershell
node generar-reporte.js http://localhost:8080/Curso.html
```

El PDF se guarda por defecto en `reportes/`. Para elegir el archivo de salida:

```powershell
npm run reporte -- --url http://localhost:8080/Curso.html --output reportes/cursos.pdf
```

Opciones disponibles:

- `--url`: URL local que se va a capturar.
- `--output`: ruta del PDF de salida.
- `--a4`: genera un PDF A4 paginado en lugar de una página del tamaño completo de la pantalla.
- `--landscape`: orientación horizontal, sólo junto con `--a4`.
- `--storage`: archivo `storageState` de Playwright para capturar vistas que requieren sesión.

### Capturar una vista protegida

Las pantallas internas redirigen al login si no hay sesión. Para generar un PDF de una vista interna, primero guardá el estado de sesión:

```powershell
npx playwright codegen --save-storage=auth.json http://localhost:8080
```

Iniciá sesión en la ventana que se abre, cerrala y después generá el PDF:

```powershell
npm run reporte -- --url http://localhost:8080/Curso.html --storage auth.json
```

> `auth.json` contiene datos de sesión: no lo subas al repositorio.
