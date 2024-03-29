package com.chaosbuffalo.mkchat.init;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.entity.TestChatReceiverEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = MKChat.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChatEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES,
            MKChat.MODID);


    public static final RegistryObject<EntityType<TestChatReceiverEntity>> TEST_CHAT = ENTITY_TYPES.register(
            "test_entity", () ->
                    EntityType.Builder.<TestChatReceiverEntity>create(TestChatReceiverEntity::new, EntityClassification.CREATURE)
                            .size(EntityType.PIG.getWidth(), EntityType.PIG.getHeight())
                            .build(new ResourceLocation(MKChat.MODID, "test_entity").toString())
    );


    @SubscribeEvent
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(TEST_CHAT.get(), PigEntity.func_234215_eI_().create());
    }
}
