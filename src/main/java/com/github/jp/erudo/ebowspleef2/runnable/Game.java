package com.github.jp.erudo.ebowspleef2.runnable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.github.jp.erudo.ebowspleef2.Main;
import com.github.jp.erudo.ebowspleef2.enums.GameState;
import com.github.jp.erudo.ebowspleef2.enums.Teams;
import com.github.jp.erudo.ebowspleef2.utils.MessageManager;
import com.github.jp.erudo.ebowspleef2.utils.PlayersSetting;
import com.github.jp.erudo.ebowspleef2.utils.TitleSender;
import com.github.jp.erudo.ebowspleef2.utils.WorldManager;

public class Game extends BukkitRunnable {

	private final Main plg;
	private BukkitTask task;
	private int count;
	private int totalTime;

	private TitleSender title;
	private BossBar Timebar;

	public Game(Main plg, int count) {
		this.plg = plg;
		this.totalTime = count;
		this.count = this.totalTime;

		Timebar = Bukkit.getServer().createBossBar("残り時間: " + count, BarColor.GREEN, BarStyle.SOLID);
		title = new TitleSender();
	}

	public void run() {

		double percentage = count / totalTime;

		Timebar.setProgress(percentage);
		Timebar.setVisible(true);


		if (plg.getCurrentGameState() == GameState.END) {
			if (plg.getMyConfig().isCanRespawn()) {
				if (Main.getBluePoint() > Main.getRedPoint()) { //青の勝ち
					for (Player p : plg.getServer().getOnlinePlayers()) {
						title.sendTitle(p, ChatColor.BLUE + "青チーム" + ChatColor.WHITE + "の勝利！！！", null, null);
					}
				} else if (Main.getRedPoint() < Main.getBluePoint()) { //赤の勝ち
					for (Player p : plg.getServer().getOnlinePlayers()) {
						title.sendTitle(p, ChatColor.RED + "赤チーム" + ChatColor.WHITE + "の勝利！！！", null, null);
					}
				} else if(Main.getRedPoint() == Main.getBluePoint()){ //引き分け
					for (Player p : plg.getServer().getOnlinePlayers()) {
						title.sendTitle(p, ChatColor.WHITE + "引き分け！！！", null, null);
					}
				} else {
					System.out.println("予期せぬ結果となりました。");
				}
			}

			MessageManager.messageAll("ワールドを復元中です・・・");
			WorldManager wm = new WorldManager();
			Location loc = new Location(plg.getServer().getWorld("world"), plg.getMyConfig().getBeginCoordinate()[0],
					plg.getMyConfig().getBeginCoordinate()[1], plg.getMyConfig().getBeginCoordinate()[2]);
			wm.loadSchematic(loc, plg.getStageName());
			MessageManager.messageAll("復元が完了しました！");

			Main.setBluePoint(0);
			Main.setRedPoint(0);

			MessageManager.broadcastMessage("試合終了！！");
			count = 0;

			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				for(Player pla : Bukkit.getServer().getOnlinePlayers()) {
					player.showPlayer(plg ,pla);
				}

				player.setSneaking(false);
				player.teleport(PlayersSetting.getLobbyPos());
				player.setGameMode(GameMode.ADVENTURE);

				if (plg.getTeam(Teams.BLUE).hasEntry(player.getName())) {
					plg.getTeam(Teams.BLUE).removeEntry(player.getName());
				} else if (plg.getTeam(Teams.RED).hasEntry(player.getName())) {
					plg.getTeam(Teams.RED).removeEntry(player.getName());
				}

				player.getInventory().setHelmet(null);
				player.getInventory().setChestplate(null);
				player.getInventory().setLeggings(null);
				player.getInventory().setBoots(null);

				for (int i = 0; i < 35; i++) { //35はインベントリのサイズ
					player.getInventory().setItem(i, null);
				}

				player.getWorld().setPVP(false);

				player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1);

			}

			plg.setCurrentGameState(GameState.PREPARE);
			Timebar.removeAll();
			plg.getServer().getScheduler().cancelTask(task.getTaskId());
		}

		if (plg.getCurrentGameState() == GameState.GAMING) {
			if (count > 0) {

				for (Player p : plg.getServer().getOnlinePlayers()) {
					Timebar.addPlayer(p);
				}

				//カウントダウン
				if (count <= 3) { //0<count<=3
					for (Player p : plg.getServer().getOnlinePlayers()) {
						title.sendTitle(p, String.valueOf(count), null, null);
						p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5F, 1);
					}
				}

				if (plg.getMyConfig().isCanRespawn()) {
					for (Player p : plg.getServer().getOnlinePlayers()) {
						title.sendTitle(p, null, null,
								ChatColor.RED + "赤チームポイント: " + Main.getRedPoint()
										+ "  " + ChatColor.BLUE + "青チームポイント: "
										+ Main.getBluePoint());
					}

				} else {
					for (Player p : plg.getServer().getOnlinePlayers()) {
						title.sendTitle(p, null, null,
								ChatColor.RED + "赤チーム残り人数: " + plg.getTeam(Teams.RED).getEntries().size()
										+ "  " + ChatColor.BLUE + "青チーム残り人数: "
										+ plg.getTeam(Teams.BLUE).getEntries().size());
					}

					if (plg.getTeam(Teams.RED).getEntries().size() <= 0) {
						for (Player p : plg.getServer().getOnlinePlayers()) {
							title.sendTitle(p, ChatColor.BLUE + "青チーム" + ChatColor.WHITE + "の勝利！！！", null, null);
						}
						plg.setCurrentGameState(GameState.END);
						return;
					}

					if (plg.getTeam(Teams.BLUE).getEntries().size() <= 0) {
						for (Player p : plg.getServer().getOnlinePlayers()) {
							title.sendTitle(p, ChatColor.RED + "赤チーム" + ChatColor.WHITE + "の勝利！！！", null, null);
						}
						plg.setCurrentGameState(GameState.END);
						return;
					}
				}

			} else {
				plg.setCurrentGameState(GameState.END);
			}
			Timebar.setTitle("残り時間: " + count);
			count--;
		}
	}

	public void setTask(BukkitTask task) {
		this.task = task;
	}

}
