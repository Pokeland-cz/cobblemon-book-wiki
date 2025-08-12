package net.ajsdev.cobblemonbookwiki.book.page;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools;
import com.cobblemon.mod.common.api.spawning.TimeRange;
import com.cobblemon.mod.common.api.spawning.condition.*;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.registry.*;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.cobblemon.mod.common.util.ResourceLocationExtensionsKt.asIdentifierDefaultingNamespace;
import static java.util.Objects.requireNonNullElse;

public class SpawnDetailPage {
    public static List<Component> build(FormData formData, Species species) {
        List<PokemonSpawnDetail> spawnDetails = getSpawnDetails(species, formData);
        List<Component> pages = new ArrayList<>(spawnDetails.size());
        AtomicInteger pageNo = new AtomicInteger(1);
        spawnDetails.forEach(sd -> handleSpawnDetail(sd, pages, pageNo.getAndIncrement(), spawnDetails.size()));
        return pages;
    }

    private static List<PokemonSpawnDetail> getSpawnDetails(Species species, FormData formData) {
        return CobblemonSpawnPools.WORLD_SPAWN_POOL.getDetails()
                .stream()
                .filter(detail -> detail instanceof PokemonSpawnDetail)
                .map(sd -> (PokemonSpawnDetail) sd)
                .peek(sd -> System.out.println("Checking spawn detail: " + sd.getPokemon().asString(",")))
                .filter(spawnDetail -> {
                    PokemonProperties pp = spawnDetail.getPokemon();
                    if (pp.getSpecies() == null) {
                        System.out.println("Null species for: " + pp.asString(","));
                        return false;
                    }
                    Species resolvedSpecies = PokemonSpecies.INSTANCE.getByIdentifier(asIdentifierDefaultingNamespace(
                            pp.getSpecies(), Cobblemon.MODID));
                    if (species != resolvedSpecies) {
                        System.out.println("Species mismatch: Expected " + species.getName() + ", got " + resolvedSpecies);
                        return false;
                    }
                    Pokemon pokemon = pp.create();
                    boolean matches = pokemon.getSpecies().getName().equals(species.getName()) &&
                            pokemon.getForm().getName().equals(formData.getName());
                    if (!matches) {
                        System.out.println("Form mismatch: Expected " + formData.getName() + ", got " + pokemon.getForm().getName());
                    }
                    return matches;
                })
                .toList();
    }


    private static void handleSpawnDetail(PokemonSpawnDetail sd, List<Component> pages, int pageNo, int pageTotal) {
        MutableComponent page = Component.empty();

        page.append(Component.literal(String.format("Spawn Detail %s/%s:\n\n", pageNo, pageTotal))
                .withStyle(ChatFormatting.BOLD));


        Component weightComponent = Component.literal(String.format("(%s)\n", sd.getWeight()))
                .withStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.literal("Weight")
                        ))
                );
        Component bucketComponent = Component.literal(String.format("%s ",
                StringUtils.capitalize(sd.getBucket().getName()))).append(weightComponent);
        page.append(bucketComponent);

        if (sd.getLevelRange() != null) {
            String levelRange = String.format("%s - %s", sd.getLevelRange().getFirst(), sd.getLevelRange().getEndInclusive());
            page.append(Component.literal(String.format("Level %s\n", levelRange)));
        }
        page.append(Component.literal(String.format("%s \n\n", StringUtils.capitalize(sd.getContext().getName()))));

        MutableComponent condHover = Component.empty();
        condHover.append("Conditions:\n");
        if (sd.getConditions().isEmpty()) condHover.append("- None\n");
        else sd.getConditions().forEach(c -> handleCondition(condHover, c));

        page.append(
                Component.literal("[CONDITIONS]\n\n")
                        .withStyle(Style.EMPTY
                                .applyFormats(ChatFormatting.BOLD, ChatFormatting.DARK_GREEN)
                                .withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        condHover
                                ))
                        ));


        MutableComponent antiCondHover = Component.empty();
        antiCondHover.append("Anti Conditions:\n");
        if (sd.getAnticonditions().isEmpty()) antiCondHover.append("- None\n");
        else sd.getAnticonditions().forEach(c -> handleCondition(antiCondHover, c));
        page.append(
                Component.literal("[ANTI-CONDITIONS]")
                        .withStyle(Style.EMPTY
                                .applyFormats(ChatFormatting.BOLD, ChatFormatting.DARK_RED)
                                .withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        antiCondHover
                                ))
                        ));
        pages.add(page);

    }


    private static void handleCondition(MutableComponent hover, SpawningCondition<?> cond) {
        if (cond.isRaining() != null)
            hover.append(String.format("Is raining: %s\n", cond.isRaining() ? "Yes" : "No"));
        if (cond.isThundering() != null)
            hover.append(String.format("Is thundering: %s\n", cond.isThundering() ? "Yes" : "No"));
        if (cond.isSlimeChunk() != null)
            hover.append(String.format("Slime chunk: %s\n", cond.isSlimeChunk() ? "Yes" : "No"));
        if (cond.getMoonPhase() != null)
            hover.append(String.format("Moon phase: %s\n", cond.getMoonPhase()));
        if (cond.getCanSeeSky() != null)
            hover.append(String.format("Can see sky: %s\n", cond.getCanSeeSky() ? "Yes" : "No"));
        if (cond.getMinY() != null)
            hover.append(String.format("Min Y: %s\n", cond.getMinY()));
        if (cond.getMaxY() != null)
            hover.append(String.format("Max Y: %s\n", cond.getMaxY()));

        if (cond.getMinLight() != null || cond.getMaxLight() != null) {
            int min = requireNonNullElse(cond.getMinLight(), 0);
            int max = requireNonNullElse(cond.getMaxLight(), 15);
            hover.append(String.format("Light level range: %d - %d\n", min, max));
        }

        if (cond.getMinSkyLight() != null || cond.getMaxSkyLight() != null) {
            int min = requireNonNullElse(cond.getMinSkyLight(), 0);
            int max = requireNonNullElse(cond.getMaxSkyLight(), 15);
            hover.append(String.format("Sky light level range: %d - %d\n", min, max));
        }

        if (cond instanceof AreaSpawningCondition asCond) {
            if (asCond.getMinHeight() != null)
                hover.append(String.format("Min Height: %s\n", asCond.getMinHeight()));
            if (asCond.getMaxHeight() != null)
                hover.append(String.format("Max Height: %s\n", asCond.getMaxHeight()));
            if (asCond.getNeededNearbyBlocks() != null) {
                String blocks = asCond.getNeededNearbyBlocks().stream().map(rlc -> {
                    if (rlc instanceof BlockIdentifierCondition bic)
                        return bic.getIdentifier().getNamespace();

                    if (rlc instanceof BlockTagCondition btc)
                        return btc.getTag().location().getNamespace();
                    return "unknown";
                }).collect(Collectors.joining(", "));
                hover.append(String.format("Nearby Blocks: %s\n", blocks));
            }
        }

        if (cond instanceof FishingSpawningCondition fCond) {
            if (fCond.getMinLureLevel() != null)
                hover.append(String.format("Min Lure Level: %s\n", fCond.getMinLureLevel()));
            if (fCond.getMaxLureLevel() != null)
                hover.append(String.format("Max Lure Level: %s\n", fCond.getMaxLureLevel()));
            if (fCond.getBait() != null)
                hover.append(String.format("Bait: %s\n", fCond.getBait()));
            if (fCond.getRodType() != null)
                hover.append(String.format("Rod Type: %s\n", fCond.getRodType()));
        }

        if (cond instanceof GroundedSpawningCondition gCond) {
            if (gCond.getNeededBaseBlocks() != null) {
                String blocks = gCond.getNeededBaseBlocks().stream().map(rlc -> {
                    if (rlc instanceof BlockIdentifierCondition bic)
                        return bic.getIdentifier().getPath();

                    if (rlc instanceof BlockTagCondition btc)
                        return btc.getTag().location().getPath();
                    return "unknown";
                }).collect(Collectors.joining(", "));
                hover.append(String.format("Base Blocks: %s\n", blocks));
            }
        }

        if (cond instanceof SubmergedSpawningCondition sCond) {
            if (sCond.getMinDepth() != null)
                hover.append(String.format("Min Depth: %s\n", sCond.getMinDepth()));
            if (sCond.getMaxDepth() != null)
                hover.append(String.format("Max Depth: %s\n", sCond.getMaxDepth()));
            if (sCond.getFluidIsSource() != null)
                hover.append(String.format("Needs Source Block: %s\n", sCond.getFluidIsSource() ? "Yes" : "No"));
            if (sCond.getFluid() != null) {
                String fluidString = "Unknown";
                if (sCond.getFluid() instanceof FluidTagCondition ftc)
                    fluidString = ftc.getTag().location().getPath();
                if (sCond.getFluid() instanceof FluidIdentifierCondition fic)
                    fluidString = fic.getIdentifier().getPath();
                hover.append(String.format("Fluid: %s\n", fluidString));
            }
        }

        TimeRange timeRange = cond.getTimeRange();
        if (timeRange != null) {
            String timeName = "custom";
            for (Map.Entry<String, TimeRange> entry : TimeRange.Companion.getTimeRanges().entrySet()) {
                if (entry.getValue().getRanges().equals(timeRange.getRanges())) {
                    timeName = entry.getKey();
                    break;
                }
            }
            hover.append(String.format("Time: %s\n", timeName));
        }

        Set<RegistryLikeCondition<Biome>> biomeConds = cond.getBiomes();
        if (biomeConds != null && !biomeConds.isEmpty()) {
            hover.append("Biomes:\n");
            for (RegistryLikeCondition<Biome> rlc : biomeConds) {
                ResourceLocation resourceLocation = null;
                if (rlc instanceof BiomeTagCondition) {
                    resourceLocation = ((BiomeTagCondition) rlc).getTag().location();
                }
                if (rlc instanceof BiomeIdentifierCondition) {
                    resourceLocation = ((BiomeIdentifierCondition) rlc).getIdentifier();
                }

                if (resourceLocation != null) {
                    String pretty = Arrays.stream(resourceLocation.getPath().split("_"))
                            .map(s -> StringUtils.capitalize(s.toLowerCase()))
                            .collect(Collectors.joining(" "));

                    hover.append(Component.literal(String.format("- %s\n", pretty)));
                } else {
                    hover.append(Component.literal("- Something went wrong!"));
                }


            }
        }

        if (cond.getStructures() != null && !cond.getStructures().isEmpty()) {
            hover.append("Structures:\n");
            for (Either<ResourceLocation, TagKey<Structure>> either : cond.getStructures()) {
                ResourceLocation resourceLocation = null;

                if (either.left().isPresent())
                    resourceLocation = either.left().get();
                if (either.right().isPresent())
                    resourceLocation = either.right().get().location();

                if (resourceLocation != null) {
                    String pretty = Arrays.stream(resourceLocation.getPath().split("_"))
                            .map(s -> StringUtils.capitalize(s.toLowerCase()))
                            .collect(Collectors.joining(" "));

                    hover.append(Component.literal(String.format("- %s\n", pretty)));
                } else {
                    hover.append(Component.literal("- Something went wrong!"));
                }
            }
        }
    }

}
