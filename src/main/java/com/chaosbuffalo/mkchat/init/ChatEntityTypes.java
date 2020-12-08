package com.chaosbuffalo.mkchat.init;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.entity.TestChatReceiverEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ChatEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES,
            MKChat.MODID);


    public static final RegistryObject<EntityType<TestChatReceiverEntity>> TEST_CHAT = ENTITY_TYPES.register(
            "test_entity", () ->
                    EntityType.Builder.<TestChatReceiverEntity>create(TestChatReceiverEntity::new, EntityClassification.CREATURE)
                            .size(EntityType.PIG.getWidth(), EntityType.PIG.getHeight())
                            .build(new ResourceLocation(MKChat.MODID, "test_entity").toString())
    );

    public static void setupAttributes(){
        GlobalEntityTypeAttributes.put(TEST_CHAT.get(), PigEntity.func_234215_eI_().create());
    }
}
