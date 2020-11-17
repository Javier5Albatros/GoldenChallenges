package su.nexmedia.goldenchallenges.manager.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.constants.JStrings;
import su.nexmedia.engine.utils.random.Rnd;
import su.nexmedia.goldenchallenges.manager.api.ChallengeConfig.Generator.RewardInfo;
import su.nexmedia.goldenchallenges.manager.type.ChallengeJobType;
import su.nexmedia.goldenchallenges.manager.type.ChallengeType;

public class ChallengeGenerated {

	private String configId;
	private ChallengeType challengeType;
	private ChallengeJobType jobType;
	private String name;
	private int level;
	private Map<String, Double> objectives;
	private Set<String> worlds;
	private Set<String> rewards;
	
	public ChallengeGenerated(@NotNull String configId, @NotNull ChallengeType type, @NotNull ChallengeConfig.Generator generator) {
		this.configId = configId.toLowerCase();
		this.challengeType = type;
		this.jobType = generator.getJobType();
		this.level = Rnd.get(generator.getLevelMin(), generator.getLevelMax());
		this.name = generator.getName(this.level);
		this.objectives = new HashMap<>();
		this.worlds = new HashSet<>();
		this.rewards = new HashSet<>();
		
		double objectivesMin = generator.getMapValue(generator.getObjAmountMin(), this.getLevel(), 1D);
		double objectivesMax = generator.getMapValue(generator.getObjAmountMax(), this.getLevel(), 1D);
		int objectivesAmount = Rnd.get((int) objectivesMin, (int) objectivesMax);
		
		Map.Entry<Integer, List<String>> objectiveEntry = generator.getObjAmountList().floorEntry(this.getLevel());
		List<String> objectivesRaw = objectiveEntry != null ? new ArrayList<>(objectiveEntry.getValue()) : Collections.emptyList();
		
		if (objectivesAmount > objectivesRaw.size()) {
			objectivesAmount = objectivesRaw.size();
		}
		if (objectivesAmount <= 0) {
			throw new IllegalStateException("Could not generate a challenge: Objectives list is empty!");
		}
		
		for (int objCount = 0; objCount < objectivesAmount; objCount++) {
			String objectiveId = objectivesRaw.remove(Rnd.get(objectivesRaw.size()));
			
			TreeMap<Integer, Double> mapProgMin = generator.getObjProgMin().getOrDefault(objectiveId, generator.getObjProgMin().getOrDefault(JStrings.DEFAULT, new TreeMap<>()));
			TreeMap<Integer, Double> mapProgMax = generator.getObjProgMax().getOrDefault(objectiveId, generator.getObjProgMax().getOrDefault(JStrings.DEFAULT, new TreeMap<>()));
			
			double progressMin = generator.getMapValue(mapProgMin, this.getLevel(), 0D);
			double progressMax = generator.getMapValue(mapProgMax, this.getLevel(), 0D);
			double progressAmount = Rnd.getDoubleNega(progressMin, progressMax);
			if (progressAmount <= 0D) {
				continue;
			}
			progressAmount = this.getJobType().formatValue(progressAmount);
			
			this.objectives.put(objectiveId, progressAmount);
		}
		
		double worldsMin = generator.getMapValue(generator.getAffectedWorldsMin(), this.getLevel(), 1D);
		double worldsMax = generator.getMapValue(generator.getAffectedWorldsMax(), this.getLevel(), 1D);
		int worldsAmount = Rnd.get((int) worldsMin, (int) worldsMax);
		
		Map.Entry<Integer, List<String>> worldsEntry = generator.getAffectedWorldsList().floorEntry(this.getLevel());
		List<String> worldsRaw = worldsEntry != null ? new ArrayList<>(worldsEntry.getValue()) : Collections.emptyList();
		
		for (int worldCount = 0; worldCount < worldsAmount; worldCount++) {
			if (worldsRaw.isEmpty()) break;
			
			String world = worldsRaw.remove(Rnd.get(worldsRaw.size()));
			if (Bukkit.getWorld(world) != null) {
				this.worlds.add(world);
			}
		}
		
		
		double rewardsMin = generator.getMapValue(generator.getRewardsMin(), this.getLevel(), 0D);
		double rewardsMax = generator.getMapValue(generator.getRewardsMax(), this.getLevel(), 0D);
		int rewardsAmount = Rnd.get((int) rewardsMin, (int) rewardsMax);
		
		Entry<Integer, Map<String, RewardInfo>> rewardsMap = generator.getRewardsList().floorEntry(this.getLevel());
		List<String> rewardsRaw = rewardsMap != null ? new ArrayList<>(rewardsMap.getValue().keySet()) : Collections.emptyList();
		
		for (int rewardCount = 0; rewardCount < rewardsAmount; rewardCount++) {
			if (rewardsRaw.isEmpty()) break;
			
			String rewardId = rewardsRaw.remove(Rnd.get(rewardsRaw.size()));
			this.rewards.add(rewardId);
		}
	}
	
	@NotNull
	public String getConfigId() {
		return configId;
	}
	
	@NotNull
	public ChallengeType getChallengeType() {
		return challengeType;
	}
	
	@NotNull
	public ChallengeJobType getJobType() {
		return jobType;
	}
	
	@NotNull
	public String getName() {
		return name;
	}
	
	public int getLevel() {
		return level;
	}
	
	@NotNull
	public Map<String, Double> getObjectives() {
		return objectives;
	}
	
	public boolean hasObjective(@NotNull String id) {
		return this.objectives.containsKey(id.toUpperCase()) || this.objectives.containsKey(JStrings.MASK_ANY);
	}
	
	public double getObjectiveValue(@NotNull String id) {
		return this.objectives.getOrDefault(id.toUpperCase(), this.objectives.getOrDefault(JStrings.MASK_ANY, 0D));
	}
	
	@NotNull
	public Set<String> getWorlds() {
		return worlds;
	}
	
	@NotNull
	public Set<String> getRewards() {
		return rewards;
	}
}
