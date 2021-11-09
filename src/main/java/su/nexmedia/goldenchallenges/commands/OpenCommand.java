package su.nexmedia.goldenchallenges.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.commands.api.ISubCommand;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.goldenchallenges.GoldenChallenges;
import su.nexmedia.goldenchallenges.Perms;
import su.nexmedia.goldenchallenges.manager.api.ChallengeSettings;
import su.nexmedia.goldenchallenges.manager.type.ChallengeType;

public class OpenCommand extends ISubCommand<GoldenChallenges> {

	public OpenCommand(@NotNull GoldenChallenges plugin) {
		super(plugin, new String[] {"open"}, Perms.CMD_OPEN);
	}

	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Command_Open_Usage.getMsg();
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_Open_Desc.getMsg();
	}

	@Override
	public boolean playersOnly() {
		return true;
	}

	@Override
	@NotNull
	public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
		if (i == 1) {
			List<String> list = Arrays.asList(ChallengeType.getEnabled())
					.stream().map(ChallengeType::name).collect(Collectors.toList());
			return list;
		}
		return super.getTab(player, i, args);
	}

	@Override
	protected void perform(@NotNull CommandSender sender, @NotNull String label,@NotNull String[] args) {
		Player p = (Player) sender;
		ChallengeType challengeType = args.length >= 2 ? CollectionsUT.getEnum(args[1], ChallengeType.class) : null;
		
		if (challengeType == null || !challengeType.isEnabled()) {
			plugin.getChallengeManager().getChallengesMainGUI().open(p, 1);
		}
		else {
			ChallengeSettings settings = plugin.getChallengeManager().getSettings(challengeType);
			if (settings == null) return;
			
			settings.openGUI(p);
		}
	}
}
