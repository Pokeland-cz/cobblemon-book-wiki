package net.ajsdev.cobblemonbookwiki.book.page;

import com.cobblemon.mod.common.api.drop.DropEntry;
import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.api.drop.ItemDropEntry;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Species;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class DropsPage {

    private static final int LINES_PER_PAGE = 13;

    public static List<Component> build(FormData formData, Species species) {
        List<Component> allLines = new ArrayList<>();

        DropTable dropTable = formData.getDrops();
        if (dropTable.getEntries().isEmpty()) {
            dropTable = species.getDrops();
        }

        allLines.add(Component.literal("Drops & Held Items").withStyle(ChatFormatting.BOLD));
        allLines.add(Component.empty());

        if (!dropTable.getEntries().isEmpty()) {
            for (DropEntry entry : dropTable.getEntries()) {
                if (entry instanceof ItemDropEntry itemDrop) {
                    float percentage = itemDrop.getPercentage();
                    String percentageStr = String.format("%.0f%%", percentage);

                    Item item = BuiltInRegistries.ITEM.get(itemDrop.getItem());
                    Component itemName = item.getName(item.getDefaultInstance());

                    MutableComponent line = Component.literal("- ")
                            .append(itemName)
                            .append(Component.literal(" (" + percentageStr + ")").withStyle(ChatFormatting.GRAY));

                    allLines.add(line);
                }
            }
        } else {
            allLines.add(Component.literal("This Pokémon drops no items."));
        }

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
}
