package su.nexmedia.goldenchallenges.config;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.IConfigTemplate;
import su.nexmedia.goldenchallenges.GoldenChallenges;
import su.nexmedia.goldenchallenges.manager.type.ChallengeType;

public class Config extends IConfigTemplate {
	
	public static boolean SETTINGS_OBJECTIVES_KILL_ENTITY_COUNT_SPAWNER;
	
	public Config(@NotNull GoldenChallenges plugin) {
		super(plugin);
	}

	@Override
	protected void load() {
		for (ChallengeType challengeType : ChallengeType.values()) {
			cfg.addMissing("challenge-types." + challengeType.name(), true);
			challengeType.setEnabled(cfg.getBoolean("challenge-types." + challengeType.name()));
		}
		
		String path = "settings.objectives.";
		cfg.addMissing(path + "kill-entity.count-spawner-mobs", false);
		SETTINGS_OBJECTIVES_KILL_ENTITY_COUNT_SPAWNER = cfg.getBoolean(path + "kill-entity.count-spawner-mobs");
	}
}
