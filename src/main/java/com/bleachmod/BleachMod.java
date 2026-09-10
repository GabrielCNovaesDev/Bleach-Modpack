package com.bleachmod;

import com.bleachmod.client.BleachClient;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Reference.MOD_ID)
public class BleachMod {
    public static final Logger LOGGER = LogUtils.getLogger();

    public BleachMod(FMLJavaModLoadingContext context) {
        BleachCommon.init(context);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BleachClient.init(context));
    }
}
