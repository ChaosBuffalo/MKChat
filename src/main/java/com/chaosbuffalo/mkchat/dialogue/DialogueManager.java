package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.HasBoolFlagCondition;
import com.chaosbuffalo.mkchat.dialogue.effects.AddFlag;
import com.chaosbuffalo.mkchat.dialogue.effects.AddLevelEffect;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.item.Item;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = MKChat.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DialogueManager extends JsonReloadListener {
    public static final String DEFINITION_FOLDER = "dialogues";

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<ResourceLocation, DialogueTree> trees = new HashMap<>();
    private static final Map<String, BiFunction<String, DialogueTree, ITextComponent>> textComponentProviders = new HashMap<>();
    private static final Map<String, Function<DialogueContext, ITextComponent>> contextProviders = new HashMap<String, Function<DialogueContext, ITextComponent>>();

    private static final Map<ResourceLocation, Supplier<DialogueEffect>> effectDeserializers = new HashMap<>();
    private static final Map<ResourceLocation, Supplier<DialogueCondition>> conditionDeserializers = new HashMap<>();

    private static final Function<DialogueContext, ITextComponent> playerNameProvider =
            (context) -> context.getPlayer().getName();

    private static final Function<DialogueContext, ITextComponent> entityNameProvider =
            (context) -> context.getSpeaker().getName();

    private static final BiFunction<String, DialogueTree, ITextComponent> contextProvider =
            (name, tree) -> {
                if (contextProviders.containsKey(name)) {
                    return new ContextAwareTextComponent("mkchat.simple_context.msg", (context) ->
                            Lists.newArrayList(contextProviders.get(name).apply(context)));
                } else {
                    return new StringTextComponent(String.format("{context:%s}", name));
                }
            };

    private static final BiFunction<String, DialogueTree, ITextComponent> promptProvider =
            (name, tree) -> {
                DialoguePrompt prompt = tree.getPrompt(name);
                if (prompt != null) {
                    return prompt.getPromptLink();
                } else {
                    return new StringTextComponent(String.format("{prompt:%s}", name));
                }
            };

    private static final BiFunction<String, DialogueTree, ITextComponent> itemProvider =
            (name, tree) -> {
                ResourceLocation itemId = new ResourceLocation(name);
                Item item = ForgeRegistries.ITEMS.getValue(itemId);
                if (item != null) {
                    return new TranslationTextComponent(item.getTranslationKey());
                } else {
                    return new StringTextComponent(String.format("{item:%s}", name));
                }
            };

    public static void dialogueSetup() {
        putEffectDeserializer(AddLevelEffect.effectTypeName, AddLevelEffect::new);
        putEffectDeserializer(AddFlag.effectTypeName, AddFlag::new);
        putConditionDeserializer(HasBoolFlagCondition.conditionTypeName, HasBoolFlagCondition::new);
        putTextComponentProvider("context", contextProvider);
        putTextComponentProvider("prompt", promptProvider);
        putTextComponentProvider("item", itemProvider);
        putContextArgProvider("player_name", playerNameProvider);
        putContextArgProvider("entity_name", entityNameProvider);
    }

    public static void putContextArgProvider(String typeName, Function<DialogueContext, ITextComponent> func) {
        contextProviders.put(typeName, func);
    }

    public static void putEffectDeserializer(ResourceLocation typeName, Supplier<DialogueEffect> func) {
        effectDeserializers.put(typeName, func);
    }

    @Nullable
    public static DialogueEffect getDialogueEffect(ResourceLocation effectType) {

        if (!effectDeserializers.containsKey(effectType)) {
            MKChat.LOGGER.error("Failed to deserialize dialogue effect {}", effectType);
            return null;
        }
        return effectDeserializers.get(effectType).get();
    }

    public static void putConditionDeserializer(ResourceLocation typeName, Supplier<DialogueCondition> func) {
        conditionDeserializers.put(typeName, func);
    }

    @Nullable
    public static DialogueCondition getDialogueCondition(ResourceLocation conditionType) {
        if (!conditionDeserializers.containsKey(conditionType)) {
            MKChat.LOGGER.error("Failed to deserialize dialogue condition {}", conditionType);
            return null;
        }
        return conditionDeserializers.get(conditionType).get();
    }

    public static void putTextComponentProvider(String typeName, BiFunction<String, DialogueTree, ITextComponent> func) {
        textComponentProviders.put(typeName, func);
    }

    public static Map<ResourceLocation, DialogueTree> getTrees() {
        return trees;
    }

    public DialogueManager() {
        super(GSON, DEFINITION_FOLDER);
        MinecraftForge.EVENT_BUS.register(this);
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn,
                         @Nullable IResourceManager resourceManagerIn,
                         @Nullable IProfiler profilerIn) {
        trees.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            MKChat.LOGGER.info("Found dialogue tree file: {}", resourcelocation);
            if (resourcelocation.getPath().startsWith("_"))
                continue; //Forge: filter anything beginning with "_" as it's used for metadata.
            DialogueTree tree = DialogueTree.deserializeTreeFromDynamic(entry.getKey(),
                    new Dynamic<>(JsonOps.INSTANCE, entry.getValue()));
            trees.put(tree.getDialogueName(), tree);
        }
    }

    @SubscribeEvent
    public void subscribeEvent(AddReloadListenerEvent event) {
        event.addListener(this);
    }

    private static ITextComponent handleTextProviderRequest(String request, DialogueTree tree) {
        if (request.contains(":")) {
            String[] requestSplit = request.split(":", 2);
            if (textComponentProviders.containsKey(requestSplit[0])) {
                return textComponentProviders.get(requestSplit[0]).apply(requestSplit[1], tree);
            } else {
                return new StringTextComponent("{" + request + "}");
            }
        } else {
            return new StringTextComponent("{" + request + "}");
        }
    }

    public static ITextComponent parseDialogueMessage(String text, DialogueTree tree) {
        String parsing = text;
        IFormattableTextComponent component = new StringTextComponent("");
        while (!parsing.isEmpty()) {
            if (parsing.contains("{") && parsing.contains("}")) {
                int index = parsing.indexOf("{");
                int endIndex = parsing.indexOf("}");
                component.appendString(parsing.substring(0, index));
                String textProviderRequest = parsing.substring(index, endIndex + 1)
                        .replace("{", "")
                        .replace("}", "");
                //handle request
                component.appendSibling(handleTextProviderRequest(textProviderRequest, tree));
                parsing = parsing.substring(endIndex + 1);
            } else {
                component.appendString(parsing);
                parsing = "";
            }
        }
        return component;
    }

    @Nullable
    public static DialogueTree getDialogueTree(ResourceLocation name) {
        return trees.get(name);
    }
}
