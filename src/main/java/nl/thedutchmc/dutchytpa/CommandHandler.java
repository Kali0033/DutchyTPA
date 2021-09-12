package nl.thedutchmc.dutchytpa;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import static nl.thedutchmc.dutchytpa.Tpa.playerMap;

public class CommandHandler implements CommandExecutor {
	private Tpa plugin;

	public CommandHandler(Tpa plugin) {
		this.plugin = plugin;
	}

	static HashMap<UUID, UUID> targetMap = new HashMap<>();

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players may use this command!");
			return true;
		}
		if (command.getName().equals("ott")) {
			if (!sender.hasPermission("ott.ott"))
				sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
			if (args.length == 1) {
				if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[0]))) {
					sender.sendMessage(ChatColor.RED + "Player is not online!");
					return true;
				}
				Player target = Bukkit.getPlayer(args[0]);
				final Player senderP = (Player)sender;
				long currentTime = new Date().getTime();

				if (target.getUniqueId().equals(senderP.getUniqueId())) {
					sender.sendMessage(ChatColor.RED + "You may not teleport to yourself!");
					return true;
				}
				if (targetMap.containsKey(senderP.getUniqueId())) {
					sender.sendMessage(ChatColor.GOLD + "You already have a pending request!");
					return false;
				}
				if (playerMap.get(senderP.getUniqueId()) == false) {
					sender.sendMessage(ChatColor.RED + "You cannot one time teleport!");
					return true;
				}
				if (senderP.getFirstPlayed() < (currentTime-86400000)) {
					sender.sendMessage(ChatColor.RED + "You cannot one time teleport!");
					Tpa.setPlayerMap(senderP.getUniqueId(), false);
					return true;
				}
				target.sendMessage(ChatColor.RED + senderP.getName() + ChatColor.GOLD + " wants to teleport to you. \nType " + ChatColor.RED + "/ottaccept" + ChatColor.GOLD + " to accept this request.\nType " + ChatColor.RED + "/ottdeny" + ChatColor.GOLD + " to deny this request.\nYou have 5 minutes to respond.");
				targetMap.put(senderP.getUniqueId(), target.getUniqueId());
				sender.sendMessage(ChatColor.GOLD + "Send OTT TPA request to " + ChatColor.RED + target.getName());
				(new BukkitRunnable() {
					public void run() {
						CommandHandler.targetMap.remove(senderP.getUniqueId());
					}
				}).runTaskLaterAsynchronously((Plugin)this.plugin, 6000L);
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid syntax!");
			}
			return true;
		}
		if (command.getName().equals("ottaccept") || command.getName().equals("ottyes")) {
			if (!sender.hasPermission("ott.accept"))
				sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
			final Player senderP = (Player)sender;
			if (targetMap.containsValue(senderP.getUniqueId())) {
				sender.sendMessage(ChatColor.GOLD + "OTT TPA request accepted!");
				for (Map.Entry<UUID, UUID> entry : targetMap.entrySet()) {
					if (((UUID)entry.getValue()).equals(senderP.getUniqueId())) {
						Player tpRequester = Bukkit.getPlayer(entry.getKey());
						SuccessfulTpaEvent event = new SuccessfulTpaEvent(tpRequester, tpRequester.getLocation());
						Bukkit.getPluginManager().callEvent(event);
						tpRequester.teleport((Entity)senderP);
						targetMap.remove(entry.getKey());
						Tpa.setPlayerMap(tpRequester.getUniqueId(), false);

						break;
					}
				}
			} else {
				sender.sendMessage(ChatColor.GOLD + "You don't have any pending requests!");
			}
			return true;
		}
		if (command.getName().equals("ottdeny") || command.getName().equals("ottno")) {
			if (!sender.hasPermission("ott.deny"))
				sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
			final Player senderP = (Player)sender;
			if (targetMap.containsValue(senderP.getUniqueId())) {
				for (Map.Entry<UUID, UUID> entry : targetMap.entrySet()) {
					if (((UUID)entry.getValue()).equals(senderP.getUniqueId())) {
						targetMap.remove(entry.getKey());
						Player originalSender = Bukkit.getPlayer(entry.getKey());
						originalSender.sendMessage(ChatColor.GOLD + "Your OTT TPA request was denied!");
						sender.sendMessage(ChatColor.GOLD + "Denied OTT TPA request.");
						break;
					}
				}
			} else {
				sender.sendMessage(ChatColor.GOLD + "You don't have any pending requests!");
			}
			return true;
		}
		return false;
	}
}