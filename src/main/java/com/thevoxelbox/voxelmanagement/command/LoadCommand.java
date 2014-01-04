package com.thevoxelbox.voxelmanagement.command;

import com.thevoxelbox.voxelmanagement.ManagementException;
import com.thevoxelbox.voxelmanagement.VoxelManagement;
import static com.thevoxelbox.voxelmanagement.command.VMCommand.VOXEL_MANAGEMENT;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @author CalDaBeast
 */
public class LoadCommand implements CommandExecutor {

	private final VoxelManagement management;

	public LoadCommand(VoxelManagement management) {
		this.management = management;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
		if (args.length == 1) {
			cs.sendMessage(VOXEL_MANAGEMENT + "You must also include the name of the plugin.");
			return true;
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			builder.append(args[i]).append(i != args.length - 1 ? " " : "");
		}
		String pluginName = builder.toString();
		if (!management.hasPermission(cs, "load", label)) {
			cs.sendMessage(VOXEL_MANAGEMENT + "You do not have permission to load " + pluginName);
			return true;
		}
		try {
			management.loadPlugin(pluginName, true);
			cs.sendMessage(VOXEL_MANAGEMENT + "Successfully loaded " + pluginName);
		} catch (ManagementException ex) {
			cs.sendMessage(VOXEL_MANAGEMENT + ex.getMessage());
		}
		return true;
	}

}
