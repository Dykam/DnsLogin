package nl.dykam.dev.dnslogin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.security.SecureRandom;

public class DnsLoginPlugin extends JavaPlugin {
  SecureRandom secureRandom;
  DnsLoginVerifier verifier;
  static final int keyLength = 16;
  static final char[] keyCharRange = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
  @Override
  public void onEnable() {
    saveDefaultConfig();
    if(!getConfig().isString("server-address")) {
      getLogger().severe("Disabling DnsLogin, server-address not set in configuration.");
      getServer().getPluginManager().disablePlugin(this);
    }
    secureRandom = new SecureRandom();

    if(!getServer().getOnlineMode()) {
      verifier = new DnsLoginVerifier(this);
    }
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if(args.length < 1)
      return false;
    if(args[0].equals("reload")) {
      reloadConfig();
      return true;
    } else if(args[0].equals("auth")) {
      String playerName;
      if(args[1].equals("self")) {
        if(!(sender instanceof Player)) {
          sender.sendMessage(ChatColor.DARK_PURPLE + "[DnsLogin] " + ChatColor.RED + "Only a player can auth himself");
          return false;
        }
        playerName = sender.getName();
      } else {
        playerName = args[1];
      }

      String key;
      if(args.length == 3) {
        key = args[2];
      } else {
        char[] keyChars = new char[keyLength];
        for(int i = 0; i < keyChars.length; i++) {
          keyChars[i] = keyCharRange[(int)(secureRandom.nextDouble() * keyCharRange.length)];
        }
        key = new String(keyChars);
      }

      getConfig().set("users." + playerName, key);
      saveConfig();

      String[] messages = {
              ChatColor.DARK_PURPLE + "[DnsLogin] " + ChatColor.GREEN + playerName + " has been added to the authentication list.",
              ChatColor.DARK_PURPLE + "[DnsLogin] " + ChatColor.GREEN + "In case session servers are offline, connect with:",
              ChatColor.DARK_PURPLE + "[DnsLogin] " + ChatColor.GREEN + ChatColor.BOLD + key + "." + getConfig().getString("server-address"),
      };

      Player player = getServer().getPlayerExact(playerName);
      if(player != null && !player.equals(sender)) {
        for(String message : messages)
          player.sendMessage(message);
      }
      for(String message : messages)
        sender.sendMessage(message);
      return true;
    } else if(args[0].equals("alt")) {
      String from, to;
      if(args.length == 2) {
        if(!(sender instanceof Player)) {
          sender.sendMessage(ChatColor.DARK_PURPLE + "[DnsLogin] " + ChatColor.RED + "Only a player can set an alt of himself");
          return false;
        }
        from = sender.getName();
        to = args[1];
      } else {
        from = args[1];
        to = args[2];
      }
      if(!getConfig().isString("users." + from)) {
        sender.sendMessage(ChatColor.DARK_PURPLE + "[DnsLogin] " + ChatColor.RED + from + " does not have a key set");
        return false;
      }
      getConfig().set("users." + to, getConfig().getString("users." + from));

      Player player = getServer().getPlayerExact(from);
      String message = ChatColor.DARK_PURPLE + "[DnsLogin] " + ChatColor.GREEN + "Key successfully copied from " + from + " to " + to;
      if(player != null && !player.equals(sender))
        player.sendMessage(message);
      sender.sendMessage(message);
      return true;
    }
    return false;
  }
}
