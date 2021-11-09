package su.nexmedia.goldenchallenges.nms;

import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityBrewingStand;
import org.bukkit.block.BrewingStand;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.Reflex;

import java.lang.reflect.Method;

public class V1_17_R1 extends ChallengeNMS {

	private static final Method BREWING_H = Reflex.getMethod(TileEntityBrewingStand.class, "h");
	
	@Override
	public boolean canBrew(@NotNull BrewingStand stand) {
		if (BREWING_H == null) return false;
		
		CraftWorld craftWorld = (CraftWorld) stand.getWorld();
		TileEntity tile = craftWorld.getHandle().getTileEntity(new BlockPosition(stand.getX(), stand.getY(), stand.getZ()));
		
		//if (!(tile instanceof TileEntityBrewingStand)) return false;
		//TileEntityBrewingStand te = (TileEntityBrewingStand) tile;
		
		Object val = Reflex.invokeMethod(BREWING_H, tile);
		return val != null && (boolean) val;
	}
}
