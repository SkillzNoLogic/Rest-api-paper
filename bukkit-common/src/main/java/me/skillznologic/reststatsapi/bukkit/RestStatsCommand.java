package me.skillznologic.reststatsapi.bukkit;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** /reststats <status|reload|refresh|interval <seconds>|messages <on|off|toggle>> */
final class RestStatsCommand implements CommandExecutor, TabCompleter {

    private static final String PREFIX = ChatColor.GOLD + "[RestStatsAPI] " + ChatColor.RESET;
    private static final List<String> SUBCOMMANDS = Arrays.asList("status", "reload", "refresh", "interval", "messages");

    private final RestStatsPlugin plugin;

    RestStatsCommand(RestStatsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = args.length == 0 ? "status" : args[0].toLowerCase();
        switch (sub) {
            case "status":
                String address = plugin.apiAddress();
                sender.sendMessage(PREFIX + "API: " + (address == null ? ChatColor.RED + "not running" : ChatColor.GREEN + address));
                sender.sendMessage(PREFIX + "Players cached: " + ChatColor.AQUA + plugin.cachedPlayers());
                sender.sendMessage(PREFIX + "Refresh interval: " + ChatColor.AQUA + plugin.intervalSeconds() + "s");
                sender.sendMessage(PREFIX + "Console messages: " + onOff(plugin.consoleMessages()));
                return true;

            case "reload":
                plugin.reload();
                sender.sendMessage(PREFIX + "Config reloaded.");
                return true;

            case "refresh":
                plugin.refreshNow();
                sender.sendMessage(PREFIX + "Refreshing stats now.");
                return true;

            case "interval":
                if (args.length < 2) {
                    sender.sendMessage(PREFIX + "Refresh interval is " + plugin.intervalSeconds() + "s. Usage: /" + label + " interval <seconds>");
                    return true;
                }
                int seconds;
                try {
                    seconds = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "'" + args[1] + "' is not a whole number of seconds.");
                    return true;
                }
                if (seconds < RestStatsPlugin.MIN_INTERVAL) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "Interval must be at least " + RestStatsPlugin.MIN_INTERVAL + " second.");
                    return true;
                }
                plugin.setIntervalSeconds(seconds);
                sender.sendMessage(PREFIX + "Stats now refresh every " + ChatColor.AQUA + seconds + "s" + ChatColor.RESET + " (saved to config.yml).");
                return true;

            case "messages":
                boolean enabled;
                String mode = args.length < 2 ? "toggle" : args[1].toLowerCase();
                if (mode.equals("on") || mode.equals("true") || mode.equals("enable")) enabled = true;
                else if (mode.equals("off") || mode.equals("false") || mode.equals("disable")) enabled = false;
                else if (mode.equals("toggle")) enabled = !plugin.consoleMessages();
                else {
                    sender.sendMessage(PREFIX + "Usage: /" + label + " messages <on|off|toggle>");
                    return true;
                }
                plugin.setConsoleMessages(enabled);
                sender.sendMessage(PREFIX + "Console messages " + onOff(enabled) + ChatColor.RESET + " (saved to config.yml).");
                return true;

            default:
                sender.sendMessage(PREFIX + "Usage: /" + label + " <status|reload|refresh|interval <seconds>|messages <on|off|toggle>>");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return matching(SUBCOMMANDS, args[0]);
        if (args.length == 2 && args[0].equalsIgnoreCase("messages")) return matching(Arrays.asList("on", "off", "toggle"), args[1]);
        if (args.length == 2 && args[0].equalsIgnoreCase("interval")) return matching(Arrays.asList("10", "30", "60", "300"), args[1]);
        return Collections.emptyList();
    }

    private static List<String> matching(List<String> options, String prefix) {
        List<String> out = new ArrayList<>();
        for (String o : options) if (o.startsWith(prefix.toLowerCase())) out.add(o);
        return out;
    }

    private static String onOff(boolean value) {
        return value ? ChatColor.GREEN + "on" : ChatColor.RED + "off";
    }
}
