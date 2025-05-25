package net.ajsdev.cobblemonbookwiki.book.page;

import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.abilities.CommonAbilityType;
import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbilityType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class OverviewPage {

    public static MutableComponent build(FormData formData, Species species, String fullName) {
        MutableComponent page = Component.empty();

        page.append(Component.literal(String.format("─ %04d ─\n\n", species.getNationalPokedexNumber())).withStyle(ChatFormatting.BOLD));
        page.append(formatName(species, fullName));
        page.append(formatTypes(formData));
        page.append(formatBaseStats(formData));
        page.append(formatTraining(formData));
        // page.append(formatBreeding(formData, species));
        page.append(formatAbilities(formData));


        return page;
    }

    private static MutableComponent formatName(Species species, String fullName) {
        MutableComponent name = Component.literal(String.format("%s\n", fullName)).withStyle(ChatFormatting.BOLD);
        if (!species.getImplemented()) name.setStyle(
                Style.EMPTY.applyFormats(ChatFormatting.RED, ChatFormatting.BOLD)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Not Implemented!"))));
        return name;
    }

    private static MutableComponent formatTypes(FormData formData) {
        MutableComponent types = Component.empty();
        boolean first = true;
        for (ElementalType type : formData.getTypes()) {
            if (first) first = false;
            else types.append(Component.literal(", "));
            types.append(type.getDisplayName().copy().withColor(type.getHue()));
        }
        types.append("\n\n");
        return types;
    }

    private static MutableComponent formatBaseStats(FormData formData) {
        Map<Stat, Integer> baseStats = formData.getBaseStats();
        MutableComponent hover = Component.empty();
        int total = 0;
        for (Map.Entry<Stat, Integer> e : baseStats.entrySet()) {
            Stat stat = e.getKey();
            int value = e.getValue();
            total += value;

            hover.append(stat.getDisplayName());
            hover.append(Component.literal(String.format(": %s\n", value)));
        }
        hover.append(" \n");
        hover.append(Component.literal(String.format("Stat Total: %s", total)));

        return Component.literal("[BASE STATS]\n")
                .withStyle(Style.EMPTY
                        .applyFormats(ChatFormatting.BOLD, ChatFormatting.BLUE)
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                hover
                        ))
                );
    }

    private static MutableComponent formatTraining(FormData formData) {
        MutableComponent hover = Component.empty();
        // EV Yield
        Map<Stat, Integer> evYield = formData.getEvYield();
        hover.append("EV Yield:\n");

        int total = 0;
        for (Map.Entry<Stat, Integer> e : evYield.entrySet()) {
            Stat stat = e.getKey();
            int value = e.getValue();
            total += value;
            if (value > 0) {
                hover.append(Component.literal(String.format("- %s ", value)));
                hover.append(stat.getDisplayName());
                hover.append("\n");
            }

        }
        if (total == 0) hover.append("- None\n");
        hover.append(" \n");


        hover.append(String.format("Catch Rate: %s\n", formData.getCatchRate()));
        hover.append(String.format("Base Friendship: %s\n", formData.getBaseFriendship()));
        hover.append(String.format("Base Exp: %s\n", formData.getBaseExperienceYield()));
        String growthRate = StringUtils.capitalize(formData.getExperienceGroup().getName().replace("_", " "));
        hover.append(String.format("Growth Rate: %s", growthRate));

        return Component.literal("[TRAINING INFO]\n")
                .withStyle(Style.EMPTY
                        .applyFormats(ChatFormatting.BOLD, ChatFormatting.DARK_GREEN)
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                hover
                        ))
                );
    }

    private static MutableComponent formatBreeding(FormData formData, Species species) {
        MutableComponent hover = Component.empty();

        hover.append("Egg Groups:\n");
        for (EggGroup eggGroup : formData.getEggGroups()) {
            hover.append(Component.literal("- "))
                    .append(Component.literal(eggGroup.getShowdownID()))
                    .append(Component.literal("\n"));
        }
        hover.append(" \n");
        hover.append(formatGenderRatio(formData));

        int eggSteps = species.getEggCycles() * 257;
        hover.append(String.format("Egg Cycles: %s (%s steps)", species.getEggCycles(), eggSteps));

        return Component.literal("[BREEDING INFO]\n")
                .withStyle(Style.EMPTY
                        .applyFormats(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE)
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                hover
                        ))
                );
    }

    private static Component formatGenderRatio(FormData formData) {
        MutableComponent genderRatio = Component.empty();
        genderRatio.append("Gender Ratio: ");
        if (formData.getMaleRatio() == -1) {
            return genderRatio.append(Component.literal("Genderless\n")
                    .withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
        }

        int male = (int) (formData.getMaleRatio() * 100);
        int female = 100 - male;

        Component femaleText = Component.literal("♀ " + female + "%").withStyle(ChatFormatting.LIGHT_PURPLE);
        Component maleText = Component.literal("♂ " + male + "%").withStyle(ChatFormatting.BLUE);

        return genderRatio
                .append(femaleText)
                .append(Component.literal(" / "))
                .append(maleText)
                .append("\n");
    }

    private static Component formatAbilities(FormData formData) {
        MutableComponent abilities = Component.empty();
        abilities.append(Component.literal("\nAbilities:\n").withStyle(ChatFormatting.BOLD));

        List<PotentialAbility> potentialAbilities = formData.getAbilities().getMapping().values()
                .stream()
                .flatMap(List::stream)
                .toList();

        List<AbilityTemplate> commonAbilities = potentialAbilities
                .stream()
                .filter((potentialAbility -> potentialAbility.getType() instanceof CommonAbilityType))
                .map(PotentialAbility::getTemplate)
                .toList();

        List<String> commonAbilityNames = commonAbilities
                .stream()
                .map(AbilityTemplate::getName)
                .toList();

        List<AbilityTemplate> hiddenAbilities = potentialAbilities.stream()
                .filter((potentialAbility -> potentialAbility.getType() instanceof HiddenAbilityType))
                .map(PotentialAbility::getTemplate)
                .filter(potentialAbility -> !commonAbilityNames.contains(potentialAbility.getName()))
                .toList();


        for (AbilityTemplate template : commonAbilities) {
            Component name = Component.translatable(template.getDisplayName());
            Component description = Component.translatable(template.getDescription());

            Component entry = name.copy().withStyle(style ->
                    style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, description))
            );

            abilities.append(Component.literal("- ").withStyle())
                    .append(entry)
                    .append(Component.literal("\n"));
        }

        for (AbilityTemplate template : hiddenAbilities) {
            Component name = Component.translatable(template.getDisplayName());
            Component description = Component.translatable(template.getDescription());

            Component entry = name.copy().withStyle(style ->
                    style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, description))
            );

            abilities.append(Component.literal("- "))
                    .append(Component.literal("[HA] ").withStyle(ChatFormatting.RED))
                    .append(entry)
                    .append(Component.literal("\n"));
        }

        return abilities;
    }

}
