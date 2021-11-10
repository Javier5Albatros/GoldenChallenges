package su.nexmedia.goldenchallenges.nms;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityBrewingStand;
import org.bukkit.block.BrewingStand;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.Reflex;

import java.lang.reflect.Method;

public class V1_17_R1 extends ChallengeNMS {
	private static final Method BREWING_A = Reflex.getMethod(TileEntityBrewingStand.class, "a", new Class[] { NonNullList.class });

	public boolean canBrew(@NotNull BrewingStand stand) {
		if (BREWING_A == null)
			return true;
		CraftWorld craftWorld = (CraftWorld)stand.getWorld();
		TileEntity tile = craftWorld.getHandle().getTileEntity(new BlockPosition(stand.getX(), stand.getY(), stand.getZ()));
		if (tile == null)
			return false;
		NonNullList<?> list = (NonNullList)Reflex.getFieldValue(tile, "l");
		if (list == null)
			return false;
		Object val = Reflex.invokeMethod(BREWING_A, null, new Object[] { list });
		return (val != null && ((Boolean)val).booleanValue());
	}
}