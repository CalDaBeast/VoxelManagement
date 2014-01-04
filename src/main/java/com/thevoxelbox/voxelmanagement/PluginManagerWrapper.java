package com.thevoxelbox.voxelmanagement;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;

/**
 * @author CalDaBeast
 */
public class PluginManagerWrapper {

	private final SimplePluginManager manager;
	private final List<Plugin> activePlugins;
	private final Map<String, Plugin> lookupNames;
	private final SimpleCommandMap commandMap;

	/**
	 * @param pluginManager Bukkit's SimplePluginManager (via Bukkit.getPluginManager())
	 * @throws ManagementException if there was an error accessing data from the Plugin Manager
	 */
	protected PluginManagerWrapper(PluginManager pluginManager) throws ManagementException {
		if(!(pluginManager instanceof SimplePluginManager)){
			throw new IllegalArgumentException("The PluginManager must be of the SimplePluginManager type.");
		}
		this.manager = (SimplePluginManager) pluginManager;
		//use reflection to access private fields
		try {
			Field pluginsField = manager.getClass().getDeclaredField("plugins");
			pluginsField.setAccessible(true);
			activePlugins = (List<Plugin>) pluginsField.get(manager);
			Field lookupNamesField = manager.getClass().getDeclaredField("lookupNames");
			lookupNamesField.setAccessible(true);
			lookupNames = (Map<String, Plugin>) lookupNamesField.get(manager);
			Field commandMapField = manager.getClass().getDeclaredField("commandMap");
			commandMapField.setAccessible(true);
			commandMap = (SimpleCommandMap) commandMapField.get(manager);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace(System.out);
			throw new ManagementException(ExceptionType.OTHER, "Unable to access Bukkit's Plugin Manager.");
		}
	}
	
	/**
	 * @return The SimplePluginManager being referenced
	 */
	public SimplePluginManager getPluginManager(){
		return manager;
	}

	/**
	 * @return The list of Active Plugins as contained by the SimplePluginManager
	 */
	public List<Plugin> getActivePlugins() {
		return activePlugins;
	}

	/**
	 * @return The Lookup Names map as contained by the SimplePluginManager
	 */
	public Map<String, Plugin> getLookupNames() {
		return lookupNames;
	}

	/**
	 * @return The SimpleCommandMap as contained by the SimplePluginManager
	 */
	public SimpleCommandMap getCommandMap() {
		return commandMap;
	}
	
}
