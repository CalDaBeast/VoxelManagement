package com.thevoxelbox.voxelmanagement;

import com.thevoxelbox.voxelmanagement.command.*;
import static com.thevoxelbox.voxelmanagement.command.VMCommand.VOXEL_MANAGEMENT;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
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

	public PluginManagerWrapper manager;

	@Override
	public void onEnable() {
		try {
			manager = new PluginManagerWrapper(Bukkit.getPluginManager());
		} catch (ManagementException ex) {
			getLogger().log(Level.SEVERE, "Unable to load VoxelManagement. Bukkit's PluginManager could not be accessed.");
			return;
		}
		VMCommand command = new VMCommand();
		command.registerSubcommand(new ContinueSubcommand());
		command.registerSubcommand(new DownloadSubcommand(this));
		command.registerSubcommand(new LoadSubcommand(this));
		command.registerSubcommand(new ReloadSubcommand(this));
		command.registerSubcommand(new UnloadSubcommand(this));
		this.getCommand("vm").setExecutor(command);
		this.getCommand("vm").setTabCompleter(command);
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

	/**
	 * Loads the specified plugin onto the plugin from the /plugins/ folder
	 *
	 * @param pluginName the name of the plugin to load
	 * @param notify whether or not the server's administrators should be notified
	 * @throws ManagementException
	 */
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

	/**
	 * Loads the specified file as a plugin onto the server
	 *
	 * @param jarFile the .jar that represents the plugin
	 * @param pluginName the name of the plugin to load
	 * @param notify whether or not the server's administrators should be notified
	 * @throws ManagementException
	 */
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
	 * Downloads the specified plugin from The Voxel Box's Repo server
	 *
	 * @param pluginName the name of the VoxelPlugin to download
	 * @param notify whether or not the server's administrators should be notified
	 * @param load whether or not the plugin should be loaded after being downloaded
	 * @throws com.thevoxelbox.voxelmanagement.ManagementException
	 */
	public void downloadVoxelPlugin(String pluginName, boolean load, boolean notify) throws ManagementException {
		try {
			int buildNumber = getLatestBuildNumber(pluginName);
			URLConnection metadata = new URL("http://ci.thevoxelbox.com/job/" + pluginName + "/" + buildNumber + "/api/xml/").openConnection();
			metadata.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			BufferedReader r = new BufferedReader(new InputStreamReader(metadata.getInputStream(), Charset.forName("UTF-8")));
			String content = r.readLine();
			while (!content.contains("<artifact>")) {
				content = r.readLine();
			}
			int first = content.indexOf("<artifact>");
			int last = content.lastIndexOf("</artifact>") + 11;
			String artifacts = content.substring(first, last);
			while (artifacts.contains("<artifact>")) {
				first = artifacts.indexOf("<artifact>");
				last = artifacts.indexOf("</artifact>") + 11;
				String artifact = artifacts.substring(first, last);
				artifacts = artifacts.substring(last);
				first = artifact.indexOf("<relativePath>") + 14;
				last = artifact.indexOf("</relativePath>");
				String fileName = artifact.substring(first, last);
				if (fileName.endsWith("-javadoc.jar") || fileName.endsWith("-sources.jar")) continue;
				String url = "http://ci.thevoxelbox.com/job/" + pluginName + "/" + buildNumber + "/artifact/" + fileName;
				downloadPlugin(url, pluginName, load, notify);
			}
		} catch (IOException ex) {
			throw new ManagementException(ExceptionType.UPDATE, "Could not download the latest version of " + pluginName);
		}
	}

	/**
	 * @param voxelPluginName The name of the Voxel Box plugin to get the latest build of
	 * @return the latest build number of the specified Voxel Box plugin. -1 if there was a problem.
	 */
	public int getLatestBuildNumber(String voxelPluginName) {
		try {
			//URLConnection metadata = new URL("http://repo.thevoxelbox.com/content/groups/public/com/thevoxelbox/" + pluginName + "/maven-metadata.xml").openConnection();
			URLConnection metadata = new URL("http://ci.thevoxelbox.com/job/" + voxelPluginName + "/api/xml/").openConnection();
			metadata.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			BufferedReader r = new BufferedReader(new InputStreamReader(metadata.getInputStream(), Charset.forName("UTF-8")));
			String content = r.readLine();
			while (!content.contains("<lastSuccessfulBuild>")) {
				content = r.readLine();
			}
			int first = content.indexOf("<lastSuccessfulBuild>") + 21;
			int last = content.indexOf("</lastSuccessfulBuild>");
			String successful = content.substring(first, last);
			first = successful.indexOf("<number>") + 8;
			last = successful.indexOf("</number>");
			String jobNumber = successful.substring(first, last);
			return Integer.parseInt(jobNumber);
		} catch (IOException ex) {
			return -1;
		}
	}

	/**
	 * Downloads the specified plugin
	 *
	 * @param pluginURL the URL of the plugin to download
	 * @param pluginName the name of the plugin to download
	 * @param notify whether or not the server's administrators should be notified
	 * @param load whether of not the plugin should be loaded onto the server after downloading
	 * @throws com.thevoxelbox.voxelmanagement.ManagementException
	 */
	public void downloadPlugin(String pluginURL, String pluginName, boolean load, boolean notify) throws ManagementException {
		try {
			//download file to temparary director
			URLConnection downloadURL = new URL(pluginURL).openConnection();
			downloadURL.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			InputStream input = downloadURL.getInputStream();
			File temp = new File(this.getDataFolder().getAbsolutePath() + "/download_temp.jar");
			temp.createNewFile();
			try (FileOutputStream output = new FileOutputStream(this.getDataFolder().getAbsolutePath() + "/download_temp.jar")) {
				int read;
				byte[] bytes = new byte[1024];
				while ((read = input.read(bytes)) != -1) {
					output.write(bytes, 0, read);
				}
				output.close();
				input.close();
			}
			String fileName;
			String[] split = pluginURL.split("/");
			fileName = split[split.length - 1];
			System.out.println(fileName);
			//delete old file
			File delete = null;
			pluginHunt:
			for (File pluginFile : getDataFolder().getParentFile().listFiles()) {
				if (!pluginFile.getAbsolutePath().endsWith(".jar")) continue;
				String jarLink = "jar:file:" + pluginFile.getAbsolutePath() + "!/plugin.yml";
				URL url = new URL(jarLink);
				InputStream inputStream = ((JarURLConnection) url.openConnection()).getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				while (true) {
					String line = reader.readLine();
					if (line.startsWith("name:")) {
						String possiblePluginName = line.substring(6);
						if (possiblePluginName.equals(pluginName)) {
							delete = pluginFile;
							break pluginHunt;
						} else break;
					}
				}
			}
			if (delete != null) {
				getLogger().log(Level.INFO, "Deleting " + delete.getName() + ", updating to " + fileName);
				delete.delete();
			}
			//set up new file
			File pluginFolderFile = new File(getDataFolder().getParent() + "/" + fileName);
			temp.renameTo(pluginFolderFile);
			if (load) {
				loadPlugin(pluginFolderFile, pluginName, notify);
			} else if (notify) notify(pluginName + " has been downloaded successful.", pluginName);
		} catch (IOException ex) {
			ex.printStackTrace(System.out);
			throw new ManagementException(ExceptionType.UPDATE, "There was a problem downloading " + pluginName);
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
		message = VOXEL_MANAGEMENT + message;
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (hasPermission(p, "notify", pluginName)) p.sendMessage(message);
		}
	}

}
