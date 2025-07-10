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
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MovesPage {
    public static List<Component> build(FormData formData) {
        Learnset learnset = formData.getMoves();
        Map<Integer, List<MoveTemplate>> levelUpMoves = learnset.getLevelUpMoves();
        List<MoveTemplate> eggMoves = learnset.getEggMoves();
        List<MoveTemplate> tutorMoves = learnset.getTutorMoves();
        List<MoveTemplate> tmMoves = learnset.getTmMoves();
        List<MoveTemplate> formChangeMoves = learnset.getFormChangeMoves();
        Set<MoveTemplate> evolutionMoves = learnset.getEvolutionMoves();

        List<Component> allLines = new ArrayList<>();

        // Add header + moves per section
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
            allLines.add(Component.literal("")); // Spacer
        }

        if (!evolutionMoves.isEmpty()) {
            allLines.add(Component.literal("Evolution Moves").withStyle(ChatFormatting.BOLD));
            for (MoveTemplate move : evolutionMoves) {
                allLines.add(Component.literal("- ").append(formatMove(move)));
            }
            allLines.add(Component.literal(""));
        }

        if (!eggMoves.isEmpty()) {
            allLines.add(Component.literal("Egg Moves").withStyle(ChatFormatting.BOLD));
            for (MoveTemplate move : eggMoves) {
                allLines.add(Component.literal("- ").append(formatMove(move)));
            }
            allLines.add(Component.literal(""));
        }

        if (!tutorMoves.isEmpty()) {
            allLines.add(Component.literal("Tutor Moves").withStyle(ChatFormatting.BOLD));
            for (MoveTemplate move : tutorMoves) {
                allLines.add(Component.literal("- ").append(formatMove(move)));
            }
            allLines.add(Component.literal(""));
        }

        if (!tmMoves.isEmpty()) {
            allLines.add(Component.literal("TM Moves").withStyle(ChatFormatting.BOLD));
            for (MoveTemplate move : tmMoves) {
                allLines.add(Component.literal("- ").append(formatMove(move)));
            }
            allLines.add(Component.literal(""));
        }

        if (!formChangeMoves.isEmpty()) {
            allLines.add(Component.literal("Form Change Moves").withStyle(ChatFormatting.BOLD));
            for (MoveTemplate move : formChangeMoves) {
                allLines.add(Component.literal("- ").append(formatMove(move)));
            }
            allLines.add(Component.literal(""));
        }


        List<Component> pages = new ArrayList<>();
        final int LINES_PER_PAGE = 13;
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

        boolean isNormal = elementalType.getName().equals(ElementalTypes.INSTANCE.getNORMAL().getName());
        Style style = Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
        if (!isNormal) {
            style = style.withColor(elementalType.getHue());
        }

        return template.getDisplayName().withStyle(style);
    }


}