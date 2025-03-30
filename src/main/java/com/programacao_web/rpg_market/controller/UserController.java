package com.programacao_web.rpg_market.controller;

import com.programacao_web.rpg_market.model.Product;
import com.programacao_web.rpg_market.model.Transaction;
import com.programacao_web.rpg_market.model.User;
import com.programacao_web.rpg_market.service.ProductService;
import com.programacao_web.rpg_market.service.UserService;
import com.programacao_web.rpg_market.dto.PasswordChangeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/aventureiro") // Prefixo RPG para todas as rotas de usuário
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
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
    public String showProfile(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
        if (userOpt.isEmpty()) {
            return "error/403";
        }
        
        User user = userOpt.get();
        // Adicione todos os atributos necessários
        model.addAttribute("user", user);
        model.addAttribute("activeItems", productService.findByUser(user)); // Ou outra lógica
        model.addAttribute("soldItems", productService.findSoldByUser(user)); // Ou outra lógica
        model.addAttribute("userPurchases", userService.getUserPurchases(user)); // Ou outra lógica
        
        return "user/profile"; // Retorno direto para a página, sem usar "content"
    }
    
    // Exibe o inventário (produtos) do usuário
    @GetMapping("/inventario")
    public String showInventory(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
        if (userOpt.isEmpty()) {
            return "error/403";
        }
        
        User user = userOpt.get();
        List<Product> products = productService.findByUser(user);
        model.addAttribute("products", products);
        
        // Retorne diretamente para a página inventory
        return "user/inventory";
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
    
    // Exibe formulário para alterar senha
    @GetMapping("/senha")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("passwordRequest", new PasswordChangeRequest());
        return "user/change-password"; // Return direct template path
    }
    
    // Processa a alteração de senha
    @PostMapping("/senha")
    public String changePassword(
            @ModelAttribute PasswordChangeRequest passwordRequest, 
            @AuthenticationPrincipal UserDetails currentUser,
            RedirectAttributes redirectAttributes) {
        
        try {
            Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
            if (userOpt.isEmpty()) {
                return "error/403";
            }
            
            userService.changePassword(userOpt.get(), 
                    passwordRequest.getCurrentPassword(), 
                    passwordRequest.getNewPassword(), 
                    passwordRequest.getConfirmPassword());
            
            redirectAttributes.addFlashAttribute("success", "Senha alterada com sucesso!");
            return "redirect:/aventureiro/perfil"; // Only redirect to profile on success
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            // Return to the change password form on error
            return "redirect:/aventureiro/senha";
        }
    }
    
    // Exibe formulário para editar perfil
    @GetMapping("/editar-perfil")
    public String showEditProfileForm(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
        if (userOpt.isEmpty()) {
            return "error/403";
        }
        
        model.addAttribute("user", userOpt.get());
        return "user/edit-profile"; // Return direct template path
    }
    
    // Processa a edição do perfil
    @PostMapping("/editar-perfil")
    public String updateProfile(
            @ModelAttribute User updatedUser,
            @AuthenticationPrincipal UserDetails currentUser,
            RedirectAttributes redirectAttributes) {
        
        try {
            Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
            if (userOpt.isEmpty()) {
                return "error/403";
            }
            
            User user = userOpt.get();
            userService.updateProfile(user, updatedUser);
            redirectAttributes.addFlashAttribute("success", "Perfil atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/aventureiro/perfil";
    }
}
