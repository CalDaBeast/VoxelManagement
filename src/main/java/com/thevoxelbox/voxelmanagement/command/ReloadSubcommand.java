package com.thevoxelbox.voxelmanagement.command;

import com.caldabeast.commander.Subcommand;
import com.caldabeast.commander.TabComplete;
import com.thevoxelbox.voxelmanagement.ManagementException;
import com.thevoxelbox.voxelmanagement.VoxelManagement;
import static com.thevoxelbox.voxelmanagement.command.VMCommand.VOXEL_MANAGEMENT;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * @author CalDaBeast
 */
public class ReloadSubcommand {

	private final VoxelManagement management;

	public ReloadSubcommand(VoxelManagement management) {
		this.management = management;
	}

	@Subcommand(
			name = "reload",
			alias = {"rl"}
	)
	public boolean onCommand(CommandSender cs, String label, String[] args) {
		if (args.length == 0) {
			cs.sendMessage(VOXEL_MANAGEMENT + "You must also include the name of the plugin.");
			return true;
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			builder.append(args[i]).append(i != args.length - 1 ? " " : "");
		}
		String pluginName = builder.toString();
		if (!management.hasPermission(cs, "reload", label)) {
			cs.sendMessage(VOXEL_MANAGEMENT + "You do not have permission to reload " + pluginName);
			return true;
		}
		try {
			management.unloadPlugin(pluginName, false);
			management.loadPlugin(pluginName, false);
			management.notify(pluginName + " has been reloaded.", pluginName);
			cs.sendMessage(VOXEL_MANAGEMENT + "Successfully reloaded " + pluginName);
		} catch (ManagementException ex) {
			cs.sendMessage(VOXEL_MANAGEMENT + ex.getMessage());
		}
		return true;
	}

	@TabComplete(
			name = "reload"
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
