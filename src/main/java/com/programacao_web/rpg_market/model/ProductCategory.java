package com.programacao_web.rpg_market.model;

public enum ProductCategory {
    ARMADURA_VESTIMENTA("Armaduras e Vestimentas"),
    ARMAS("Armas e Equipamentos de Combate"),
    RELIQUIAS_TECNOLOGICAS("Relíquias Tecnológicas"),
    POCOES_ELIXIRES("Poções e Elixires"),
    PERGAMINHOS_LIVROS("Pergaminhos e Livros"),
    JOIAS_ARTEFATOS("Jóias e Artefatos Mágicos"),
    MONTARIAS_BESTAS("Montarias e Bestas"),
    DIVERSOS("Itens Diversos");
    
    private String displayName;
    
    ProductCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
