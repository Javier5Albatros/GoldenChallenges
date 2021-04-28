package su.nexmedia.goldenchallenges.data;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.data.users.IUserManager;
import su.nexmedia.goldenchallenges.GoldenChallenges;
import su.nexmedia.goldenchallenges.data.object.ChallengeUser;

public class UserManager extends IUserManager<GoldenChallenges, ChallengeUser> {

	public UserManager(@NotNull GoldenChallenges plugin) {
		super(plugin);
	}

	@Override
	@NotNull
	protected ChallengeUser createData(@NotNull Player player) {
		return new ChallengeUser(plugin, player);
	}
}
