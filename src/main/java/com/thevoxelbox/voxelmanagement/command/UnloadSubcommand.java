package com.thevoxelbox.voxelmanagement.command;

import com.caldabeast.commander.Subcommand;
import com.caldabeast.commander.TabComplete;
import com.thevoxelbox.voxelmanagement.ManagementException;
import com.thevoxelbox.voxelmanagement.VoxelManagement;
import static com.thevoxelbox.voxelmanagement.command.VMCommand.VOXEL_MANAGEMENT;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * @author CalDaBeast
 */
public class UnloadSubcommand {

	private final VoxelManagement management;

	public UnloadSubcommand(VoxelManagement management) {
		this.management = management;
	}

	@Subcommand(
			name = "unload",
			alias = {"ul"}
	)
	public boolean onCommand(final CommandSender cs, String label, String[] args) {
		if (args.length == 0) {
			cs.sendMessage(VOXEL_MANAGEMENT + "You must also include the name of the plugin.");
			return true;
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			builder.append(args[i]).append(i != args.length - 1 ? " " : "");
		}
		final String pluginName = builder.toString();
		if (!management.hasPermission(cs, "unload", label)) {
			cs.sendMessage(VOXEL_MANAGEMENT + "You do not have permission to unload " + pluginName);
			return true;
		}
		if (pluginName.equals("VoxelManagement")) {
			cs.sendMessage(VOXEL_MANAGEMENT + "If you unload VoxelManagement, you will be unable to load it again without reloading or restarting the server.");
			cs.sendMessage(VOXEL_MANAGEMENT + "Type " + ChatColor.ITALIC + "/vm continue" + ChatColor.GRAY + " if you are sure you would like to continue.");
			ContinueSubcommand.savePlayer(cs.getName(), new Runnable() {

				@Override
				public void run() {
					try {
						management.unloadPlugin(pluginName, true);
						cs.sendMessage(VOXEL_MANAGEMENT + "Successfully unloaded " + pluginName);
					} catch (ManagementException ex) {
						cs.sendMessage(VOXEL_MANAGEMENT + ex.getMessage());
					}
				}

			});
			return true;
		}
		try {
			management.unloadPlugin(pluginName, true);
			cs.sendMessage(VOXEL_MANAGEMENT + "Successfully unloaded " + pluginName);
		} catch (ManagementException ex) {
			cs.sendMessage(VOXEL_MANAGEMENT + ex.getMessage());
		}
		return true;
	}

	@TabComplete(
			name = "unload"
	)
	public List<String> onTabComplete(CommandSender cs, String[] args) {
		if (args.length == 1) {
			ArrayList<String> pluginNames = new ArrayList<>();
			for (Plugin plugin : management.manager.getActivePlugins()) {
				pluginNames.add(plugin.getName());
			}
			return pluginNames;
		}
		return null;
	}

}
