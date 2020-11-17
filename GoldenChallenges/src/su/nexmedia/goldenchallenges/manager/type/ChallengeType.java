package su.nexmedia.goldenchallenges.manager.type;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.constants.JStrings;
import su.nexmedia.goldenchallenges.Perms;

public enum ChallengeType {

	DAILY,
	WEEKLY,
	MONTHLY,
	;
	
	private boolean enabled;
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean hasPermission(@NotNull Player player) {
		return player.hasPermission(Perms.CHALLENGE_TYPE + this.name().toLowerCase())
				|| player.hasPermission(Perms.CHALLENGE_TYPE + JStrings.MASK_ANY);
	}
	
	@NotNull
	public static ChallengeType[] getEnabled() {
		List<ChallengeType> types = new ArrayList<>();
		for (ChallengeType bonusType : ChallengeType.values()) {
			if (bonusType.isEnabled()) {
				types.add(bonusType);
			}
		}
		return types.toArray(new ChallengeType[types.size()]);
	}
	
	public long getNewCycleDate() {
		LocalDateTime dateNow = LocalDateTime.now();
		LocalDateTime dateCycle;
		
		switch (this) {
			case DAILY: {
				dateCycle = dateNow.plusDays(1);
				break;
			}
			case WEEKLY: {
				dateCycle = dateNow.plusDays(7);
				break;
			}
			case MONTHLY: {
				dateCycle = dateNow.withDayOfMonth(1).plusMonths(1);
				break;
			}
			default: {
				return 0L;
			}
		}
		
		Instant instant = dateCycle.withHour(0).withMinute(0).withSecond(0)
					.atZone(ZoneId.systemDefault()).toInstant();
		
		return instant.toEpochMilli();
	}
}
