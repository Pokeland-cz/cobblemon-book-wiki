package net.ajsdev.cobblemonbookwiki.book.page;

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition;
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution;
import com.cobblemon.mod.common.api.pokemon.requirement.Requirement;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.evolution.variants.BlockClickEvolution;
import com.cobblemon.mod.common.pokemon.evolution.variants.ItemInteractionEvolution;
import com.cobblemon.mod.common.pokemon.evolution.variants.LevelUpEvolution;
import com.cobblemon.mod.common.pokemon.evolution.variants.TradeEvolution;
import com.cobblemon.mod.common.registry.BlockIdentifierCondition;
import com.cobblemon.mod.common.registry.BlockTagCondition;
import net.ajsdev.cobblemonbookwiki.book.WikiBookBuilder;
import net.ajsdev.cobblemonbookwiki.util.EvolutionRequirementUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EvolutionPage {

    public static List<MutableComponent> build(FormData formData, RegistryAccess ra) {
        List<MutableComponent> components = new ArrayList<>();
        components.add(Component.literal("Evolutions: \n\n").withStyle(ChatFormatting.BOLD));

        List<Evolution> evolutions = formData.getEvolutions().stream().toList();
        if (evolutions.isEmpty()) {
            components.add(Component.literal("This pokemon does not evolve."));
            return components; // Fast return if empty
        }

        for (Evolution evolution : evolutions) {
            MutableComponent hover = Component.empty();

            switch (evolution) {
                case LevelUpEvolution ignored: {
                    hover.append("Level Up Evolution\n");
                    break;
                }
                case TradeEvolution te: {
                    hover.append("Trade Evolution\n");
                    if (te.getRequiredContext().getSpecies() != null) {
                        Pokemon tradeForPokemon = te.getRequiredContext().create();
                        String tradeForName = WikiBookBuilder.getFullNameString(tradeForPokemon.getForm(),
                                tradeForPokemon.getSpecies());
                        hover.append(String.format("- Trade For: %s\n", tradeForName));
                    }
                    break;
                }
                case BlockClickEvolution bce: {
                    hover.append("Block Click Evolution\n");
                    RegistryLikeCondition<Block> cond = bce.getRequiredContext();
                    String blockString = "Unknown";

                    if (cond instanceof BlockTagCondition btc) {
                        blockString = formatName(btc.getTag().location().getPath());
                    } else if (cond instanceof BlockIdentifierCondition bic) {
                        blockString = formatName(bic.getIdentifier().getPath());
                    }

                    hover.append("- Interact With: ").append(blockString).append("\n");
                    break;
                }

                case ItemInteractionEvolution iie: {
                    hover.append("Use Item Evolution\n");
                    Optional<HolderSet<Item>> items = iie.getRequiredContext().items();

                    if (items.isPresent()) {
                        // Fix: Standard for-loop and added a newline to prevent text smooshing
                        for (var itemHolder : items.get()) {
                            Item item = itemHolder.value();
                            Component itemName = item.getName(new ItemStack(item));
                            hover.append(Component.literal("- Item: ").append(itemName).append("\n"));
                        }
                    } else {
                        hover.append("- Item: Unknown\n");
                    }
                    break;
                }
                default: {
                    hover.append("Unknown?\n");
                }
            }

            hover.append(" \n");
            if (!evolution.getRequirements().isEmpty()) {
                hover.append(Component.literal("Requirements: \n").withStyle(ChatFormatting.BOLD));

                for (Requirement req : evolution.getRequirements()) {
                    hover.append(Component.literal(EvolutionRequirementUtil.getReadableString(req, ra)))
                            .append("\n");
                }
            }

            Pokemon evo = evolution.getResult().create();
            String evoName = WikiBookBuilder.getFullNameString(evo.getForm(), evo.getSpecies());
            String name = evolution.getResult().getSpecies() == null ? "Error" : evoName;

            components.add(
                    Component.literal(String.format("%s\n\n", name))
                            .withStyle(Style.EMPTY
                                    .applyFormats(ChatFormatting.BOLD, ChatFormatting.BLUE)
                                    .withHoverEvent(new HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            hover
                                    ))
                                    .withClickEvent(new ClickEvent(
                                            ClickEvent.Action.RUN_COMMAND,
                                            "/wiki " +
                                                    evo.getSpecies().getName().toLowerCase() + " " +
                                                    evo.getForm().getName().toLowerCase()
                                    ))
                            ));
        }
        return components;
    }

    /**
     * Helper to pretty-print raw path strings (e.g., "mossy_cobblestone" -> "Mossy Cobblestone")
     */
    private static String formatName(String path) {
        if (path == null) return "Unknown";
        return Arrays.stream(path.split("_"))
                .map(s -> StringUtils.capitalize(s.toLowerCase()))
                .collect(Collectors.joining(" "));
    }
}