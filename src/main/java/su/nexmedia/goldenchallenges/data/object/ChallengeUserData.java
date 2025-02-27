package su.nexmedia.goldenchallenges.data.object;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.goldenchallenges.api.GoldenChallengesAPI;
import su.nexmedia.goldenchallenges.manager.api.ChallengeGenerated;
import su.nexmedia.goldenchallenges.manager.api.RewardInfo;
import su.nexmedia.goldenchallenges.manager.type.ChallengeJobType;

public class ChallengeUserData {

	private long nextChallengeDate;
	private Set<ChallengeUserProgress> challengeProgresses;
	
	public ChallengeUserData() {
		this(0, new HashSet<>());
	}
	
	public ChallengeUserData(long nextChallengeDate, @NotNull Set<ChallengeUserProgress> challengeProgresses) {
		this.setNextChallengeDate(nextChallengeDate);
		this.setChallengeProgresses(challengeProgresses);
	}
	
	public long getNextChallengeDate() {
		return nextChallengeDate;
	}
	
	public void setNextChallengeDate(long nextChallengeDate) {
		this.nextChallengeDate = nextChallengeDate;
	}
	
	public boolean isNewChallengesTime() {
		return System.currentTimeMillis() > this.getNextChallengeDate();
	}
	
	@NotNull
	public Set<ChallengeUserProgress> getChallengeProgresses() {
		return challengeProgresses;
	}
	
	public void setChallengeProgresses(@NotNull Set<ChallengeUserProgress> challengeProgresses) {
		this.challengeProgresses = challengeProgresses;
	}
	
	public double getProgressPercent() {
		double sum = this.getChallengeProgresses().stream().map(prog -> prog.getProgressPercent())
				.mapToDouble(d -> d).sum();
		return sum / (double) this.getChallengeProgresses().size();
	}
	
	/**
	 * 
	 * @param p2
	 * @param type
	 * @param id
	 * @param amount
	 * @return Set of incompleted challenge progresses that accept provided objective.
	 */
	@NotNull
	public Set<ChallengeUserProgress> addObjectiveProgress(@NotNull OfflinePlayer p2, @NotNull ChallengeJobType type, @NotNull String id, double amount) {
		Player p = p2.getPlayer();
		
		Set<ChallengeUserProgress> set = this.getChallengeProgresses().stream()
		.filter(progress -> {
			ChallengeGenerated generated = progress.getChallengeGenerated();
			if (generated.getJobType() != type) return false;
			if (p != null && !generated.getWorlds().isEmpty() && !generated.getWorlds().contains(p.getWorld().getName())) return false;
			
			return true;
		})
		.filter(progress -> progress.addObjectiveProgress(id, amount)).collect(Collectors.toSet());
		
		set.forEach(progress -> {
			if (progress.isCompleted() && p != null) {
				ChallengeGenerated generated = progress.getChallengeGenerated();
				
				List<RewardInfo> rewards = generated.getRewards().stream()
					.map((rewardId) -> {
						return GoldenChallengesAPI.getChallengeManager().getChallengeReward(rewardId);
					})
					.filter(reward -> reward != null)
					.collect(Collectors.toList());
				
				rewards.forEach(rewardInfo -> {
					rewardInfo.getActionManipulator().process(p);
				});
			}
		});
		
		return set;
	}
}
