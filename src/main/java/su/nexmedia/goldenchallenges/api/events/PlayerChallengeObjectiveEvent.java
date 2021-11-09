package su.nexmedia.goldenchallenges.api.events;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.goldenchallenges.data.object.ChallengeUser;
import su.nexmedia.goldenchallenges.data.object.ChallengeUserProgress;
import su.nexmedia.goldenchallenges.manager.type.ChallengeType;

public class PlayerChallengeObjectiveEvent extends PlayerChallengeEvent {

	protected String objective;
	protected double amount;
	
	public PlayerChallengeObjectiveEvent(
			@NotNull OfflinePlayer player,
			@NotNull ChallengeType type, 
			@NotNull ChallengeUser user, 
			@NotNull ChallengeUserProgress progress,
			@NotNull String objective,
			double amount) {
		super(player, type, user, progress);
		
		this.objective = objective.toUpperCase();
		this.amount = amount;
	}

	@NotNull
	public String getObjective() {
		return objective;
	}
	
	public double getAmount() {
		return amount;
	}
}
