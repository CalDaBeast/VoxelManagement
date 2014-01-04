package com.thevoxelbox.voxelmanagement.command;

import static com.thevoxelbox.voxelmanagement.command.VMCommand.VOXEL_MANAGEMENT;
import java.util.HashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ContinueCommand implements CommandExecutor {

	private final static HashMap<String, Runnable> playerContinues = new HashMap<>();

	public static void savePlayer(String name, Runnable run) {
		playerContinues.put(name, run);
	}

	public static boolean playerSaved(String name) {
		return playerContinues.containsKey(name);
	}

	public static void runPlayer(String name) {
		Runnable cont = playerContinues.get(name);
		playerContinues.remove(name);
		cont.run();
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
		String name = cs.getName();
		if (playerSaved(name)) {
			runPlayer(name);
		} else cs.sendMessage(VOXEL_MANAGEMENT + "You do not have anything queued to continue.");
		return true;
	}

}
