package com.instituto.gestion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Al iniciar la aplicación, crea los ingresos pendientes de las cuotas que ya
 * estaban pagadas antes de integrar Pagos con Finanzas. Es idempotente: las
 * cuotas ya sincronizadas se omiten.
 */
@Component
@Order(2)
public class CuotaFinanzasSincronizador implements CommandLineRunner {

    @Autowired
    private CuotaService cuotaService;

    @Override
    public void run(String... args) {
        try {
            int sincronizadas = cuotaService.sincronizarCuotasPagadas();
            if (sincronizadas > 0) {
                System.out.println("Cuotas pagadas sincronizadas con Finanzas: " + sincronizadas);
            }
        } catch (Exception e) {
            // La sincronización no debe impedir que la aplicación arranque.
            System.err.println("No se pudieron sincronizar las cuotas pagadas con Finanzas: "
                    + e.getMessage());
        }
    }
}
