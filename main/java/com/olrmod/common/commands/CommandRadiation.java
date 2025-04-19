package com.olrmod.common.commands;

import com.olrmod.common.radiation.RadiationManager;
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

public class CommandRadiation extends CommandBase {
    
    @Override
    public String getName() {
        return "radiation";
    }
    
    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.radiation.usage";
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 2; // Уровень op
    }
    
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, Arrays.asList("create", "remove", "check"));
        }
        
        if (args.length == 2) {
            if (args[0].equals("create")) {
                return getListOfStringsMatchingLastWord(args, Arrays.asList("1", "2", "3"));
            }
        }
        
        return Collections.emptyList();
    }
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException("commands.radiation.usage");
        }
        
        if (!(sender instanceof EntityPlayer)) {
            sender.sendMessage(new TextComponentString("Эту команду может использовать только игрок"));
            return;
        }
        
        EntityPlayer player = (EntityPlayer) sender;
        World world = player.world;
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                handleCreate(world, player, args);
                break;
            case "remove":
                handleRemove(world, player);
                break;
            case "check":
                handleCheck(world, player);
                break;
            default:
                throw new WrongUsageException("commands.radiation.usage");
        }
    }
    
    private void handleCreate(World world, EntityPlayer player, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new WrongUsageException("commands.radiation.create.usage");
        }
        
        int level;
        try {
            level = Integer.parseInt(args[1]);
            if (level < 1 || level > 3) {
                throw new WrongUsageException("Уровень радиации должен быть от 1 до 3");
            }
        } catch (NumberFormatException e) {
            throw new WrongUsageException("Неверный формат уровня радиации");
        }
        
        BlockPos currentPos = player.getPosition();
        
        // Создаем зону радиации 9x9 вокруг игрока
        BlockPos pos1 = new BlockPos(currentPos.getX() - 4, currentPos.getY() - 4, currentPos.getZ() - 4);
        BlockPos pos2 = new BlockPos(currentPos.getX() + 4, currentPos.getY() + 4, currentPos.getZ() + 4);
        
        RadiationManager.createRadiationZone(world, pos1, pos2, level);
        
        player.sendMessage(new TextComponentString(TextFormatting.GREEN + 
            "Создана радиационная зона уровня " + level + " от " + pos1 + " до " + pos2));
    }
    
    private void handleRemove(World world, EntityPlayer player) {
        BlockPos currentPos = player.getPosition();
        
        boolean removed = RadiationManager.removeRadiationZone(world, currentPos);
        
        if (removed) {
            player.sendMessage(new TextComponentString(TextFormatting.GREEN + 
                "Радиационная зона удалена"));
        } else {
            player.sendMessage(new TextComponentString(TextFormatting.RED + 
                "В этой точке нет радиационной зоны"));
        }
    }
    
    private void handleCheck(World world, EntityPlayer player) {
        BlockPos currentPos = player.getPosition();
        
        int level = RadiationManager.getRadiationLevel(world, currentPos);
        
        if (level > 0) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + 
                "Вы находитесь в радиационной зоне уровня " + level));
        } else {
            player.sendMessage(new TextComponentString(TextFormatting.GREEN + 
                "Вы не находитесь в радиационной зоне"));
        }
    }
} 