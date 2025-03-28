package com.programacao_web.rpg_market.controller;

import com.programacao_web.rpg_market.model.ProductCategory;
import com.programacao_web.rpg_market.model.ProductStatus;
import com.programacao_web.rpg_market.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/mercado")
public class MarketController {

    @Autowired
    private ProductService productService;
    
    // Página principal do mercado
    @GetMapping
    public String showMarketplace(
            Model model, 
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        // Produtos de venda direta disponíveis
        model.addAttribute("products", productService.findAvailable(pageable));
        model.addAttribute("categories", ProductCategory.values());
        // Explicitly set view name with suffix
        model.addAttribute("title", "Mercado do Reino - RPG Market");
        return "market/index";
    }
    
    // Exibe produtos de uma categoria específica
    @GetMapping("/categoria/{category}")
    public String showCategoryProducts(
            @PathVariable ProductCategory category,
            Model model,
            @PageableDefault(size = 12) Pageable pageable) {
        
        model.addAttribute("products", productService.findByCategory(category, pageable));
        model.addAttribute("currentCategory", category);
        model.addAttribute("categories", ProductCategory.values());
        return "market/category";
    }
    
    // Busca produtos por nome
    @GetMapping("/buscar")
    public String searchProducts(
            @RequestParam(required = false) String keyword,
            Model model,
            @PageableDefault(size = 12) Pageable pageable) {
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("products", 
                    productService.search(keyword, ProductStatus.AVAILABLE, pageable));
            model.addAttribute("keyword", keyword);
        } else {
            model.addAttribute("products", productService.findAvailable(pageable));
        }
        
        model.addAttribute("categories", ProductCategory.values());
        return "market/search";
    }
    
    // Exibe todos os leilões ativos
    @GetMapping("/masmorra-dos-leiloes")
    public String showAuctions(
            Model model,
            @PageableDefault(size = 12) Pageable pageable) {
        
        model.addAttribute("auctions", productService.findActiveAuctions(pageable));
        model.addAttribute("categories", ProductCategory.values());
        return "market/auctions";
    }
    
    // Exibe ranking de vendedores
    @GetMapping("/ranking-dos-nobres")
    public String showRanking(Model model) {
        model.addAttribute("topSellers", productService.getTopSellers());
        model.addAttribute("topBuyers", productService.getTopBuyers());
        return "market/ranking";
    }
}
