package com.olrmod.commands;

import com.olrmod.emission.SafeZoneManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

public class CommandListSafeZones extends CommandBase {

    @Override
    public String getName() {
        return "listsafezones";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/listsafezones";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (SafeZoneManager.getAllZones().isEmpty()) {
            sender.sendMessage(new TextComponentString("Нет зарегистрированных зон убежищ."));
        } else {
            sender.sendMessage(new TextComponentString("Список зон:"));
            SafeZoneManager.getAllZones().keySet().forEach(id ->
                sender.sendMessage(new TextComponentString(" - " + id)));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
