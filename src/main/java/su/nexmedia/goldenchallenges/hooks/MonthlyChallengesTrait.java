package su.nexmedia.goldenchallenges.hooks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import su.nexmedia.goldenchallenges.api.GoldenChallengesAPI;
import su.nexmedia.goldenchallenges.manager.api.ChallengeSettings;
import su.nexmedia.goldenchallenges.manager.type.ChallengeType;

@TraitName("challenges-monthly")
public class MonthlyChallengesTrait extends Trait {

	public MonthlyChallengesTrait() {
		super("challenges-monthly");
	}
	
	@EventHandler
	public void onClickLeft(NPCRightClickEvent e) {
        if (e.getNPC() == this.getNPC()) {
        	Player p = e.getClicker();
        	ChallengeSettings settings = GoldenChallengesAPI.getChallengeManager().getSettings(ChallengeType.MONTHLY);
        	if (settings == null) return;
        	
        	settings.openGUI(p);
        }
	}
}
