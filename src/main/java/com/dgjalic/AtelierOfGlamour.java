package com.dgjalic;

import com.dgjalic.command.AtelierCommands;
import com.dgjalic.registry.AtelierItems;
import com.dgjalic.registry.AtelierRecipeSerializers;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AtelierOfGlamour implements ModInitializer {
    public static final String MOD_ID = "atelier_of_glamour";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        AtelierItems.register();
        AtelierRecipeSerializers.register();
        AtelierCommands.register();
    }
}
