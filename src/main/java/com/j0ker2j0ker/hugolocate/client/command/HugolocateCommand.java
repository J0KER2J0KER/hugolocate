package com.j0ker2j0ker.hugolocate.client.command;

import com.j0ker2j0ker.hugolocate.client.locate.LocateResult;
import com.j0ker2j0ker.hugolocate.client.locate.MonumentLocator;
import com.j0ker2j0ker.hugolocate.client.locate.RuinedPortalLocator;
import com.j0ker2j0ker.hugolocate.client.locate.ShipwreckLocator;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class HugolocateCommand {

    private static final String[] STRUCTURES = {"shipwreck", "ruinedportal", "monument"};

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("hugolocate")
                    .then(argument("struktur", StringArgumentType.word())
                            .suggests((ctx, builder) -> suggestStructures(builder))
                            .executes(ctx -> run(ctx.getSource(), StringArgumentType.getString(ctx, "struktur"), null, null))
                            .then(argument("x", IntegerArgumentType.integer())
                                    .then(argument("z", IntegerArgumentType.integer())
                                            .executes(ctx -> run(
                                                    ctx.getSource(),
                                                    StringArgumentType.getString(ctx, "struktur"),
                                                    IntegerArgumentType.getInteger(ctx, "x"),
                                                    IntegerArgumentType.getInteger(ctx, "z")
                                            ))
                                    )
                            )
                    )
            );
        });
    }

    private static com.mojang.brigadier.suggestion.Suggestions suggestStructuresBlocking(SuggestionsBuilder builder) {
        for (String s : STRUCTURES) {
            if (s.startsWith(builder.getRemaining().toLowerCase())) {
                builder.suggest(s);
            }
        }
        return builder.build();
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestStructures(SuggestionsBuilder builder) {
        return java.util.concurrent.CompletableFuture.completedFuture(suggestStructuresBlocking(builder));
    }

    private static int run(FabricClientCommandSource source, String struktur, Integer x, Integer z) {
        long playerX = x != null ? x : (long) source.getPosition().x;
        long playerZ = z != null ? z : (long) source.getPosition().z;

        List<LocateResult> results;
        if (struktur.equalsIgnoreCase("shipwreck")) {
            results = ShipwreckLocator.findNearest(playerX, playerZ, 5);
        } else if (struktur.equalsIgnoreCase("ruinedportal")) {
            results = RuinedPortalLocator.findNearest(playerX, playerZ, 5);
        } else if (struktur.equalsIgnoreCase("monument")) {
            results = MonumentLocator.findNearest(playerX, playerZ, 5);
        } else {
            source.sendError(Component.literal("Unbekannte Struktur: " + struktur + " (unterstützt: shipwreck, ruinedportal, monument)"));
            return 0;
        }

        if (results.isEmpty()) {
            source.sendError(Component.literal("Keine Struktur im Suchradius gefunden."));
            return 0;
        }

        for (LocateResult r : results) {
            source.sendFeedback(Component.literal(
                    "X=" + r.x() + ", Z=" + r.z() + " (" + Math.round(r.distance()) + " Blöcke entfernt)"
            ));
        }
        return 1;
    }
}