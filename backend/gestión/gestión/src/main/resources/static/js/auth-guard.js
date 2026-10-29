/* ==========================================================================
   Instituto — Protección de pantallas y sesión del usuario
   Se carga al inicio de cada pantalla interna. Si no hay sesión, redirige
   automáticamente a login.html antes de que se muestre el contenido.
   ========================================================================== */

(function () {
    'use strict';

    const SESION_KEY = 'institutoUsuario';
    const PANTALLA_LOGIN = 'login.html?v=20261028-2';

    function leerSesion() {
        try {
            const guardado = localStorage.getItem(SESION_KEY);
            if (!guardado) return null;

            const datos = JSON.parse(guardado);
            if (!datos || !datos.nombreUsuario) return null;

            return datos;
        } catch (_) {
            return null;
        }
    }

    function limpiarSesion() {
        try {
            localStorage.removeItem(SESION_KEY);
        } catch (_) {
            // El navegador puede bloquear el almacenamiento local.
        }
    }

    function irAlLogin() {
        limpiarSesion();
        window.location.replace(new URL(PANTALLA_LOGIN, window.location.href).href);
    }

    // La pantalla de login debe poder abrirse sin sesión.
    if (/\/login\.html$/i.test(window.location.pathname)) {
        return;
    }

    const sesion = leerSesion();
    if (!sesion) {
        irAlLogin();
        return;
    }

    window.institutoUsuario = sesion;

    window.cerrarSesionInstituto = function () {
        limpiarSesion();
        window.location.replace(new URL(PANTALLA_LOGIN, window.location.href).href);
    };

    // Agrega una salida rápida a la barra de navegación de todas las pantallas.
    document.addEventListener('DOMContentLoaded', function () {
        const nav = document.querySelector('.topbar nav');
        if (!nav || nav.querySelector('.nav-logout')) return;

        const boton = document.createElement('button');
        boton.type = 'button';
        boton.className = 'nav-logout';
        boton.textContent = 'Salir';

        const detalle = (sesion.nombre || sesion.nombreUsuario)
            + (sesion.rolEtiqueta ? ' · ' + sesion.rolEtiqueta : '');
        boton.title = 'Cerrar sesión (' + detalle + ')';
        boton.addEventListener('click', window.cerrarSesionInstituto);

        nav.appendChild(boton);
    });
})();
