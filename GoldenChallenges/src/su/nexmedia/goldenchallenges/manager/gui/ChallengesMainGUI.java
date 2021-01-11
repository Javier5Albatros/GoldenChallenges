package su.nexmedia.goldenchallenges.manager.gui;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.goldenchallenges.GoldenChallenges;
import su.nexmedia.goldenchallenges.data.object.ChallengeUser;
import su.nexmedia.goldenchallenges.data.object.ChallengeUserData;
import su.nexmedia.goldenchallenges.manager.api.ChallengeSettings;
import su.nexmedia.goldenchallenges.manager.type.ChallengeType;

public class ChallengesMainGUI extends NGUI<GoldenChallenges> {

	public ChallengesMainGUI(@NotNull GoldenChallenges plugin, @NotNull JYML cfg) {
		super(plugin, cfg, "gui.");
		
		GuiClick click = (p, type, e) -> {
			if (type == null) return;
			
			Class<?> clazz = type.getClass();
			if (clazz.equals(ContentType.class)) {
				ContentType type2 = (ContentType) type;
				if (type2 == ContentType.EXIT) {
					p.closeInventory();
				}
				return;
			}
			
			if (clazz.equals(ChallengeType.class)) {
				ChallengeType challengeType = (ChallengeType) type;
				if (!challengeType.isEnabled()) return;
				
				ChallengeSettings settings = plugin.getChallengeManager().getSettings(challengeType);
				if (settings == null) return;
				
				settings.openGUI(p);
			}
		};
		
		for (String id : cfg.getSection("gui.content")) {
			GuiItem guiItem = cfg.getGuiItem("gui.content." + id, ChallengeType.class);
			if (guiItem == null) continue;
			
			Enum<?> type = guiItem.getType();
			if (type != null && type.getClass().equals(ChallengeType.class)) {
				ChallengeType challengeType = (ChallengeType) type;
				if (!challengeType.isEnabled()) continue;
			}
			
			if (type != null) {
				guiItem.setClick(click);
			}
			this.addButton(guiItem);
		}
	}

	@Override
	protected void onCreate(@NotNull Player p, @NotNull Inventory inv, int page) {
		
	}

	@Override
	protected boolean ignoreNullClick() {
		return true;
	}

	@Override
	protected boolean cancelClick(int slot) {
		return true;
	}

	@Override
	protected boolean cancelPlayerClick() {
		return true;
	}

	@Override
	protected void replaceMeta(@NotNull Player p, @NotNull ItemStack item, @NotNull GuiItem guiItem) {
		super.replaceMeta(p, item, guiItem);
		
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;
		
		List<String> lore = meta.getLore();
		if (lore == null) return;
		
		Enum<?> type = guiItem.getType();
		if (type == null || !type.getClass().equals(ChallengeType.class)) return;
		
		ChallengeType challengeType = (ChallengeType) type;
		ChallengeUser user = plugin.getUserManager().getOrLoadUser(p);
		if (user == null) return;
		
		ChallengeUserData userData = user.getChallengeData(challengeType);
		
		int incompleted = (int) userData.getChallengeProgresses().stream()
				.filter(progress -> !progress.isCompleted()).count();
		
		String name = challengeType.name().toLowerCase();
		lore.replaceAll(line -> line
				.replace("%available-" + name + "%", String.valueOf(incompleted))
		);
		
		meta.setLore(lore);
		item.setItemMeta(meta);
	}
}
