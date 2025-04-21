package com.olrmod.commands;

import com.olrmod.emission.SafeZoneManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CommandTeleportToZone extends CommandBase {

    @Override
    public String getName() {
        return "teleporttozone";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/teleporttozone <id>";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayerMP)) {
            sender.sendMessage(new TextComponentString("Только игрок может использовать эту команду."));
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(new TextComponentString("Использование: /teleporttozone <id>"));
            return;
        }

        AxisAlignedBB zone = SafeZoneManager.getAllZones().get(args[0]);
        if (zone == null) {
            sender.sendMessage(new TextComponentString("Зона с ID \"" + args[0] + "\" не найдена."));
            return;
        }

        BlockPos center = new BlockPos(zone.getCenter());
        ((EntityPlayerMP) sender).connection.setPlayerLocation(center.getX(), center.getY(), center.getZ(), 0, 0);
        sender.sendMessage(new TextComponentString("Телепортировано в центр зоны \"" + args[0] + "\"."));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
