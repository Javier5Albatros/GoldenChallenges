package su.nexmedia.goldenchallenges.manager.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.LoadableItem;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.eval.Evaluator;
import su.nexmedia.engine.utils.random.Rnd;
import su.nexmedia.goldenchallenges.GoldenChallenges;
import su.nexmedia.goldenchallenges.manager.type.ChallengeJobType;

public class ChallengeConfig extends LoadableItem {

	private String name;
	private ChallengeJobType jobType;
	private ItemStack icon;
	private Generator generator;
	
	public ChallengeConfig(@NotNull GoldenChallenges plugin, @NotNull JYML cfg) {
		super(plugin, cfg);
		
		this.name = StringUT.color(cfg.getString("name", this.getId()));
		this.jobType = CollectionsUT.getEnum(cfg.getString("type", ""), ChallengeJobType.class);
		if (this.jobType == null) {
			throw new IllegalStateException("Could not load '" + getId() + "' challenge config: Invalid challenge type!");
		}
		this.icon = cfg.getItem("icon");
		this.generator = new Generator();
		
		// Notify about invalid rewards and remove them from the generator.
		this.generator.getRewardsList().values().forEach(rewardIds -> {
			rewardIds.removeIf(rewardId -> {
				RewardInfo rewardInfo = plugin.getChallengeManager().getChallengeReward(rewardId);
				if (rewardInfo == null) {
					plugin.warn("Invalid reward id '" + rewardId + "' in '" + this.getId() + "' challenge!");
					return true;
				}
				return false;
			});
		});
	}

	@Override
	protected void save(@NotNull JYML cfg) {
		
	}

	@NotNull
	public String getName() {
		return name;
	}
	
	@NotNull
	public ChallengeJobType getJobType() {
		return jobType;
	}
	
	@NotNull
	public ItemStack getIcon() {
		return new ItemStack(this.icon);
	}
	
	@NotNull
	public Generator getGenerator() {
		return generator;
	}
	
	public class Generator {
		
		private JYML cfg;
		private List<String> names;
		
		private int levelMin;
		private int levelMax;
		
		private TreeMap<Integer, Double> objAmountMin;
		private TreeMap<Integer, Double> objAmountMax;
		private TreeMap<Integer, List<String>> objAmountList;
		
		private Map<String, TreeMap<Integer, Double>> objProgMin;
		private Map<String, TreeMap<Integer, Double>> objProgMax;
		
		private TreeMap<Integer, Double> affectedWorldsMin;
		private TreeMap<Integer, Double> affectedWorldsMax;
		private TreeMap<Integer, List<String>> affectedWorldsList;
		
		private TreeMap<Integer, Double> rewardsMin;
		private TreeMap<Integer, Double> rewardsMax;
		private TreeMap<Integer, List<String>> rewardsList;
		
		Generator() {
			this.cfg = ChallengeConfig.this.getConfig();
			
			String path = "generator.";
			this.names = StringUT.color(cfg.getStringList(path + "names"));
			
			this.levelMin = cfg.getInt(path + "levels.minimum", 1);
			this.levelMax = cfg.getInt(path + "levels.maximum", 1);
			
			this.objAmountMin = new TreeMap<>();
			this.objAmountMax = new TreeMap<>();
			this.objAmountList = new TreeMap<>();
			this.loadMapValues(this.objAmountMin, path + "objectives.amount.minimum");
			this.loadMapValues(this.objAmountMax, path + "objectives.amount.maximum");
			this.cfg.getSection(path + "objectives.amount.list").forEach(sLevel -> {
				int level = StringUT.getInteger(sLevel, -1);
				if (level <= 0) return;
				
				List<String> objectives = cfg.getStringList(path + "objectives.amount.list." + sLevel);
				objectives.replaceAll(line -> line.toUpperCase());
				
				this.objAmountList.put(level, objectives);
			});
			
			
			this.objProgMin = new HashMap<>();
			this.objProgMax = new HashMap<>();
			this.cfg.getSection(path + "objectives.progress").forEach(objId -> {
				TreeMap<Integer, Double> mapMin = this.objProgMin.computeIfAbsent(objId, map -> new TreeMap<>());
				TreeMap<Integer, Double> mapMax = this.objProgMax.computeIfAbsent(objId, map -> new TreeMap<>());
				
				this.loadMapValues(mapMin, path + "objectives.progress." + objId + ".minimum");
				this.loadMapValues(mapMax, path + "objectives.progress." + objId + ".maximum");
			});
			
			
			this.affectedWorldsMin = new TreeMap<>();
			this.affectedWorldsMax = new TreeMap<>();
			this.affectedWorldsList = new TreeMap<>();
			this.loadMapValues(this.affectedWorldsMin, path + "affected-worlds.minimum");
			this.loadMapValues(this.affectedWorldsMax, path + "affected-worlds.maximum");
			this.cfg.getSection(path + "affected-worlds.list").forEach(sLevel -> {
				int level = StringUT.getInteger(sLevel, -1);
				if (level <= 0) return;
				
				List<String> worlds = cfg.getStringList(path + "affected-worlds.list." + sLevel);
				this.affectedWorldsList.put(level, worlds);
			});
			
			
			this.rewardsMin = new TreeMap<>();
			this.rewardsMax = new TreeMap<>();
			this.rewardsList = new TreeMap<>();
			this.loadMapValues(this.rewardsMin, path + "rewards.minimum");
			this.loadMapValues(this.rewardsMax, path + "rewards.maximum");
			this.cfg.getSection(path + "rewards.list").forEach(sLevel -> {
				int level = StringUT.getInteger(sLevel, -1);
				if (level <= 0) return;
				
				List<String> rewardIds = this.cfg.getStringList(path + "rewards.list." + sLevel);
				this.rewardsList.put(level, rewardIds);
			});
		}
		
		@NotNull
		public String getName(int level) {
			String name = Rnd.get(this.getNames());
			if (name == null) name = ChallengeConfig.this.getId();
			
			return ChallengeConfig.this.name
					.replace("%generator-name%", name)
					.replace("%generator-level%", String.valueOf(level));
		}
		
		@NotNull
		public ChallengeJobType getJobType() {
			return jobType;
		}
		
		@NotNull
		public List<String> getNames() {
			return names;
		}
		
		public int getLevelMin() {
			return levelMin;
		}
		
		public int getLevelMax() {
			return levelMax;
		}
		
		@NotNull
		public TreeMap<Integer, Double> getObjAmountMin() {
			return objAmountMin;
		}
		
		@NotNull
		public TreeMap<Integer, Double> getObjAmountMax() {
			return objAmountMax;
		}
		
		@NotNull
		public TreeMap<Integer, List<String>> getObjAmountList() {
			return objAmountList;
		}
		
		@NotNull
		public Map<String, TreeMap<Integer, Double>> getObjProgMin() {
			return objProgMin;
		}
		
		@NotNull
		public Map<String, TreeMap<Integer, Double>> getObjProgMax() {
			return objProgMax;
		}
		
		@NotNull
		public TreeMap<Integer, Double> getAffectedWorldsMin() {
			return affectedWorldsMin;
		}
		
		@NotNull
		public TreeMap<Integer, Double> getAffectedWorldsMax() {
			return affectedWorldsMax;
		}
		
		@NotNull
		public TreeMap<Integer, List<String>> getAffectedWorldsList() {
			return affectedWorldsList;
		}
		
		@NotNull
		public TreeMap<Integer, Double> getRewardsMin() {
			return rewardsMin;
		}
		
		@NotNull
		public TreeMap<Integer, Double> getRewardsMax() {
			return rewardsMax;
		}
		
		@NotNull
		public TreeMap<Integer, List<String>> getRewardsList() {
			return rewardsList;
		}
		
		public final void loadMapValues(@NotNull TreeMap<Integer, Double> map, @NotNull String path2) {
			// Load different values for each challenge level.
			Set<String> lvlKeys = cfg.getSection(path2);
			if (!lvlKeys.isEmpty()) {
				for (String sLvl : lvlKeys) {
					int eLvl = StringUT.getInteger(sLvl, 0);
					if (eLvl < this.getLevelMin() || eLvl > this.getLevelMax()) continue;
					
					String formula = cfg.getString(path2 + "." + sLvl, "0").replace("%level%", sLvl);
					map.put(eLvl, Evaluator.eval(formula, 1));
				}
				return;
			}
			
			// Load the single formula for all challenge levels.
			for (int lvl = this.getLevelMin(); lvl < (this.getLevelMax() + 1); lvl++) {
				String sLvl = String.valueOf(lvl);
				String exChance = cfg.getString(path2, "").replace("%level%", sLvl);
				if (exChance.isEmpty()) continue;
				
				map.put(lvl, Evaluator.eval(exChance, 1));
			}
		}
		
		public final double getMapValue(@NotNull TreeMap<Integer, Double> map, int lvl, double def) {
			Map.Entry<Integer, Double> e = map.floorEntry(lvl);
			return e != null ? e.getValue() : def;
		}
	}
}
