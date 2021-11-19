package su.nexmedia.goldenchallenges.api.events.custom;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.goldenchallenges.data.object.ChallengeUser;
import su.nexmedia.goldenchallenges.data.object.ChallengeUserProgress;
import su.nexmedia.goldenchallenges.manager.type.ChallengeType;

public class PlayerChallengeCompleteEvent extends PlayerChallengeEvent {

    public PlayerChallengeCompleteEvent(
            @NotNull OfflinePlayer player,
            @NotNull ChallengeType type,
            @NotNull ChallengeUser user,
            @NotNull double oldProgress,
            @NotNull ChallengeUserProgress newProgress) {
        super(player, type, user, oldProgress, newProgress);
    }
}
