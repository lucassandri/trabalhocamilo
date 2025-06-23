package com.programacao_web.rpg_market.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.programacao_web.rpg_market.model.User;
import com.programacao_web.rpg_market.service.FileStorageService;
import com.programacao_web.rpg_market.service.UserService;

@Controller
@RequestMapping("/aventureiro")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/registrar")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "user/register";
    }

    @PostMapping("/registrar")
    public String registerUser(
            @ModelAttribute User user, 
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            RedirectAttributes redirectAttributes) {
        try {
            if (profileImage != null && !profileImage.isEmpty()) {
                String imageFilename = fileStorageService.storeAndResizeImage(profileImage, 200, 200);
                user.setProfileImageUrl(imageFilename);
            }
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success", "Conta criada com sucesso!");
            return "redirect:/login";
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Username já existe")) {
                errorMessage = "Este nome de aventureiro já está em uso. Por favor, escolha outro.";
            } else if (errorMessage.contains("Email já está em uso")) {
                errorMessage = "Este email já está cadastrado. Tente fazer login ou recuperar sua senha.";
            } else {
                errorMessage = "Ocorreu um erro ao criar sua conta. Por favor, tente novamente.";
            }
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/aventureiro/registrar";
        }
    }