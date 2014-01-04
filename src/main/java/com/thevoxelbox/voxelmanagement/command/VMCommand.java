package com.thevoxelbox.voxelmanagement.command;

import com.thevoxelbox.voxelmanagement.VoxelManagement;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VMCommand implements CommandExecutor {

	public final static String VOXEL_MANAGEMENT = ChatColor.DARK_GRAY + "[" + ChatColor.DARK_PURPLE + "Voxel"
			+ ChatColor.DARK_AQUA + "Management" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
	private final UnloadCommand unload;
	private final VoxelManagement management;
	
	public VMCommand(VoxelManagement management){
		this.management = management;
		unload = new UnloadCommand(management);
	}
	
	@Override
	public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
		if (args.length == 0) sendCommandHelp(cs);
		else if (args[0].equalsIgnoreCase("unload")) unload.onCommand(cs, cmnd, label, args);
		else if (args[0].equalsIgnoreCase("commands")) System.out.println(management.manager.getKnownCommands());
		else sendCommandHelp(cs);
		return true;
	}

	public static void sendCommandHelp(CommandSender cs) {
		cs.sendMessage(VOXEL_MANAGEMENT + "Subcommands: ");
		cs.sendMessage(VOXEL_MANAGEMENT + "/vm unload [plugin]");
	}

}
