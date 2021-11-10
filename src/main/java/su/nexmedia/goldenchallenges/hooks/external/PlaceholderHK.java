package su.nexmedia.goldenchallenges.hooks.external;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.goldenchallenges.GoldenChallenges;
import su.nexmedia.goldenchallenges.data.object.ChallengeUser;
import su.nexmedia.goldenchallenges.data.object.ChallengeUserData;
import su.nexmedia.goldenchallenges.data.object.ChallengeUserProgress;
import su.nexmedia.goldenchallenges.manager.type.ChallengeType;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderHK extends NHook<GoldenChallenges> {
	
	private Expansion expansion;
	
	public PlaceholderHK(@NotNull GoldenChallenges plugin) {
		super(plugin);
	}

	@Override
	@NotNull
	protected HookState setup() {
		(this.expansion = new Expansion()).register();
		return HookState.SUCCESS;
	}

	@Override
	protected void shutdown() {
		if (this.expansion != null) {
			this.expansion.unregister();
			this.expansion = null;
		}
	}

	class Expansion extends PlaceholderExpansion {

		@Override
		@NotNull
		public String getAuthor() {
			return plugin.getAuthor();
		}

		@Override
		@NotNull
		public String getIdentifier() {
			return plugin.getNameRaw();
		}

		@Override
		@NotNull
		public String getVersion() {
			return plugin.getDescription().getVersion();
		}

		@Override
		public String onPlaceholderRequest(Player player, @NotNull String params) {
			if (player == null) return null;
			
			if (params.startsWith("progress_")) {
				String type = params.replace("progress_", "");
				ChallengeType cType = CollectionsUT.getEnum(type, ChallengeType.class);
				if (cType == null) return null;
				
				ChallengeUser user = plugin.getUserManager().getOrLoadUser(player);
				return user == null ? "N/A" : NumberUT.format(user.getChallengeData(cType).getProgressPercent());
			}
			if (params.startsWith("completed_")) {
				String type = params.replace("completed_", "");
				ChallengeType cType = CollectionsUT.getEnum(type, ChallengeType.class);
				if (cType == null) return null;
				
				ChallengeUser user = plugin.getUserManager().getOrLoadUser(player);
				return user == null ? "N/A" : NumberUT.format(user.getChallengeCount(cType));
			}

			if(params.startsWith("nombre_")) {
				ChallengeUser user = plugin.getUserManager().getOrLoadUser(player);
				String[] parts = params.split("_");
				ChallengeType challengeType = CollectionsUT.getEnum(parts[1], ChallengeType.class);
				ChallengeUserData userData = user.getChallengeData(challengeType);
				List<ChallengeUserProgress> progressList = new ArrayList<>(userData.getChallengeProgresses());
				return progressList.get(Integer.parseInt(parts[2])-1).getChallengeGenerated().getName();
			}

			if(params.startsWith("progreso_")) {
				ChallengeUser user = plugin.getUserManager().getOrLoadUser(player);
				String[] parts = params.split("_");
				ChallengeType challengeType = CollectionsUT.getEnum(parts[1], ChallengeType.class);
				ChallengeUserData userData = user.getChallengeData(challengeType);
				List<ChallengeUserProgress> progressList = new ArrayList<>(userData.getChallengeProgresses());
				return NumberUT.format(progressList.get(Integer.parseInt(parts[2])-1).getProgressPercent());
			}
			
			return null;
		}
	}
}
