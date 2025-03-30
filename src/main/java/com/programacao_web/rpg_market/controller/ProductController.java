package com.programacao_web.rpg_market.controller;

import com.programacao_web.rpg_market.model.*;
import com.programacao_web.rpg_market.service.FileStorageService;
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
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/item")
public class ProductController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    // Test endpoint to verify controller mapping is working
    @GetMapping("/test")
    @ResponseBody
    public String testEndpoint() {
        return "ProductController is working!";
    }
    
    // Exibe formulário para criar um novo produto com dados enriquecidos
    @GetMapping("/novo")
    public String showCreateProductForm(Model model) {
        try {
            model.addAttribute("product", new Product());
            model.addAttribute("categories", ProductCategory.values());
            model.addAttribute("rarities", ItemRarity.values());
            model.addAttribute("types", ProductType.values());
            
            // Add information about magic properties if used in template
            model.addAttribute("magicProperties", MagicProperty.values());
            
            return "product/create";
        } catch (Exception e) {
            // Log exception details
            System.err.println("Error in showCreateProductForm: " + e.getMessage());
            e.printStackTrace();
            throw e; // Rethrow to see the error in browser
        }
    }
    
    // Processa a criação de um novo produto com melhor validação e feedback
    @PostMapping("/novo")
    public String createProduct(
            @ModelAttribute Product product,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) String[] magicProperties,
            @AuthenticationPrincipal UserDetails currentUser,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Busca o usuário atual para definir como vendedor
            Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Usuário não encontrado no reino.");
                return "redirect:/item/novo";
            }
            
            // Processa a imagem do item
            if (image != null && !image.isEmpty()) {
                String imageFilename = fileStorageService.storeFile(image);
                product.setImageUrl(imageFilename);
            }
            
            // Define propriedades mágicas se fornecidas
            if (magicProperties != null && magicProperties.length > 0) {
                for (String property : magicProperties) {
                    product.addMagicProperty(MagicProperty.valueOf(property));
                }
            }
            
            // Define o status baseado no tipo de produto
            if (product.getType() == ProductType.AUCTION) {
                product.setStatus(ProductStatus.AUCTION_ACTIVE);
                
                // Configure auction end date if not set
                if (product.getAuctionEndDate() == null) {
                    product.setAuctionEndDate(LocalDateTime.now().plusDays(7));
                }
            } else {
                product.setStatus(ProductStatus.AVAILABLE);
            }
            
            // Data de criação
            product.setCreatedAt(LocalDateTime.now());
            
            // Salva o produto
            Product savedProduct = productService.create(product, userOpt.get());
            
            redirectAttributes.addFlashAttribute("success", 
                "Seu item foi colocado à venda no mercado! Os aventureiros já podem vê-lo.");
            return "redirect:/aventureiro/inventario";
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erro ao processar a imagem do item. Os orcs devem ter interferido na transmissão.");
            return "redirect:/item/novo";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erro ao criar item: " + e.getMessage());
            return "redirect:/item/novo";
        }
    }
    
    // Exibe detalhes de um produto
    @GetMapping("/{id}")
    public String showProduct(@PathVariable String id, Model model) {
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
            @PathVariable String id,
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
            @PathVariable String id,
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
    public String showEditForm(@PathVariable String id, Model model, @AuthenticationPrincipal UserDetails currentUser) {
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
            @PathVariable String id,
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
