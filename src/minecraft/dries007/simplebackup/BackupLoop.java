package dries007.simplebackup;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;

public class BackupLoop  implements Runnable
{
	private boolean run = true;
	
	@Override
	public void run() 
	{
		while(run)
		{
			try 
			{
				//			 ms>s  s>m
				Thread.sleep(1000 * 60 * SimpleBackup.interval);
			}
			catch (final InterruptedException e){}
			
			if(FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getAllUsernames().length == 0)
			{
				if(SimpleBackup.backupIfEmpty)
				{
					new BackupTask();
				}
				else
				{
					FMLCommonHandler.instance().getMinecraftServerInstance().logInfo("Backup skipped.");
				}
			}
			else
			{
				new BackupTask();
			}
		}
	}

}
