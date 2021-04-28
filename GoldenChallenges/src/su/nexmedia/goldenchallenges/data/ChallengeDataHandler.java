package su.nexmedia.goldenchallenges.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import com.google.gson.reflect.TypeToken;

import su.nexmedia.engine.data.DataTypes;
import su.nexmedia.engine.data.IDataHandler;
import su.nexmedia.goldenchallenges.GoldenChallenges;
import su.nexmedia.goldenchallenges.data.object.ChallengeUser;
import su.nexmedia.goldenchallenges.data.object.ChallengeUserData;
import su.nexmedia.goldenchallenges.manager.type.ChallengeType;

public class ChallengeDataHandler extends IDataHandler<GoldenChallenges, ChallengeUser> {

	private static ChallengeDataHandler instance;
	private final Function<ResultSet, ChallengeUser> FUNC_USER;
	
	protected ChallengeDataHandler(@NotNull GoldenChallenges plugin) throws SQLException {
		super(plugin);
		
		this.FUNC_USER = (rs) -> {
			try {
				UUID uuid = UUID.fromString(rs.getString(COL_USER_UUID));
				String name = rs.getString(COL_USER_NAME);
				long lastOnline = rs.getLong(COL_USER_LAST_ONLINE);
				Map<ChallengeType, ChallengeUserData> challengeData = gson.fromJson(rs.getString("challengeData"), new TypeToken<Map<ChallengeType, ChallengeUserData>>(){}.getType());
				Map<ChallengeType, Integer> challengeCount = gson.fromJson(rs.getString("challengeCount"), new TypeToken<Map<ChallengeType, Integer>>(){}.getType());
				
				return new ChallengeUser(plugin, uuid, name, lastOnline, challengeData, challengeCount);
			}
			catch (SQLException e) {
				return null;
			}
		};
	}

	@NotNull
	public static ChallengeDataHandler getInstance(@NotNull GoldenChallenges plugin) throws SQLException {
		if (instance == null) {
			instance = new ChallengeDataHandler(plugin);
		}
		return instance;
	}
	
	@Override
	protected void onTableCreate() {
		super.onTableCreate();
		
		this.addColumn(TABLE_USERS, "challengeCount", DataTypes.STRING.build(this.dataType), "{}");
	}

	@Override
	@NotNull
	protected LinkedHashMap<String, String> getColumnsToCreate() {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("challengeData", DataTypes.STRING.build(this.dataType));
		map.put("challengeCount", DataTypes.STRING.build(this.dataType));
		return map;
	}

	@Override
	@NotNull
	protected LinkedHashMap<String, String> getColumnsToSave(@NotNull ChallengeUser user) {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("challengeData", this.gson.toJson(user.getChallengeData()));
		map.put("challengeCount", this.gson.toJson(user.getChallengeCount()));
		return map;
	}

	@Override
	@NotNull
	protected Function<ResultSet, ChallengeUser> getFunctionToUser() {
		return this.FUNC_USER;
	}
}
