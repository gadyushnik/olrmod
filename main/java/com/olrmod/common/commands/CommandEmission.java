package com.olrmod.common.commands;

import com.olrmod.common.emission.EmissionManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandEmission extends CommandBase {
    
    @Override
    public String getName() {
        return "emission";
    }
    
    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.emission.usage";
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 2; // Уровень op
    }
    
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, Arrays.asList("start", "stop", "status"));
        }
        
        return Collections.emptyList();
    }
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            // Если команда введена без аргументов, показать справку
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Использование: /emission <start|stop|status>"));
            return;
        }

        World world = server.getWorld(0); // Получаем мир по умолчанию
        
        switch (args[0].toLowerCase()) {
            case "start":
                // Запуск выброса
                if (EmissionManager.isEmissionActive()) {
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + "Выброс уже активен!"));
                } else {
                    EmissionManager.startEmission(world);
                    sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Выброс запущен."));
                }
                break;
                
            case "stop":
                // Остановка выброса
                if (!EmissionManager.isEmissionActive()) {
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + "Выброс не активен!"));
                } else {
                    EmissionManager.stopEmission();
                    sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Выброс остановлен."));
                }
                break;
                
            case "status":
                // Проверка статуса выброса
                if (EmissionManager.isEmissionActive()) {
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + "Выброс активен."));
                } else {
                    sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Выброс не активен."));
                }
                break;
                
            default:
                // Некорректный аргумент
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Неизвестная подкоманда: " + args[0]));
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Использование: /emission <start|stop|status>"));
        }
    }
} 