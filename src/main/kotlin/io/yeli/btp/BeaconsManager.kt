package io.yeli.btp

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.yeli.btp.tools.MessageTool
import net.minecraft.block.entity.BeaconBlockEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.io.File
import java.util.concurrent.atomic.AtomicLong

data class BeaconData(
    val id: Long,
    val world: String,
    @JsonDeserialize(using = BlockPosDeserializer::class)
    val position: BlockPos,
    val isActive: Boolean,
)

class BlockPosDeserializer : JsonDeserializer<BlockPos>() {
    override fun deserialize(
        parser: JsonParser?,
        ctxt: DeserializationContext?,
    ): BlockPos? {
        val node = parser?.codec?.readTree<JsonNode>(parser) ?: return null
        val x = node.get("x")?.asInt() ?: return null
        val y = node.get("y")?.asInt() ?: return null
        val z = node.get("z")?.asInt() ?: return null
        return BlockPos(x, y, z)
    }
}

private data class PersistentData(
    val idCounter: Long,
    val beacons: List<BeaconData>,
)

object BeaconsManager {
    private val file = File("beacons.txt")
    private val objectMapper = jacksonObjectMapper()

    private var idCounter = AtomicLong(0)

    private val id2Pos = HashMap<Long, BlockPos>()
    private val beaconStates = HashMap<BlockPos, BeaconData>()

    init {
        if (file.exists()) {
            loadBeacons()
        }
    }

    fun updateBeaconStates(
        world: World,
        pos: BlockPos,
        beacon: BeaconBlockEntity,
    ) {
        println("beacon: ${beacon.pos} ${beacon.cachedState} ${beacon.beamSegments}")

        val isActive = !beacon.beamSegments.isEmpty()
        beaconStates.get(pos)?.let { beaconState ->
            if (beaconState.isActive != isActive) {
                beaconStates.put(pos, beaconState.copy(isActive = isActive))
                saveBeacons()
//                if (isActive) {
//                    MessageTool.broadcast(world, Text.of("New beacon active at ${world.registryKey.value}(${pos.toShortString()})."))
//                }
            }
        } ?: run {
            val id = idCounter.incrementAndGet()
            beaconStates.put(pos, BeaconData(id, world.registryKey.value.toString(), pos, isActive))
            id2Pos.put(id, pos)
        }
    }

    fun printActiveBeacons(): String {
        var sb = StringBuilder()
        sb.append("Beacons: \\id  \\world  \\position\n")
        beaconStates.filter { it.value.isActive }.forEach {
            sb.append("${String.format("%4d", it.value.id)} ${it.value.world} ${it.value.position.toShortString()}\n")
        }
        return sb.toString()
    }

    fun getBeaconById(id: Long): BeaconData? = beaconStates[id2Pos[id]]

    private fun loadBeacons() {
        objectMapper.readValue<PersistentData>(file, PersistentData::class.java)?.let {
            idCounter.set(it.idCounter)
            it.beacons.forEach { beacon ->
                beaconStates.put(beacon.position, beacon)
                id2Pos.put(beacon.id, beacon.position)
            }
        }
    }

    private fun saveBeacons() {
        objectMapper.writeValue(file, PersistentData(idCounter.get(), beaconStates.values.toList()))
    }
}
