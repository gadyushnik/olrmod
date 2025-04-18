package com.olrmod.common.commands;

import com.olrmod.common.emission.EmissionManager;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandSafeZone implements ICommand {
    private final List<String> aliases;
    
    public CommandSafeZone() {
        aliases = new ArrayList<>();
        aliases.add("safezone");
        aliases.add("sz");
    }
    
    @Override
    public String getName() {
        return "safezone";
    }
    
    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.safezone.usage";
    }
    
    @Override
    public List<String> getAliases() {
        return aliases;
    }
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Использование: /safezone <add|remove|list>"));
            return;
        }
        
        World world = server.getWorld(0); // Получаем мир по умолчанию
        
        switch (args[0].toLowerCase()) {
            case "add":
                // Создание безопасной зоны
                if (args.length < 7) {
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + 
                        "Использование: /safezone add <x1> <y1> <z1> <x2> <y2> <z2>"));
                    return;
                }
                
                try {
                    int x1 = Integer.parseInt(args[1]);
                    int y1 = Integer.parseInt(args[2]);
                    int z1 = Integer.parseInt(args[3]);
                    int x2 = Integer.parseInt(args[4]);
                    int y2 = Integer.parseInt(args[5]);
                    int z2 = Integer.parseInt(args[6]);
                    
                    BlockPos pos1 = new BlockPos(x1, y1, z1);
                    BlockPos pos2 = new BlockPos(x2, y2, z2);
                    
                    EmissionManager.createSafeZone(world, pos1, pos2);
                    sender.sendMessage(new TextComponentString(TextFormatting.GREEN + 
                        "Безопасная зона создана от " + pos1 + " до " + pos2));
                } catch (NumberFormatException e) {
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + 
                        "Координаты должны быть числами!"));
                }
                break;
                
            case "remove":
                // Удаление безопасной зоны
                if (args.length < 4) {
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + 
                        "Использование: /safezone remove <x> <y> <z>"));
                    return;
                }
                
                try {
                    int x = Integer.parseInt(args[1]);
                    int y = Integer.parseInt(args[2]);
                    int z = Integer.parseInt(args[3]);
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    
                    boolean removed = EmissionManager.removeSafeZone(world, pos);
                    if (removed) {
                        sender.sendMessage(new TextComponentString(TextFormatting.GREEN + 
                            "Безопасная зона, содержащая " + pos + ", была удалена"));
                    } else {
                        sender.sendMessage(new TextComponentString(TextFormatting.RED + 
                            "Безопасная зона, содержащая " + pos + ", не найдена"));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + 
                        "Координаты должны быть числами!"));
                }
                break;
                
            case "list":
                // TODO: Добавить вывод списка безопасных зон
                sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + 
                    "Список безопасных зон пока не реализован"));
                break;
                
            default:
                sender.sendMessage(new TextComponentString(TextFormatting.RED + 
                    "Неизвестная подкоманда: " + args[0]));
                sender.sendMessage(new TextComponentString(TextFormatting.RED + 
                    "Использование: /safezone <add|remove|list>"));
        }
    }
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender.canUseCommand(2, "safezone"); // Требуется уровень OP 2 или выше
    }
    
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "add", "remove", "list");
        }
        return Collections.emptyList();
    }
    
    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }
    
    @Override
    public int compareTo(ICommand o) {
        return getName().compareTo(o.getName());
    }
    
    private List<String> getListOfStringsMatchingLastWord(String[] args, String... possibilities) {
        List<String> list = new ArrayList<>();
        for (String s : possibilities) {
            if (s.startsWith(args[args.length - 1])) {
                list.add(s);
            }
        }
        return list;
    }
} 