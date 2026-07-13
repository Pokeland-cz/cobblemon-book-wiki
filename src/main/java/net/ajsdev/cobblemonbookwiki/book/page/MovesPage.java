package net.ajsdev.cobblemonbookwiki.book.page;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.pokemon.moves.Learnset;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.pokemon.FormData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MovesPage {

    private static final int LINES_PER_PAGE = 13;

    public static List<Component> build(FormData formData) {
        Learnset learnset = formData.getMoves();
        List<Component> allLines = new ArrayList<>();

        // 1. Level-Up Moves (Handled uniquely because of the level numbers)
        Map<Integer, List<MoveTemplate>> levelUpMoves = learnset.getLevelUpMoves();
        if (!levelUpMoves.isEmpty()) {
            allLines.add(Component.literal("Level-Up Moves").withStyle(ChatFormatting.BOLD));
            levelUpMoves.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        int level = entry.getKey();
                        for (MoveTemplate move : entry.getValue()) {
                            MutableComponent levelComponent = Component.literal("[" + level + "] ")
                                    .withStyle(ChatFormatting.DARK_GRAY);
                            MutableComponent moveComponent = Component.empty()
                                    .append(levelComponent)
                                    .append(formatMove(move));

                            allLines.add(moveComponent);
                        }
                    });
            allLines.add(Component.empty()); // Spacer
        }

        // 2. All other move categories using our new helper method
        addMoveSection(allLines, "Evolution Moves", learnset.getEvolutionMoves());
        addMoveSection(allLines, "Egg Moves", learnset.getEggMoves());
        addMoveSection(allLines, "Tutor Moves", learnset.getTutorMoves());
        addMoveSection(allLines, "TM Moves", learnset.getTmMoves());
        addMoveSection(allLines, "Form Change Moves", learnset.getFormChangeMoves());

        // 3. Pagination Logic
        List<Component> pages = new ArrayList<>();
        for (int i = 0; i < allLines.size(); i += LINES_PER_PAGE) {
            int end = Math.min(i + LINES_PER_PAGE, allLines.size());
            List<Component> pageLines = allLines.subList(i, end);

            MutableComponent page = Component.empty();
            for (Component line : pageLines) {
                page.append(line).append("\n");
            }
            pages.add(page);
        }

        return pages;
    }

    /**
     * Helper method to add standard move lists to prevent code duplication.
     */
    private static void addMoveSection(List<Component> allLines, String header, Collection<MoveTemplate> moves) {
        if (!moves.isEmpty()) {
            allLines.add(Component.literal(header).withStyle(ChatFormatting.BOLD));
            for (MoveTemplate move : moves) {
                allLines.add(Component.literal("- ").append(formatMove(move)));
            }
            allLines.add(Component.empty()); // Spacer
        }
    }

    private static MutableComponent formatMove(MoveTemplate template) {
        MutableComponent hover = Component.literal("Move Info:\n");

        ElementalType elementalType = template.getElementalType();
        Component type = Component.literal("Type: ").append(elementalType.getDisplayName()).append("\n");
        Component category = Component.literal("Category: ").append(template.getDamageCategory().getDisplayName()).append("\n");

        // Only show power if > 0
        double powerValue = template.getPower();
        Component power = powerValue > 0
                ? Component.literal("Power: ").append(Component.literal(String.format("%.0f BP\n", powerValue)))
                : null;

        // Only show accuracy if >= 0
        double accuracyValue = template.getAccuracy();
        Component accuracy = accuracyValue >= 0
                ? Component.literal("Accuracy: ").append(Component.literal(String.format("%.0f%%\n", accuracyValue)))
                : null;

        Component description = Component.literal("\n").append(template.getDescription());

        hover.append(type).append(category);
        if (power != null) hover.append(power);
        if (accuracy != null) hover.append(accuracy);
        hover.append(description);

        boolean isNormal = elementalType.getName().equals(ElementalTypes.NORMAL.getName());
        Style style = Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));

        if (!isNormal) {
            style = style.withColor(elementalType.getHue());
        }

        return template.getDisplayName().withStyle(style);
    }
}