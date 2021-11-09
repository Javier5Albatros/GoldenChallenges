package su.nexmedia.goldenchallenges.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.commands.api.ISubCommand;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.PlayerUT;
import su.nexmedia.goldenchallenges.GoldenChallenges;
import su.nexmedia.goldenchallenges.Perms;
import su.nexmedia.goldenchallenges.data.object.ChallengeUser;
import su.nexmedia.goldenchallenges.manager.type.ChallengeType;

public class ResetCommand extends ISubCommand<GoldenChallenges> {

	public ResetCommand(@NotNull GoldenChallenges plugin) {
		super(plugin, new String[] {"reset"}, Perms.ADMIN);
	}

	@Override
	public boolean playersOnly() {
		return false;
	}

	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Command_Reset_Usage.getMsg();
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_Reset_Desc.getMsg();
	}

	@Override
	@NotNull
	public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
		if (i == 1) {
			return PlayerUT.getPlayerNames();
		}
		if (i == 2) {
			List<String> list = Arrays.asList(ChallengeType.values()).stream().map(ChallengeType::name)
					.collect(Collectors.toList());
			return list;
		}
		return super.getTab(player, i, args);
	}

	@Override
	protected void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (args.length < 2) {
			this.printUsage(sender);
			return;
		}
		
		String pName = args[1];
		ChallengeUser user = plugin.getUserManager().getOrLoadUser(pName, false);
		if (user == null) {
			this.errPlayer(sender);
			return;
		}
		
		String type = args.length >= 3 ? args[2] : null;
		ChallengeType cType = type != null ? CollectionsUT.getEnum(type, ChallengeType.class) : null;
		ChallengeType[] types = cType != null ? new ChallengeType[] {cType} : ChallengeType.values();
		
		for (ChallengeType type2 : types) {
			user.updateChallenges(type2, true);
			
			plugin.lang().Command_Reset_Done
				.replace("%type%", plugin.lang().getEnum(type2))
				.replace("%player%", user.getName())
				.send(sender);
		}
	}
}
