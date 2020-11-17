package su.nexmedia.goldenchallenges.nms;

import java.lang.reflect.Method;

import org.bukkit.block.BrewingStand;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.jetbrains.annotations.NotNull;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.TileEntity;
import net.minecraft.server.v1_15_R1.TileEntityBrewingStand;
import su.nexmedia.engine.utils.Reflex;

public class V1_15_R1 extends ChallengeNMS {

	private static final Method BREWING_H = Reflex.getMethod(TileEntityBrewingStand.class, "h");
	
	@Override
	public boolean canBrew(@NotNull BrewingStand stand) {
		if (BREWING_H == null) return false;
		
		CraftWorld craftWorld = (CraftWorld) stand.getWorld();
		TileEntity tile = craftWorld.getHandle().getTileEntity(new BlockPosition(stand.getX(), stand.getY(), stand.getZ()));
		
		Object val = Reflex.invokeMethod(BREWING_H, tile);
		return val != null ? (boolean) val : false;
	}
}
