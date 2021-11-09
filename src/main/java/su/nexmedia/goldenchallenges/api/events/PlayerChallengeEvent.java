package su.nexmedia.goldenchallenges.api.events;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.manager.api.event.IEvent;
import su.nexmedia.goldenchallenges.data.object.ChallengeUser;
import su.nexmedia.goldenchallenges.data.object.ChallengeUserData;
import su.nexmedia.goldenchallenges.data.object.ChallengeUserProgress;
import su.nexmedia.goldenchallenges.manager.type.ChallengeType;

public abstract class PlayerChallengeEvent extends IEvent {
	
	protected OfflinePlayer player;
	protected ChallengeType type;
	protected ChallengeUser user;
	protected ChallengeUserData userData;
	protected ChallengeUserProgress progress;

	public PlayerChallengeEvent(
			@NotNull OfflinePlayer player,
			@NotNull ChallengeType type,
			@NotNull ChallengeUser user,
			@NotNull ChallengeUserProgress progress) {
		this.player = player;
		this.type = type;
	    this.user = user;
	    this.userData = this.user.getChallengeData(this.type);
	    this.progress = progress;
	}
	
	@NotNull
	public OfflinePlayer getPlayer() {
		return player;
	}
	
	@NotNull
	public ChallengeType getType() {
		return type;
	}
	
	@NotNull
	public ChallengeUser getUser() {
		return user;
	}
	
	@NotNull
	public ChallengeUserData getUserData() {
		return userData;
	}
	
	@NotNull
	public ChallengeUserProgress getProgress() {
		return progress;
	}
}
