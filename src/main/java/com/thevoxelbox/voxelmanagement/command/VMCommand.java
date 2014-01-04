package com.thevoxelbox.voxelmanagement.command;

import com.thevoxelbox.voxelmanagement.VoxelManagement;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @author CalDaBeast
 */
public class VMCommand implements CommandExecutor {

	public final static String VOXEL_MANAGEMENT = ChatColor.DARK_GRAY + "[" + ChatColor.DARK_PURPLE + "Voxel"
			+ ChatColor.DARK_AQUA + "Management" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
	private final UnloadCommand unload;
	private final LoadCommand load;
	private final ReloadCommand reload;
	private final ContinueCommand cont;
	
	public VMCommand(VoxelManagement management){
		unload = new UnloadCommand(management);
		load = new LoadCommand(management);
		reload = new ReloadCommand(management);
		cont = new ContinueCommand();
	}
	
	@Override
	public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
		if (args.length == 0) sendCommandHelp(cs);
		else if (args[0].equalsIgnoreCase("unload")) unload.onCommand(cs, cmnd, label, args);
		else if (args[0].equalsIgnoreCase("load")) load.onCommand(cs, cmnd, label, args);
		else if (args[0].equalsIgnoreCase("reload")) reload.onCommand(cs, cmnd, label, args);
		else if (args[0].equalsIgnoreCase("continue") || args[0].equalsIgnoreCase("c")) cont.onCommand(cs, cmnd, label, args);
		else sendCommandHelp(cs);
		return true;
	}

	public static void sendCommandHelp(CommandSender cs) {
		cs.sendMessage(VOXEL_MANAGEMENT + "Subcommands: ");
		cs.sendMessage(VOXEL_MANAGEMENT + "/vm load [plugin]");
		cs.sendMessage(VOXEL_MANAGEMENT + "/vm unload [plugin]");
		cs.sendMessage(VOXEL_MANAGEMENT + "/vm reload [plugin]");
		cs.sendMessage(VOXEL_MANAGEMENT + "/vm check [plugin] (Incomplete)");
		cs.sendMessage(VOXEL_MANAGEMENT + "/vm update [plugin] (Incomplete)");
	}

}
