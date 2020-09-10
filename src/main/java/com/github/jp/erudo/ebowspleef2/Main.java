package com.github.jp.erudo.ebowspleef2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import com.github.jp.erudo.ebowspleef2.enums.GameState;
import com.github.jp.erudo.ebowspleef2.enums.Teams;
import com.github.jp.erudo.ebowspleef2.listener.ArrowListener;
import com.github.jp.erudo.ebowspleef2.listener.ClickVillagerListener;
import com.github.jp.erudo.ebowspleef2.listener.DeathListener;
import com.github.jp.erudo.ebowspleef2.listener.EntityDamageListener;
import com.github.jp.erudo.ebowspleef2.listener.ItemDropListener;
import com.github.jp.erudo.ebowspleef2.listener.MoveListener;
import com.github.jp.erudo.ebowspleef2.listener.RespawnListener;
import com.github.jp.erudo.ebowspleef2.runnable.EquipmentObserver;
import com.github.jp.erudo.ebowspleef2.utils.Config;

public class Main extends JavaPlugin {

	//チーム関連
	private ScoreboardManager scoreManager;
	private Scoreboard board;

	private static Team red;
	private static Team blue;
	private static Team spectator;

	//両者ポイント
	private static int redPoint = 0;
	private static int bluePoint = 0;

	//ゲーム状態
	private GameState currentState;

	//コンフィグ
	private Config config;

	//使用ステージ
	private String stageName;

	@Override
	public void onDisable() {
		getLogger().info("プラグインが停止しました");

		scoreManager = Bukkit.getScoreboardManager();
		board = scoreManager.getMainScoreboard();

		//チームを消去
		if (board.getTeam(Teams.RED.getName()) != null) {
			board.getTeam(Teams.RED.getName()).unregister();
		} else if (board.getTeam(Teams.BLUE.getName()) != null) {
			board.getTeam(Teams.BLUE.getName()).unregister();
		} else if (board.getTeam(Teams.SPECTATOR.getName()) != null) {
			board.getTeam(Teams.SPECTATOR.getName()).unregister();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		getLogger().info("プラグインが起動しました");

		////////////////////////
		///		Command		///
		///////////////////////
		getCommand("ebs").setExecutor(new CommandManager(this));

		///////////////////////
		///		Config		///
		///////////////////////
		config = new Config(this);
		stageName = config.getStageName();

		///////////////////////////
		///		ScoreBoard		///
		///////////////////////////
		scoreManager = Bukkit.getScoreboardManager();
		board = scoreManager.getMainScoreboard();

		//Team
		if (board.getTeam(Teams.RED.getName()) == null) {
			red = board.registerNewTeam(Teams.RED.getName());
		} else {
			red = board.getTeam(Teams.RED.getName());
		}
		red.setPrefix("§c");
		red.setSuffix(ChatColor.WHITE.toString());
		red.setDisplayName("赤チーム");
		red.setAllowFriendlyFire(true);

		if (board.getTeam(Teams.BLUE.getName()) == null) {
			blue = board.registerNewTeam(Teams.BLUE.getName());
		} else {
			blue = board.getTeam(Teams.BLUE.getName());
		}
		blue.setPrefix("§9");
		blue.setSuffix(ChatColor.WHITE.toString());
		blue.setDisplayName("青チーム");
		blue.setAllowFriendlyFire(true);

		if (board.getTeam(Teams.SPECTATOR.getName()) == null) {
			spectator = board.registerNewTeam(Teams.SPECTATOR.getName());
		} else {
			spectator = board.getTeam(Teams.SPECTATOR.getName());
		}
		spectator.setPrefix("§7");
		spectator.setSuffix(ChatColor.WHITE.toString());
		spectator.setDisplayName("観戦");
		spectator.setAllowFriendlyFire(false);

		///////////////////////////
		///		Listener		///
		///////////////////////////
		new ArrowListener(this);
		new DeathListener(this);
		new ClickVillagerListener(this);
		new ItemDropListener(this);
		new MoveListener(this);
		new EntityDamageListener(this);
		new RespawnListener(this);

		setCurrentGameState(GameState.PREPARE);
		this.getServer().getWorld("world").setPVP(false);

		EquipmentObserver eo = new EquipmentObserver(this);
		this.getServer().getScheduler().runTaskTimer(this, eo, 0, 20L);

		getServer().getWorld("world").setGameRuleValue("keepInventory", "true");
	}

	public GameState getCurrentGameState() {
		return currentState;
	}

	public void setCurrentGameState(GameState state) {
		this.currentState = state;
	}

	public Team getTeam(Teams teams) {
		if (teams == Teams.RED) {
			return red;
		} else {
			return blue;
		}
	}

	public Config getMyConfig() {
		return config;
	}

	public static Team getRed() {
		return red;
	}

	public static void setRed(Team red) {
		Main.red = red;
	}

	public static Team getBlue() {
		return blue;
	}

	public static void setBlue(Team blue) {
		Main.blue = blue;
	}

	public static Team getSpectator() {
		return spectator;
	}

	public static void setSpectator(Team spectator) {
		Main.spectator = spectator;
	}

	public static int getRedPoint() {
		return redPoint;
	}

	public static void setRedPoint(int num) {
		redPoint = num;
	}

	public static int getBluePoint() {
		return bluePoint;
	}

	public static void setBluePoint(int num) {
		bluePoint = num;
	}

	public static void addRedPoint() {
		redPoint += 1;
	}

	public static void addBluePoint() {
		bluePoint += 1;
	}

	public String getStageName() {
		return stageName;
	}

	public void setStageName(String stageName) {
		this.stageName = stageName;
	}

}
