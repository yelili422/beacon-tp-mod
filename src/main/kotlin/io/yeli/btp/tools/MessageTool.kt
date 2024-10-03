package io.yeli.btp.tools

import net.minecraft.text.Text
import net.minecraft.world.World

object MessageTool {
    fun broadcast(
        world: World,
        message: Text,
    ) {
        for (player in world.players) {
            player.sendMessage(message, false)
        }
    }
}
