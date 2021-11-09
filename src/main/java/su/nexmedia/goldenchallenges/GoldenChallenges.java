package su.nexmedia.goldenchallenges;

import java.sql.SQLException;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexDataPlugin;
import su.nexmedia.engine.commands.api.IGeneralCommand;
import su.nexmedia.engine.core.Version;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.modules.IExternalModule.LoadPriority;
import su.nexmedia.engine.utils.Reflex;
import su.nexmedia.goldenchallenges.commands.OpenCommand;
import su.nexmedia.goldenchallenges.commands.ResetCommand;
import su.nexmedia.goldenchallenges.config.Config;
import su.nexmedia.goldenchallenges.config.Lang;
import su.nexmedia.goldenchallenges.data.ChallengeDataHandler;
import su.nexmedia.goldenchallenges.data.UserManager;
import su.nexmedia.goldenchallenges.data.object.ChallengeUser;
import su.nexmedia.goldenchallenges.hooks.external.PlaceholderHK;
import su.nexmedia.goldenchallenges.manager.ChallengeManager;
import su.nexmedia.goldenchallenges.nms.ChallengeNMS;

public class GoldenChallenges extends NexDataPlugin<GoldenChallenges, ChallengeUser> {

	private static GoldenChallenges instance;
	
	private Config config;
	private Lang lang;
	
	private ChallengeDataHandler dataHandler;
	private ChallengeManager challengeManager;
	private ChallengeNMS challengeNMS;
	
	public GoldenChallenges() {
		instance = this;
	}
	
	@NotNull
	public static GoldenChallenges getInstance() {
		return instance;
	}
	
	@Override
	public void enable() {
		if (!this.setupNMS()) {
			this.error("Could not setup internal NMS Handler. Disabling...");
			this.getPluginManager().disablePlugin(this);
			return;
		}
		
		this.challengeManager = new ChallengeManager(this);
		this.challengeManager.setup();
		
		this.getModuleManager().registerExternal(LoadPriority.HIGH);
		this.getModuleManager().registerExternal(LoadPriority.LOW);
	}

	@Override
	public void disable() {
		if (this.challengeManager != null) {
			this.challengeManager.shutdown();
			this.challengeManager = null;
		}
	}

	private boolean setupNMS() {
    	Version current = Version.CURRENT;
		System.out.println(current);
    	if (current == null) return false;
    	
    	String pack = ChallengeNMS.class.getPackage().getName();
    	Class<?> clazz = Reflex.getClass(pack, current.name());
    	if (clazz == null) return false;
    	
    	try {
			this.challengeNMS = (ChallengeNMS) clazz.getConstructor().newInstance();
		} 
    	catch (Exception e) {
			e.printStackTrace();
		}
		return this.challengeNMS != null;
	}
	
	@Override
	protected boolean setupDataHandlers() {
		try {
			this.dataHandler = ChallengeDataHandler.getInstance(this);
			this.dataHandler.setup();
		}
		catch (SQLException e) {
			this.error("Could not setup data handler!");
			return false;
		}
		
		this.userManager = new UserManager(this);
		this.userManager.setup();
		
		return true;
	}

	@Override
	public void setConfig() {
		this.config = new Config(this);
		this.config.setup();
		
		this.lang = new Lang(this);
		this.lang.setup();
	}

	@Override
	public void registerHooks() {
		if (Hooks.hasPlaceholderAPI()) {
			this.registerHook(Hooks.PLACEHOLDER_API, PlaceholderHK.class);
		}
	}

	@Override
	public void registerCmds(@NotNull IGeneralCommand<GoldenChallenges> mainCommand) {
		mainCommand.addDefaultCommand(new OpenCommand(this));
		mainCommand.addSubCommand(new ResetCommand(this));
	}

	@Override
	public void registerEditor() {
		
	}

	@Override
	@NotNull
	public Config cfg() {
		return this.config;
	}

	@Override
	@NotNull
	public Lang lang() {
		return this.lang;
	}

	@Override
	public ChallengeDataHandler getData() {
		return this.dataHandler;
	}
	
	@NotNull
	public ChallengeManager getChallengeManager() {
		return challengeManager;
	}

	@NotNull
	public ChallengeNMS getChallengeNMS() {
		return challengeNMS;
	}
}
