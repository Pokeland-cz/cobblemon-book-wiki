package net.ajsdev.cobblemonbookwiki.book.page;

import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.abilities.CommonAbilityType;
import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
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
import java.util.Set;
import java.util.stream.Collectors;

public class OverviewPage {

    public static MutableComponent build(FormData formData, Species species, String fullName) {
        MutableComponent page = Component.empty();

        page.append(Component.literal(String.format("─ %04d ─\n\n", species.getNationalPokedexNumber())).withStyle(ChatFormatting.BOLD));
        page.append(formatName(species, fullName));
        page.append(formatTypes(formData));
        page.append(formatBaseStats(formData));
        page.append(formatTraining(formData));
        page.append(formatBreeding(formData, species));
        page.append(formatAbilities(formData));

        return page;
    }

    private static MutableComponent formatName(Species species, String fullName) {
        MutableComponent name = Component.literal(String.format("%s\n", fullName)).withStyle(ChatFormatting.BOLD);
        if (!species.getImplemented()) {
            name.setStyle(
                    Style.EMPTY.applyFormats(ChatFormatting.RED, ChatFormatting.BOLD)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Not Implemented!")))
            );
        }
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
        MutableComponent hover = Component.literal("Base Stats:\n");

        hover.append(Component.literal(String.format("HP: %s\n", baseStats.get(Stats.HP))));
        hover.append(Component.literal(String.format("Attack: %s\n", baseStats.get(Stats.ATTACK))));
        hover.append(Component.literal(String.format("Defence: %s\n", baseStats.get(Stats.DEFENCE))));
        hover.append(Component.literal(String.format("Special Attack: %s\n", baseStats.get(Stats.SPECIAL_ATTACK))));
        hover.append(Component.literal(String.format("Special Defense: %s\n", baseStats.get(Stats.SPECIAL_DEFENCE))));
        hover.append(Component.literal(String.format("Speed: %s\n", baseStats.get(Stats.SPEED))));

        int total = baseStats.entrySet().stream()
                .filter(entry -> entry.getKey().getType() == Stat.Type.PERMANENT)
                .mapToInt(Map.Entry::getValue)
                .sum();

        hover.append(Component.literal(String.format("\nStat Total: %s", total)));

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
            // Fix: capitalize the raw showdown IDs
            String groupName = StringUtils.capitalize(eggGroup.getShowdownID());
            hover.append(Component.literal("- " + groupName + "\n"));
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

        // Fix: Use floats to prevent dropping precision on 87.5% / 12.5% ratios
        float male = formData.getMaleRatio() * 100f;
        float female = 100f - male;

        // Strip the decimal if it's a whole number
        String maleStr = (male % 1 == 0) ? String.format("%.0f%%", male) : String.format("%.1f%%", male);
        String femaleStr = (female % 1 == 0) ? String.format("%.0f%%", female) : String.format("%.1f%%", female);

        Component femaleText = Component.literal("♀ " + femaleStr).withStyle(ChatFormatting.LIGHT_PURPLE);
        Component maleText = Component.literal("♂ " + maleStr).withStyle(ChatFormatting.BLUE);

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
                .filter(potentialAbility -> potentialAbility.getType() instanceof CommonAbilityType)
                .map(PotentialAbility::getTemplate)
                .toList();

        // Fix: Change to a Set for optimized lookups
        Set<String> commonAbilityNames = commonAbilities
                .stream()
                .map(AbilityTemplate::getName)
                .collect(Collectors.toSet());

        List<AbilityTemplate> hiddenAbilities = potentialAbilities.stream()
                .filter(potentialAbility -> potentialAbility.getType() instanceof HiddenAbilityType)
                .map(PotentialAbility::getTemplate)
                .filter(potentialAbility -> !commonAbilityNames.contains(potentialAbility.getName()))
                .toList();

        for (AbilityTemplate template : commonAbilities) {
            addAbilityEntry(abilities, template, false);
        }

        for (AbilityTemplate template : hiddenAbilities) {
            addAbilityEntry(abilities, template, true);
        }

        return abilities;
    }

    // Fix: Helper method to prevent repeating the hover logic
    private static void addAbilityEntry(MutableComponent abilities, AbilityTemplate template, boolean isHidden) {
        Component name = Component.translatable(template.getDisplayName());
        Component description = Component.translatable(template.getDescription());

        Component entry = name.copy().withStyle(style ->
                style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, description))
        );

        abilities.append(Component.literal("- "));
        if (isHidden) {
            abilities.append(Component.literal("[HA] ").withStyle(ChatFormatting.RED));
        }
        abilities.append(entry).append(Component.literal("\n"));
    }
}