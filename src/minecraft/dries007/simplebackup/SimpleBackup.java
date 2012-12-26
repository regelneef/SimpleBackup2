package dries007.simplebackup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MinecraftError;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarted;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = "SimpleBackup", name = "SimpleBackup", version = "2.0")
public class SimpleBackup 
{
	public static final String VERSION = "2.0";
	
	private static MinecraftServer server;
	private static boolean isClient;

	public static Configuration config;
	public static File configFile;
	public static String filenameTemplate;
	public static String basePath;
	public static List<File> folders = new ArrayList<File>();
	
	public static int interval;
	public static boolean useInterval;
	public static boolean backupIfEmpty;
	
	public static int maxBackups;
	public static boolean activateMax;
	
	public static String messageWaring;
	public static String messageDone;
	public static boolean sentToAll;

	private static List<String> fileList;
	
	@PreInit
	public void preInit(FMLPreInitializationEvent e)
	{
		if(e.getSide().isClient())
		{
			isClient = true;
			return;
		}
		configFile = e.getSuggestedConfigurationFile();
	}
	
	private void versionCheck() 
	{
		try
		{
			URL versionFile = new URL("http://driesgames.game-server.cc/SimpleBackup/version.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(versionFile.openStream()));
			String version = reader.readLine();
			if(!version.equals(VERSION))
			{
				FMLLog.warning("SimpleBackup is out of date! Get the new version at http://www.dries007.net/SimpleBackup/");
				server.logWarning("SimpleBackup is out of date! Get the new version at http://www.dries007.net/SimpleBackup/");
			}
		}
		catch (Exception e)
		{
			server.logWarning("Failed to check for simplebackup updates.");
			FMLLog.warning("Failed to check for simplebackup updates.");
			e.printStackTrace();
		}
	}

	@ServerStarting
	public void serverStarting(FMLServerStartingEvent e)
	{
		if(isClient) return;
		server = e.getServer();
		e.registerServerCommand(new CommandBackup());
		doConfig();
	}
	
	@ServerStarted
	public void serverStarted(FMLServerStartedEvent e)
	{
		if(isClient) return;
		initiateTimer();
		versionCheck();
	}
	
	private void initiateTimer() 
	{
		if(useInterval) new Thread(new BackupLoop(), "Minecraft - SimpleBackup - interval thread").start();
	}

	/**
	 * config time!
	 */
	public static void doConfig()
	{
		String cat = "SimpleBackup";
		String worlddir = server.getFolderName();
		config = new Configuration(configFile);
		
		messageWaring = config.get(cat + ".Message", "message-Waring", "Making a server backup!", "The message will be send in purple.").value;
		messageDone = config.get(cat + ".Message", "message-Done", "Backup done.", "The message will be send in purple.").value;
		sentToAll = config.get(cat + ".Message", "sentToAll", true, "If false, only console and the ops will get the message.").getBoolean(true);		
		
		interval = config.get(cat + ".Interval", "interval", 15, "Value in minutes").getInt(15);
		useInterval = config.get(cat + ".Interval", "useInterval", true, "if true, make a backup every X minutes, If false only manually").getBoolean(true);
		backupIfEmpty = config.get(cat + ".Interval", "backupIfEmply", false, "Make a backup if there are 0 players online.").getBoolean(false);
		
		maxBackups = config.get(cat + ".MaxBackups", "maxBackups", 100, "If the amount of files in the backup folder gets higher than this number, delete the oldest one.").getInt(100);
		activateMax = config.get(cat + ".MaxBackups", "activateMax", true, "If you don't have to worry about diskspace, set this to false :p").getBoolean(true);
		
		
		basePath = config.get(cat + ".Filename", "backupFolder", "backup", "Gets made if it doesn't exist").value;
		filenameTemplate = config.get(cat + ".Filename", "filename", "%day-%month-%year_%hourh%min", "The %-codes get replaced by there appropriate value.").value;
		
		String[] folderList = config.get(cat + ".Folders", "folders", new String[]{"config", worlddir}, "Folders to backup (config & worldname). Waring: Case sensitive!").valueList;

		for (String folderName : folderList)
		{
			File folder = new File(folderName);
			if(folder.exists() && folder.isDirectory())
			{
				new File(basePath, folderName).mkdirs();
				folders.add(folder);
			}
			else
			{
				FMLLog.severe("WARING! The folder '" + "' isnt a folder or it doesn't exist.");
			}
		}
		
		config.save();
	}
	
	
	/**
	 * Get all the folder names in String form.
	 * @return
	 */
	public static List<String> getFolders()
	{
		List<String> list = new ArrayList();
		for(File folder : SimpleBackup.folders)
		{
			list.add(folder.getName());
		}
		return list;
	}
    
}
