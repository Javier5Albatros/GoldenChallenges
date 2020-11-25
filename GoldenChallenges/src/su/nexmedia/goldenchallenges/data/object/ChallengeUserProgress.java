package su.nexmedia.goldenchallenges.data.object;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.goldenchallenges.GoldenChallenges;
import su.nexmedia.goldenchallenges.manager.api.ChallengeGenerated;

public class ChallengeUserProgress {

	private ChallengeGenerated challengeGenerated;
	private Map<String, Double> objectiveProgress;
	
	public ChallengeUserProgress(@NotNull ChallengeGenerated challengeGenerated) {
		this.setChallengeGenerated(challengeGenerated);
		this.setObjectiveProgress(new HashMap<>());
	}
	
	@NotNull
	public ChallengeGenerated getChallengeGenerated() {
		return challengeGenerated;
	}
	
	public void setChallengeGenerated(@NotNull ChallengeGenerated challengeGenerated) {
		this.challengeGenerated = challengeGenerated;
	}
	
	@NotNull
	public Map<String, Double> getObjectiveProgress() {
		return objectiveProgress;
	}
	
	public double getObjectiveProgress(@NotNull String id) {
		return this.getObjectiveProgress().getOrDefault(id.toUpperCase(), 0D);
	}

	public void setObjectiveProgress(@NotNull Map<String, Double> objectiveProgress) {
		this.objectiveProgress = objectiveProgress;
	}
	
	public boolean addObjectiveProgress(@NotNull String id, double amount) {
		if (!this.getChallengeGenerated().hasObjective(id) || this.isCompleted(id) || this.isCompleted()) return false;
		
		id = id.toUpperCase();
		double amount2 = this.getChallengeGenerated().getJobType().formatValue(amount);
		double max = this.getChallengeGenerated().getObjectiveValue(id);
		
		this.objectiveProgress.computeIfAbsent(id, has -> 0D);
		this.objectiveProgress.computeIfPresent(id, (idHas, valHas) -> Math.min(max, valHas + amount2));
		
		return true;
	}
	
	public boolean isCompleted() {
		for (Map.Entry<String, Double> e : this.challengeGenerated.getObjectives().entrySet()) {
			if (this.getObjectiveProgress(e.getKey()) < e.getValue()) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isCompleted(@NotNull String id) {
		return this.getObjectiveProgress(id) >= this.challengeGenerated.getObjectiveValue(id);
	}
	
	public double getProgressPercent() {
		double has = this.getObjectiveProgress().values().stream().mapToDouble(d -> d).sum();
		double need = this.getChallengeGenerated().getObjectives().values().stream().mapToDouble(d -> d).sum();
		
		return has / need * 100D;
	}
	
	public double getProgressPercent(@NotNull String id) {
		double has = this.getObjectiveProgress(id);
		double need = this.getChallengeGenerated().getObjectiveValue(id);
		
		return has / need * 100D;
	}
	
	@NotNull
	public UnaryOperator<String> replacePlaceholders(@NotNull String objId) {
		ChallengeGenerated generated = this.getChallengeGenerated();
		UnaryOperator<String> oper = line -> {
			line = line.replace("%challenge-name%", generated.getName());
			if (!objId.isEmpty()) {
				line = line
					.replace("%objective-type%", GoldenChallenges.getInstance().lang().getEnum(generated.getJobType()))
					.replace("%objective-name%", generated.getJobType().formatObjective(objId))
					.replace("%objective-amount%", NumberUT.format(generated.getObjectiveValue(objId)))
					.replace("%objective-progress-current%", NumberUT.format(this.getObjectiveProgress(objId)))
					.replace("%objective-progress-total%", NumberUT.format(generated.getObjectiveValue(objId)))
					.replace("%objective-progress-percent%", NumberUT.format(this.getProgressPercent(objId)))
					;
			}
			return line;
		};
		
		return oper;
	}
}
