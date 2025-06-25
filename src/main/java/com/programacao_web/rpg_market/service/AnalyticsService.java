package com.programacao_web.rpg_market.service;

import com.programacao_web.rpg_market.dto.AnalyticsData;
import com.programacao_web.rpg_market.model.*;
import com.programacao_web.rpg_market.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serviço para coleta e análise de dados do marketplace
 */
@Service
public class AnalyticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BidRepository bidRepository;

    /**
     * Retorna dados completos de análise
     */
    public AnalyticsData getCompleteAnalytics() {
        AnalyticsData analytics = new AnalyticsData();

        // Métricas básicas
        analytics.setTotalUsuarios(userRepository.count());
        analytics.setTotalProdutos(productRepository.count());
        analytics.setTotalTransacoes(transactionRepository.count());

        // Volume total de vendas e valor médio
        List<Transaction> todasTransacoes = transactionRepository.findAll();
        BigDecimal volumeTotal = todasTransacoes.stream()
            .map(Transaction::getAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        analytics.setVolumeTotalVendas(volumeTotal);
        
        if (todasTransacoes.size() > 0) {
            BigDecimal valorMedio = volumeTotal.divide(
                new BigDecimal(todasTransacoes.size()), 2, RoundingMode.HALF_UP);
            analytics.setValorMedioTransacao(valorMedio);
        } else {
            analytics.setValorMedioTransacao(BigDecimal.ZERO);
        }

        // Métricas dos últimos 30 dias
        LocalDateTime dataLimite = LocalDateTime.now().minusDays(30);
        
        analytics.setVendasUltimos30Dias(countTransactionsSince(dataLimite));
        analytics.setNovasContasUltimos30Dias(countUsersSince(dataLimite));
        analytics.setProdutosCadastradosUltimos30Dias(countProductsSince(dataLimite));

        // Rankings
        analytics.setTopVendedores(getTopSellers(10));
        analytics.setTopCompradores(getTopBuyers(10));

        // Dados para gráficos
        analytics.setVendasPorDia(getVendasPorDia());
        analytics.setVendasPorCategoria(getVendasPorCategoria());
        analytics.setTransacoesPorStatus(getTransacoesPorStatus());

        // Análise de produtos
        analytics.setProdutoMaisCaro(findMostExpensiveProduct());
        analytics.setProdutoMaisVendido(findMostSoldProduct());
        analytics.setCategoriaMaisPopular(findMostPopularCategory());

        // Métricas de leilões
        analytics.setLeiloesAtivos(countActiveAuctions());
        analytics.setLeiloesFinalizados(countFinishedAuctions());
        analytics.setMediaLancesLeilao(getAverageBidsPerAuction());

        return analytics;
    }

    /**
     * Retorna top vendedores com mais detalhes
     */
    public List<AnalyticsData.UserRankingData> getTopSellers(int limit) {
        List<User> topSellers = transactionRepository.findTopSellers(limit);
        List<AnalyticsData.UserRankingData> result = new ArrayList<>();

        for (User seller : topSellers) {
            List<Transaction> vendas = transactionRepository.findBySeller(seller);
            Long quantidade = (long) vendas.size();
            BigDecimal valorTotal = vendas.stream()
                .map(Transaction::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(new AnalyticsData.UserRankingData(seller, quantidade, valorTotal));
        }

        return result;
    }

    /**
     * Retorna top compradores com mais detalhes
     */
    public List<AnalyticsData.UserRankingData> getTopBuyers(int limit) {
        List<User> topBuyers = transactionRepository.findTopBuyers(limit);
        List<AnalyticsData.UserRankingData> result = new ArrayList<>();

        for (User buyer : topBuyers) {
            List<Transaction> compras = transactionRepository.findByBuyer(buyer);
            Long quantidade = (long) compras.size();
            BigDecimal valorTotal = compras.stream()
                .map(Transaction::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(new AnalyticsData.UserRankingData(buyer, quantidade, valorTotal));
        }

        return result;
    }

    /**
     * Retorna vendas agrupadas por dia dos últimos 30 dias
     */
    private List<AnalyticsData.VendasPorDiaData> getVendasPorDia() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(30);
        List<Transaction> transacoes = transactionRepository.findAll().stream()
            .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(inicio))
            .collect(Collectors.toList());

        // Agrupa por dia
        Map<LocalDateTime, List<Transaction>> porDia = new HashMap<>();
        
        for (Transaction t : transacoes) {
            LocalDateTime dia = t.getCreatedAt().toLocalDate().atStartOfDay();
            porDia.computeIfAbsent(dia, k -> new ArrayList<>()).add(t);
        }

        List<AnalyticsData.VendasPorDiaData> resultado = new ArrayList<>();
        for (Map.Entry<LocalDateTime, List<Transaction>> entry : porDia.entrySet()) {
            Long quantidade = (long) entry.getValue().size();
            BigDecimal valor = entry.getValue().stream()
                .map(Transaction::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            resultado.add(new AnalyticsData.VendasPorDiaData(entry.getKey(), quantidade, valor));
        }

        return resultado.stream()
            .sorted((a, b) -> a.getData().compareTo(b.getData()))
            .collect(Collectors.toList());
    }

    /**
     * Retorna vendas agrupadas por categoria
     */
    private List<AnalyticsData.CategoriaData> getVendasPorCategoria() {
        List<Transaction> transacoes = transactionRepository.findAll();
        Map<String, List<Transaction>> porCategoria = new HashMap<>();
        
        for (Transaction t : transacoes) {
            if (t.getProduct() != null && t.getProduct().getCategory() != null) {
                String categoria = t.getProduct().getCategory().toString();
                porCategoria.computeIfAbsent(categoria, k -> new ArrayList<>()).add(t);
            }
        }

        List<AnalyticsData.CategoriaData> resultado = new ArrayList<>();
        for (Map.Entry<String, List<Transaction>> entry : porCategoria.entrySet()) {
            Long quantidade = (long) entry.getValue().size();
            BigDecimal valorTotal = entry.getValue().stream()
                .map(Transaction::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            resultado.add(new AnalyticsData.CategoriaData(entry.getKey(), quantidade, valorTotal));
        }

        return resultado.stream()
            .sorted((a, b) -> b.getQuantidade().compareTo(a.getQuantidade()))
            .collect(Collectors.toList());
    }

    /**
     * Retorna transações agrupadas por status
     */
    private List<AnalyticsData.StatusTransacaoData> getTransacoesPorStatus() {
        List<Transaction> transacoes = transactionRepository.findAll();
        Map<String, Long> porStatus = new HashMap<>();
        
        for (Transaction t : transacoes) {
            if (t.getStatus() != null) {
                String status = t.getStatus().toString();
                porStatus.put(status, porStatus.getOrDefault(status, 0L) + 1);
            }
        }

        List<AnalyticsData.StatusTransacaoData> resultado = new ArrayList<>();
        for (Map.Entry<String, Long> entry : porStatus.entrySet()) {
            resultado.add(new AnalyticsData.StatusTransacaoData(entry.getKey(), entry.getValue()));
        }

        return resultado.stream()
            .sorted((a, b) -> b.getQuantidade().compareTo(a.getQuantidade()))
            .collect(Collectors.toList());
    }

    /**
     * Métodos auxiliares
     */
    private Long countTransactionsSince(LocalDateTime since) {
        return transactionRepository.findAll().stream()
            .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(since))
            .count();
    }

    private Long countUsersSince(LocalDateTime since) {
        // Como não temos campo createdAt em User, retornamos 0 por enquanto
        return 0L;
    }

    private Long countProductsSince(LocalDateTime since) {
        return productRepository.findAll().stream()
            .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(since))
            .count();
    }

    private Product findMostExpensiveProduct() {
        return productRepository.findAll().stream()
            .filter(p -> p.getPrice() != null)
            .max((a, b) -> a.getPrice().compareTo(b.getPrice()))
            .orElse(null);
    }

    private Product findMostSoldProduct() {
        Map<String, Long> vendas = new HashMap<>();
        
        transactionRepository.findAll().forEach(t -> {
            if (t.getProduct() != null && t.getProduct().getId() != null) {
                String produtoId = t.getProduct().getId();
                vendas.put(produtoId, vendas.getOrDefault(produtoId, 0L) + 1);
            }
        });

        if (vendas.isEmpty()) return null;

        String produtoMaisVendidoId = vendas.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);

        return produtoMaisVendidoId != null ? 
            productRepository.findById(produtoMaisVendidoId).orElse(null) : null;
    }

    private String findMostPopularCategory() {
        Map<String, Long> categorias = new HashMap<>();
        
        transactionRepository.findAll().forEach(t -> {
            if (t.getProduct() != null && t.getProduct().getCategory() != null) {
                String categoria = t.getProduct().getCategory().toString();
                categorias.put(categoria, categorias.getOrDefault(categoria, 0L) + 1);
            }
        });

        return categorias.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Nenhuma");
    }

    private Long countActiveAuctions() {
        return productRepository.findAll().stream()
            .filter(p -> p.getType() == ProductType.AUCTION && 
                        p.getStatus() == ProductStatus.AUCTION_ACTIVE)
            .count();
    }

    private Long countFinishedAuctions() {
        return productRepository.findAll().stream()
            .filter(p -> p.getType() == ProductType.AUCTION && 
                        p.getStatus() == ProductStatus.AUCTION_ENDED)
            .count();
    }    private BigDecimal getAverageBidsPerAuction() {
        List<Product> leiloes = productRepository.findAll().stream()
            .filter(p -> p.getType() == ProductType.AUCTION)
            .collect(Collectors.toList());

        if (leiloes.isEmpty()) return BigDecimal.ZERO;

        long totalLances = leiloes.stream()
            .mapToLong(p -> bidRepository.findByProductOrderByAmountDesc(p).size())
            .sum();

        return new BigDecimal(totalLances).divide(
            new BigDecimal(leiloes.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Retorna relatório de atividades detalhado
     */
    public Map<String, Object> getActivityReport() {
        Map<String, Object> report = new HashMap<>();
        
        // Atividades recentes
        report.put("transacoesRecentes", getRecentTransactions(10));
        report.put("novosUsuarios", getRecentUsers(10));
        report.put("novosProdutos", getRecentProducts(10));
        
        // Estatísticas por período
        report.put("estatisticasSemanais", getWeeklyStats());
        report.put("estatisticasMensais", getMonthlyStats());
        
        return report;
    }

    private List<Transaction> getRecentTransactions(int limit) {
        return transactionRepository.findAll().stream()
            .sorted((a, b) -> {
                if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            })
            .limit(limit)
            .collect(Collectors.toList());
    }

    private List<User> getRecentUsers(int limit) {
        return userRepository.findAll().stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    private List<Product> getRecentProducts(int limit) {
        return productRepository.findAll().stream()
            .sorted((a, b) -> {
                if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            })
            .limit(limit)
            .collect(Collectors.toList());
    }

    private Map<String, Object> getWeeklyStats() {
        LocalDateTime semanaAtras = LocalDateTime.now().minusDays(7);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("novasTransacoes", countTransactionsSince(semanaAtras));
        stats.put("novosProdutos", countProductsSince(semanaAtras));
        
        return stats;
    }

    private Map<String, Object> getMonthlyStats() {
        LocalDateTime mesAtras = LocalDateTime.now().minusDays(30);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("novasTransacoes", countTransactionsSince(mesAtras));
        stats.put("novosProdutos", countProductsSince(mesAtras));
        
        return stats;
    }
    
    /**
     * Métodos para contagens de atividades recentes (últimos 7 dias)
     */
    public long getRecentProductsCount() {
        LocalDateTime semanaAtras = LocalDateTime.now().minusDays(7);
        return countProductsSince(semanaAtras);
    }
    
    public long getRecentTransactionsCount() {
        LocalDateTime semanaAtras = LocalDateTime.now().minusDays(7);
        return countTransactionsSince(semanaAtras);
    }
    
    public long getRecentBidsCount() {
        LocalDateTime semanaAtras = LocalDateTime.now().minusDays(7);
        return countBidsSince(semanaAtras);
    }
    
    public long getNewUsersCount() {
        // Como não temos data de criação para usuários, vamos usar uma estimativa
        return Math.max(0, userRepository.count() - 10); // Assumindo que os últimos são novos
    }
    
    /**
     * Métodos para listas de atividades recentes
     */
    public List<Transaction> getRecentTransactionsList() {
        return transactionRepository.findAll().stream()
            .sorted((a, b) -> {
                if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            })
            .limit(15)
            .collect(Collectors.toList());
    }
    
    public List<Product> getRecentProductsList() {
        return getRecentProducts(10);
    }
    
    public List<Bid> getRecentBidsList() {
        return bidRepository.findAll().stream()
            .sorted((a, b) -> {
                if (a.getBidTime() == null && b.getBidTime() == null) return 0;
                if (a.getBidTime() == null) return 1;
                if (b.getBidTime() == null) return -1;
                return b.getBidTime().compareTo(a.getBidTime());
            })
            .limit(15)
            .collect(Collectors.toList());
    }
    
    /**
     * Método auxiliar para contar lances desde uma data
     */
    private long countBidsSince(LocalDateTime since) {
        return bidRepository.findAll().stream()
            .filter(bid -> bid.getBidTime() != null && bid.getBidTime().isAfter(since))
            .count();
    }
}
