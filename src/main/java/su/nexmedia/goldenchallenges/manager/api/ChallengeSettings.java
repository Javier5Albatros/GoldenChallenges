package su.nexmedia.goldenchallenges.manager.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.core.config.CoreConfig;
import su.nexmedia.engine.manager.LoadableItem;
import su.nexmedia.engine.manager.api.Cleanable;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.JIcon;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.manager.api.task.ITask;
import su.nexmedia.engine.manager.editor.object.IEditorActionsMain.ActionBuilder;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.TimeUT;
import su.nexmedia.engine.utils.actions.ActionCategory;
import su.nexmedia.engine.utils.actions.ActionManipulator;
import su.nexmedia.goldenchallenges.GoldenChallenges;
import su.nexmedia.goldenchallenges.api.GoldenChallengesAPI;
import su.nexmedia.goldenchallenges.data.object.ChallengeUser;
import su.nexmedia.goldenchallenges.data.object.ChallengeUserData;
import su.nexmedia.goldenchallenges.data.object.ChallengeUserProgress;
import su.nexmedia.goldenchallenges.manager.type.ChallengeType;

public class ChallengeSettings extends LoadableItem implements Cleanable {
	
	private ChallengeType type;
	
	private boolean updateAtNewCycle;
	private long updateCustomTime;
	private int challengesAmount;
	
	private Map<String, ChallengeConfig> challenges;
	private ChallengeSettings.GUI gui;
	
	public ChallengeSettings(@NotNull GoldenChallenges plugin, @NotNull JYML cfg, @NotNull JYML cfgGui,
			@NotNull ChallengeType type) {
		super(plugin, cfg);
		this.type = type;
		
		if (!(this.updateAtNewCycle = cfg.getBoolean("settings.cooldown.update-at-new-cycle"))) {
			this.updateCustomTime = cfg.getLong("settings.cooldown.update-custom-time") * 1000L;
		}
		this.challengesAmount = cfg.getInt("settings.challenges-amount", 3);
		this.challenges = new HashMap<>();
		
		List<String> rewardCount = new ArrayList<>();
		for (JYML cfg2 : JYML.loadAll(cfg.getFile().getParentFile().getAbsolutePath() + "/challenges/", true)) {
			
			// ---------------- Update rewards section to new version.
			JYML cfgRewards = new JYML(plugin.getDataFolder() + "/rewards/", "rewards_" + type.name().toLowerCase() + "_backup.yml");
			
			cfg2.getSection("generator.rewards.list").forEach(sLevel -> {
				if (cfg2.getSection("generator.rewards.list." + sLevel).isEmpty()) return;
				
				int level = StringUT.getInteger(sLevel, -1);
				if (level <= 0) return;
				
				List<String> oldRewards = new ArrayList<>();
				
				cfg2.getSection("generator.rewards.list." + sLevel).forEach(rewardId -> {
					oldRewards.add(rewardId);
					
					String path2 = "generator.rewards.list." + sLevel + "." + rewardId + ".";
					List<String> lore = StringUT.color(cfg2.getStringList(path2 + "lore"));
					ActionManipulator manipulator = new ActionManipulator(plugin, cfg2, path2 + "custom-actions");
					
					String rewardNewPath = cfgRewards.contains(rewardId) ? rewardId + rewardCount.size() : rewardId;
					
					cfgRewards.set(rewardNewPath + ".lore", lore);
					
					Map<String, ActionBuilder> actionBuilders = new HashMap<>();
					manipulator.getActions().forEach((id, section) -> {
						ActionBuilder builder = new ActionBuilder(section, id);
						actionBuilders.put(id, builder);
					});
					
					actionBuilders.forEach((actId, builder) -> {
						for (ActionCategory category : ActionCategory.values()) {
							List<String> lines = new ArrayList<>();
							String sub;
							if (category == ActionCategory.TARGETS) sub = "target-selectors";
							else if (category == ActionCategory.CONDITIONS) sub = "conditions.list";
							else if (category == ActionCategory.ACTIONS) sub = "action-executors";
							else continue;
							
							builder.getParametized(category).values().forEach(mapPz -> {
								mapPz.forEach((pz, mapParam) -> {
									String prefix = "[" + pz.getKey() + "] ";
									StringBuilder paramBuilder = new StringBuilder();
									
									mapParam.forEach((param, value) -> {
										if (paramBuilder.length() > 0) paramBuilder.append(" ");
										paramBuilder.append("~" + param + ": " + value + ";");
									});
									
									String line = prefix + paramBuilder.toString();
									lines.add(line);
								});
							});
							
							cfgRewards.set(rewardNewPath + ".custom-actions." + actId + "." + sub, lines);
						}
					});
					
					
					rewardCount.add(rewardId);
				});
				
				cfg2.set("generator.rewards.list." + sLevel, oldRewards);
			});
			
			cfg2.saveChanges();
			cfgRewards.saveChanges();
			// ---------------- Update rewards end
			
			
			try {
				ChallengeConfig challengeConfig = new ChallengeConfig(plugin, cfg2);
				this.challenges.put(challengeConfig.getId(), challengeConfig);
			}
			catch (Exception e) {
				plugin.error("Could not load " + getType().name() + " '" + cfg2.getFile().getName() + "' challenge!");
				e.printStackTrace();
			}
		}
		this.plugin.info("Loaded " + this.challenges.size() + " challenges for " + getType().name() + " type!");
		
		this.gui = new ChallengeSettings.GUI(plugin, cfgGui);
	}

	@Override
	protected void save(@NotNull JYML cfg) {
		
	}
	
	@Override
	public void clear() {
		if (this.gui != null) {
			this.gui.shutdown();
			this.gui = null;
		}
	}

	@NotNull
	public ChallengeType getType() {
		return type;
	}
	
	@NotNull
	public Map<String, ChallengeConfig> getChallengeConfigs() {
		return challenges;
	}
	
	@Nullable
	public ChallengeConfig getChallengeConfig(@NotNull String id) {
		return this.challenges.get(id.toLowerCase());
	}

	public boolean isUpdateAtNewCycle() {
		return updateAtNewCycle;
	}
	
	public long getUpdateCustomTime() {
		return updateCustomTime;
	}
	
	public int getChallengesAmount() {
		return challengesAmount;
	}
	
	public void openGUI(@NotNull Player player) {
		if (!this.getType().hasPermission(player)) {
			plugin.lang().Error_NoPerm.send(player);
			return;
		}
		this.gui.open(player, 1);
	}
	
	public class GUI extends NGUI<GoldenChallenges> {

		private int[] challengesSlots;
		private String formatName;
		private List<String> formatLore;
		private List<String> formatWorlds;
		private List<String> formatRewards;
		
		private Updater updater;
		
		public GUI(@NotNull GoldenChallenges plugin, @NotNull JYML cfg) {
			super(plugin, cfg, "");
			
			this.challengesSlots = cfg.getIntArray("challenges.slots");
			this.formatName = StringUT.color(cfg.getString("challenges.format.challenge.name", "%challenge-name%"));
			this.formatLore = StringUT.color(cfg.getStringList("challenges.format.challenge.lore"));
			this.formatWorlds = StringUT.color(cfg.getStringList("challenges.format.worlds"));
			this.formatRewards = StringUT.color(cfg.getStringList("challenges.format.rewards"));
			
			GuiClick click = (p, type, e) -> {
				if (type == null || !type.getClass().equals(ContentType.class)) return;
				ContentType type2 = (ContentType) type;
				switch (type2) {
					case EXIT: {
						p.closeInventory();
						break;
					}
					case RETURN: {
						plugin.getChallengeManager().getChallengesMainGUI().open(p, 1);
						break;
					}
					default: {
						break;
					}
				}
			};
			
			for (String id : cfg.getSection("content")) {
				GuiItem guiItem = cfg.getGuiItem("content." + id, ContentType.class);
				if (guiItem == null) continue;
				
				if (guiItem.getType() != null) {
					guiItem.setClick(click);
				}
				this.addButton(guiItem);
			}
			
			this.updater = new Updater();
			this.updater.start();
		}

		@Override
		public void shutdown() {
			if (this.updater != null) {
				this.updater.stop();
				this.updater = null;
			}
			super.shutdown();
		}

		@Override
		protected void onCreate(@NotNull Player p, @NotNull Inventory inv, int page) {
			ChallengeSettings settings = plugin.getChallengeManager().getSettings(getType());
			if (settings == null) {
				throw new IllegalStateException("Attempt to open disabled challenges GUI!");
			}
			
			ChallengeUser user = plugin.getUserManager().getOrLoadUser(p);
			if (user == null) return;
			
			ChallengeUserData userData = user.getChallengeData(settings.getType());
			
			user.validateChallenges();
			user.updateChallenges(getType(), false);
			
			long next = userData.getNextChallengeDate();
			
			int count = 0;
			for (ChallengeUserProgress progress : userData.getChallengeProgresses()) {
				ChallengeGenerated generated = progress.getChallengeGenerated();
				ChallengeConfig config = getChallengeConfig(generated.getConfigId());
				if (config == null) {
					plugin.error("Internal error (ex01) while displaying '" + generated.getConfigId() + "' challenge!");
					return;
				}
				
				ItemStack item = config.getIcon();
				ItemMeta meta = item.getItemMeta();
				if (meta == null) return;
				
				meta.setDisplayName(meta.getDisplayName().replace("%name%", generated.getName()));
				String name = this.formatName.replace("%challenge-item-name%", meta.getDisplayName());
				meta.setDisplayName(name);
				
				List<String> lore = meta.getLore();
				if (lore == null) lore = new ArrayList<>();
				
				List<String> lore2 = new ArrayList<>();
				for (String line : this.formatLore) {
					if (line.equalsIgnoreCase("%challenge-item-lore%")) {
						lore2.addAll(lore);
						continue;
					}
					if (line.equalsIgnoreCase("%worlds%")) {
						if (generated.getWorlds().isEmpty()) continue;
						
						for (String line2 : this.formatWorlds) {
							if (line2.contains("%world-name%")) {
								for (String world : generated.getWorlds()) {
									String line3 = line2.replace("%world-name%", CoreConfig.getWorldName(world));
									lore2.add(line3);
								}
								continue;
							}
							lore2.add(line2);
						}
						
						continue;
					}
					if (line.equalsIgnoreCase("%rewards%")) {
						List<RewardInfo> rewards = generated.getRewards().stream()
							.map((rewardId) -> {
								return GoldenChallengesAPI.getChallengeManager().getChallengeReward(rewardId);
							})
							.filter(reward -> reward != null)
							.collect(Collectors.toList());
						if (rewards.isEmpty()) continue;
						
						// Display reward lore.
						for (String line2 : this.formatRewards) {
							if (line2.contains("%reward-lore%")) {
								rewards.forEach(rewardInfo -> {
									rewardInfo.getLore().forEach(rewardLine -> {
										lore2.add(line2.replace("%reward-lore%", rewardLine));
									});
								});
								continue;
							}
							lore2.add(line2);
						}
						continue;
					}
					if (line.contains("%objective-")) {
						generated.getObjectives().forEach((objId, objVal) -> {
							lore2.add(progress.replacePlaceholders(objId).apply(line));
						});
						continue;
					}
					lore2.add(line
						.replace("%challenge-progress-percent%", NumberUT.format(progress.getProgressPercent()))
						.replace("%next%", TimeUT.formatTimeLeft(next == 0 ? System.currentTimeMillis() : next))
					);
				}
				meta.setLore(lore2);
				item.setItemMeta(meta);
				
				JIcon icon = new JIcon(item);
				this.addButton(p, icon, this.challengesSlots[count++]);
			}
		}

		@Override
		protected boolean ignoreNullClick() {
			return true;
		}

		@Override
		protected boolean cancelClick(int slot) {
			return true;
		}

		@Override
		protected boolean cancelPlayerClick() {
			return true;
		}
		
		class Updater extends ITask<GoldenChallenges> {

			public Updater() {
				super(GUI.this.plugin, 1, false);
			}

			@Override
			public void action() {
				reopen();
			}
		}
	}
}
