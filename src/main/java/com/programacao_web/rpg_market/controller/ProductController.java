package com.programacao_web.rpg_market.controller;

import com.programacao_web.rpg_market.model.Product;
import com.programacao_web.rpg_market.model.ProductCategory;
import com.programacao_web.rpg_market.model.ProductType;
import com.programacao_web.rpg_market.model.User;
import com.programacao_web.rpg_market.service.ProductService;
import com.programacao_web.rpg_market.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/item") // Prefixo RPG para produtos
public class ProductController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    // Exibe formulário para criar um novo produto
    @GetMapping("/novo")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("types", ProductType.values());
        return "product/create";
    }
    
    // Processa a criação de um novo produto
    @PostMapping("/novo")
    public String createProduct(
            @ModelAttribute Product product,
            @RequestParam(required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetails currentUser,
            RedirectAttributes redirectAttributes) throws IOException {
        
        // Busca o usuário atual para definir como vendedor
        Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Usuário não encontrado");
            return "redirect:/item/novo";
        }
        
        // Se houver imagem, processa ela (implementação de upload omitida)
        if (image != null && !image.isEmpty()) {
            String imageUrl = "caminho/para/imagem/" + System.currentTimeMillis(); // Exemplo simplificado
            product.setImageUrl(imageUrl);
        }
        
        // Salva o produto
        productService.create(product, userOpt.get());
        
        redirectAttributes.addFlashAttribute("success", "Item criado com sucesso!");
        return "redirect:/aventureiro/inventario";
    }
    
    // Exibe detalhes de um produto
    @GetMapping("/{id}")
    public String showProduct(@PathVariable Long id, Model model) {
        Optional<Product> productOpt = productService.findById(id);
        if (productOpt.isEmpty()) {
            return "error/404"; // Página não encontrada
        }
        
        model.addAttribute("product", productOpt.get());
        
        // Se for leilão, busca os lances
        if (productOpt.get().getType() == ProductType.AUCTION) {
            model.addAttribute("bids", productService.getProductBids(productOpt.get()));
        }
        
        return "product/details";
    }
    
    // Processa a compra direta de um produto
    @PostMapping("/{id}/comprar")
    public String buyProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser,
            RedirectAttributes redirectAttributes) {
        
        try {
            Optional<Product> productOpt = productService.findById(id);
            Optional<User> buyerOpt = userService.findByUsername(currentUser.getUsername());
            
            if (productOpt.isEmpty() || buyerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Produto ou usuário não encontrado");
                return "redirect:/item/" + id;
            }
            
            productService.buyNow(productOpt.get(), buyerOpt.get());
            redirectAttributes.addFlashAttribute("success", "Compra realizada com sucesso!");
            return "redirect:/aventureiro/compras";
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/item/" + id;
        }
    }
    
    // Processa um lance em um leilão
    @PostMapping("/{id}/lance")
    public String makeBid(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @AuthenticationPrincipal UserDetails currentUser,
            RedirectAttributes redirectAttributes) {
        
        try {
            Optional<Product> productOpt = productService.findById(id);
            Optional<User> bidderOpt = userService.findByUsername(currentUser.getUsername());
            
            if (productOpt.isEmpty() || bidderOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Produto ou usuário não encontrado");
                return "redirect:/item/" + id;
            }
            
            productService.makeBid(productOpt.get(), bidderOpt.get(), amount);
            redirectAttributes.addFlashAttribute("success", "Lance registrado com sucesso!");
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/item/" + id;
    }
    
    // Exibe formulário de edição de produto
    @GetMapping("/{id}/editar")
    public String showEditForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails currentUser) {
        Optional<Product> productOpt = productService.findById(id);
        Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
        
        if (productOpt.isEmpty() || userOpt.isEmpty()) {
            return "error/404";
        }
        
        // Verifica se o produto pertence ao usuário
        if (!productOpt.get().getSeller().getId().equals(userOpt.get().getId())) {
            return "error/403"; // Acesso negado
        }
        
        model.addAttribute("product", productOpt.get());
        model.addAttribute("categories", ProductCategory.values());
        
        return "product/edit";
    }
    
    // Processa a edição de um produto
    @PostMapping("/{id}/editar")
    public String editProduct(
            @PathVariable Long id,
            @ModelAttribute Product product,
            @AuthenticationPrincipal UserDetails currentUser,
            RedirectAttributes redirectAttributes) {
        
        Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
        if (userOpt.isEmpty()) {
            return "error/403";
        }
        
        try {
            productService.update(id, product, userOpt.get());
            redirectAttributes.addFlashAttribute("success", "Item atualizado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/item/" + id;
    }
}
