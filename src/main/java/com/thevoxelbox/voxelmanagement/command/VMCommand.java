package com.thevoxelbox.voxelmanagement.command;

import com.caldabeast.commander.Commander;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * @author CalDaBeast
 */
public class VMCommand extends Commander {

	public final static String VOXEL_MANAGEMENT = ChatColor.DARK_GRAY + "[" + ChatColor.DARK_PURPLE + "Voxel"
			+ ChatColor.DARK_AQUA + "Management" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;

	@Override
	public void ifNoPermission(CommandSender cs, String label, String[] args) {
		cs.sendMessage(VOXEL_MANAGEMENT + "You do not have permission to " + label + " " + args[1]);
	}

	@Override
	public void ifNotSubcommand(CommandSender cs, String label, String[] args) {
		cs.sendMessage(VOXEL_MANAGEMENT + "Subcommands: ");
		cs.sendMessage(VOXEL_MANAGEMENT + "/vm load [plugin]");
		cs.sendMessage(VOXEL_MANAGEMENT + "/vm unload [plugin]");
		cs.sendMessage(VOXEL_MANAGEMENT + "/vm reload [plugin]");
		cs.sendMessage(VOXEL_MANAGEMENT + "/vm check [plugin] (Incomplete)");
		cs.sendMessage(VOXEL_MANAGEMENT + "/vm update [plugin] (Incomplete)");
		cs.sendMessage(VOXEL_MANAGEMENT + "/vm download [plugin]");
	}

}
