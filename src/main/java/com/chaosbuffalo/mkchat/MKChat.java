package com.chaosbuffalo.mkchat;


import com.chaosbuffalo.mkchat.capabilities.ChatCapabilities;
import com.chaosbuffalo.mkchat.command.ChatCommands;
import com.chaosbuffalo.mkchat.dialogue.DialogueManager;
import com.chaosbuffalo.mkchat.dialogue.IDialogueExtension;
import com.chaosbuffalo.mkchat.event.DialogueManagerSetupEvent;
import com.chaosbuffalo.mkchat.init.ChatEntityTypes;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
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
    public static final String REGISTER_DIALOGUE_EXTENSION = "register_dialogue_extension";
    private DialogueManager dialogueManager;

    public MKChat() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        ChatEntityTypes.ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        dialogueManager = new DialogueManager();
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        LOGGER.info("In MKChat command registration");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event){
        ChatCommands.register(event.getDispatcher());
    }

    private void setup(final FMLCommonSetupEvent event){
        ChatCapabilities.registerCapabilities();
        DialogueManager.dialogueSetup();
        ChatEntityTypes.setupAttributes();
    }


    private void processIMC(final InterModProcessEvent event)
    {
        MKChat.LOGGER.info("MKChat.processIMC");
        event.getIMCStream().forEach(m -> {
            if (m.getMethod().equals(REGISTER_DIALOGUE_EXTENSION)) {
                MKChat.LOGGER.info("IMC register dialogue extension from mod {} {}", m.getSenderModId(),
                        m.getMethod());
                IDialogueExtension ext = (IDialogueExtension) m.getMessageSupplier().get();
                ext.registerDialogueExtension();
            }
        });
    }

    public void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("In MKChat client setup");
        RenderingRegistry.registerEntityRenderingHandler(
                ChatEntityTypes.TEST_CHAT.get(),
                PigRenderer::new);
    }

}
