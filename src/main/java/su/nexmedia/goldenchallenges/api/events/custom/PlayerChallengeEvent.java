package su.nexmedia.goldenchallenges.api.events.custom;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.goldenchallenges.data.object.ChallengeUser;
import su.nexmedia.goldenchallenges.data.object.ChallengeUserData;
import su.nexmedia.goldenchallenges.data.object.ChallengeUserProgress;
import su.nexmedia.goldenchallenges.manager.type.ChallengeType;

public class PlayerChallengeEvent extends Event {

    protected OfflinePlayer player;
    protected ChallengeType type;
    protected ChallengeUser user;
    protected ChallengeUserData userData;
    protected double oldProgress;
    protected ChallengeUserProgress newProgress;
    private static final HandlerList HANDLERS = new HandlerList();

    public PlayerChallengeEvent(
            @NotNull OfflinePlayer player,
            @NotNull ChallengeType type,
            @NotNull ChallengeUser user,
            @NotNull double oldProgress,
            @NotNull ChallengeUserProgress newProgress) {
        this.player = player;
        this.type = type;
        this.user = user;
        this.userData = this.user.getChallengeData(this.type);
        this.oldProgress = oldProgress;
        this.newProgress = newProgress;
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
    public ChallengeUserProgress getNewProgress() {
        return newProgress;
    }

    @NotNull
    public double getOldProgress() {
        return oldProgress;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
