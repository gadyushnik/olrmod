package com.olrmod.commands;

import com.olrmod.artifacts.ArtifactEffectRegistry;
import com.olrmod.weight.WeightManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Loader;

import java.io.File;

public class CommandReloadOLR extends CommandBase {
    @Override
    public String getName() {
        return "reloadolr";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/reloadolr - перезагрузить все конфиги OLR Mod";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        File configDir = Loader.instance().getConfigDir();
        ArtifactEffectRegistry.loadArtifacts();
        WeightManager.loadWeights(configDir);
        sender.sendMessage(new TextComponentString("Конфиги OLR Mod перезагружены."));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
