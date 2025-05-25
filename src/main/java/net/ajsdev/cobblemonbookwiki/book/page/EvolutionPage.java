package net.ajsdev.cobblemonbookwiki.book.page;

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition;
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution;
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement;
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
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class EvolutionPage {

    public static List<MutableComponent> build(FormData formData, RegistryAccess ra) {
        List<MutableComponent> components = new ArrayList<>();
        components.add(Component.literal("Evolutions: \n\n").withStyle(ChatFormatting.BOLD));

        List<Evolution> evolutions = formData.getEvolutions().stream().toList();
        if (evolutions.isEmpty()) {
            components.add(Component.literal("This pokemon does not evolve."));
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
                    String blockString = "unknown";
                    if (cond instanceof BlockTagCondition btc)
                        blockString = btc.getTag().location().getPath();
                    if (cond instanceof BlockIdentifierCondition bic)
                        blockString = bic.getIdentifier().getPath();
                    MutableComponent bceComponent = Component.literal("- Interact With: " + blockString);
                    hover.append(bceComponent);
                    break;
                }

                case ItemInteractionEvolution iie: {
                    hover.append("Use Item Evolution\n");
                    HolderSet<Item> cond = iie.getRequiredContext().items().get();
                    String itemString = "unknown";

                    // Check if the HolderSet contains a tag or identifier condition
                    if (cond instanceof HolderSet.Named<Item> namedSet) {
                        // Handle tag-based condition
                        ResourceLocation tagLocation = namedSet.key().location();
                        itemString = tagLocation.getPath();
                    } else if (cond instanceof HolderSet.Direct<Item> directSet) {
                        // Handle direct item identifier condition
                        Holder<Item> itemHolder = directSet.getRandomElement(RandomSource.create()).orElse(null);
                        if (itemHolder != null) {
                            Item item = itemHolder.value(); // Extract Item from Holder<Item>
                            ResourceLocation itemLocation = BuiltInRegistries.ITEM.getKey(item);
                            itemString = itemLocation.getPath();
                        }
                    } else {
                        // Fallback for unexpected condition types
                        itemString = "unknown_item_condition";
                    }

                    MutableComponent iieComponent = Component.literal("- Item: " + itemString);
                    hover.append(iieComponent);
                    break;
                }
                default:
                    hover.append("Unknown condition type\n");
                }

            hover.append(" \n");
            if (!evolution.getRequirements().isEmpty()) {
                hover.append(Component.literal("Requirements: \n").withStyle(ChatFormatting.BOLD));

                for (EvolutionRequirement req : evolution.getRequirements()) {
                    hover.append(Component.literal(EvolutionRequirementUtil.getReadableString(req, ra)))
                            .append("\n");

                }
            }

            Pokemon evo = evolution.getResult().create();
            String evoName = WikiBookBuilder.getFullNameString(evo.getForm(), evo.getSpecies());
            String name = evolution.getResult().getSpecies() == null ?
                    "Error"
                    : evoName;

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
}
