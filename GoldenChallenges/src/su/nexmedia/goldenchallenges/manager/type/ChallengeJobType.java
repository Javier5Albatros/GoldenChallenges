package su.nexmedia.goldenchallenges.manager.type;

import java.util.function.UnaryOperator;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.hooks.external.MythicMobsHK;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.constants.JStrings;
import su.nexmedia.goldenchallenges.GoldenChallenges;

public enum ChallengeJobType {
	
	BLOCK_BREAK(Constants.VALUE_FORMATTER_INT, Constants.OBJECTIVE_FORMATTER_MATERIAL),
	BLOCK_PLACE(Constants.VALUE_FORMATTER_INT, Constants.OBJECTIVE_FORMATTER_MATERIAL),
	ITEM_CONSUME(Constants.VALUE_FORMATTER_INT, Constants.OBJECTIVE_FORMATTER_MATERIAL),
	ITEM_CRAFT(Constants.VALUE_FORMATTER_INT, Constants.OBJECTIVE_FORMATTER_MATERIAL),
	ITEM_COOK(Constants.VALUE_FORMATTER_INT, Constants.OBJECTIVE_FORMATTER_MATERIAL),
	ITEM_ENCHANT(Constants.VALUE_FORMATTER_INT, Constants.OBJECTIVE_FORMATTER_ENCHANT),
	ITEM_DISENCHANT(Constants.VALUE_FORMATTER_INT, Constants.OBJECTIVE_FORMATTER_MATERIAL),
	ITEM_TRADE(Constants.VALUE_FORMATTER_INT, Constants.OBJECTIVE_FORMATTER_MATERIAL),
	ITEM_FISH(Constants.VALUE_FORMATTER_INT, Constants.OBJECTIVE_FORMATTER_MATERIAL),
	ENTITY_KILL(Constants.VALUE_FORMATTER_INT, Constants.OBJECTIVE_FORMATTER_ENTITY),
	ENTITY_KILL_MYTHIC(Constants.VALUE_FORMATTER_INT, Constants.OBJECTIVE_FORMATTER_MYTHIC),
	ENTITY_TAME(Constants.VALUE_FORMATTER_INT, Constants.OBJECTIVE_FORMATTER_ENTITY),
	ENTITY_BREED(Constants.VALUE_FORMATTER_INT, Constants.OBJECTIVE_FORMATTER_ENTITY),
	ENTITY_SHEAR(Constants.VALUE_FORMATTER_INT, Constants.OBJECTIVE_FORMATTER_ENTITY),
	PROJECTILE_LAUNCH(Constants.VALUE_FORMATTER_INT, Constants.OBJECTIVE_FORMATTER_ENTITY),
	POTION_BREW(Constants.VALUE_FORMATTER_INT, Constants.OBJECTIVE_FORMATTER_POTION),
	ANVIL_RENAME(Constants.VALUE_FORMATTER_INT, Constants.OBJECTIVE_FORMATTER_MATERIAL),
	DAMAGE_RECEIVE(Constants.VALUE_FORMATTER_INT, Constants.OBJECTIVE_FORMATTER_DAMAGE_CAUSE),
	DAMAGE_INFLICT(Constants.VALUE_FORMATTER_INT, Constants.OBJECTIVE_FORMATTER_DAMAGE_CAUSE),
	;
	
	private UnaryOperator<Double> valueFormatter;
	private UnaryOperator<String> objectiveNameFormatter;
	
	private ChallengeJobType(
			@NotNull UnaryOperator<Double> valueFormatter,
			@NotNull UnaryOperator<String> objectiveNameFormatter
			) {
		this.valueFormatter = valueFormatter;
		this.objectiveNameFormatter = objectiveNameFormatter;
	}
	
	public double formatValue(double d) {
		return this.valueFormatter.apply(d);
	}
	
	@NotNull
	public String formatObjective(@NotNull String obj) {
		if (obj.equalsIgnoreCase(JStrings.MASK_ANY)) {
			return GoldenChallenges.getInstance().lang().Other_Any.getMsg();
		}
		return this.objectiveNameFormatter.apply(obj);
	}
	
	private static class Constants {
		
		private static final UnaryOperator<Double> VALUE_FORMATTER_INT = (d -> Double.valueOf(((int)(double)d)));
		
		private static final UnaryOperator<String> OBJECTIVE_FORMATTER_MATERIAL = (obj -> {
			Material material = Material.getMaterial(obj.toUpperCase());
			if (material == null) return obj;
			
			return NexEngine.get().lang().getEnum(material);
		});
		
		private static final UnaryOperator<String> OBJECTIVE_FORMATTER_ENTITY = (obj -> {
			EntityType type = CollectionsUT.getEnum(obj, EntityType.class);
			if (type == null) return obj;
			
			return NexEngine.get().lang().getEnum(type);
		});
		
		private static final UnaryOperator<String> OBJECTIVE_FORMATTER_POTION = (obj -> {
			PotionEffectType type = PotionEffectType.getByName(obj.toUpperCase());
			if (type == null) return obj;
			
			return NexEngine.get().lang().getPotionType(type);
		});
		
		private static final UnaryOperator<String> OBJECTIVE_FORMATTER_ENCHANT = (obj -> {
			Enchantment e = Enchantment.getByKey(NamespacedKey.minecraft(obj.toLowerCase()));
			if (e == null) return obj;
			
			return NexEngine.get().lang().getEnchantment(e);
		});
		
		private static final UnaryOperator<String> OBJECTIVE_FORMATTER_MYTHIC = (obj -> {
			MythicMobsHK hk = NexEngine.get().getMythicMobs();
			return hk == null ? obj : hk.getName(obj);
		});
		
		private static final UnaryOperator<String> OBJECTIVE_FORMATTER_DAMAGE_CAUSE = (obj -> {
			DamageCause cause = CollectionsUT.getEnum(obj, DamageCause.class);
			if (cause == null) return obj;
			
			return GoldenChallenges.getInstance().lang().getEnum(cause);
		});
	}
}
