package com.programacao_web.rpg_market.controller;

import com.programacao_web.rpg_market.model.User;
import com.programacao_web.rpg_market.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/aventureiro") // Prefixo RPG para todas as rotas de usuário
public class UserController {

    @Autowired
    private UserService userService;
    
    // Exibe formulário de registro
    @GetMapping("/registrar")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "user/register"; // Caminho para o template Thymeleaf
    }
    
    // Processa o registro de novos usuários
    @PostMapping("/registrar")
    public String registerUser(@ModelAttribute User user) {
        userService.registerUser(user);
        return "redirect:/login"; // Redireciona para a página de login após registro
    }
    
    // Exibe o perfil do usuário logado
    @GetMapping("/perfil")
    public String showUserProfile(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        userService.findByUsername(currentUser.getUsername()).ifPresent(user -> {
            model.addAttribute("user", user);
        });
        return "user/profile"; // Página de perfil
    }
    
    // Exibe o inventário (produtos) do usuário
    @GetMapping("/inventario")
    public String showUserInventory(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        userService.findByUsername(currentUser.getUsername()).ifPresent(user -> {
            model.addAttribute("user", user);
            model.addAttribute("products", user.getProducts());
        });
        return "user/inventory"; // Página de inventário
    }
    
    // Exibe as compras realizadas pelo usuário
    @GetMapping("/compras")
    public String showUserPurchases(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        userService.findByUsername(currentUser.getUsername()).ifPresent(user -> {
            model.addAttribute("transactions", userService.getUserPurchases(user));
        });
        return "user/purchases";
    }
    
    // Exibe as vendas realizadas pelo usuário
    @GetMapping("/vendas")
    public String showUserSales(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        userService.findByUsername(currentUser.getUsername()).ifPresent(user -> {
            model.addAttribute("transactions", userService.getUserSales(user));
        });
        return "user/sales";
    }
}
