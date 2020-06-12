package the_fireplace.fans.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;
import the_fireplace.fans.Fans;
import the_fireplace.fans.client.screen.Generic1x1ContainerScreen;
import the_fireplace.fans.screen.Generic1x1ContainerScreenHandler;

@Environment(EnvType.CLIENT)
public class FansClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerGuis();
    }

    private void registerGuis() {
        assert MinecraftClient.getInstance().player != null;
        ScreenProviderRegistry.INSTANCE.<Generic1x1ContainerScreenHandler>registerFactory(Fans.FAN_BLOCK_ENTITY_ID,
            (container) -> new Generic1x1ContainerScreen(container, MinecraftClient.getInstance().player.inventory, new TranslatableText("container.fan")));
    }
}
