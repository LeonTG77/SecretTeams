/*
 * Project: SecretTeams
 * Class: com.leontg77.secretteams.commands.SecretTeamsCommand
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Leon Vaktskjold <leontg77@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.leontg77.secretteams.commands;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.collect.Lists;
import com.leontg77.secretteams.Main;
import com.leontg77.secretteams.listener.KillListener;
import com.leontg77.secretteams.protocol.TeamUpdateAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Secret Teams command class.
 *
 * @author LeonTG77
 */
public class SecretTeamsCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION = "secretteams.manage";

    private final Main plugin;

    private final TeamUpdateAdapter adapter;
    private final KillListener listener;

    private final ProtocolManager manager = ProtocolLibrary.getProtocolManager();

    public SecretTeamsCommand(Main plugin, KillListener listener, TeamUpdateAdapter adapter) {
        this.plugin = plugin;

        this.listener = listener;
        this.adapter = adapter;
    }

    private boolean enabled = false;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Main.PREFIX + "Usage: /secretteams <info|enable|disable|killreveal>");
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            sender.sendMessage(Main.PREFIX + "Plugin creator: §aLeonTG77");
            sender.sendMessage(Main.PREFIX + "Version: §a" + plugin.getDescription().getVersion());
            sender.sendMessage(Main.PREFIX + "Description:");
            sender.sendMessage("§8» §f" + plugin.getDescription().getDescription());
            return true;
        }

        if (args[0].equalsIgnoreCase("enable")) {
            if (!sender.hasPermission(PERMISSION)) {
                sender.sendMessage(ChatColor.RED + "You don't have permission.");
                return true;
            }

            if (enabled) {
                sender.sendMessage(Main.PREFIX + "Secret Teams is already enabled.");
                return true;
            }

            plugin.broadcast(Main.PREFIX + "Secret Teams has been enabled.");
            enabled = true;

            manager.addPacketListener(adapter);
            return true;
        }

        if (args[0].equalsIgnoreCase("disable")) {
            if (!sender.hasPermission(PERMISSION)) {
                sender.sendMessage(ChatColor.RED + "You don't have permission.");
                return true;
            }

            if (!enabled) {
                sender.sendMessage(Main.PREFIX + "Secret Teams is not enabled.");
                return true;
            }

            plugin.broadcast(Main.PREFIX + "Secret Teams has been disabled.");
            enabled = false;

            manager.removePacketListener(adapter);
            return true;
        }

        if (args[0].equalsIgnoreCase("killreveal")) {
            if (!sender.hasPermission(PERMISSION)) {
                sender.sendMessage(ChatColor.RED + "You don't have permission.");
                return true;
            }

            if (!enabled) {
                sender.sendMessage(Main.PREFIX + "Secret Teams is not enabled.");
                return true;
            }

            if (plugin.killreveal) {
                plugin.broadcast(Main.PREFIX + "Kill Reveal has been disabled.");
                HandlerList.unregisterAll(listener);

                plugin.killreveal = false;
                plugin.hasAKill.clear();
            } else {
                plugin.broadcast(Main.PREFIX + "Kill Reveal has been enabled.");

                Bukkit.getPluginManager().registerEvents(listener, plugin);
                plugin.killreveal = true;
            }
            return true;
        }

        sender.sendMessage(Main.PREFIX + "Usage: /secretteams <info|enable|disable|killreveal>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> toReturn = Lists.newArrayList();
        List<String> list = Lists.newArrayList();

        if (args.length != 1) {
            return toReturn;
        }

        list.add("info");

        if (sender.hasPermission(PERMISSION)) {
            list.add("enable");
            list.add("disable");
            list.add("killreveal");
        }

        // make sure to only tab complete what starts with what they
        // typed or everything if they didn't type anything
        toReturn.addAll(list
                .stream()
                .filter(str -> args[args.length - 1].isEmpty() || str.startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList()));

        return toReturn;
    }
}
