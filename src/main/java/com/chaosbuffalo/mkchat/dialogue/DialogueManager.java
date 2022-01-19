package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.HasBoolFlagCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.InvalidCondition;
import com.chaosbuffalo.mkchat.dialogue.effects.AddLevelEffect;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.chaosbuffalo.mkchat.dialogue.effects.AddFlag;
import com.chaosbuffalo.mkchat.dialogue.effects.InvalidEffect;
import com.google.common.collect.Lists;
import com.google.gson.*;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.item.Item;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid=MKChat.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class DialogueManager extends JsonReloadListener {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<ResourceLocation, DialogueTree> trees = new HashMap<>();
    private static final Map<String, BiFunction<String, DialogueTree, ITextComponent>> textComponentProviders = new HashMap<>();
    private static final Map<String,Function<DialogueContext, String>> contextProviders = new HashMap<>();

    private static final Map<ResourceLocation, Supplier<DialogueEffect>> effectDeserializers = new HashMap<>();
    private static final Map<ResourceLocation, Supplier<DialogueCondition>> conditionDeserializers = new HashMap<>();

    private static final Function<DialogueContext, String> playerNameProvider =
            (context) -> context.getPlayer().getName().getString();

    private static final Function<DialogueContext, String> entityNameProvider =
            (context) -> context.getSpeaker().getName().getString();

    private static final BiFunction<String, DialogueTree, ITextComponent> contextProvider =
            (name, tree) -> {
                if (contextProviders.containsKey(name)){
                    return new ContextAwareTextComponent("%s", (context) ->
                            Lists.newArrayList(contextProviders.get(name).apply(context)));
                } else {
                    return new StringTextComponent(String.format("{context:%s}", name));
                }
            };

    private static final BiFunction<String, DialogueTree, ITextComponent> promptProvider =
            (name, tree) -> {
                DialoguePrompt prompt = tree.getPrompt(name);
                if (prompt != null){
                    return prompt.getPromptLink();
                } else {
                    return new StringTextComponent(String.format("{prompt:%s}", name));
                }
            };

    private static final BiFunction<String, DialogueTree, ITextComponent> itemProvider =
            (name, tree) -> {
                ResourceLocation itemId = new ResourceLocation(name);
                Item item = ForgeRegistries.ITEMS.getValue(itemId);
                if (item != null){
                    return new TranslationTextComponent(item.getTranslationKey());
                } else {
                    return new StringTextComponent(String.format("{item:%s}", name));
                }
            };

    public static void dialogueSetup(){
        putEffectDeserializer(AddLevelEffect.effectTypeName, AddLevelEffect::new);
        putEffectDeserializer(AddFlag.effectTypeName, AddFlag::new);
        putConditionDeserializer(HasBoolFlagCondition.conditionTypeName, HasBoolFlagCondition::new);
        putTextComponentProvider("context", contextProvider);
        putTextComponentProvider("prompt", promptProvider);
        putTextComponentProvider("item", itemProvider);
        putContextArgProvider("player_name", playerNameProvider);
        putContextArgProvider("entity_name", entityNameProvider);
    }

    public static void putContextArgProvider(String typeName, Function<DialogueContext, String> func){
        contextProviders.put(typeName, func);
    }

    public static void putEffectDeserializer(ResourceLocation typeName, Supplier<DialogueEffect> func){
        effectDeserializers.put(typeName, func);
    }

    @Nullable
    public static DialogueEffect getDialogueEffect(ResourceLocation effectType){

        if (!effectDeserializers.containsKey(effectType)){
            MKChat.LOGGER.error("Failed to deserialize dialogue effect {}", effectType);
            return null;
        }
        return effectDeserializers.get(effectType).get();
    }

    @Nonnull
    public static <D> DialogueEffect deserializeEffect(Dynamic<D> dynamic){
        ResourceLocation type = DialogueEffect.getType(dynamic);
        DialogueEffect effect = getDialogueEffect(type);
        if (effect != null){
            effect.deserialize(dynamic);
        }
        return effect != null ? effect : InvalidEffect.INVALID_EFFECT;
    }


    public static void putConditionDeserializer(ResourceLocation typeName, Supplier<DialogueCondition> func){
        conditionDeserializers.put(typeName, func);
    }

    @Nullable
    public static DialogueCondition getDialogueCondition(ResourceLocation conditionType){
        if (!conditionDeserializers.containsKey(conditionType)){
            MKChat.LOGGER.error("Failed to deserialize dialogue condition {}", conditionType);
            return null;
        }
        return conditionDeserializers.get(conditionType).get();
    }

    @Nonnull
    public static <D> DialogueCondition deserializeCondition(Dynamic<D> dynamic){
        ResourceLocation type = DialogueCondition.getType(dynamic);
        DialogueCondition cond = getDialogueCondition(type);
        if (cond != null){
            cond.deserialize(dynamic);
        }
        return cond != null ? cond : InvalidCondition.INVALID_CONDITION;
    }


    public static void putTextComponentProvider(String typeName, BiFunction<String, DialogueTree, ITextComponent> func){
        textComponentProviders.put(typeName, func);
    }

    public static Map<ResourceLocation, DialogueTree> getTrees() {
        return trees;
    }

    public DialogueManager() {
        super(GSON, "dialogues");
        MinecraftForge.EVENT_BUS.register(this);
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn,
                         IResourceManager resourceManagerIn,
                         IProfiler profilerIn) { ;
        trees.clear();
        for(Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            MKChat.LOGGER.info("Found dialogue tree file: {}", resourcelocation);
            if (resourcelocation.getPath().startsWith("_")) continue; //Forge: filter anything beginning with "_" as it's used for metadata.
            DialogueTree tree = DialogueTree.deserializeTreeFromDynamic(entry.getKey(),
                    new Dynamic<>(JsonOps.INSTANCE, entry.getValue()));
            tree.bake();
            trees.put(tree.getDialogueName(), tree);
        }
    }

    @SubscribeEvent
    public void subscribeEvent(AddReloadListenerEvent event){
        event.addListener(this);
    }

    private static ITextComponent handleTextProviderRequest(String request, DialogueTree tree){
        if (request.contains(":")){
            String[] requestSplit = request.split(":", 2);
            if (textComponentProviders.containsKey(requestSplit[0])){
                return textComponentProviders.get(requestSplit[0]).apply(requestSplit[1], tree);
            } else {
                return new StringTextComponent(String.format("{%s}", request));
            }
        } else {
            return new StringTextComponent(String.format("{%s}", request));
        }
    }

    public static ITextComponent parseDialogueMessage(String text, DialogueTree tree){
        String parsing = text;
        ITextComponent component = new StringTextComponent("");
        while (!parsing.isEmpty()){
            if (parsing.contains("{") && parsing.contains("}")){
                int index = parsing.indexOf("{");
                int endIndex = parsing.indexOf("}");
                component.getSiblings().add(new StringTextComponent(parsing.substring(0, index)));
                String textProviderRequest = parsing.substring(index, endIndex + 1)
                        .replace("{", "").replace("}", "");
                //handle request
                component.getSiblings().add(handleTextProviderRequest(textProviderRequest, tree));
                parsing = parsing.substring(endIndex + 1);
            } else {
                component.getSiblings().add(new StringTextComponent(parsing));
                parsing = "";
            }
        }
        return component;
    }

    @Nullable
    public static DialogueTree getDialogueTree(ResourceLocation name){
        return trees.get(name);
    }


}
