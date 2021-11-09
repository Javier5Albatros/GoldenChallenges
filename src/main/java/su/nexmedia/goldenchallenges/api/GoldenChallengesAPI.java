package su.nexmedia.goldenchallenges.api;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.goldenchallenges.GoldenChallenges;
import su.nexmedia.goldenchallenges.manager.ChallengeManager;

public class GoldenChallengesAPI {

	private static GoldenChallenges plugin = GoldenChallenges.getInstance();
	
	@NotNull
	public static ChallengeManager getChallengeManager() {
		return plugin.getChallengeManager();
	}
}
