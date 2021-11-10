package su.nexmedia.goldenchallenges.data.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.data.users.IAbstractUser;
import su.nexmedia.engine.utils.constants.JStrings;
import su.nexmedia.engine.utils.random.Rnd;
import su.nexmedia.goldenchallenges.GoldenChallenges;
import su.nexmedia.goldenchallenges.api.events.PlayerChallengeCompleteEvent;
import su.nexmedia.goldenchallenges.api.events.PlayerChallengeObjectiveEvent;
import su.nexmedia.goldenchallenges.api.events.custom.PlayerChallengeEvent;
import su.nexmedia.goldenchallenges.manager.api.ChallengeConfig;
import su.nexmedia.goldenchallenges.manager.api.ChallengeGenerated;
import su.nexmedia.goldenchallenges.manager.api.ChallengeSettings;
import su.nexmedia.goldenchallenges.manager.type.ChallengeJobType;
import su.nexmedia.goldenchallenges.manager.type.ChallengeType;

public class ChallengeUser extends IAbstractUser<GoldenChallenges> {

	private Map<ChallengeType, ChallengeUserData> challengeData;
	private Map<ChallengeType, Integer> challengeCount;
	
	public ChallengeUser(@NotNull GoldenChallenges plugin, @NotNull Player player) {
		this(
			plugin, 
			player.getUniqueId(), 
			player.getName(), 
			System.currentTimeMillis(), 
			new HashMap<>(),
			new HashMap<>()
		);
	}

	public ChallengeUser(
			@NotNull GoldenChallenges plugin, 
			@NotNull UUID uuid, 
			@NotNull String name, 
			long lastOnline,
			@NotNull Map<ChallengeType, ChallengeUserData> challengeData,
			@NotNull Map<ChallengeType, Integer> challengeCount
	) {
		super(plugin, uuid, name, lastOnline);
		this.challengeData = challengeData;
		this.challengeCount = challengeCount;
		
		this.validateChallenges();
		this.updateChallenges(false);
	}

	@NotNull
	public Map<ChallengeType, ChallengeUserData> getChallengeData() {
		return challengeData;
	}
	
	@NotNull
	public ChallengeUserData getChallengeData(@NotNull ChallengeType type) {
		return this.challengeData.computeIfAbsent(type, data -> new ChallengeUserData());
	}
	
	@NotNull
	public Map<ChallengeType, Integer> getChallengeCount() {
		return this.challengeCount;
	}
	
	public int getChallengeCount(@NotNull ChallengeType type) {
		return this.challengeCount.computeIfAbsent(type, amount -> 0);
	}
	
	public void addChallengeCount(@NotNull ChallengeType type, int amount) {
		this.challengeCount.put(type, Math.max(0, this.getChallengeCount(type) + amount));
	}
	
	public boolean hasChallenges(@NotNull ChallengeType type) {
		return !this.getChallengeData(type).getChallengeProgresses().isEmpty();
	}
	
	public boolean hasActualChallenges(@NotNull ChallengeType type) {
		return !this.getChallengeData(type).isNewChallengesTime();
	}
	
	public void validateChallenges() {
		for (ChallengeType type : ChallengeType.getEnabled()) {
			ChallengeSettings settings = plugin.getChallengeManager().getSettings(type);
			if (settings == null) continue;
			
			ChallengeUserData userData = this.getChallengeData(type);
			
			// Remove challenges with invalid config id.
			userData.getChallengeProgresses().removeIf(progress -> {
				String configId = progress.getChallengeGenerated().getConfigId();
				ChallengeConfig config = settings.getChallengeConfig(configId);
				return config == null;
			});
			
			int has = userData.getChallengeProgresses().size();
			int min = settings.getChallengesAmount();
			int diff = min - has;
			
			// Remove extra user challenges 
			// if user has stored challenges more than allowed in the settings.
			if (diff < 0) {
				diff = Math.abs(diff);
				while (diff-- > 0) {
					Optional<ChallengeUserProgress> opt = userData.getChallengeProgresses().stream().findFirst();
					if (!opt.isPresent()) continue;
					
					userData.getChallengeProgresses().remove(opt.get());
				}
				return;
			}
			
			// Prepare challenge configs to add missing challenges to fit
			// the setting of challenges amount.
			// Also, do not generate challenges of the same configs if user already have it.
			List<ChallengeConfig> challengeConfigs = new ArrayList<>(settings.getChallengeConfigs().values());
			challengeConfigs.removeIf(cfg -> 
				userData.getChallengeProgresses().stream().anyMatch(progress -> 
					progress.getChallengeGenerated().getConfigId().equalsIgnoreCase(cfg.getId())));
			
			this.addChallenges(type, userData, challengeConfigs, diff);
		}
	}
	
	private void addChallenges(@NotNull ChallengeType type, @NotNull ChallengeUserData userData, @NotNull List<ChallengeConfig> challengeConfigs, int amount) {
		for (int count = 0; count < amount; count++) {
			if (challengeConfigs.isEmpty()) break;
			
			ChallengeConfig config = challengeConfigs.remove(Rnd.get(challengeConfigs.size()));
			ChallengeGenerated generated = new ChallengeGenerated(config.getId(), type, config.getGenerator());
			ChallengeUserProgress progress = new ChallengeUserProgress(generated);
			userData.getChallengeProgresses().add(progress);
		}
	}
	
	public boolean updateChallenges(boolean force) {
		boolean any = false;
		for (ChallengeType type : ChallengeType.getEnabled()) {
			if (this.updateChallenges(type, force)) {
				any = true;
			}
		}
		return any;
	}
	
	public boolean updateChallenges(@NotNull ChallengeType type, boolean force) {
		if (!type.isEnabled()) return false;
		if (!force && this.hasChallenges(type) && this.hasActualChallenges(type)) return false;
		
		ChallengeUserData userData = this.getChallengeData(type);
		userData.getChallengeProgresses().clear();
		
		ChallengeSettings settings = plugin.getChallengeManager().getSettings(type);
		if (settings == null) return false;
		
		long nextChallengeDate = settings.isUpdateAtNewCycle() ? type.getNewCycleDate() : System.currentTimeMillis() + settings.getUpdateCustomTime() * 1000L;
		List<ChallengeConfig> challengeConfigs = new ArrayList<>(settings.getChallengeConfigs().values());
		
		this.addChallenges(type, userData, challengeConfigs, settings.getChallengesAmount());
		userData.setNextChallengeDate(nextChallengeDate);
		
		return true;
	}
	
	public void progressChallenge(@NotNull ChallengeJobType jobType, @NotNull String obj, double amount) {
		OfflinePlayer op = plugin.getServer().getOfflinePlayer(this.getUUID());
		
		for (ChallengeType challengeType : ChallengeType.getEnabled()) {
			ChallengeUserData userData = this.getChallengeData(challengeType);
			userData.addObjectiveProgress(op, jobType, obj, amount).forEach(progress -> {
				if (progress.isCompleted()) {
					su.nexmedia.goldenchallenges.api.events.PlayerChallengeCompleteEvent event = new su.nexmedia.goldenchallenges.api.events.PlayerChallengeCompleteEvent(op, challengeType, this, progress);
					su.nexmedia.goldenchallenges.api.events.custom.PlayerChallengeCompleteEvent customEvent = new su.nexmedia.goldenchallenges.api.events.custom.PlayerChallengeCompleteEvent(op, challengeType, this, progress);
					plugin.getPluginManager().callEvent(event);
					plugin.getPluginManager().callEvent(customEvent);
				}
				else {
					String obj2 = obj;
					if (!progress.getChallengeGenerated().hasObjectiveExact(obj)) obj2 = JStrings.MASK_ANY;

					su.nexmedia.goldenchallenges.api.events.PlayerChallengeObjectiveEvent event = new su.nexmedia.goldenchallenges.api.events.PlayerChallengeObjectiveEvent(op, challengeType, this, progress, obj2, amount);
					su.nexmedia.goldenchallenges.api.events.custom.PlayerChallengeObjectiveEvent customEvent = new su.nexmedia.goldenchallenges.api.events.custom.PlayerChallengeObjectiveEvent(op, challengeType, this, progress, obj2, amount);
					plugin.getPluginManager().callEvent(event);
					plugin.getPluginManager().callEvent(customEvent);
				}
			});
		}
	}
}
