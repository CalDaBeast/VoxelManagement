package com.thevoxelbox.voxelmanagement;

import com.thevoxelbox.voxelmanagement.command.VMCommand;
import static com.thevoxelbox.voxelmanagement.command.VMCommand.VOXEL_MANAGEMENT;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class VoxelManagement extends JavaPlugin {

	//will be made private in a release version
	public PluginManagerWrapper manager;

	@Override
	public void onEnable() {
		try {
			manager = new PluginManagerWrapper(Bukkit.getPluginManager());
		} catch (ManagementException ex) {
			getLogger().log(Level.SEVERE, "Unable to load VoxelManagement. Bukkit's PluginManager could not be accessed.");
			return;
		}
		this.getCommand("vm").setExecutor(new VMCommand(this));
	}

	/**
	 * Unloads the specified plugin from the server
	 *
	 * @param pluginName the name of the plugin to unload
	 * @param notify whether or not the server's administrators should be notified
	 * @throws com.thevoxelbox.voxelmanagement.ManagementException
	 */
	public void unloadPlugin(String pluginName, boolean notify) throws ManagementException {
		Plugin plugin = manager.getPluginManager().getPlugin(pluginName);
		if (plugin == null) {
			throw new ManagementException(ExceptionType.UNLOAD, pluginName + " does not exist or is not currently loaded onto the server.");
		}
		unloadPlugin(plugin, notify);
	}

	/**
	 * Unloads the specified plugin from the server
	 *
	 * @param plugin the plugin to unload
	 * @param notify whether or not the server's administrators should be notified
	 */
	public void unloadPlugin(Plugin plugin, boolean notify) {
		manager.getPluginManager().disablePlugin(plugin);
		manager.getActivePlugins().remove(plugin);
		Collection<Command> serverCommands = manager.getCommandMap().getCommands();
		ArrayList<Command> disable = new ArrayList<>();
		for (Command cmd : serverCommands) {
			if (cmd instanceof PluginCommand) {
				PluginCommand command = (PluginCommand) cmd;
				if (command.getPlugin() == plugin) {
					disable.add(command);
				}
			}
		}
		for (Command cmd : disable) {
			serverCommands.remove(cmd);
			System.out.println(cmd);
		}
	}

	/**
	 * Check is a CommandSender has a certain permission.
	 * All of the following will return true for the queried permission:
	 * voxelmanagement.*
	 * voxelmanagement.pluginname
	 * voxelmanagement.PluginName
	 * voxelmanagement.base.*
	 * voxelmanagement.base.pluginname
	 * voxelmanagement.base.PluginName
	 *
	 * @param cs The originator of the command
	 * @param base The sub-permission to check within. EX: unload, notify, load, reload, update
	 * @param plugin The name of the plugin
	 * @param ignoreOp If the permission check should not include Operator status as a bypass for permission nodes
	 * @return if the CommandSender has permission to use the given plugin with the given part of VoxelManagement
	 */
	public boolean hasPermission(CommandSender cs, String base, String plugin, boolean ignoreOp) {
		if (plugin == null) plugin = "";
		if (!ignoreOp && cs.isOp()) return true;
		else if (cs.hasPermission("voxelmanagement.*")) return true;
		else if (cs.hasPermission("voxelmanagement." + plugin.toLowerCase())) return true;
		else if (cs.hasPermission("voxelmanagement." + plugin)) return true;
		else if (cs.hasPermission("voxelmanagement." + base + ".*")) return true;
		else if (cs.hasPermission("voxelmanagement." + base + "." + plugin.toLowerCase())) return true;
		else if (cs.hasPermission("voxelmanagement." + base + "." + plugin)) return true;
		return false;
	}

	/**
	 * Check is a CommandSender has a certain permission.
	 * All of the following will return true for the queried permission:
	 * voxelmanagement.*
	 * voxelmanagement.pluginname
	 * voxelmanagement.PluginName
	 * voxelmanagement.base.*
	 * voxelmanagement.base.pluginname
	 * voxelmanagement.base.PluginName
	 *
	 * @param cs The originator of the command
	 * @param base The sub-permission to check within. EX: unload, notify, load, reload, update
	 * @param plugin The name of the plugin
	 * @return if the CommandSender has permission to use the given plugin with the given part of VoxelManagement
	 */
	public boolean hasPermission(CommandSender cs, String base, String plugin) {
		return hasPermission(cs, base, plugin, false);
	}

	/**
	 * Sends a message to all players with the permission node 'voxelmanagement.notify.pluginname'
	 * and without the permission node 'voxelmanagement.notify.ignore.pluginname'
	 *
	 * @param message The message to send to the proper players
	 * @param pluginName The plugin in which the notification spawned from
	 */
	public void notify(String message, String pluginName) {
		getLogger().log(Level.INFO, message);
		message = VOXEL_MANAGEMENT + "message";
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (hasPermission(p, "notify", pluginName) && !hasPermission(p, "notify.ignore", pluginName, true)) {
				p.sendMessage(message);
			}
		}
	}

}
