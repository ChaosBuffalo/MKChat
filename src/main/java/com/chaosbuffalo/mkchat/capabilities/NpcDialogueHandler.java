package com.chaosbuffalo.mkchat.capabilities;

import com.chaosbuffalo.mkchat.ChatConstants;
import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.DialogueManager;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mkchat.event.PlayerNpcDialogueTreeStackSetupEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.*;

public class NpcDialogueHandler implements INpcDialogue{

    private LivingEntity entity;
    public static final String NO_THANKS = "no thanks";
    private ResourceLocation dialogueName;
    private Map<UUID, Stack<DialogueTree>> playerDialogueStacks;
    private List<DialogueTree> additionalTrees = new ArrayList<>();

    public NpcDialogueHandler(){
        playerDialogueStacks = new HashMap<>();
    }

    @Override
    public void attach(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean hasDialogue() {
        return dialogueName != null || additionalTrees.size() > 0 || playerDialogueStacks.size() > 0;
    }

    public void popState(ServerPlayerEntity player){
        Stack<DialogueTree> treeStack = playerDialogueStacks.get(player.getUniqueID());
        if (treeStack != null){
            if (treeStack.size() > 1) {
                treeStack.pop();
            } else {
                playerDialogueStacks.remove(player.getUniqueID());
            }
        }
    }

    @Override
    public void addAdditionalDialogueTree(DialogueTree tree){
        additionalTrees.add(tree);
    }

    public void setupDialogueForPlayer(ServerPlayerEntity player){
        Stack<DialogueTree> playerStack = new Stack<>();
        DialogueTree tree = DialogueManager.getDialogueTree(dialogueName);
        if (tree != null){
            playerStack.push(tree);
        }
        for (DialogueTree add : additionalTrees){
            playerStack.push(add);
        }
        MinecraftForge.EVENT_BUS.post(new PlayerNpcDialogueTreeStackSetupEvent(player, getEntity(), playerStack));
        if (!playerStack.isEmpty()){
            playerDialogueStacks.put(player.getUniqueID(), playerStack);
        }
    }

    @Nullable
    public DialogueTree getTreeForPlayer(ServerPlayerEntity player){
        if (!playerDialogueStacks.containsKey(player.getUniqueID())){
            setupDialogueForPlayer(player);
        }
        Stack<DialogueTree> dialogues = playerDialogueStacks.get(player.getUniqueID());
        return dialogues.size() > 0 ? dialogues.peek() : null;
    }

    @Override
    public void receiveMessage(ServerPlayerEntity player, String message) {
        DialogueTree tree = getTreeForPlayer(player);
        if (hasDialogue()){
            if (message.equals(NO_THANKS)){
                popState(player);
            }
            if (tree != null){
                tree.handlePlayerMessage(player, message, entity);
            }
        }
    }

    @Override
    public void startDialogue(ServerPlayerEntity player) {
        DialogueTree tree = getTreeForPlayer(player);
        if (tree != null && tree.getHailPrompt() != null) {
            if (player.getServer() != null){
                player.getServer().getPlayerList().sendToAllNearExcept(null,
                        player.getPosX(), player.getPosY(), player.getPosZ(), ChatConstants.CHAT_RADIUS,
                        player.getServerWorld().getDimensionKey(),
                        new SChatPacket(new StringTextComponent(String.format("<%s> Hail, %s",
                                player.getName().getString(), this.getEntity().getName().getString())),
                                ChatType.CHAT, player.getUniqueID()));
            }
            tree.getHailPrompt().handlePrompt(player, entity, tree);
        } else {
            MKChat.LOGGER.info("Failed to find dialogue {}", getDialogueTreeName());
        }
    }

    @Override
    public void setDialogueTree(ResourceLocation treeName) {
        dialogueName = treeName;
    }

    @Override
    public LivingEntity getEntity() {
        return entity;
    }

    @Nullable
    @Override
    public ResourceLocation getDialogueTreeName() {
        return dialogueName;
    }

    @Override
    public CompoundNBT serializeNBT() {
        return new CompoundNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {

    }

    public static class Storage implements Capability.IStorage<INpcDialogue> {

        @Nullable
        @Override
        public INBT writeNBT(Capability<INpcDialogue> capability, INpcDialogue instance, Direction side) {
            if (instance == null){
                return null;
            }
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<INpcDialogue> capability, INpcDialogue instance, Direction side, INBT nbt) {
            if (nbt instanceof CompoundNBT && instance != null) {
                CompoundNBT tag = (CompoundNBT) nbt;
                instance.deserializeNBT(tag);
            }
        }
    }
}
