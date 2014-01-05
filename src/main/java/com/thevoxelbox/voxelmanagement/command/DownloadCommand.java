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
public class DownloadCommand implements CommandExecutor {

	private final VoxelManagement management;

	public DownloadCommand(VoxelManagement management) {
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
		if (!management.hasPermission(cs, "reload", label)) {
			cs.sendMessage(VOXEL_MANAGEMENT + "You do not have permission to reload " + pluginName);
			return true;
		}
		try {
			management.downloadVoxelPlugin(pluginName, false, true);
			cs.sendMessage(VOXEL_MANAGEMENT + "Successfully downloaded " + pluginName);
			cs.sendMessage(VOXEL_MANAGEMENT + "Type " + ChatColor.ITALIC + "/vm continue" + ChatColor.GRAY + " if you would like to load " + pluginName + " onto the server.");
			ContinueCommand.savePlayer(cs.getName(), new Runnable() {

				@Override
				public void run() {
					try {
						management.loadPlugin(pluginName, true);
						cs.sendMessage(VOXEL_MANAGEMENT + "Successfully loaded " + pluginName);
					} catch (ManagementException ex) {
						cs.sendMessage(VOXEL_MANAGEMENT + ex.getMessage());
					}
				}

			});
		} catch (ManagementException ex) {
			cs.sendMessage(VOXEL_MANAGEMENT + ex.getMessage());
		}
		return true;
	}

}
