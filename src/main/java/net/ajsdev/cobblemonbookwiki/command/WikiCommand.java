package net.ajsdev.cobblemonbookwiki.command;

import com.cobblemon.mod.common.command.argument.FormArgumentType;
import com.cobblemon.mod.common.command.argument.SpeciesArgumentType;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Species;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.sgui.api.gui.BookGui;
import net.ajsdev.cobblemonbookwiki.book.WikiBookBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;


public class WikiCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("wiki")
                .executes(WikiCommand::executeWithoutArguments)
                .then(
                        argument("species", SpeciesArgumentType.Companion.species())
                                .executes(WikiCommand::executeWithStandardForm)
                                .then(
                                        argument("form", FormArgumentType.Companion.form())
                                                .executes(WikiCommand::executeWithForm)

                                )
                )
        );
    }

    // Handles: /wiki (without arguments)
    private static int executeWithoutArguments(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        player.sendSystemMessage(Component.literal("Použij /wiki <pokemon> [forma] nebo /webwiki pro naši webovou wiki"));
        return 0;
    }

    // Handles: /wiki <species>
    private static int executeWithStandardForm(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Species species = ctx.getArgument("species", Species.class);
        FormData form = species.getStandardForm();

        return sendWikiBook(player, form);
    }

    // Handles: /wiki <species> <form>
    private static int executeWithForm(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        FormData form = ctx.getArgument("form", FormData.class);
        return sendWikiBook(player, form);
    }


    private static int sendWikiBook(ServerPlayer player, FormData formData) {
        ItemStack book = WikiBookBuilder.build(formData, player.registryAccess());
        BookGui gui = new BookGui(player, book);
        gui.open();
        return 1;
    }
}


