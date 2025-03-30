package com.programacao_web.rpg_market.model;

public enum MagicProperty {
    FOGO("Fogo", "Causa dano adicional de fogo"),
    GELO("Gelo", "Causa dano adicional de gelo"),
    RAIO("Raio", "Causa dano adicional de raio"),
    VENENO("Veneno", "Envenena o alvo"),
    CURA("Cura", "Restaura pontos de vida"),
    VELOCIDADE("Velocidade", "Aumenta a velocidade do portador"),
    FORCA("Força", "Aumenta a força do portador"),
    RESISTENCIA("Resistência", "Aumenta a resistência a danos"),
    INVISIBILIDADE("Invisibilidade", "Concede invisibilidade parcial"),
    VITALIDADE("Vitalidade", "Aumenta os pontos de vida máximos"),
    SORTE("Sorte", "Aumenta a chance de crítico"),
    MANA("Mana", "Aumenta pontos de mana"),
    TELEPORTE("Teleporte", "Permite teleportar curtas distâncias"),
    ENCANTAMENTO("Encantamento", "Melhora os efeitos de magias"),
    SABEDORIA("Sabedoria", "Aumenta inteligência e sabedoria"),
    LUZ("Luz", "Emite luz em locais escuros");
    
    private final String displayName;
    private final String description;
    
    MagicProperty(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}