package com.chaosbuffalo.mkchat;


import com.chaosbuffalo.mkchat.capabilities.Capabilities;
import com.chaosbuffalo.mkchat.command.ChatCommand;
import com.chaosbuffalo.mkchat.dialogue.DialogueManager;
import com.chaosbuffalo.mkchat.init.ChatEntityTypes;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MKChat.MODID)
public class MKChat
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "mkchat";
    private DialogueManager dialogueManager;

    public MKChat() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        ChatEntityTypes.ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void aboutToStart(FMLServerAboutToStartEvent event){
        dialogueManager = new DialogueManager();
        event.getServer().getResourceManager().addReloadListener(dialogueManager);
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        LOGGER.info("In MKChat command registration");
        ChatCommand.register(event.getCommandDispatcher());
    }

    private void setup(final FMLCommonSetupEvent event){
        Capabilities.registerCapabilities();
    }

    public void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("In MKChat client setup");
        RenderingRegistry.registerEntityRenderingHandler(
                ChatEntityTypes.TEST_CHAT.get(),
                PigRenderer::new);
    }

}
