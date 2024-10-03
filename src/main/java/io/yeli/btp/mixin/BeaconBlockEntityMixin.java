package io.yeli.btp.mixin;

import io.yeli.btp.BeaconsManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin {
	@Inject(at = @At("HEAD"), method = "tick")
	private static void onTick(World world, BlockPos pos, BlockState state, BeaconBlockEntity beacon, CallbackInfo callbackInfo) {
		if (!world.isClient()) {
			BeaconsManager.INSTANCE.updateBeaconStates(world, pos, beacon);
		}
	}
}