import { test, expect } from '@playwright/test';

test.describe('Flujos criticos', () => {
  test('Reportes ofrece los 6 generadores de Excel', async ({ page }) => {
    await page.goto('/');
    await page.click('a[href="/reportes"]');
    await expect(page.locator('.report-card')).toHaveCount(6);
  });

  test('Tratamientos muestra el grid de estaciones', async ({ page }) => {
    await page.goto('/');
    await page.click('a[href="/tratamientos"]');
    await expect(page.locator('.station-grid')).toBeVisible();
  });

  test('Asistencia permite registrar docentes', async ({ page }) => {
    await page.goto('/');
    await page.click('a[href="/asistencia"]');
    await expect(page.getByText('Docentes registrados')).toBeVisible();
  });

  test('Catalogos expone las 6 pestañas', async ({ page }) => {
    await page.goto('/');
    await page.click('a[href="/catalogos"]');
    await expect(page.locator('.tab')).toHaveCount(6);
  });

  test('Crear tratamiento ya no ofrece el tipo Avance', async ({ page }) => {
    await page.goto('/');
    await page.click('a[href="/tratamientos"]');
    await page.getByRole('button', { name: 'Nuevo tratamiento (manual)' }).click();
    await expect(page.getByRole('button', { name: 'Avance' })).toHaveCount(0);
    await expect(page.getByRole('button', { name: 'Común' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Continuo' })).toBeVisible();
  });
});
