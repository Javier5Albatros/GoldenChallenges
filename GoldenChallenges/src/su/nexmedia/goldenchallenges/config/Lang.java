package su.nexmedia.goldenchallenges.config;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.ILangTemplate;
import su.nexmedia.goldenchallenges.GoldenChallenges;
import su.nexmedia.goldenchallenges.manager.type.ChallengeJobType;
import su.nexmedia.goldenchallenges.manager.type.ChallengeType;

public class Lang extends ILangTemplate {

	public Lang(@NotNull GoldenChallenges plugin) {
		super(plugin);
	}

	@Override
	protected void setupEnums() {
		this.setupEnum(ChallengeJobType.class);
		this.setupEnum(ChallengeType.class);
		this.setupEnum(DamageCause.class);
	}

	public JLangMsg Command_Open_Desc = new JLangMsg("Open challenges menu.");
	public JLangMsg Command_Open_Usage = new JLangMsg("[type]");
	
	public JLangMsg Command_Reset_Desc = new JLangMsg("Resets player's challenges.");
	public JLangMsg Command_Reset_Usage = new JLangMsg("<player> [type]");
	public JLangMsg Command_Reset_Done = new JLangMsg("Reset &a%type% &7challenges for &a%player%&7!");
	
	public JLangMsg Challenge_Notify_Objective_Progress = new JLangMsg("[ACTION_BAR] %challenge-name%: &e%objective-name% &f%objective-progress-current%&7/&f%objective-progress-total%");
	public JLangMsg Challenge_Notify_Challenge_Completed = new JLangMsg("[ACTION_BAR] %challenge-name%: &a&lCOMPLETED!");
	
	public JLangMsg Other_Any = new JLangMsg("Any");
}
