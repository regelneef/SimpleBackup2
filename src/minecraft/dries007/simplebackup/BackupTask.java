package dries007.simplebackup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;

public class BackupTask 
{
	private int maxBackups = SimpleBackup.maxBackups;
	private boolean activateMax = SimpleBackup.activateMax;
	private String basePath = SimpleBackup.basePath;
	private List<String> fileList;
	private MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
	
	public BackupTask()
	{
		sendWaring();
		fullsave();
		
		String msg = "Failed to check for simplebackup updates.";
		try
		{
			if(SimpleBackup.versionCheck()) msg = "Your server is protected by SimpleBackup version " + SimpleBackup.VERSION + ". Up to date!";
			else msg = "SimpleBackup is out of date! Get the new version at http://www.dries007.net/SimpleBackup/";
		}
		catch(Exception ex){}
		FMLLog.warning(msg);
		server.logWarning(msg);
		
		for(File folder : SimpleBackup.folders)
		{
			doFolder(folder);
		}
		
		sendDone();
	}

	public BackupTask(File[] folders)
	{
		sendWaring();
		fullsave();
		
		String msg = "Failed to check for simplebackup updates.";
		try
		{
			if(SimpleBackup.versionCheck()) msg = "Your server is protected by SimpleBackup version " + SimpleBackup.VERSION + ". Up to date!";
			else msg = "SimpleBackup is out of date! Get the new version at http://www.dries007.net/SimpleBackup/";
		}
		catch(Exception ex){}
		FMLLog.warning(msg);
		server.logWarning(msg);
				
		for(File folder : folders)
		{
			doFolder(folder);
		}
		
		sendDone();
	}
	
	private void sendWaring() 
	{
		server.logInfo(SimpleBackup.messageWaring);
		if(SimpleBackup.sentToAll)server.getConfigurationManager().sendPacketToAllPlayers(new Packet3Chat("\u00a75" + SimpleBackup.messageWaring));
		else
		{
			for (Object username : server.getConfigurationManager().getOps())
			{
				server.getConfigurationManager().getPlayerForUsername((String) username).sendChatToPlayer("\u00a75" + SimpleBackup.messageWaring);
			}
		}
		
	}

	private void sendDone() 
	{
		server.logInfo(SimpleBackup.messageDone);
		if(SimpleBackup.sentToAll)server.getConfigurationManager().sendPacketToAllPlayers(new Packet3Chat("\u00a75" + SimpleBackup.messageDone));
		else
		{
			for (Object username : server.getConfigurationManager().getOps())
			{
				server.getConfigurationManager().getPlayerForUsername((String) username).sendChatToPlayer("\u00a75" + SimpleBackup.messageDone);
			}
		}
	}
	
	/**
	 * Save all data of all worlds
	 */
	private void fullsave()
	{
		server.getConfigurationManager().saveAllPlayerData();
		
		for (WorldServer world : server.worldServers)
        {
			boolean var6 = world.canNotSave;
			world.canNotSave = false;
			try
			{
				world.saveAllChunks(true, (IProgressUpdate)null);
			}
			catch (MinecraftException e) 
			{
				FMLLog.severe("SAVING WORLD " + world.getWorldInfo().getWorldName() + " DIM " + world.getWorldInfo().getDimension() + " FAILED!");
				e.printStackTrace();
			}
			world.canNotSave = var6;
        }
	}
	
	/**
	 * make a folder backup
	 * @param folder
	 */
	private void doFolder(File folder)
	{
		if(activateMax) checkMax(basePath + File.separator + folder.getName());
		
		fileList = new ArrayList<String>();
		String dir = folder.getAbsolutePath().replace(folder.getName(), "");
			
		generateFileList(dir, folder);
		zipIt(dir, basePath + File.separator + folder.getName() + File.separator + getFilename());
		
		server.logInfo("Backup of '" + folder.getName() + "' saved.");
	}
	
	/**
	 * Make the filelist
	 * @param dir
	 * @param node
	 */
	private void generateFileList(String dir, File node)
    {
    	if(node.isFile())
    	{
    		fileList.add(generateZipEntry(dir, node.getAbsolutePath().toString()));
    	}
    	
    	if(node.isDirectory()){
    		String[] subNote = node.list();
    		for(String filename : subNote){
    			generateFileList(dir, new File(node, filename));
    		}
    	}
    }
	
	 /**
     * format the entry
     * @param dir
     * @param file
     * @return
     */
	private String generateZipEntry(String dir, String file)
    {
    	return file.substring(dir.length(), file.length());
    }

	/**
	 * Make the actual zip from the filelist
	 * @param dir
	 * @param filename
	 */
	private void zipIt(String dir, String filename)
    {
    	byte[] buffer = new byte[1024]; 
    	try
    	{ 
    		FileOutputStream fos = new FileOutputStream(filename + ".zip");
    		ZipOutputStream zos = new ZipOutputStream(fos);
 
    		for(String file : fileList)
    		{
    			ZipEntry ze= new ZipEntry(file);
    			zos.putNextEntry(ze);
 
    			FileInputStream in = new FileInputStream(dir + file);
 
    			int len;
    			while ((len = in.read(buffer)) > 0) 
    			{
    				zos.write(buffer, 0, len);
    			}
 
    			in.close();
    		}
 
    		zos.closeEntry();
    		//remember close it
    		zos.close();
    	}
    	catch(IOException ex)
    	{
    		FMLLog.severe(ex.getMessage());
    	}
    }

	
	/**
	 * Formats the backups filename
	 * @return
	 */
	private String getFilename()
	{
		Calendar cal = Calendar.getInstance();
		String day = cal.get(cal.DAY_OF_MONTH) + "";
		String month = cal.get(cal.MONTH) + "";
		String year = cal.get(cal.YEAR) + "";
		String hour = cal.get(cal.HOUR_OF_DAY) + "";
		String min = cal.get(cal.MINUTE) + "";
		return SimpleBackup.filenameTemplate.replaceAll("%day", day).replaceAll("%month", month).replaceAll("%year", year).replaceAll("%hour", hour).replaceAll("%min", min);
	}
	
	/**
	 * Check to see if max amount of backups is reached
	 * @param folderPath
	 */
	private void checkMax(String folderPath)
	{
		File folder = new File(folderPath);
		File[] files = folder.listFiles();
		if (files.length > maxBackups)
		{
			Comparator comparator = new Comparator<File> ()
			{
				public int compare(File f1, File f2)
				{
					return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
				}
			};
			
			Arrays.sort(files, comparator);
			
			for (int i = maxBackups; i < files.length; i++)
			{
				server.logInfo("Delete old backup: " + files[i].getName());
				files[i].delete();
			}
		}
	}
}
