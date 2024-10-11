package org.example.lab720210795;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClienteController {
    @GetMapping("/cliente")
    public String verCliente() {
        return "cliente";
    }
}
