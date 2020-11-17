package su.nexmedia.goldenchallenges.manager;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import su.nexmedia.engine.manager.IListener;
import su.nexmedia.goldenchallenges.GoldenChallenges;
import su.nexmedia.goldenchallenges.manager.type.ChallengeJobType;

public class ChallengeMythicListener extends IListener<GoldenChallenges> {

	private ChallengeManager manager;
	
	public ChallengeMythicListener(@NotNull ChallengeManager manager) {
		super(manager.plugin);
		this.manager = manager;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChallengeEntityKillMythic(MythicMobDeathEvent e) {
		LivingEntity killer = e.getKiller();
		if (killer == null || !(killer instanceof Player)) return;
		
		Player player = (Player) killer;
		String mobId = e.getMob().getType().getInternalName();
		this.manager.progressChallenge(player, ChallengeJobType.ENTITY_KILL_MYTHIC, mobId, 1);
	}
}
