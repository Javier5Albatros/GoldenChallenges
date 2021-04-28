package su.nexmedia.goldenchallenges.manager.api;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.actions.ActionManipulator;

public class RewardInfo {
	
	private String id;
	private List<String> lore;
	private ActionManipulator actionManipulator;
	
	public RewardInfo(@NotNull String id, @NotNull List<String> lore, @NotNull ActionManipulator actionManipulator) {
		this.id = id.toLowerCase();
		this.lore = lore;
		this.actionManipulator = actionManipulator;
	}
	
	@NotNull
	public String getId() {
		return id;
	}
	
	@NotNull
	public List<String> getLore() {
		return lore;
	}
	
	@NotNull
	public ActionManipulator getActionManipulator() {
		return actionManipulator;
	}
}