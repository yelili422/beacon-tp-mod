package io.yeli.btp

import com.mojang.brigadier.arguments.LongArgumentType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager.*
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import org.slf4j.LoggerFactory

object BeaconTPMod : ModInitializer {
    private val logger = LoggerFactory.getLogger("beacon-tp")

    override fun onInitialize() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                literal("btp")
                    .then(
                        literal("list")
                            .executes { context ->
                                context.source.sendFeedback(Text.of(BeaconsManager.printActiveBeacons()), false)
                                1
                            },
                    ).then(
                        argument("id", LongArgumentType.longArg())
                            .executes { context ->
                                val id = LongArgumentType.getLong(context, "id")
                                BeaconsManager.getBeaconById(id)?.let { beacon ->
                                    val player = context.source.player
                                    val targetWorld =
                                        context.source.server.getWorld(
                                            RegistryKey.of<World>(Registry.WORLD_KEY, Identifier(beacon.world)),
                                        )
                                    player.teleport(
                                        targetWorld,
                                        beacon.position.x.toDouble(),
                                        beacon.position.y.toDouble(),
                                        beacon.position.z.toDouble(),
                                        player.yaw,
                                        player.pitch,
                                    )
                                } ?: run {
                                    context.source.sendError(Text.of("Beacon not found"))
                                    return@executes -1
                                }
                                1
                            },
                    ),
            )
        }
    }
}
