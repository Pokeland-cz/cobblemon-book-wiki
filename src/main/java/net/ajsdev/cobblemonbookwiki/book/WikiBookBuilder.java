package net.ajsdev.cobblemonbookwiki.book;

import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Species;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import net.ajsdev.cobblemonbookwiki.book.page.EvolutionPage;
import net.ajsdev.cobblemonbookwiki.book.page.OverviewPage;
import net.ajsdev.cobblemonbookwiki.book.page.SpawnDetailPage;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;

import java.util.List;


public class WikiBookBuilder {

    public static ItemStack build(FormData formData, RegistryAccess ra) {
        Species species = formData.getSpecies();
        String fullName = getFullNameString(formData, species);

        BookElementBuilder builder = new BookElementBuilder()
                .setAuthor("Cobblemon")
                .setTitle("Cobblemon Book Wiki");

        builder.addPage(OverviewPage.build(formData, species, fullName));
        addEvoPages(EvolutionPage.build(formData, ra), builder);
        SpawnDetailPage.build(formData, species).forEach(builder::addPage);
        return builder.asStack();
    }

    private static void addEvoPages(List<MutableComponent> entries, BookElementBuilder builder) {
        int pageSize = 5;
        for (int i = 0; i < entries.size(); i += pageSize) {
            int end = Math.min(i + pageSize, entries.size());
            MutableComponent page = Component.empty();
            for (MutableComponent entry : entries.subList(i, end)) {
                page.append(entry);
            }
            builder.addPage(page);
        }
    }

    public static String getFullNameString(FormData formData, Species species) {
        String formName = null;
        if (!formData.getName().equals("Normal") && !formData.getAspects().isEmpty()) {
            formName = StringUtils.capitalize(formData.getAspects().getFirst());
        }
        String speciesName = StringUtils.capitalize(species.getName());
        return formName != null ?
                String.format("%s %s", formName, speciesName) :
                speciesName;
    }
}
