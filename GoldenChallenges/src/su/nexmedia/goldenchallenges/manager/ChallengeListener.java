package su.nexmedia.goldenchallenges.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.hooks.external.MythicMobsHK;
import su.nexmedia.engine.manager.IListener;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.goldenchallenges.GoldenChallenges;
import su.nexmedia.goldenchallenges.api.events.PlayerChallengeCompleteEvent;
import su.nexmedia.goldenchallenges.api.events.PlayerChallengeObjectiveEvent;
import su.nexmedia.goldenchallenges.data.object.ChallengeUserProgress;
import su.nexmedia.goldenchallenges.manager.type.ChallengeJobType;

public class ChallengeListener extends IListener<GoldenChallenges> {

	private ChallengeManager manager;
	
	private static final String META_BREWING_UUID = "CHALLENGES_BREWING";
	private static final String META_BLOCK_PLACED = "CHALLEGNES_USER_BLOCK";
	
	public ChallengeListener(@NotNull GoldenChallenges plugin, @NotNull ChallengeManager manager) {
		super(plugin);
		this.manager = manager;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChallengeObjectiveEvent(PlayerChallengeObjectiveEvent e) {
		Player player = e.getPlayer().getPlayer();
		if (player == null) return;
		
		ChallengeUserProgress progress = e.getProgress();
		String objId = e.getObjective();
		plugin.lang().Challenge_Notify_Objective_Progress
			.replace(progress.replacePlaceholders(objId))
			.send(player, false);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChallengeCompleteEvent(PlayerChallengeCompleteEvent e) {
		Player player = e.getPlayer().getPlayer();
		if (player == null) return;
		
		ChallengeUserProgress progress = e.getProgress();
		plugin.lang().Challenge_Notify_Challenge_Completed
			.replace(progress.replacePlaceholders(""))
			.send(player, false);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChallengeBlockBreak(BlockBreakEvent e) {
		if (e.getBlock().hasMetadata(META_BLOCK_PLACED)) return;
		
		Player p = e.getPlayer();
		this.manager.progressChallenge(p, ChallengeJobType.BLOCK_BREAK, e.getBlock().getType().name(), 1);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChallengeBlockPlace(BlockPlaceEvent e) {
		e.getBlock().setMetadata(META_BLOCK_PLACED, new FixedMetadataValue(plugin, true));
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChallengeItemCraft(CraftItemEvent e) {
		ItemStack item = e.getCurrentItem();
		if (item == null || ItemUT.isAir(item)) return;
		
		// Get recipe items.
		List<ItemStack> mapIng;
		Recipe r = e.getRecipe();
		if (r instanceof ShapedRecipe) {
			mapIng = new ArrayList<>(((ShapedRecipe)r).getIngredientMap().values());
		}
		else if (r instanceof ShapelessRecipe) {
			mapIng = ((ShapelessRecipe)r).getIngredientList();
		}
		else return;
		
		// Store recipe minimal items amount requirement.
		Map<ItemStack, Integer> mapNeed = new HashMap<>();
		mapIng.forEach(itemNeed -> {
			if (ItemUT.isAir(itemNeed)) return;
			ItemStack itemCopy = new ItemStack(itemNeed);
			
			int amountNeed = itemCopy.getAmount();
			itemCopy.setAmount(1);
			mapNeed.merge(itemCopy, amountNeed, Integer::sum);
		});
		
		// Store craft matrix items amount.
		Map<ItemStack, Integer> mapHas = new HashMap<>();
		for (ItemStack itemHas : e.getInventory().getMatrix()) {
			if (ItemUT.isAir(itemHas)) continue;
			ItemStack itemCopy = new ItemStack(itemHas);
			
			int amountHas = itemCopy.getAmount();
			itemCopy.setAmount(1);
			mapHas.merge(itemCopy, amountHas, Integer::sum);
		}
		
		// Get the maximal possible amount of crafted item.
		int lowest = -1;
		for (Map.Entry<ItemStack, Integer> en : mapNeed.entrySet()) {
			int amountCan = (int) ((double) mapHas.getOrDefault(en.getKey(), 0) / (double) en.getValue());
			if (lowest < 0 || amountCan <= lowest) lowest = amountCan;
		}
		
		int amount = e.isShiftClick() ? lowest : 1;
		int space = 0;
		
		// Calculate available inventory space and fine item amount.
		Player player = (Player) e.getWhoClicked();
		for (ItemStack store : player.getInventory().getStorageContents()) {
			if (store == null || ItemUT.isAir(store)) {
				space += item.getMaxStackSize();
			}
			else if (store.isSimilar(item)) {
				space += Math.max(0, (store.getMaxStackSize() - store.getAmount()));
			}
			
		}
		amount = Math.min(amount, space);
		
		// Calculate cursor amount for stackable items.
		ItemStack cursor = e.getCursor();
		if (!e.isShiftClick()) {
			if (cursor != null && !ItemUT.isAir(cursor)) {
				amount--;
				int canAdd = cursor.getMaxStackSize() - cursor.getAmount();
				amount += Math.min(item.getAmount(), canAdd);
			}
		}
		if (amount <= 0) return;
		
		this.manager.progressChallenge(player, ChallengeJobType.ITEM_CRAFT, item.getType().name(), amount);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChallengeItemCook(FurnaceExtractEvent e) {
		OfflinePlayer player = e.getPlayer();
		
		String obj = e.getItemType().name();
		int amount = e.getItemAmount();
		
		this.manager.progressChallenge(player, ChallengeJobType.ITEM_COOK, obj, amount);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChallengeItemConsume(PlayerItemConsumeEvent e) {
		Player player = e.getPlayer();
		ItemStack item = e.getItem();
		
		this.manager.progressChallenge(player, ChallengeJobType.ITEM_CONSUME, item.getType().name(), 1);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChallengeItemEnchant(EnchantItemEvent e) {
		Player player = e.getEnchanter();
		e.getEnchantsToAdd().forEach((en, lvl) -> {
			this.manager.progressChallenge(player, ChallengeJobType.ITEM_ENCHANT, en.getKey().getKey(), 1);
		});
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChallengeItemFish(PlayerFishEvent e) {
		Player player = e.getPlayer();
		Entity caught = e.getCaught();
		if (!(caught instanceof Item)) return;
		
		Item item = (Item) caught;
		ItemStack stack = item.getItemStack();
		
		this.manager.progressChallenge(player, ChallengeJobType.ITEM_FISH, stack.getType().name(), stack.getAmount());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChallengeEntityKill(EntityDeathEvent e) {
		LivingEntity entity = e.getEntity();
		Player killer = entity.getKiller();
		if (killer == null) return;
		
		// Do not count MythicMobs here.
		MythicMobsHK mythicMobsHK = NexPlugin.getEngine().getMythicMobs();
		if (mythicMobsHK != null && mythicMobsHK.isMythicMob(entity)) return;
		
		this.manager.progressChallenge(killer, ChallengeJobType.ENTITY_KILL, entity.getType().name(), 1);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChallengeEntityTame(EntityTameEvent e) {
		Player player = (Player) e.getOwner();
		LivingEntity entity = e.getEntity();
		
		this.manager.progressChallenge(player, ChallengeJobType.ENTITY_TAME, entity.getType().name(), 1);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChallengeEntityBreed(EntityBreedEvent e) {
		LivingEntity bred = e.getBreeder();
		if (!(bred instanceof Player)) return;
		
		Player player = (Player) bred;
		this.manager.progressChallenge(player, ChallengeJobType.ENTITY_BREED, e.getEntityType().name(), 1);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChallengeEntityShear(PlayerShearEntityEvent e) {
		Player player = e.getPlayer();
		Entity entity = e.getEntity();
		
		this.manager.progressChallenge(player, ChallengeJobType.ENTITY_SHEAR, entity.getType().name(), 1);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChallengeDamageReceiveInflict(EntityDamageEvent e) {
		Entity victim = e.getEntity();
		EntityDamageByEntityEvent ede = e instanceof EntityDamageByEntityEvent ? (EntityDamageByEntityEvent) e : null;
		DamageCause cause = e.getCause();
		double damage = e.getDamage();
		
		if (victim instanceof Player) {
			Player playerVictim = (Player) victim;
			this.manager.progressChallenge(playerVictim, ChallengeJobType.DAMAGE_RECEIVE, cause.name(), damage);
		}
		if (ede != null) {
			Entity damager = ede.getDamager();
			if (damager instanceof Player) {
				Player playerDamager = (Player) damager;
				this.manager.progressChallenge(playerDamager, ChallengeJobType.DAMAGE_INFLICT, cause.name(), damage);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChallengeProjectileLaunch(ProjectileLaunchEvent e) {
		Projectile projectile = e.getEntity();
		ProjectileSource source = projectile.getShooter();
		if (!(source instanceof Player)) return;
		
		Player player = (Player) source;
		this.manager.progressChallenge(player, ChallengeJobType.PROJECTILE_LAUNCH, projectile.getType().name(), 1);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChallengePotionBrew(BrewEvent e) {
		BrewerInventory bInventory = e.getContents();
		
		BrewingStand stand = bInventory.getHolder();
		if (stand == null || !stand.hasMetadata(META_BREWING_UUID)) return;
		
		UUID uuid = (UUID) stand.getMetadata(META_BREWING_UUID).get(0).value();
		if (uuid == null) return;
		
		OfflinePlayer player = plugin.getServer().getOfflinePlayer(uuid);
		int[] slots = new int[] {0,1,2};
		
		plugin.getServer().getScheduler().runTask(plugin, () -> {
			for (int slot : slots) {
				ItemStack item = bInventory.getItem(slot);
				if (item == null || ItemUT.isAir(item)) continue;
				
				ItemMeta meta = item.getItemMeta();
				if (!(meta instanceof PotionMeta)) continue;
				
				PotionMeta potionMeta = (PotionMeta) meta;
				PotionType potionType = potionMeta.getBasePotionData().getType();
				PotionEffectType effectType = potionType.getEffectType();
				if (effectType != null) {
					this.manager.progressChallenge(player, ChallengeJobType.POTION_BREW, effectType.getName(), item.getAmount());
				}
				potionMeta.getCustomEffects().forEach(effect -> {
					this.manager.progressChallenge(player, ChallengeJobType.POTION_BREW, effect.getType().getName(), item.getAmount());
				});
			}
		});
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChallengeInventoryHandler(InventoryClickEvent e) {
		Inventory inv = e.getInventory();
		Player player = (Player) e.getWhoClicked();
		
		if (inv.getType() == InventoryType.BREWING) {
			BrewerInventory bInventory = (BrewerInventory) inv;
			
			BrewingStand stand = bInventory.getHolder();
			if (stand == null) return;
			
			boolean canBrew1 = plugin.getChallengeNMS().canBrew(stand);
			if (canBrew1) return;
			
			plugin.getServer().getScheduler().runTask(plugin, () -> {
				boolean canBrew2 = plugin.getChallengeNMS().canBrew(stand);
				if (!canBrew2) return;
				
				stand.setMetadata(META_BREWING_UUID, new FixedMetadataValue(plugin, player.getUniqueId()));
			});
		}
		else if (inv.getType() == InventoryType.ANVIL) {
			if (e.getRawSlot() != 2) return;
			
			AnvilInventory aInventory = (AnvilInventory) inv;
			if (aInventory.getRepairCost() <= 0) return;
			
			ItemStack src = aInventory.getItem(0);
			if (src == null || ItemUT.isAir(src)) return;
			
			ItemStack result = aInventory.getItem(2);
			if (result == null || ItemUT.isAir(result)) return;
			
			String nameSrc = ItemUT.getItemName(src);
			String nameResult = aInventory.getRenameText();
			if (nameResult == null || nameSrc.equalsIgnoreCase(nameResult)) return;
			
			plugin.getServer().getScheduler().runTask(plugin, () -> {
				ItemStack result2 = aInventory.getItem(2);
				if (result2 != null || !ItemUT.isAir(result2)) return;
				
				this.manager.progressChallenge(player, ChallengeJobType.ANVIL_RENAME, result.getType().name(), result.getAmount());
			});
		}
		else if (inv.getType() == InventoryType.MERCHANT) {
			MerchantInventory mInventory = (MerchantInventory) inv;
			
			MerchantRecipe recipe = mInventory.getSelectedRecipe();
			if (recipe == null) return;
			
			ItemStack result = recipe.getResult();
			int uses = recipe.getUses();
			
			plugin.getServer().getScheduler().runTask(plugin, () -> {
				int uses2 = recipe.getUses();
				if (uses2 <= uses) return;
				
				this.manager.progressChallenge(player, ChallengeJobType.ITEM_TRADE, result.getType().name(), result.getAmount());
			});
		}
		else if (inv.getType() == InventoryType.GRINDSTONE) {
			if (e.getRawSlot() != 2) return;
			
			ItemStack result = inv.getItem(2);
			if (result == null || ItemUT.isAir(result)) return;
			
			this.plugin.getServer().getScheduler().runTask(plugin, () -> {
				ItemStack result2 = inv.getItem(2);
				if (result2 != null || !ItemUT.isAir(result2)) return;
				
				this.manager.progressChallenge(player, ChallengeJobType.ITEM_DISENCHANT, result.getType().name(), result.getAmount());
			});
		}
	}
}
