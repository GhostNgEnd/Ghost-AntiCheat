package ac.ghost.anticheat.data.inventory;





public record PotionMixData(
        int inputId,
        int inputMeta,
        int reagentId,
        int reagentMeta,
        int outputId,
        int outputMeta
) {
}
