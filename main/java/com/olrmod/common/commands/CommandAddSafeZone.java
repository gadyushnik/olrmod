package com.olrmod.commands;

import com.olrmod.emission.SafeZoneManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.util.HashMap;
import java.util.Map;

public class CommandAddSafeZone extends CommandBase {
    private static final Map<String, BlockPos> playerFirstPos = new HashMap<>();

    @Override
    public String getName() {
        return "addsafezone";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/addsafezone <id>";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayer)) {
            sender.sendMessage(new TextComponentString("Команда доступна только игрокам"));
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(new TextComponentString("Использование: /addsafezone <id>"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;
        String id = args[0];

        if (!playerFirstPos.containsKey(player.getName())) {
            playerFirstPos.put(player.getName(), player.getPosition());
            player.sendMessage(new TextComponentString("Первая точка зоны сохранена. Теперь встаньте на вторую и повторите команду."));
        } else {
            BlockPos first = playerFirstPos.remove(player.getName());
            BlockPos second = player.getPosition();
            SafeZoneManager.addZone(id, first, second);
            player.sendMessage(new TextComponentString("Зона \"" + id + "\" успешно создана."));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
