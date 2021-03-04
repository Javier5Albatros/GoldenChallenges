package su.nexmedia.goldenchallenges.config;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.ILangMsg;
import su.nexmedia.engine.core.config.CoreLang;
import su.nexmedia.goldenchallenges.GoldenChallenges;
import su.nexmedia.goldenchallenges.manager.type.ChallengeJobType;
import su.nexmedia.goldenchallenges.manager.type.ChallengeType;

public class Lang extends CoreLang {

	public Lang(@NotNull GoldenChallenges plugin) {
		super(plugin);
	}

	@Override
	protected void setupEnums() {
		this.setupEnum(ChallengeJobType.class);
		this.setupEnum(ChallengeType.class);
		this.setupEnum(DamageCause.class);
	}

	public ILangMsg Command_Open_Desc = new ILangMsg(this, "Open challenges menu.");
	public ILangMsg Command_Open_Usage = new ILangMsg(this, "[type]");
	
	public ILangMsg Command_Reset_Desc = new ILangMsg(this, "Resets player's challenges.");
	public ILangMsg Command_Reset_Usage = new ILangMsg(this, "<player> [type]");
	public ILangMsg Command_Reset_Done = new ILangMsg(this, "Reset &a%type% &7challenges for &a%player%&7!");
	
	public ILangMsg Challenge_Notify_Objective_Progress = new ILangMsg(this, "{message: ~type: ACTION_BAR;} %challenge-name%: &e%objective-name% &f%objective-progress-current%&7/&f%objective-progress-total%");
	public ILangMsg Challenge_Notify_Challenge_Completed = new ILangMsg(this, "{message: ~type: ACTION_BAR;} %challenge-name%: &a&lCOMPLETED!");
	
	public ILangMsg Other_Any = new ILangMsg(this, "Any");
}
