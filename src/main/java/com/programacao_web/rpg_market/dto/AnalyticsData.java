package com.programacao_web.rpg_market.dto;

import com.programacao_web.rpg_market.model.User;
import com.programacao_web.rpg_market.model.Transaction;
import com.programacao_web.rpg_market.model.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO para dados de análises e métricas do marketplace
 */
public class AnalyticsData {
    
    // Métricas gerais
    private Long totalUsuarios;
    private Long totalProdutos;
    private Long totalTransacoes;
    private BigDecimal volumeTotalVendas;
    private BigDecimal valorMedioTransacao;
    
    // Métricas de período
    private Long vendasUltimos30Dias;
    private Long novasContasUltimos30Dias;
    private Long produtosCadastradosUltimos30Dias;
    private BigDecimal taxaAtividade; // Nova métrica para taxa de atividade
    
    // Rankings
    private List<UserRankingData> topVendedores;
    private List<UserRankingData> topCompradores;
    
    // Dados para gráficos
    private List<VendasPorDiaData> vendasPorDia;
    private List<CategoriaData> vendasPorCategoria;
    private List<StatusTransacaoData> transacoesPorStatus;
    
    // Análise de produtos
    private Product produtoMaisCaro;
    private Product produtoMaisVendido;
    private String categoriaMaisPopular;
    
    // Métricas de leilões
    private Long leiloesAtivos;
    private Long leiloesFinalizados;
    private BigDecimal mediaLancesLeilao;
    
    // Construtor padrão
    public AnalyticsData() {}
    
    // Classes internas para estruturar dados
    public static class UserRankingData {
        private String userId;
        private String username;
        private String characterClass;
        private Integer level;
        private Long quantidadeTransacoes;
        private BigDecimal valorTotal;
        private String profileImageUrl;
        
        // Construtores
        public UserRankingData() {}
        
        public UserRankingData(User user, Long quantidade, BigDecimal valor) {
            this.userId = user.getId();
            this.username = user.getUsername();
            this.characterClass = user.getCharacterClass();
            this.level = user.getLevel();
            this.quantidadeTransacoes = quantidade;
            this.valorTotal = valor;
            this.profileImageUrl = user.getProfileImageUrl();
        }
        
        // Getters e Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getCharacterClass() { return characterClass; }
        public void setCharacterClass(String characterClass) { this.characterClass = characterClass; }
        
        public Integer getLevel() { return level; }
        public void setLevel(Integer level) { this.level = level; }
        
        public Long getQuantidadeTransacoes() { return quantidadeTransacoes; }
        public void setQuantidadeTransacoes(Long quantidadeTransacoes) { this.quantidadeTransacoes = quantidadeTransacoes; }
        
        public BigDecimal getValorTotal() { return valorTotal; }
        public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
        
        public String getProfileImageUrl() { return profileImageUrl; }
        public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
        
        /**
         * Métodos adicionais para compatibilidade com templates
         */
        public Long getTotalSales() { 
            return quantidadeTransacoes; 
        }
        
        public BigDecimal getTotalRevenue() { 
            return valorTotal; 
        }
        
        public Long getTotalPurchases() { 
            return quantidadeTransacoes; 
        }
        
        public BigDecimal getTotalSpent() { 
            return valorTotal; 
        }
    }
    
    public static class VendasPorDiaData {
        private LocalDateTime data;
        private Long quantidade;
        private BigDecimal valor;
        
        public VendasPorDiaData() {}
        
        public VendasPorDiaData(LocalDateTime data, Long quantidade, BigDecimal valor) {
            this.data = data;
            this.quantidade = quantidade;
            this.valor = valor;
        }
        
        // Getters e Setters
        public LocalDateTime getData() { return data; }
        public void setData(LocalDateTime data) { this.data = data; }
        
        public Long getQuantidade() { return quantidade; }
        public void setQuantidade(Long quantidade) { this.quantidade = quantidade; }
        
        public BigDecimal getValor() { return valor; }
        public void setValor(BigDecimal valor) { this.valor = valor; }
    }
    
    public static class CategoriaData {
        private String categoria;
        private Long quantidade;
        private BigDecimal valorTotal;
        
        public CategoriaData() {}
        
        public CategoriaData(String categoria, Long quantidade, BigDecimal valorTotal) {
            this.categoria = categoria;
            this.quantidade = quantidade;
            this.valorTotal = valorTotal;
        }
        
        // Getters e Setters
        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }
        
        public Long getQuantidade() { return quantidade; }
        public void setQuantidade(Long quantidade) { this.quantidade = quantidade; }
        
        public BigDecimal getValorTotal() { return valorTotal; }
        public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
    }
    
    public static class StatusTransacaoData {
        private String status;
        private Long quantidade;
        
        public StatusTransacaoData() {}
        
        public StatusTransacaoData(String status, Long quantidade) {
            this.status = status;
            this.quantidade = quantidade;
        }
        
        // Getters e Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Long getQuantidade() { return quantidade; }
        public void setQuantidade(Long quantidade) { this.quantidade = quantidade; }
    }
    
    // Getters e Setters principais
    public Long getTotalUsuarios() { return totalUsuarios; }
    public void setTotalUsuarios(Long totalUsuarios) { this.totalUsuarios = totalUsuarios; }
    
    public Long getTotalProdutos() { return totalProdutos; }
    public void setTotalProdutos(Long totalProdutos) { this.totalProdutos = totalProdutos; }
    
    public Long getTotalTransacoes() { return totalTransacoes; }
    public void setTotalTransacoes(Long totalTransacoes) { this.totalTransacoes = totalTransacoes; }
    
    public BigDecimal getVolumeTotalVendas() { return volumeTotalVendas; }
    public void setVolumeTotalVendas(BigDecimal volumeTotalVendas) { this.volumeTotalVendas = volumeTotalVendas; }
    
    public BigDecimal getValorMedioTransacao() { return valorMedioTransacao; }
    public void setValorMedioTransacao(BigDecimal valorMedioTransacao) { this.valorMedioTransacao = valorMedioTransacao; }
    
    public Long getVendasUltimos30Dias() { return vendasUltimos30Dias; }
    public void setVendasUltimos30Dias(Long vendasUltimos30Dias) { this.vendasUltimos30Dias = vendasUltimos30Dias; }
    
    public Long getNovasContasUltimos30Dias() { return novasContasUltimos30Dias; }
    public void setNovasContasUltimos30Dias(Long novasContasUltimos30Dias) { this.novasContasUltimos30Dias = novasContasUltimos30Dias; }
    
    public Long getProdutosCadastradosUltimos30Dias() { return produtosCadastradosUltimos30Dias; }
    public void setProdutosCadastradosUltimos30Dias(Long produtosCadastradosUltimos30Dias) { this.produtosCadastradosUltimos30Dias = produtosCadastradosUltimos30Dias; }
    
    public List<UserRankingData> getTopVendedores() { return topVendedores; }
    public void setTopVendedores(List<UserRankingData> topVendedores) { this.topVendedores = topVendedores; }
    
    public List<UserRankingData> getTopCompradores() { return topCompradores; }
    public void setTopCompradores(List<UserRankingData> topCompradores) { this.topCompradores = topCompradores; }
    
    public List<VendasPorDiaData> getVendasPorDia() { return vendasPorDia; }
    public void setVendasPorDia(List<VendasPorDiaData> vendasPorDia) { this.vendasPorDia = vendasPorDia; }
    
    public List<CategoriaData> getVendasPorCategoria() { return vendasPorCategoria; }
    public void setVendasPorCategoria(List<CategoriaData> vendasPorCategoria) { this.vendasPorCategoria = vendasPorCategoria; }
    
    public List<StatusTransacaoData> getTransacoesPorStatus() { return transacoesPorStatus; }
    public void setTransacoesPorStatus(List<StatusTransacaoData> transacoesPorStatus) { this.transacoesPorStatus = transacoesPorStatus; }
    
    public Product getProdutoMaisCaro() { return produtoMaisCaro; }
    public void setProdutoMaisCaro(Product produtoMaisCaro) { this.produtoMaisCaro = produtoMaisCaro; }
    
    public Product getProdutoMaisVendido() { return produtoMaisVendido; }
    public void setProdutoMaisVendido(Product produtoMaisVendido) { this.produtoMaisVendido = produtoMaisVendido; }
    
    public String getCategoriaMaisPopular() { return categoriaMaisPopular; }
    public void setCategoriaMaisPopular(String categoriaMaisPopular) { this.categoriaMaisPopular = categoriaMaisPopular; }
    
    public Long getLeiloesAtivos() { return leiloesAtivos; }
    public void setLeiloesAtivos(Long leiloesAtivos) { this.leiloesAtivos = leiloesAtivos; }
    
    public Long getLeiloesFinalizados() { return leiloesFinalizados; }
    public void setLeiloesFinalizados(Long leiloesFinalizados) { this.leiloesFinalizados = leiloesFinalizados; }
    
    public BigDecimal getMediaLancesLeilao() { return mediaLancesLeilao; }
    public void setMediaLancesLeilao(BigDecimal mediaLancesLeilao) { this.mediaLancesLeilao = mediaLancesLeilao; }
    
    public BigDecimal getTaxaAtividade() { return taxaAtividade; }
    public void setTaxaAtividade(BigDecimal taxaAtividade) { this.taxaAtividade = taxaAtividade; }
    
    /**
     * Métodos adicionais para compatibilidade com os templates
     */
    public Long getTotalUsers() { 
        return totalUsuarios; 
    }
    
    public Long getCompletedTransactions() { 
        return totalTransacoes; 
    }
    
    public BigDecimal getTotalRevenue() { 
        return volumeTotalVendas; 
    }
    
    public Long getAuctionCount() { 
        return leiloesAtivos; 
    }
}
