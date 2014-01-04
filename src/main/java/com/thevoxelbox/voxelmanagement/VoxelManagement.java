package com.thevoxelbox.voxelmanagement;

import com.thevoxelbox.voxelmanagement.command.VMCommand;
import static com.thevoxelbox.voxelmanagement.command.VMCommand.VOXEL_MANAGEMENT;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author CalDaBeast
 */
public class VoxelManagement extends JavaPlugin {

	private PluginManagerWrapper manager;

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
		manager.getLookupNames().remove(plugin.getName());
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
		}
		if (notify) notify("The plugin " + plugin.getName() + " has been unloaded from the server.", plugin.getName());
	}

	public void loadPlugin(String pluginName, boolean notify) throws ManagementException {
		Plugin plugin = manager.getPluginManager().getPlugin(pluginName);
		if (plugin != null) {
			throw new ManagementException(ExceptionType.LOAD, pluginName + " is already loaded onto the server.");
		}
		File pluginsFolder = this.getDataFolder().getParentFile();
		File jarFile = null;
		pluginHunt:
		for (File pluginFile : pluginsFolder.listFiles()) {
			String fileName = pluginFile.getAbsolutePath();
			if (!fileName.endsWith(".jar")) continue;
			String jarLink = "jar:file:" + pluginFile.getAbsolutePath() + "!/plugin.yml";
			try {
				URL url = new URL(jarLink);
				InputStream input = ((JarURLConnection) url.openConnection()).getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(input));
				while (true) {
					String line = reader.readLine();
					if (line.startsWith("name:")) {
						String name = line.substring(6);
						if (name.equals(pluginName)) {
							jarFile = pluginFile;
							break pluginHunt;
						} else break;
					}
				}
			} catch (IOException ex) {
				//some jar was invalid. 
				//As long as it isn't the one we're looking for then it doesn't matter.
			}
		}
		if (jarFile == null) {
			throw new ManagementException(ExceptionType.LOAD, "No plugin exists inside of /plugins/ with the name " + pluginName);
		}
		loadPlugin(jarFile, pluginName, notify);
	}

	public void loadPlugin(File jarFile, String pluginName, boolean notify) throws ManagementException {
		Plugin plugin = manager.getPluginManager().getPlugin(pluginName);
		if (plugin != null) unloadPlugin(plugin, false);
		try {
			Plugin loaded = manager.getPluginManager().loadPlugin(jarFile);
			manager.getPluginManager().enablePlugin(loaded);
		} catch (InvalidPluginException | UnknownDependencyException ex) {
			throw new ManagementException(ExceptionType.LOAD, "The Jar File " + jarFile.getName() + " is an invalid plugin and could not be loaded.");
		}
		if (notify) notify(pluginName + " has been loaded onto the server from " + jarFile.getName(), pluginName);
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
		if (plugin == null) plugin = "";
		if (cs.isOp()) return true;
		else if (cs.hasPermission("voxelmanagement.*")) return true;
		else if (cs.hasPermission("voxelmanagement." + plugin.toLowerCase())) return true;
		else if (cs.hasPermission("voxelmanagement." + plugin)) return true;
		else if (cs.hasPermission("voxelmanagement." + base + ".*")) return true;
		else if (cs.hasPermission("voxelmanagement." + base + "." + plugin.toLowerCase())) return true;
		else if (cs.hasPermission("voxelmanagement." + base + "." + plugin)) return true;
		return false;
	}

	/**
	 * Sends a message to all players with the permission node 'voxelmanagement.notify.pluginname'
	 *
	 * @param message The message to send to the proper players
	 * @param pluginName The plugin in which the notification spawned from
	 */
	public void notify(String message, String pluginName) {
		getLogger().log(Level.INFO, message);
		message = VOXEL_MANAGEMENT + "message";
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (hasPermission(p, "notify", pluginName)) p.sendMessage(message);
		}
	}
	
}
