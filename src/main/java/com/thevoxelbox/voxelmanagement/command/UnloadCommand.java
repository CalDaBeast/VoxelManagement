package com.thevoxelbox.voxelmanagement.command;

import com.thevoxelbox.voxelmanagement.ManagementException;
import com.thevoxelbox.voxelmanagement.VoxelManagement;
import static com.thevoxelbox.voxelmanagement.command.VMCommand.VOXEL_MANAGEMENT;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UnloadCommand implements CommandExecutor {

	private final VoxelManagement management;

	public UnloadCommand(VoxelManagement management) {
		this.management = management;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
		StringBuilder builder = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			builder.append(args[i]).append(i != args.length - 1 ? " " : "");
		}
		String pluginName = builder.toString();
		if (!management.hasPermission(cs, "", label)) {
			cs.sendMessage(VOXEL_MANAGEMENT + "You do not have permission to unload " + pluginName);
			return true;
		}
		try {
			management.unloadPlugin(pluginName, true);
			cs.sendMessage(VOXEL_MANAGEMENT + pluginName);
		} catch (ManagementException ex) {
			cs.sendMessage(VOXEL_MANAGEMENT + ex.getMessage());
		}
		return true;
	}

}
