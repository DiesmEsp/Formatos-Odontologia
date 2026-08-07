import { test, expect } from '@playwright/test';

test.describe('Navegacion principal', () => {
  test('la pagina carga y muestra el sidebar', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('.sidebar')).toBeVisible();
    await expect(page.locator('.sidebar-link').first()).toBeVisible();
  });

  test('navega a Catalogos y muestra tabs', async ({ page }) => {
    await page.goto('/');
    await page.click('a[href="/catalogos"]');
    await expect(page.locator('.view-title')).toContainText('Catalogos');
    await expect(page.locator('.tab')).toHaveCount(6);
  });

  test('navega a Unidades y muestra la tabla', async ({ page }) => {
    await page.goto('/');
    await page.click('a[href="/unidades"]');
    await expect(page.locator('.view-title')).toContainText('Unidades');
  });

  test('navega a Reportes y muestra el picker', async ({ page }) => {
    await page.goto('/');
    await page.click('a[href="/reportes"]');
    await expect(page.locator('.view-title')).toContainText('Reportes');
    await expect(page.locator('.month-year-picker')).toBeVisible();
  });

  test('navega a Asistencia y muestra la tabla de docentes', async ({ page }) => {
    await page.goto('/');
    await page.click('a[href="/asistencia"]');
    await expect(page.locator('.view-title')).toContainText('Asistencia');
  });

  test('navega a Tratamientos y muestra el grid', async ({ page }) => {
    await page.goto('/');
    await page.click('a[href="/tratamientos"]');
    await expect(page.locator('.view-title')).toContainText('Tratamientos');
  });

  test('el Dashboard carga KPIs', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('.view-title')).toContainText('Dashboard');
    await expect(page.locator('.kpi-card')).toHaveCount(4);
  });
});
