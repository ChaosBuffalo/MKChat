package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.HasBoolFlagCondition;
import com.chaosbuffalo.mkchat.dialogue.effects.AddLevelEffect;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.chaosbuffalo.mkchat.dialogue.effects.AddFlag;
import com.chaosbuffalo.mkchat.json.SerializationUtils;
import com.google.common.collect.Lists;
import com.google.gson.*;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid=MKChat.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class DialogueManager extends JsonReloadListener {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<ResourceLocation, DialogueTree> trees = new HashMap<>();
    private static final Map<String, BiFunction<Gson, JsonObject, DialogueEffect>> effectDeserializers = new HashMap<>();
    private static final Map<String, BiFunction<Gson, JsonObject, DialogueCondition>> conditionDeserializers = new HashMap<>();
    private static final Map<String, BiFunction<String, DialogueTree, ITextComponent>> textComponentProviders = new HashMap<>();
    private static final Map<String,Function<DialogueContext, String>> contextProviders = new HashMap<>();

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
                    return new StringTextComponent(String.format("{prompt:%s", name));
                }
            };

    public static void dialogueSetup(){
        putEffectDeserializer(AddLevelEffect.effectTypeName,
                SerializationUtils.deserialize(AddLevelEffect.class));
        putEffectDeserializer(AddFlag.effectTypeName,
                SerializationUtils.deserialize(AddFlag.class));
        putConditionDeserializer(HasBoolFlagCondition.conditionTypeName,
                SerializationUtils.deserialize(HasBoolFlagCondition.class));
        putTextComponentProvider("context", contextProvider);
        putTextComponentProvider("prompt", promptProvider);
        putContextArgProvider("player_name", playerNameProvider);
        putContextArgProvider("entity_name", entityNameProvider);
    }

    public static void putContextArgProvider(String typeName, Function<DialogueContext, String> func){
        contextProviders.put(typeName, func);
    }

    public static void putEffectDeserializer(String typeName, BiFunction<Gson, JsonObject, DialogueEffect> func){
        effectDeserializers.put(typeName, func);
    }

    public static void putConditionDeserializer(String typeName, BiFunction<Gson, JsonObject, DialogueCondition> func){
        conditionDeserializers.put(typeName, func);
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
            DialogueTree tree = parseDialogueTree(entry.getKey(), entry.getValue().getAsJsonObject());
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

    private DialoguePrompt parsePrompt(String name, JsonObject object){
        DialoguePrompt newPrompt = new DialoguePrompt(name, object.get("phrase").getAsString(),
                object.get("defaultPhrase").getAsString(), object.get("text").getAsString());
        MKChat.LOGGER.info("Loading dialogue prompt: {} with text: {}", name, object.get("text").getAsString());
        JsonArray responseArray = object.getAsJsonArray("responses");
        for (JsonElement ele : responseArray){
            JsonObject responseObj = ele.getAsJsonObject();
            String nodeId = responseObj.get("node").getAsString();
            DialogueResponse response = new DialogueResponse(nodeId);
            if (responseObj.has("conditions")){
                JsonArray conditionArray = responseObj.getAsJsonArray("conditions");
                for (JsonElement condEle : conditionArray){
                    DialogueCondition condition = conditionDeserialize(condEle.getAsJsonObject());
                    if (condition != null){
                        response.addCondition(condition);
                    } else {
                        MKChat.LOGGER.error("Failed to parse condition {} for prompt {}",
                                condEle.getAsJsonObject(), name);
                    }
                }
            }
            newPrompt.addResponse(response);
        }
        return newPrompt;
    }

    private DialogueNode parseNode(String name, JsonObject object){
        DialogueNode newNode = new DialogueNode(name, object.get("text").getAsString());
        MKChat.LOGGER.info("Loading dialogue node: {} with text: {}", name, object.get("text").getAsString());
        if (object.has("effects")){
            JsonArray effectArray = object.getAsJsonArray("effects");
            for (JsonElement ele : effectArray){
                JsonObject effectObj = ele.getAsJsonObject();
                DialogueEffect effect = effectDeserialize(effectObj);
                if (effect != null){
                    newNode.addEffect(effect);
                } else {
                    MKChat.LOGGER.error("Failed to parse dialogue effect {} for node {}",
                            ele.getAsJsonObject(), name);
                }
            }
        }
        return newNode;
    }

    @Nullable
    private DialogueCondition conditionDeserialize(JsonObject conditionJson){
        String type = conditionJson.get("type").getAsString();
        if (conditionDeserializers.containsKey(type)){
            return conditionDeserializers.get(type).apply(GSON, conditionJson);
        }
        return null;
    }

    @Nullable
    private DialogueEffect effectDeserialize(JsonObject effectJson){
        String type = effectJson.get("type").getAsString();
        if (effectDeserializers.containsKey(type)){
            return effectDeserializers.get(type).apply(GSON, effectJson);
        }
        return null;
    }

    private DialogueTree parseDialogueTree(ResourceLocation loc, JsonObject json){
        MKChat.LOGGER.info("Parsing Dialogue Tree Json for {}", loc);
        DialogueTree tree = new DialogueTree(loc);
        if (json.has("prompts")){
            JsonObject promptDict = json.getAsJsonObject("prompts");
            for (Map.Entry<String, JsonElement> promptEntry : promptDict.entrySet()){
                DialoguePrompt prompt = parsePrompt(promptEntry.getKey(), promptEntry.getValue().getAsJsonObject());
                tree.addPrompt(prompt);
            }
        }
        if (json.has("nodes")){
            JsonObject nodeDict = json.getAsJsonObject("nodes");
            for (Map.Entry<String, JsonElement> nodeEntry : nodeDict.entrySet()){
                DialogueNode node = parseNode(nodeEntry.getKey(), nodeEntry.getValue().getAsJsonObject());
                tree.addNode(node);
            }
        }
        if (json.has("hailPrompt")){
            tree.setHailPrompt(tree.getPrompt(json.get("hailPrompt").getAsString()));
            MKChat.LOGGER.info("Set start node for {} as {}", loc, json.get("hailPrompt").getAsString());
        }
        tree.bake();
        return tree;
    }
}
