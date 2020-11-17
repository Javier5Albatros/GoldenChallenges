package su.nexmedia.goldenchallenges.manager;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.citizens.CitizensHK;
import su.nexmedia.engine.manager.IListener;
import su.nexmedia.engine.manager.api.Loadable;
import su.nexmedia.goldenchallenges.GoldenChallenges;
import su.nexmedia.goldenchallenges.data.object.ChallengeUser;
import su.nexmedia.goldenchallenges.hooks.ChallengesTrait;
import su.nexmedia.goldenchallenges.hooks.DailyChallengesTrait;
import su.nexmedia.goldenchallenges.hooks.MonthlyChallengesTrait;
import su.nexmedia.goldenchallenges.hooks.WeeklyChallengesTrait;
import su.nexmedia.goldenchallenges.manager.api.ChallengeSettings;
import su.nexmedia.goldenchallenges.manager.gui.ChallengesMainGUI;
import su.nexmedia.goldenchallenges.manager.type.ChallengeJobType;
import su.nexmedia.goldenchallenges.manager.type.ChallengeType;

public class ChallengeManager extends IListener<GoldenChallenges> implements Loadable {

	private Map<ChallengeType, ChallengeSettings> challengeSettings;
	private ChallengesMainGUI challengesMainGUI;
	private ChallengeListener challengeListener;
	private ChallengeMythicListener challengeMythicListener;
	
	public ChallengeManager(@NotNull GoldenChallenges plugin) {
		super(plugin);
	}
	
	@Override
	public void setup() {
		this.challengeSettings = new HashMap<>();
		
		CitizensHK citizensHook = plugin.getCitizens();
		
		for (ChallengeType challengeType : ChallengeType.getEnabled()) {
			String name = challengeType.name().toLowerCase();
			this.plugin.getConfigManager().extractFullPath(plugin.getDataFolder() + "/challenges/" + name);
			
			JYML cfg = JYML.loadOrExtract(plugin, "/challenges/" + name + "/settings.yml");
			JYML cfgGui = JYML.loadOrExtract(plugin, "/challenges/" + name + "/gui.yml");
			
			ChallengeSettings settings = new ChallengeSettings(plugin, cfg, cfgGui, challengeType);
			this.challengeSettings.put(challengeType, settings);
			
			if (citizensHook != null) {
				if (challengeType == ChallengeType.DAILY) {
					citizensHook.registerTrait(plugin, DailyChallengesTrait.class);
				}
				else if (challengeType == ChallengeType.WEEKLY) {
					citizensHook.registerTrait(plugin, WeeklyChallengesTrait.class);
				}
				else if (challengeType == ChallengeType.MONTHLY) {
					citizensHook.registerTrait(plugin, MonthlyChallengesTrait.class);
				}
			}
		}
		
		this.challengesMainGUI = new ChallengesMainGUI(plugin, plugin.cfg().getJYML());
		this.plugin.info("Loaded " + this.challengeSettings.size() + " challenge types!");
		
		if (citizensHook != null) {
			citizensHook.registerTrait(plugin, ChallengesTrait.class);
		}
		if (Hooks.hasPlugin(Hooks.MYTHIC_MOBS)) {
			this.challengeMythicListener = new ChallengeMythicListener(this);
			this.challengeMythicListener.registerListeners();
		}
		
		this.challengeListener = new ChallengeListener(plugin, this);
		this.challengeListener.registerListeners();
	}

	@Override
	public void shutdown() {
		if (this.challengeListener != null) {
			this.challengeListener.unregisterListeners();
			this.challengeListener = null;
		}
		if (this.challengeMythicListener != null) {
			this.challengeMythicListener.unregisterListeners();
			this.challengeMythicListener = null;
		}
		if (this.challengesMainGUI != null) {
			this.challengesMainGUI.shutdown();
			this.challengesMainGUI = null;
		}
		if (this.challengeSettings != null) {
			this.challengeSettings.values().forEach(settings -> settings.clear());
			this.challengeSettings.clear();
			this.challengeSettings = null;
		}
	}

	@NotNull
	public ChallengesMainGUI getChallengesMainGUI() {
		return challengesMainGUI;
	}
	
	@Nullable
	public ChallengeSettings getSettings(@NotNull ChallengeType bonusType) {
		return this.challengeSettings.get(bonusType);
	}
	
	public void progressChallenge(@NotNull OfflinePlayer oplayer, @NotNull ChallengeJobType type, @NotNull String obj, double amount) {
		Player player = oplayer.getPlayer();
		ChallengeUser user = null;
		
		if (player != null) {
			if (player.getGameMode() == GameMode.CREATIVE) return;
			user = plugin.getUserManager().getOrLoadUser(player);
		}
		else {
			user = plugin.getUserManager().getOrLoadUser(oplayer.getUniqueId().toString(), true);
		}
		if (user == null) return;
		
		user.progressChallenge(type, obj, amount);
	}
}
