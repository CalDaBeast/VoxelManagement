package com.thevoxelbox.voxelmanagement.command;

import com.caldabeast.commander.Subcommand;
import static com.thevoxelbox.voxelmanagement.command.VMCommand.VOXEL_MANAGEMENT;
import java.util.HashMap;
import org.bukkit.command.CommandSender;

public class ContinueSubcommand {

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

	@Subcommand(
			name = "continue",
			alias = {"c"}
	
	)
	public boolean onCommand(CommandSender cs, String label, String[] args) {
		String name = cs.getName();
		if (playerSaved(name)) {
			runPlayer(name);
		} else cs.sendMessage(VOXEL_MANAGEMENT + "You do not have anything queued to continue.");
		return true;
	}

}
