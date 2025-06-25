package com.programacao_web.rpg_market.controller;

import com.programacao_web.rpg_market.model.Product;
import com.programacao_web.rpg_market.model.ProductCategory;
import com.programacao_web.rpg_market.model.ItemRarity;
import com.programacao_web.rpg_market.model.ProductStatus;
import com.programacao_web.rpg_market.model.ProductType;
import com.programacao_web.rpg_market.service.ProductService;
import com.programacao_web.rpg_market.model.User;
import com.programacao_web.rpg_market.service.UserService;
import com.programacao_web.rpg_market.util.ClassCategoryPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/mercado")
public class MarketController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    // Página principal do mercado
    @GetMapping
    public String showMarketplace(
            Model model, 
            @Qualifier("products") @PageableDefault(size = 6, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable productPageable,
            
            @Qualifier("auctions") @PageableDefault(size = 3, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable auctionPageable) {
        // Obter usuário logado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String characterClass = null;
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            Object principal = auth.getPrincipal();
            if (principal instanceof com.programacao_web.rpg_market.service.CustomUserDetailsService.CustomUserDetails) {
                characterClass = ((com.programacao_web.rpg_market.service.CustomUserDetailsService.CustomUserDetails) principal).getCharacterClass();
            } else {
                String username = auth.getName();
                Optional<User> userOpt = userService.findByUsername(username);
                if (userOpt.isPresent()) {
                    characterClass = userOpt.get().getCharacterClass();
                }
            }
        }        if (characterClass != null) characterClass = capitalize(characterClass.trim());
        Set<ProductCategory> allowedCategories = ClassCategoryPermission.getAllowedCategories(characterClass);
        
        // Produtos de venda direta disponíveis - filtrados por classe de usuário
        List<Product> productsList = new ArrayList<>(productService.findAvailable(productPageable).getContent());
        productsList.removeIf(p -> p.getCategory() == null || !allowedCategories.contains(p.getCategory()));
        model.addAttribute("products", productsList);
        
        // Leilões ativos filtrados
        List<Product> auctionsList = new ArrayList<>(productService.findActiveAuctions(auctionPageable).getContent());
        auctionsList.removeIf(a -> a.getCategory() == null || !allowedCategories.contains(a.getCategory()));
        model.addAttribute("auctions", auctionsList);
        
        // Categorias permitidas para navegação
        model.addAttribute("categories", allowedCategories);
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
    
    // Exibe todos os leilões ativos com opções de filtro e ordenação
    @GetMapping("/masmorra-dos-leiloes")
    public String showAuctions(
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) ItemRarity rarity,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean endingSoon,
            @RequestParam(required = false, defaultValue = "auctionEndDate,asc") String sort,
            Model model,
            @PageableDefault(size = 12) Pageable pageable) {
        // Obter usuário logado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String characterClass = null;
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            Object principal = auth.getPrincipal();
            if (principal instanceof com.programacao_web.rpg_market.service.CustomUserDetailsService.CustomUserDetails) {
                characterClass = ((com.programacao_web.rpg_market.service.CustomUserDetailsService.CustomUserDetails) principal).getCharacterClass();
            } else {
                String username = auth.getName();
                Optional<User> userOpt = userService.findByUsername(username);
                if (userOpt.isPresent()) {
                    characterClass = userOpt.get().getCharacterClass();
                }
            }
        }
        if (characterClass != null) characterClass = capitalize(characterClass.trim());
        Set<ProductCategory> allowedCategories = ClassCategoryPermission.getAllowedCategories(characterClass);
        // Processar parâmetros de ordenação
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        // Criar pageable com ordenação específica
        PageRequest pageRequest = PageRequest.of(
            pageable.getPageNumber(), 
            pageable.getPageSize(), 
            Sort.by(direction, sortField)
        );
        // Usar o novo método de filtro
        List<Product> auctionsList = new ArrayList<>(productService.findAuctionsWithFilters(
            category, rarity, minPrice, maxPrice, endingSoon, pageRequest).getContent());
        auctionsList.removeIf(a -> a.getCategory() == null || !allowedCategories.contains(a.getCategory()));
        Page<Product> auctions = new PageImpl<>(auctionsList, pageRequest, auctionsList.size());        model.addAttribute("auctions", auctions);
        model.addAttribute("categories", allowedCategories);
        model.addAttribute("rarities", com.programacao_web.rpg_market.model.ProductRarity.values());
        return "market/auctions";
    }
    
    // Exibe ranking de vendedores
    @GetMapping("/ranking-dos-nobres")
    public String showRanking(Model model) {
        model.addAttribute("topSellers", productService.getTopSellers());
        model.addAttribute("topBuyers", productService.getTopBuyers());
        return "market/ranking";
    }
      /**
     * Exibe todos os itens de venda direta com opções de filtro e ordenação
     */
    @GetMapping("/vendas-diretas")
    public String showDirectSales(
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) ItemRarity rarity,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Model model,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        // Obter usuário logado e suas permissões de classe
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String characterClass = null;
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            Object principal = auth.getPrincipal();
            if (principal instanceof com.programacao_web.rpg_market.service.CustomUserDetailsService.CustomUserDetails) {
                characterClass = ((com.programacao_web.rpg_market.service.CustomUserDetailsService.CustomUserDetails) principal).getCharacterClass();
            } else {
                String username = auth.getName();
                Optional<User> userOpt = userService.findByUsername(username);
                if (userOpt.isPresent()) {
                    characterClass = userOpt.get().getCharacterClass();
                }
            }
        }
        if (characterClass != null) characterClass = capitalize(characterClass.trim());
        Set<ProductCategory> allowedCategories = ClassCategoryPermission.getAllowedCategories(characterClass);

        // Filtrar produtos de venda direta (não leilões)
        Page<Product> productsPage = productService.findDirectSalesWithFilters(
            category, 
            rarity,
            minPrice, 
            maxPrice, 
            pageable
        );
        
        // Filtrar produtos pela classe do usuário
        List<Product> productsList = new ArrayList<>(productsPage.getContent());
        productsList.removeIf(p -> p.getCategory() == null || !allowedCategories.contains(p.getCategory()));
        Page<Product> filteredProductsPage = new PageImpl<>(productsList, pageable, productsList.size());
        
        model.addAttribute("products", filteredProductsPage);
        model.addAttribute("categories", allowedCategories); // Mostra apenas categorias permitidas
        model.addAttribute("rarities", ItemRarity.values());
        
        return "market/direct-sales";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
