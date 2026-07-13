package net.ajsdev.cobblemonbookwiki.util;

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.requirement.Requirement;
import com.cobblemon.mod.common.pokemon.requirements.*;
import com.cobblemon.mod.common.registry.BiomeIdentifierCondition;
import com.cobblemon.mod.common.registry.BiomeTagCondition;
import com.cobblemon.mod.common.registry.StructureIdentifierCondition;
import com.cobblemon.mod.common.registry.StructureTagCondition;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EvolutionRequirementUtil {
    public static String getReadableString(Requirement req, RegistryAccess ra) {
        switch (req) {
            case AreaRequirement ar: {
                AABB box = ar.getBox();
                Vec3 min = box.getMinPosition();
                Vec3 max = box.getMaxPosition();
                return String.format("Within area from (%.1f, %.1f, %.1f) to (%.1f, %.1f, %.1f)", min.x, min.y, min.z, max.x, max.y, max.z);
            }
            case AttackDefenceRatioRequirement adr: {
                return switch (adr.getRatio()) {
                    case ATTACK_HIGHER -> "Attack is higher than defence";
                    case DEFENCE_HIGHER -> "Defence is higher than attack";
                    case EQUAL -> "Attack equals defence";
                };
            }
            case BattleCriticalHitsRequirement bchr: {
                return String.format("At least %d critical hits in a single battle", bchr.getAmount());
            }
            case BiomeRequirement br: {
                return buildConditionString("biome", "biomes", extractConditionNames(br.getBiomeCondition()), extractConditionNames(br.getBiomeAnticondition()));
            }
            case BlocksTraveledRequirement btr: {
                return String.format("Traveled at least %d blocks", btr.getAmount());
            }
            case DamageTakenRequirement dtr: {
                return String.format("Taken at least %d damage", dtr.getAmount());
            }
            case DefeatRequirement dr: {
                PokemonProperties target = dr.getTarget();
                int amount = dr.getAmount();
                return String.format("Defeat %d %s%s", amount, target.getOriginalString(), amount == 1 ? "" : "s");
            }
            case FriendshipRequirement fr: {
                return String.format("Friendship at least %d", fr.getAmount());
            }
            case HeldItemRequirement hir: {
                Optional<HolderSet<Item>> cond = hir.getItemCondition().items();
                if (cond.isEmpty()) return "Held Item: Unknown";
                List<String> itemNames = cond.get().stream().map(holder -> holder.value().getName(new ItemStack(holder.value())).getString()).toList();
                return "Held item: [" + String.join(", ", itemNames) + "]";
            }
            case LevelRequirement lr: {
                return (lr.getMaxLevel() == Integer.MAX_VALUE) ? String.format("Reach at least level %d", lr.getMinLevel()) : String.format("Reach level %d to %d", lr.getMinLevel(), lr.getMaxLevel());
            }
            case MoonPhaseRequirement mpr: {
                return String.format("During %s", mpr.getMoonPhase().name().toLowerCase().replace('_', ' '));
            }
            case MoveSetRequirement msr: {
                return String.format("Knows move %s", StringUtils.capitalize(msr.getMove().getName()));
            }
            case MoveTypeRequirement mtr: {
                return String.format("Has a %s-type move", StringUtils.capitalize(mtr.getType().getName().toLowerCase()));
            }
            case PartyMemberRequirement pmr: {
                String name = StringUtils.capitalize(pmr.getTarget().getOriginalString().toLowerCase());
                return pmr.getContains() ? String.format("Party must contain %s", name) : String.format("Party must not contain %s", name);
            }
            case AdvancementRequirement phr: {
                ResourceLocation adv = phr.getRequiredAdvancement();
                return String.format("Completed advancement %s:%s", adv.getNamespace(), StringUtils.capitalize(adv.getPath().replace('/', ' ').replace('_', ' ')));
            }
            case PokemonPropertiesRequirement ppr: {
                return String.format("Properties must match: %s", ppr.getTarget().getOriginalString());
            }
            case PropertyRangeRequirement prr: {
                int min = prr.getRange().getStart();
                int max = prr.getRange().getEndInclusive();
                return (min == max) ? String.format("%s must be %d", prr.getFeature(), min) : String.format("%s must be between %d and %d", prr.getFeature(), min, max);
            }
            case RecoilRequirement rr: {
                return String.format("Accumulated at least %d recoil damage without fainting", rr.getAmount());
            }
            case StatCompareRequirement scr: {
                return String.format("%s must be higher than %s", StringUtils.capitalize(scr.getHighStat().toLowerCase()), StringUtils.capitalize(scr.getLowStat().toLowerCase()));
            }
            case StatEqualRequirement ser: {
                return String.format("%s must equal %s", StringUtils.capitalize(ser.getStatOne().toLowerCase()), StringUtils.capitalize(ser.getStatTwo().toLowerCase()));
            }
            case StructureRequirement sr: {
                return buildConditionString("structure", "structures", extractConditionNames(sr.getStructureCondition()), extractConditionNames(sr.getStructureAnticondition()));
            }
            case TimeRangeRequirement tr: {
                List<String> segments = tr.getRange().getRanges().stream().map(r -> {
                    int startH = ((r.getStart() / 1000) + 6) % 24, startM = (int) (((r.getStart() % 1000) * 60) / 1000.0);
                    int endH = ((r.getEndInclusive() / 1000) + 6) % 24, endM = (int) (((r.getEndInclusive() % 1000) * 60) / 1000.0);
                    return String.format("%02d:%02d–%02d:%02d", startH, startM, endH, endM);
                }).toList();
                return segments.isEmpty() ? "Any time of day" : "Active between " + String.join(", ", segments);
            }
            case UseMoveRequirement umr: {
                return String.format("Use move %s %d times", StringUtils.capitalize(umr.getMove().getName().toLowerCase()), umr.getAmount());
            }
            case WeatherRequirement wr: {
                List<String> parts = new ArrayList<>();
                if (wr.isRaining() != null) parts.add(wr.isRaining() ? "raining" : "not raining");
                if (wr.isThundering() != null) parts.add(wr.isThundering() ? "thundering" : "not thundering");
                return parts.isEmpty() ? "Any weather" : "Weather must be " + String.join(" and ", parts);
            }
            case WorldRequirement wr: {
                ResourceLocation id = wr.getIdentifier();
                String name = StringUtils.capitalize(id.getPath().replace('_', ' ').toLowerCase());
                return "minecraft".equals(id.getNamespace()) ? "In " + name : String.format("In %s:%s", id.getNamespace(), name);
            }
            default: {
                String className = req.getClass().getSimpleName();
                String rawString = req.toString();

                // If it contains an '@', it's a raw memory reference; strip it
                if (rawString.contains("@")) {
                    rawString = rawString.substring(0, rawString.indexOf("@"));
                }
                // Extract just the last part of the class name for readability
                String cleanName = rawString.contains(".") ?
                        rawString.substring(rawString.lastIndexOf(".") + 1) : rawString;

                return "During: " + StringUtils.capitalize(cleanName.replace("Requirement", "").toLowerCase());
            }
        }
    }

    private static List<String> extractConditionNames(RegistryLikeCondition<?> cond) {
        List<String> names = new ArrayList<>();
        if (cond == null) return names;
        if (cond instanceof BiomeTagCondition btc) names.add(formatName(btc.getTag().location().getPath()));
        else if (cond instanceof BiomeIdentifierCondition bic) names.add(formatName(bic.getIdentifier().getPath()));
        else if (cond instanceof StructureTagCondition stc) names.add(formatName(stc.getTag().location().getPath()));
        else if (cond instanceof StructureIdentifierCondition sic) names.add(formatName(sic.getIdentifier().getPath()));
        return names;
    }

    private static String buildConditionString(String sing, String plur, List<String> allow, List<String> deny) {
        if (!allow.isEmpty() && !deny.isEmpty()) return String.format("In %s: %s; not in %s: %s", plur, String.join(", ", allow), plur, String.join(", ", deny));
        if (!allow.isEmpty()) return String.format("In %s: %s", plur, String.join(", ", allow));
        if (!deny.isEmpty()) return String.format("Not in %s: %s", plur, String.join(", ", deny));
        return "Any " + sing;
    }

    private static String formatName(String path) {
        return (path == null) ? "Unknown" : Arrays.stream(path.split("_")).map(s -> StringUtils.capitalize(s.toLowerCase())).collect(Collectors.joining(" "));
    }
}