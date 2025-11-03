package net.ajsdev.cobblemonbookwiki.util;

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement;
import com.cobblemon.mod.common.api.spawning.TimeRange;
import com.cobblemon.mod.common.api.spawning.condition.MoonPhase;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.pokemon.evolution.requirements.*;
import com.cobblemon.mod.common.registry.ItemIdentifierCondition;
import com.cobblemon.mod.common.registry.ItemTagCondition;
import com.cobblemon.mod.common.registry.StructureIdentifierCondition;
import com.cobblemon.mod.common.registry.StructureTagCondition;
import kotlin.ranges.IntRange;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class EvolutionRequirementUtil {
    public static String getReadableString(EvolutionRequirement req, RegistryAccess ra) {
        switch (req) {
            case AreaRequirement ar: {
                AABB box = ar.getBox();
                Vec3 min = box.getMinPosition();
                Vec3 max = box.getMaxPosition();
                return String.format(
                        "Within area from (%.1f, %.1f, %.1f) to (%.1f, %.1f, %.1f)",
                        min.x, min.y, min.z,
                        max.x, max.y, max.z
                );
            }
            case AttackDefenceRatioRequirement adr: {
                return switch (adr.getRatio()) {
                    case ATTACK_HIGHER -> "Attack is higher than defence";
                    case DEFENCE_HIGHER -> "Defence is higher than attack";
                    case EQUAL -> "Attack equals defence";
                };
            }
            case BattleCriticalHitsRequirement bchr: {
                int amount = bchr.getAmount();
                return String.format(
                        "At least %d critical hits in a single battle",
                        amount
                );
            }
            case BiomeRequirement br: {
                RegistryLikeCondition<Biome> cond = br.getBiomeCondition();
                RegistryLikeCondition<Biome> anti = br.getBiomeAnticondition();
                Registry<Biome> biomeRegistry = ra.registryOrThrow(Registries.BIOME);
                List<String> allowedBiomes = new ArrayList<>();
                List<String> deniedBiomes = new ArrayList<>();

                biomeRegistry.keySet().forEach(rl -> {
                    Biome biome = biomeRegistry.get(rl);
                    if (cond != null && cond.fits(biome, biomeRegistry)) allowedBiomes.add(rl.getPath());
                    if (anti != null && anti.fits(biome, biomeRegistry)) deniedBiomes.add(rl.getPath());
                });


                if (!allowedBiomes.isEmpty() && !deniedBiomes.isEmpty()) {
                    return String.format(
                            "In biomes: %s; not in biomes: %s",
                            String.join(", ", allowedBiomes),
                            String.join(", ", deniedBiomes)
                    );
                } else if (!allowedBiomes.isEmpty()) {
                    return "In biomes: " + String.join(", ", allowedBiomes);
                } else if (!deniedBiomes.isEmpty()) {
                    return "Not in biomes: " + String.join(", ", deniedBiomes);
                } else {
                    return "Any biome";
                }
            }
            case BlocksTraveledRequirement btr: {
                int amount = btr.getAmount();
                return String.format(
                        "Traveled at least %d blocks",
                        amount
                );
            }
            case DamageTakenRequirement dtr: {
                int amount = dtr.getAmount();
                return String.format(
                        "Taken at least %d damage",
                        amount
                );
            }
            case DefeatRequirement dr: {
                PokemonProperties target = dr.getTarget();
                int amount = dr.getAmount();
                return String.format(
                        "Defeat %d %s%s",
                        amount,
                        target.getOriginalString(),
                        amount == 1 ? "" : "s"
                );
            }
            case FriendshipRequirement fr: {
                int amount = fr.getAmount();
                return String.format(
                        "Friendship at least %d",
                        amount
                );
            }
            case HeldItemRequirement hir: {
                HolderSet<Item> cond = hir.getItemCondition().items().get();
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

                return "Held item: " + itemString;
            }
            case LevelRequirement lr: {
                int min = lr.getMinLevel();
                int max = lr.getMaxLevel();
                if (max == Integer.MAX_VALUE) {
                    return String.format(
                            "Reach at least level %d",
                            min
                    );
                } else {
                    return String.format(
                            "Reach level %d to %d",
                            min,
                            max
                    );
                }
            }
            case MoonPhaseRequirement mpr: {
                MoonPhase phase = mpr.getMoonPhase();
                String name = phase.name().toLowerCase().replace('_', ' ');
                return String.format("During %s", name);
            }
            case MoveSetRequirement msr: {
                MoveTemplate mt = msr.getMove();
                String moveName = mt.getName();
                return String.format(
                        "Knows move %s",
                        StringUtils.capitalize(moveName)
                );
            }
            case MoveTypeRequirement mtr: {
                ElementalType type = mtr.getType();
                // e.g. "fire" → "Fire"
                String displayName = StringUtils.capitalize(type.getName().toLowerCase());
                return String.format(
                        "Has a %s-type move",
                        displayName
                );
            }
            case PartyMemberRequirement pmr: {
                PokemonProperties target = pmr.getTarget();
                boolean contains = pmr.getContains();
                String name = StringUtils.capitalize(target.getOriginalString().toLowerCase());
                return contains
                        ? String.format("Party must contain %s", name)
                        : String.format("Party must not contain %s", name);
            }
            case PlayerHasAdvancementRequirement phr: {
                ResourceLocation adv = phr.getRequiredAdvancement();
                if (adv == null) {
                    return "Requires an unknown or missing advancement";
                }
                try {
                    String displayName = StringUtils.capitalize(
                            adv.getPath().replace('/', ' ').replace('_', ' ')
                    );
                    return String.format(
                            "Completed advancement %s:%s",
                            adv.getNamespace(),
                            displayName
                    );
                } catch (Exception e) {
                    return "Requires advancement: " + adv.toString();
                }
            }
            case PokemonPropertiesRequirement ppr: {
                PokemonProperties target = ppr.getTarget();
                return String.format(
                        "Properties must match: %s",
                        target.getOriginalString()
                );
            }
            case PropertyRangeRequirement prr: {
                String featureKey = prr.getFeature();
                IntRange range = prr.getRange();
                int min = range.getStart();
                int max = range.getEndInclusive();
                if (min == max) {
                    return String.format("%s must be %d", featureKey, min);
                } else {
                    return String.format("%s must be between %d and %d", featureKey, min, max);
                }
            }
            case RecoilRequirement rr: {
                int amount = rr.getAmount();
                return String.format(
                        "Accumulated at least %d recoil damage without fainting",
                        amount
                );
            }
            case StatCompareRequirement scr: {
                String high = StringUtils.capitalize(scr.getHighStat().toLowerCase());
                String low = StringUtils.capitalize(scr.getLowStat().toLowerCase());
                return String.format(
                        "%s must be higher than %s",
                        high,
                        low
                );
            }
            case StatEqualRequirement ser: {
                String one = StringUtils.capitalize(ser.getStatOne().toLowerCase());
                String two = StringUtils.capitalize(ser.getStatTwo().toLowerCase());
                return String.format(
                        "%s must equal %s",
                        one,
                        two
                );
            }
            case StructureRequirement sr: {
                RegistryLikeCondition<Structure> cond = sr.getStructureCondition();
                RegistryLikeCondition<Structure> anti = sr.getStructureAnticondition();

                List<String> allowed = new ArrayList<>();
                List<String> denied = new ArrayList<>();


                if (cond instanceof StructureTagCondition stc)
                    allowed.add(stc.getTag().location().getPath());
                if (cond instanceof StructureIdentifierCondition sic)
                    allowed.add(sic.getIdentifier().getPath());
                if (anti instanceof StructureTagCondition stc)
                    denied.add(stc.getTag().location().getPath());
                if (anti instanceof StructureIdentifierCondition sic)
                    denied.add(sic.getIdentifier().getPath());

                if (!allowed.isEmpty() && !denied.isEmpty()) {
                    return String.format(
                            "In structures: %s; not in structures: %s",
                            String.join(", ", allowed),
                            String.join(", ", denied)
                    );
                } else if (!allowed.isEmpty()) {
                    return "In structures: " + String.join(", ", allowed);
                } else if (!denied.isEmpty()) {
                    return "Not in structures: " + String.join(", ", denied);
                } else {
                    return "Any structure";
                }
            }
            case TimeRangeRequirement tr: {
                TimeRange timeRange = tr.getRange();
                List<String> segments = new ArrayList<>();

                // Assume TimeRange inherits a getRanges() method returning List<IntRange>
                for (IntRange r : timeRange.getRanges()) {
                    int startTick = r.getStart();
                    int endTick = r.getEndInclusive();

                    // convert ticks to clock time: 0 ticks = 06:00, 1000 ticks = 1h
                    int startHour = (startTick / 1000 + 6) % 24;
                    int startMin = (int) ((startTick % 1000) * 60 / 1000.0);
                    int endHour = (endTick / 1000 + 6) % 24;
                    int endMin = (int) ((endTick % 1000) * 60 / 1000.0);

                    segments.add(String.format("%02d:%02d–%02d:%02d",
                            startHour, startMin,
                            endHour, endMin));
                }

                if (segments.isEmpty()) {
                    return "Any time of day";
                } else if (segments.size() == 1) {
                    return "Active between " + segments.getFirst();
                } else {
                    // multiple windows, join with commas
                    return "Active between " + String.join(", ", segments);
                }
            }
            case UseMoveRequirement umr: {
                MoveTemplate move = umr.getMove();
                int amount = umr.getAmount();
                String moveName = StringUtils.capitalize(move.getName().toLowerCase());
                return String.format(
                        "Use move %s %d times",
                        moveName,
                        amount
                );
            }
            case WeatherRequirement wr: {
                Boolean raining = wr.isRaining();
                Boolean thundering = wr.isThundering();
                List<String> parts = new ArrayList<>();

                if (raining != null) {
                    parts.add(raining ? "raining" : "not raining");
                }
                if (thundering != null) {
                    parts.add(thundering ? "thundering" : "not thundering");
                }

                if (parts.isEmpty()) {
                    return "Any weather";
                } else {
                    // join multiple conditions with “ and ”
                    return "Weather must be " + String.join(" and ", parts);
                }
            }
            case WorldRequirement wr: {
                ResourceLocation id = wr.getIdentifier();
                String namespace = id.getNamespace();
                // e.g. "the_overworld" -> "The overworld"
                String worldName = StringUtils.capitalize(id.getPath().replace('_', ' ').toLowerCase());
                if ("minecraft".equals(namespace)) {
                    return "In " + worldName;
                } else {
                    return String.format("In %s:%s", namespace, worldName);
                }
            }
            default: {
                return "Unknown Requirement";
            }
        }
    }
}
