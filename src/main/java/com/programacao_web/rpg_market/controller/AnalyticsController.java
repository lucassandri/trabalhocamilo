package com.programacao_web.rpg_market.controller;

import com.programacao_web.rpg_market.model.User;
import com.programacao_web.rpg_market.model.Product;
import com.programacao_web.rpg_market.model.Transaction;
import com.programacao_web.rpg_market.model.Bid;
import com.programacao_web.rpg_market.service.UserService;
import com.programacao_web.rpg_market.repository.UserRepository;
import com.programacao_web.rpg_market.repository.TransactionRepository;
import com.programacao_web.rpg_market.repository.ProductRepository;
import com.programacao_web.rpg_market.repository.BidRepository;
import com.programacao_web.rpg_market.dto.AnalyticsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Controlador para análises e ranking - Acesso restrito para ROLE_MESTRE
 */
@Controller
@RequestMapping("/mestre")
public class AnalyticsController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private BidRepository bidRepository;

    /**
     * Página principal de análises (dashboard simplificado)
     * Restrito para usuários com ROLE_MESTRE
     */
    @GetMapping("/dashboard")
    public String showAnalyticsDashboard(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam(defaultValue = "7") int periodo,
            Model model) {
        
        try {
          
        // Verificação de autorização
        Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
        if (userOpt.isEmpty()) {
            return "error/403";
        }

        User user = userOpt.get();
        if (!user.getRole().toString().equals("ROLE_MESTRE")) {
            return "error/403";
        }

        // Dados básicos para o dashboard
        AnalyticsData analytics = new AnalyticsData();
        analytics.setTotalUsuarios(userRepository.count());
        analytics.setTotalProdutos(productRepository.count());
        analytics.setTotalTransacoes(transactionRepository.count());
        analytics.setLeiloesAtivos(bidRepository.count());
        analytics.setLeiloesFinalizados(0L);
        
        // Calcular estatísticas baseadas no período selecionado
        LocalDateTime dataInicio = LocalDateTime.now().minusDays(periodo);
        
        // Transações no período
        List<Transaction> transacoesPeriodo = transactionRepository.findAll()
            .stream()
            .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(dataInicio))
            .collect(Collectors.toList());
        
        // Valor médio das transações no período
        BigDecimal valorMedio = BigDecimal.ZERO;
        if (!transacoesPeriodo.isEmpty()) {
            BigDecimal soma = transacoesPeriodo.stream()
                .map(Transaction::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (soma.compareTo(BigDecimal.ZERO) > 0) {
                valorMedio = soma.divide(BigDecimal.valueOf(transacoesPeriodo.size()), 2, RoundingMode.HALF_UP);
            }
        }
        analytics.setValorMedioTransacao(valorMedio);
        
        // Taxa de atividade (transações por dia no período)
        BigDecimal taxaAtividade = BigDecimal.ZERO;
        if (periodo > 0) {
            taxaAtividade = BigDecimal.valueOf(transacoesPeriodo.size())
                .divide(BigDecimal.valueOf(periodo), 2, RoundingMode.HALF_UP);
        }
        analytics.setTaxaAtividade(taxaAtividade);
        
        // Top vendedores (ordenados by gold coins)
        List<User> topVendedores = userRepository.findAll()
                .stream()
                .filter(u -> u.getGoldCoins() != null)
                .sorted((u1, u2) -> u2.getGoldCoins().compareTo(u1.getGoldCoins()))
                .limit(5)
                .collect(Collectors.toList());
        
        // Produtos mais caros
        List<Product> produtosMaisCaros = productRepository.findAll()
                .stream()
                .filter(p -> p.getPrice() != null)
                .sorted((p1, p2) -> p2.getPrice().compareTo(p1.getPrice()))
                .limit(5)
                .collect(Collectors.toList());
        
        // Volume total de vendas
        BigDecimal volumeTotal = productRepository.findAll()
                .stream()
                .map(p -> p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        analytics.setVolumeTotalVendas(volumeTotal);
        
        // Produtos por categoria (para o gráfico melhorado)
        Map<String, Long> produtosPorCategoria = productRepository.findAll()
                .stream()
                .filter(p -> p.getCategory() != null)
                .collect(Collectors.groupingBy(
                    p -> p.getCategory().toString(),
                    Collectors.counting()
                ));
        
        // Adicionar todos os dados ao modelo
        model.addAttribute("analytics", analytics);
        model.addAttribute("currentUser", user);
        model.addAttribute("topVendedores", topVendedores);
        model.addAttribute("produtosMaisCaros", produtosMaisCaros);
        model.addAttribute("produtos", productRepository.findAll());
        model.addAttribute("periodoSelecionado", periodo);
        model.addAttribute("transacoesPeriodo", transacoesPeriodo.size());
        model.addAttribute("produtosPorCategoria", produtosPorCategoria);
        
        return "analytics/dashboard";
        
        } catch (Exception e) {
            System.err.println("Erro no dashboard: " + e.getMessage());
            // Log do erro sem stack trace
            model.addAttribute("error", "Erro interno: " + e.getMessage());
            return "error/500";
        }
    }

    /**
     * Página de ranking detalhado dos nobres
     * Restrito para usuários com ROLE_MESTRE
     */
    @GetMapping("/ranking-nobres")
    public String showRankingDetalhado(
            @AuthenticationPrincipal UserDetails currentUser,
            Model model) {
        
        try {
            // Verificação de autorização
            Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
            if (userOpt.isEmpty()) {
                return "error/403";
            }

            User user = userOpt.get();
            if (!user.getRole().toString().equals("ROLE_MESTRE")) {
                return "error/403";
            }

            // Diferentes rankings
            List<User> topVendedores = userRepository.findAll().stream()
                    .filter(u -> u.getGoldCoins() != null)
                    .sorted((u1, u2) -> u2.getGoldCoins().compareTo(u1.getGoldCoins()))
                    .limit(10)
                    .collect(Collectors.toList());
            
            // Top compradores baseado em transações realizadas (como comprador)
            Map<User, Long> comprasPorUsuario = transactionRepository.findAll().stream()
                    .filter(t -> t.getBuyer() != null)
                    .collect(Collectors.groupingBy(
                        Transaction::getBuyer,
                        Collectors.counting()
                    ));
                    
            List<User> topCompradores = comprasPorUsuario.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .map(Map.Entry::getKey)
                    .limit(10)
                    .collect(Collectors.toList());
            
            // Se não houver compradores baseado em transações, usar nível como critério alternativo
            if (topCompradores.isEmpty()) {
                topCompradores = userRepository.findAll().stream()
                        .filter(u -> u.getLevel() > 0)
                        .sorted((u1, u2) -> Integer.compare(u2.getLevel(), u1.getLevel()))
                        .limit(10)
                        .collect(Collectors.toList());
            }
            
            List<User> usuariosMaisRicos = userRepository.findAll().stream()
                    .filter(u -> u.getGoldCoins() != null && u.getGoldCoins().compareTo(BigDecimal.ZERO) > 0)
                    .sorted((u1, u2) -> u2.getGoldCoins().compareTo(u1.getGoldCoins()))
                    .limit(10)
                    .collect(Collectors.toList());
            
            // Estatísticas gerais
            long totalAventureiros = userRepository.count();
            long totalMestres = userRepository.findAll().stream()
                    .filter(u -> u.getRole() != null && u.getRole().toString().equals("ROLE_MESTRE"))
                    .count();
            
            // Adicionar dados ao modelo
            model.addAttribute("currentUser", user);
            model.addAttribute("topVendedores", topVendedores);
            model.addAttribute("topCompradores", topCompradores);
            model.addAttribute("usuariosMaisRicos", usuariosMaisRicos);
            model.addAttribute("totalAventureiros", totalAventureiros);
            model.addAttribute("totalMestres", totalMestres);
            model.addAttribute("totalTransacoes", transactionRepository.count());
            model.addAttribute("totalLances", bidRepository.count());
            
            // Volume total de vendas
            BigDecimal volumeTotalVendas = productRepository.findAll()
                    .stream()
                    .map(p -> p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("volumeTotalVendas", volumeTotalVendas);
            
            return "analytics/ranking-nobres";
            
        } catch (Exception e) {
            // Log do erro para debug
            System.err.println("Erro no ranking-nobres: " + e.getMessage());
            // Log do erro sem stack trace
            model.addAttribute("error", "Erro interno: " + e.getMessage());
            return "error/500";
        }
    }

    /**
     * Página de relatório de atividades
     * Restrito para usuários com ROLE_MESTRE
     */
    @GetMapping("/relatorio-atividades")
    public String showRelatorioAtividades(
            @AuthenticationPrincipal UserDetails currentUser,
            Model model) {
        
        try {
            // Verificação de autorização
            Optional<User> userOpt = userService.findByUsername(currentUser.getUsername());
            if (userOpt.isEmpty()) {
                return "error/403";
            }

            User user = userOpt.get();
            if (!user.getRole().toString().equals("ROLE_MESTRE")) {
                return "error/403";
            }

            // Atividades recentes (últimos 7 dias)
            LocalDateTime seteDiasAtras = LocalDateTime.now().minusDays(7);
            
            List<Transaction> transacoesRecentes = transactionRepository.findAll()
                    .stream()
                    .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(seteDiasAtras))
                    .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                    .limit(10)
                    .collect(Collectors.toList());
            
            List<Bid> lancesRecentes = bidRepository.findAll()
                    .stream()
                    .filter(b -> b.getBidTime() != null && b.getBidTime().isAfter(seteDiasAtras))
                    .sorted((b1, b2) -> b2.getBidTime().compareTo(b1.getBidTime()))
                    .limit(10)
                    .collect(Collectors.toList());
            
            List<Product> produtosRecentes = productRepository.findAll()
                    .stream()
                    .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(seteDiasAtras))
                    .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                    .limit(10)
                    .collect(Collectors.toList());
            
            // Adicionar dados ao modelo
            model.addAttribute("currentUser", user);
            model.addAttribute("totalUsuarios", userRepository.count());
            model.addAttribute("vendasHoje", transacoesRecentes.size());
            model.addAttribute("totalLances", lancesRecentes.size());
            model.addAttribute("novosUsuariosHoje", 0L);
            model.addAttribute("novosProdutosHoje", produtosRecentes.size());
            
            // Listas de atividades
            model.addAttribute("transacoesRecentes", transacoesRecentes);
            model.addAttribute("lancesRecentes", lancesRecentes);
            model.addAttribute("produtosRecentes", produtosRecentes);
            
            return "analytics/relatorio-atividades";
            
        } catch (Exception e) {
            System.err.println("Erro no relatório de atividades: " + e.getMessage());
            // Log do erro sem stack trace
            model.addAttribute("error", "Erro interno: " + e.getMessage());
            return "error/500";
        }
    }
}