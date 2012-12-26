package dries007.simplebackup;

import java.io.File;
import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

public class CommandBackup extends CommandBase
{
	@Override
	public String getCommandName() 
	{
		return "backup";
	}

	@Override
	public List getCommandAliases()
    {
        return null;
    }
	
	@Override
	public int getRequiredPermissionLevel()
	{
		return 4;
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args)
	{
		if(args.length == 0) return this.getListOfStringsMatchingLastWord(args, "", "make");
		if(args.length == 1) return this.getListOfStringsFromIterableMatchingLastWord(args, SimpleBackup.getFolders());
        return null;
    }
	
	@Override
	public void processCommand(ICommandSender sender, String[] args) 
	{
		if (args.length == 0) sendInfo(sender);
		else if (args.length == 1)
		{
			if (args[0].equalsIgnoreCase("make"))
			{
				sender.sendChatToPlayer("Making a backup of all folders.");
				new BackupTask();
			}
			else throw new WrongUsageException(getCommandUsage(sender), new Object[0]);
		}
		else if (args.length == 2)
		{
			if(args[0].equalsIgnoreCase("make"))
			{
				if(SimpleBackup.getFolders().contains(args[1]))
				{
					sender.sendChatToPlayer("Making a backup of " + args[1] + ".");
					new BackupTask(new File[]{new File(args[1])});
				}
				else
				{
					sender.sendChatToPlayer("The folder you specified is not in the list of available folders!");
					sender.sendChatToPlayer("Here is a list: " + SimpleBackup.getFolders().toString());
				}
			}
			else throw new WrongUsageException(getCommandUsage(sender), new Object[0]);
		}
		else throw new WrongUsageException(getCommandUsage(sender), new Object[0]);
	}
	
	public void sendInfo(ICommandSender sender)
	{
		sender.sendChatToPlayer("\u00a7a" + "=== Backup Status ===");
		
		String msg = "Failed to check for simplebackup updates.";
		try
		{
			if(SimpleBackup.versionCheck()) msg = "SimpleBackup version " + SimpleBackup.VERSION + ". Up to date!";
			else msg = "SimpleBackup is out of date! Get the new version at http://www.dries007.net/SimpleBackup/";
		}
		catch(Exception ex){}
		sender.sendChatToPlayer(msg);
		
		if (SimpleBackup.useInterval)
		{
			sender.sendChatToPlayer("Backup interval: " + SimpleBackup.interval + " minutes.");
			sender.sendChatToPlayer("Backup when 0 players online: " + SimpleBackup.backupIfEmpty);
		}
		else
		{
			sender.sendChatToPlayer("No backup on interval.");
		}
		if(SimpleBackup.activateMax)
		{
			sender.sendChatToPlayer("Maximum amount of backups: " + SimpleBackup.maxBackups);
		}
		else 
		{
			sender.sendChatToPlayer("Old backups won't get deleted.");
		}
		if(SimpleBackup.sentToAll)
		{
			sender.sendChatToPlayer("Messages for all players:");
		}
		else
		{
			sender.sendChatToPlayer("Message for ops & console only:");
		}
		sender.sendChatToPlayer("-> Waring message: \"" + SimpleBackup.messageWaring + "\"");
		sender.sendChatToPlayer("-> Done message: \"" + SimpleBackup.messageDone + "\"");
	}

}
