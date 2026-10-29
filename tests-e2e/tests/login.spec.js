const { test, expect } = require('@playwright/test');

test.describe('Acceso al sistema', () => {
    test('la página principal carga y muestra la marca del instituto', async ({ page }) => {
        const respuesta = await page.goto('/', { waitUntil: 'networkidle' });

        expect(respuesta, 'El servidor no devolvió una respuesta.').not.toBeNull();
        expect(
            respuesta.ok(),
            `La página respondió con HTTP ${respuesta.status()}.`
        ).toBeTruthy();

        await expect(page).toHaveTitle(/Instituto/i);

        const marca = page.locator('.login-brand');
        await expect(marca).toBeVisible();
        await expect(
            marca.getByRole('heading', { name: 'Instituto', exact: true })
        ).toBeVisible();
        await expect(marca).toContainText('Gestión Académica');

        await expect(page.locator('#form-login')).toBeVisible();
        await expect(page.locator('#login-identificador')).toBeVisible();
    });
});
