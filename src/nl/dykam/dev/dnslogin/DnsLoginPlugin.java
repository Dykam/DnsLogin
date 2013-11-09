package nl.dykam.dev.dnslogin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class DnsLoginPlugin extends JavaPlugin {
  SecureRandom secureRandom;
  DnsLoginVerifier verifier;
  static final int keyLength = 16;
  static final char[] keyCharRange = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
  @Override
  public void onEnable() {
    saveDefaultConfig();
    if(!getConfig().isString("server-address") && getConfig().getBoolean("enabled", false)) {
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
      if(!sender.hasPermission("dnslogin.reload"))
        return false;
      reloadConfig();
      return true;
    } else if(args[0].equals("auth")) {
      if(!sender.hasPermission("dnslogin.auth"))
        return false;
      String playerName;
      if(args[1].equals("self")) {
        if(!(sender instanceof Player)) {
          sender.sendMessage(ChatColor.DARK_PURPLE + "[DnsLogin] " + ChatColor.RED + "Only a player can auth himself");
          return false;
        }
        playerName = sender.getName();
      } else {
        if(!sender.hasPermission("dnslogin.auth.other"))
          return false;
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

      try {
        String hash = calculateHash(key);
        getConfig().set("users." + playerName, hash);
        saveConfig();
      } catch (Exception ex) {
        sender.sendMessage(ChatColor.DARK_PURPLE + "[DnsLogin] " + ChatColor.RED + "An error occured.");
        ex.printStackTrace();
        return false;
      }

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
      if(!sender.hasPermission("dnslogin.alt"))
        return false;
      String from, to;
      if(args.length == 2) {
        if(!(sender instanceof Player)) {
          sender.sendMessage(ChatColor.DARK_PURPLE + "[DnsLogin] " + ChatColor.RED + "Only a player can set an alt of himself");
          return false;
        }
        from = sender.getName();
        to = args[1];
      } else {
        if(!sender.hasPermission("dnslogin.alt.other"))
          return false;
        from = args[1];
        to = args[2];
      }
      if(!getConfig().isString("users." + from)) {
        sender.sendMessage(ChatColor.DARK_PURPLE + "[DnsLogin] " + ChatColor.RED + from + " does not have a key set");
        return false;
      }
      getConfig().set("users." + to, getConfig().getString("users." + from));
      saveConfig();

      Player player = getServer().getPlayerExact(from);
      String message = ChatColor.DARK_PURPLE + "[DnsLogin] " + ChatColor.GREEN + "Key successfully copied from " + from + " to " + to;
      if(player != null && !player.equals(sender))
        player.sendMessage(message);
      sender.sendMessage(message);
      return true;
    }
    return false;
  }

  public static String calculateHash(String key)  throws NoSuchAlgorithmException, UnsupportedEncodingException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(key.getBytes("UTF-8"));
    return DatatypeConverter.printHexBinary(hash);
  }
}
