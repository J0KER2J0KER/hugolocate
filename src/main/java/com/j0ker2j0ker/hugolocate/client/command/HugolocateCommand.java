package com.j0ker2j0ker.hugolocate.client.command;

import com.j0ker2j0ker.hugolocate.client.locate.BastionLocator;
import com.j0ker2j0ker.hugolocate.client.locate.FortressLocator;
import com.j0ker2j0ker.hugolocate.client.locate.LocateResult;
import com.j0ker2j0ker.hugolocate.client.locate.MonumentLocator;
import com.j0ker2j0ker.hugolocate.client.locate.RuinedPortalLocator;
import com.j0ker2j0ker.hugolocate.client.locate.ShipwreckLocator;
import com.j0ker2j0ker.hugolocate.client.locate.VillageLocator;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Set;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class HugolocateCommand {

    private static final String[] STRUCTURES = {
            "shipwreck", "ruinedportal", "monument", "village", "bastion", "fortress"
    };

    private static final Set<String> NETHER_STRUCTURES = Set.of("bastion", "fortress");
    private static final Set<String> OVERWORLD_STRUCTURES = Set.of("shipwreck", "ruinedportal", "monument", "village");

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

   private static boolean isInNether(FabricClientCommandSource source) {
        return source.getLevel().dimensionType().hasCeiling();
    }

    private static int run(FabricClientCommandSource source, String struktur, Integer x, Integer z) {
        String key = struktur.toLowerCase();

        if (!List.of(STRUCTURES).contains(key)) {
            source.sendError(Component.literal("Unbekannte Struktur: " + struktur + " (unterstützt: " + String.join(", ", STRUCTURES) + ")"));
            return 0;
        }

        boolean inNether = isInNether(source);
        if (NETHER_STRUCTURES.contains(key) && !inNether) {
            source.sendError(Component.literal(key + " ist eine Nether-Struktur, du bist aber nicht im Nether."));
            return 0;
        }
        if (OVERWORLD_STRUCTURES.contains(key) && inNether) {
            source.sendError(Component.literal(key + " ist eine Overworld-Struktur, du bist aber im Nether."));
            return 0;
        }

        long playerX = x != null ? x : (long) source.getPosition().x;
        long playerZ = z != null ? z : (long) source.getPosition().z;

        List<LocateResult> results = switch (key) {
            case "shipwreck" -> ShipwreckLocator.findNearest(playerX, playerZ, 5);
            case "ruinedportal" -> RuinedPortalLocator.findNearest(playerX, playerZ, 5);
            case "monument" -> MonumentLocator.findNearest(playerX, playerZ, 5);
            case "village" -> VillageLocator.findNearest(playerX, playerZ, 5);
            case "bastion" -> BastionLocator.findNearest(playerX, playerZ, 5);
            case "fortress" -> FortressLocator.findNearest(playerX, playerZ, 5);
            default -> List.of();
        };

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