const { defineConfig, devices } = require('@playwright/test');

module.exports = defineConfig({
    testDir: './tests',
    outputDir: './test-results',
    fullyParallel: false,
    forbidOnly: Boolean(process.env.CI),
    retries: process.env.CI ? 2 : 0,
    // Un worker evita cierres inestables de Chromium en este entorno de Windows.
    workers: 1,
    timeout: 30_000,
    expect: {
        timeout: 5_000
    },
    reporter: [
        ['list'],
        ['html', { outputFolder: 'playwright-report', open: 'never' }]
    ],
    use: {
        baseURL: process.env.BASE_URL || 'http://localhost:8080',
        headless: true,
        trace: 'retain-on-failure',
        screenshot: 'only-on-failure',
        ignoreHTTPSErrors: true
    },
    projects: [
        {
            name: 'chromium',
            use: {
                ...devices['Desktop Chrome'],
                channel: 'chromium',
                viewport: { width: 1440, height: 900 }
            }
        }
    ]
});
