package com.thevoxelbox.voxelmanagement.command;

import com.thevoxelbox.voxelmanagement.ManagementException;
import com.thevoxelbox.voxelmanagement.VoxelManagement;
import static com.thevoxelbox.voxelmanagement.command.VMCommand.VOXEL_MANAGEMENT;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @author CalDaBeast
 */
public class UnloadCommand implements CommandExecutor {

	private final VoxelManagement management;

	public UnloadCommand(VoxelManagement management) {
		this.management = management;
	}

	@Override
	public boolean onCommand(final CommandSender cs, Command cmnd, String label, String[] args) {
		if (args.length == 1) {
			cs.sendMessage(VOXEL_MANAGEMENT + "You must also include the name of the plugin.");
			return true;
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
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
			ContinueCommand.savePlayer(cs.getName(), new Runnable() {

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

}
