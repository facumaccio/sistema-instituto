package com.instituto.gestion.controller;

import com.instituto.gestion.dto.LoginRequest;
import com.instituto.gestion.dto.LoginResponse;
import com.instituto.gestion.dto.RegistroRequest;
import com.instituto.gestion.dto.UsuarioResponse;
import com.instituto.gestion.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioService service;

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@RequestBody RegistroRequest request) {
        return service.registrar(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return service.login(request);
    }
}
