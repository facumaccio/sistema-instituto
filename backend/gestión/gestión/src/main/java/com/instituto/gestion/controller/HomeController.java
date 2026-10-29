package com.instituto.gestion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** La raíz del sitio lleva siempre a la pantalla de ingreso. */
@Controller
public class HomeController {

    @GetMapping("/")
    public String inicio() {
        return "redirect:/login.html?v=20261028-2";
    }
}
