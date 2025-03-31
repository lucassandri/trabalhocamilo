package com.programacao_web.rpg_market.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorController {

    @GetMapping("/404")
    public String notFound() {
        return "error/404"; 
    }

    @GetMapping("/403")
    public String forbidden() {
        return "error/403";
    }
    
    @GetMapping("/500")
    public String serverError() {
        return "error/500";
    }
}