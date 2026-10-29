#!/usr/bin/env node
'use strict';

const fs = require('node:fs');
const path = require('node:path');
const { chromium } = require('@playwright/test');

const AYUDA = `
Genera un PDF de una vista HTML local con Playwright.

Uso:
  node generar-reporte.js --url http://localhost:8080/Curso.html
  node generar-reporte.js http://localhost:8080/Curso.html
  npm run reporte -- --url http://localhost:8080/Curso.html

Opciones:
  -u, --url <url>       URL local a capturar (obligatoria).
  -o, --output <ruta>   Archivo PDF de salida. Por defecto: reportes/<vista>-<fecha>.pdf
      --a4              Usa una hoja A4 paginada en lugar del tamaño completo de la pantalla.
      --landscape       Orientación horizontal (sólo con --a4).
      --storage <ruta>  Archivo storageState de Playwright para capturar vistas con sesión.
  -h, --help            Muestra esta ayuda.
`.trim();

function parsearArgumentos(argv) {
    const opciones = {
        url: '',
        salida: '',
        storage: '',
        a4: false,
        landscape: false,
        ayuda: false
    };

    for (let i = 0; i < argv.length; i += 1) {
        const argumento = argv[i];

        switch (argumento) {
            case '--url':
            case '-u':
                opciones.url = argv[++i] || '';
                break;
            case '--output':
            case '-o':
                opciones.salida = argv[++i] || '';
                break;
            case '--storage':
                opciones.storage = argv[++i] || '';
                break;
            case '--a4':
                opciones.a4 = true;
                break;
            case '--landscape':
                opciones.landscape = true;
                break;
            case '--help':
            case '-h':
                opciones.ayuda = true;
                break;
            default:
                if (!argumento.startsWith('-') && !opciones.url) {
                    opciones.url = argumento;
                } else {
                    throw new Error(`Opción no reconocida: ${argumento}`);
                }
        }
    }

    return opciones;
}

function validarUrl(valor) {
    let url;

    try {
        url = new URL(valor);
    } catch (_) {
        throw new Error('La URL no es válida. Ejemplo: http://localhost:8080/Curso.html');
    }

    if (!['http:', 'https:'].includes(url.protocol)) {
        throw new Error('La URL debe usar http o https.');
    }

    const hostsLocales = ['localhost', '127.0.0.1', '::1', '[::1]'];
    if (!hostsLocales.includes(url.hostname)) {
        console.warn('Aviso: la URL no apunta a localhost. Se generará igualmente.');
    }

    return url;
}

function convertirASlug(texto) {
    return texto
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/[^a-zA-Z0-9]+/g, '-')
        .replace(/^-+|-+$/g, '')
        .toLowerCase() || 'pagina';
}

function resolverDestino(url, salida) {
    if (salida) {
        return path.resolve(process.cwd(), salida);
    }

    const ruta = url.pathname && url.pathname !== '/' ? url.pathname : '/inicio';
    const nombre = convertirASlug(ruta.replace(/\.html?$/i, ''));
    const fecha = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);

    return path.join(__dirname, 'reportes', `${nombre}-${fecha}.pdf`);
}

async function generarReporte(opciones) {
    const url = validarUrl(opciones.url);
    const destino = resolverDestino(url, opciones.salida);

    fs.mkdirSync(path.dirname(destino), { recursive: true });

    const navegador = await chromium.launch({ headless: true, channel: 'chromium' });

    try {
        const contexto = await navegador.newContext({
            viewport: { width: 1440, height: 900 },
            deviceScaleFactor: 1,
            storageState: opciones.storage
                ? path.resolve(process.cwd(), opciones.storage)
                : undefined
        });

        const pagina = await contexto.newPage();
        const respuesta = await pagina.goto(url.href, {
            waitUntil: 'networkidle',
            timeout: 60_000
        });

        if (!respuesta || !respuesta.ok()) {
            const detalle = respuesta ? `HTTP ${respuesta.status()}` : 'sin respuesta';
            throw new Error(`La página no cargó correctamente (${detalle}).`);
        }

        // Evita que el PDF se genere con una fuente de reemplazo.
        await pagina.evaluate(() => document.fonts.ready);

        // Conserva la apariencia de pantalla, no la hoja de estilos de impresión.
        await pagina.emulateMedia({ media: 'screen' });

        if (opciones.a4) {
            await pagina.pdf({
                path: destino,
                format: 'A4',
                landscape: opciones.landscape,
                printBackground: true,
                margin: {
                    top: '12mm',
                    right: '12mm',
                    bottom: '12mm',
                    left: '12mm'
                }
            });
        } else {
            const dimensiones = await pagina.evaluate(() => ({
                width: Math.max(document.documentElement.scrollWidth, window.innerWidth),
                height: Math.max(document.documentElement.scrollHeight, window.innerHeight)
            }));

            await pagina.pdf({
                path: destino,
                width: `${dimensiones.width}px`,
                height: `${dimensiones.height}px`,
                printBackground: true,
                margin: { top: '0', right: '0', bottom: '0', left: '0' },
                pageRanges: '1'
            });
        }

        console.log(`PDF generado: ${destino}`);
    } finally {
        await navegador.close();
    }
}

async function main() {
    const opciones = parsearArgumentos(process.argv.slice(2));

    if (opciones.ayuda || !opciones.url) {
        console.log(AYUDA);
        process.exitCode = opciones.ayuda ? 0 : 1;
        return;
    }

    await generarReporte(opciones);
}

main().catch((error) => {
    console.error(`No se pudo generar el PDF: ${error.message}`);
    process.exitCode = 1;
});
