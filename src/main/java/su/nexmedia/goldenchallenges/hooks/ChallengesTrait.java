package su.nexmedia.goldenchallenges.hooks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import su.nexmedia.goldenchallenges.api.GoldenChallengesAPI;

@TraitName("challenges")
public class ChallengesTrait extends Trait {

	public ChallengesTrait() {
		super("challenges");
	}
	
	@EventHandler
	public void onClickLeft(NPCRightClickEvent e) {
        if (e.getNPC() == this.getNPC()) {
        	Player p = e.getClicker();
        	GoldenChallengesAPI.getChallengeManager().getChallengesMainGUI().open(p, 1);
        }
	}
}
