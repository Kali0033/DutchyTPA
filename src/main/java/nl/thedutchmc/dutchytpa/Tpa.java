package nl.thedutchmc.dutchytpa;

import java.util.*;

import org.bukkit.event.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;


public class Tpa extends JavaPlugin implements Listener{

	static HashMap<UUID, Boolean> playerMap = new HashMap<>();
	FileConfiguration config = this.getConfig();

	@Override
	public void onEnable() {
		getCommand("ott").setExecutor(new CommandHandler(this));
		getCommand("ottaccept").setExecutor(new CommandHandler(this));
		getCommand("ottdeny").setExecutor(new CommandHandler(this));
		getCommand("ottyes").setExecutor(new CommandHandler(this));
		getCommand("ottno").setExecutor(new CommandHandler(this));
		getServer().getPluginManager().registerEvents(this, this);

		this.getConfig();

		playerMap = loadHashMap();

	}

	@Override
	public void onDisable()
	{
		saveHashMap(playerMap);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (playerMap.containsKey(event.getPlayer().getUniqueId())) {
			String playerName = event.getPlayer().getDisplayName();
			System.out.println("[GrimurOTT] " + playerName + " is a known player.");
		}
		else {
			playerMap.put(event.getPlayer().getUniqueId(), true);
			String playerName = event.getPlayer().getDisplayName();
			System.out.println("[GrimurOTT] " + playerName + " is an unknown player, adding to list.");
		}
	}

	public void saveHashMap(HashMap<UUID, Boolean> playerMap) {
		for (Object key : playerMap.keySet()) {
			getConfig().set(""+key, playerMap.get(key));
		}
		saveConfig();
	}

	public HashMap<UUID, Boolean> loadHashMap() {
		try {
			HashMap<UUID, Boolean> playerMap = new HashMap<UUID, Boolean>();

			config.getConfigurationSection("").getKeys(false).forEach(uuid -> playerMap.put(UUID.fromString(uuid), config.getBoolean("" + uuid)));
			return playerMap;
		}
		catch(Exception e)
		{
			System.out.println("GrimurOTT could not load from config.");
			HashMap<UUID, Boolean> playerMap = new HashMap<UUID, Boolean>();
			return playerMap;
		}
	}
	static void setPlayerMap(UUID uuid, boolean bool){
		playerMap.put(uuid, bool);
	}
}
