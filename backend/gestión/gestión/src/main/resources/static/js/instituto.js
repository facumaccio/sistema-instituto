/* ==========================================================================
   Instituto — Utilidades compartidas del frontend
   ========================================================================== */

const API = 'http://localhost:8080/api';

async function apiFetch(url, options) {
    const response = await fetch(url, options);

    if (!response.ok) {
        let mensaje = 'Error ' + response.status;
        try {
            const datos = await response.json();
            mensaje = datos.mensaje || datos.message || datos.error || mensaje;
        } catch (_) {
            // La respuesta de error no era JSON.
        }
        throw new Error(mensaje);
    }

    const tipo = response.headers.get('content-type') || '';
    return tipo.includes('application/json') ? response.json() : response.text();
}

function escapeHtml(texto) {
    if (texto === null || texto === undefined) return '';
    return String(texto).replace(/[&<>"']/g, caracter => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    }[caracter]));
}

function normalizarTexto(texto) {
    return String(texto || '')
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .trim();
}

function formatearFecha(fecha) {
    if (!fecha) return '—';
    const soloFecha = String(fecha).slice(0, 10);
    const partes = soloFecha.split('-');
    if (partes.length !== 3) return String(fecha);
    return partes[2] + '/' + partes[1] + '/' + partes[0];
}

function formatearMoneda(valor) {
    return '$ ' + Number(valor || 0).toLocaleString('es-AR', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function hoyISO() {
    const hoy = new Date();
    return new Date(hoy.getTime() - hoy.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
}

function pluralizar(cantidad, singular, plural) {
    return Number(cantidad) === 1 ? singular : (plural || singular + 's');
}

function debounce(funcion, espera = 300) {
    let temporizador;
    return function (...argumentos) {
        clearTimeout(temporizador);
        temporizador = setTimeout(() => funcion.apply(this, argumentos), espera);
    };
}

function mostrarToast(mensaje, tipo) {
    let contenedor = document.getElementById('toasts');
    if (!contenedor) {
        contenedor = document.createElement('div');
        contenedor.id = 'toasts';
        contenedor.className = 'toasts';
        contenedor.setAttribute('aria-live', 'polite');
        document.body.appendChild(contenedor);
    }

    const toast = document.createElement('div');
    toast.className = 'toast' + (tipo ? ' ' + tipo : '');
    toast.textContent = mensaje;
    contenedor.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('salir');
        setTimeout(() => toast.remove(), 260);
    }, 4400);
}

function sincronizarBloqueoScroll() {
    const hayModal = Array.from(document.querySelectorAll('.modal-overlay'))
        .some(overlay => !overlay.classList.contains('hidden'));
    document.body.classList.toggle('modal-open', hayModal);
}

function abrirModal(id) {
    const modal = document.getElementById(id);
    if (!modal) return;
    modal.classList.remove('hidden');
    sincronizarBloqueoScroll();
    const enfocable = modal.querySelector('input, select, textarea, button');
    if (enfocable) setTimeout(() => enfocable.focus(), 20);
}

function cerrarModal(id) {
    const modal = document.getElementById(id);
    if (!modal) return;
    modal.classList.add('hidden');
    sincronizarBloqueoScroll();
}

function cerrarModalPorOverlay(event, id) {
    if (event.target === event.currentTarget) {
        cerrarModal(id);
    }
}

function setBotonCargando(boton, cargando, textoCargando = 'Guardando...') {
    if (!boton) return;

    if (cargando) {
        boton.dataset.textoOriginal = boton.textContent;
        boton.textContent = textoCargando;
        boton.disabled = true;
    } else {
        boton.textContent = boton.dataset.textoOriginal || boton.textContent;
        boton.disabled = false;
    }
}

function llenarSelect(select, items, opciones = {}) {
    if (!select) return;

    const {
        placeholder = 'Seleccione una opción...',
        valor = item => item.id,
        texto = item => item.nombre
    } = opciones;

    const anterior = select.value;
    select.innerHTML = '<option value="">' + escapeHtml(placeholder) + '</option>' +
        (items || []).map(item =>
            '<option value="' + escapeHtml(valor(item)) + '">' + escapeHtml(texto(item)) + '</option>'
        ).join('');

    if (anterior && Array.from(select.options).some(opcion => opcion.value === String(anterior))) {
        select.value = anterior;
    }
}

/**
 * Confirmación formal con modal. Devuelve una promesa con true/false.
 * No usa window.confirm para respetar la estética del sistema.
 */
function confirmarAccion({
    titulo = 'Confirmar acción',
    mensaje = '¿Querés continuar?',
    textoConfirmar = 'Confirmar',
    textoCancelar = 'Cancelar',
    peligro = true
} = {}) {
    return new Promise(resolve => {
        const overlay = document.createElement('div');
        overlay.className = 'modal-overlay';

        const claseBoton = peligro ? 'btn btn-danger' : 'btn btn-primary';
        overlay.innerHTML =
            '<div class="modal modal-sm" role="dialog" aria-modal="true" aria-labelledby="confirmar-titulo">' +
                '<div class="modal-head">' +
                    '<h3 id="confirmar-titulo">' + escapeHtml(titulo) + '</h3>' +
                    '<button type="button" class="icon-btn" aria-label="Cerrar" data-cancelar>&times;</button>' +
                '</div>' +
                '<div class="modal-body">' +
                    '<p class="modal-message">' + escapeHtml(mensaje) + '</p>' +
                '</div>' +
                '<div class="modal-actions">' +
                    '<button type="button" class="btn btn-ghost" data-cancelar>' + escapeHtml(textoCancelar) + '</button>' +
                    '<button type="button" class="' + claseBoton + '" data-confirmar>' + escapeHtml(textoConfirmar) + '</button>' +
                '</div>' +
            '</div>';

        const finalizar = valor => {
            document.removeEventListener('keydown', alPresionarTecla);
            overlay.remove();
            sincronizarBloqueoScroll();
            resolve(valor);
        };

        const alPresionarTecla = event => {
            if (event.key === 'Escape') finalizar(false);
        };

        overlay.querySelectorAll('[data-cancelar]').forEach(boton => {
            boton.addEventListener('click', () => finalizar(false));
        });

        overlay.querySelector('[data-confirmar]').addEventListener('click', () => finalizar(true));
        overlay.addEventListener('click', event => {
            if (event.target === overlay) finalizar(false);
        });
        document.addEventListener('keydown', alPresionarTecla);

        document.body.appendChild(overlay);
        sincronizarBloqueoScroll();
        overlay.querySelector('[data-cancelar]').focus();
    });
}
