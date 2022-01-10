package com.chaosbuffalo.mkchat.capabilities;

import com.chaosbuffalo.mkchat.ChatConstants;
import com.chaosbuffalo.mkchat.dialogue.DialogueManager;
import com.chaosbuffalo.mkchat.dialogue.DialoguePrompt;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mkchat.event.PlayerNpcDialogueTreeGatherEvent;
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
    protected static final int TICK_TIMEOUT = 20 * 60;

    public static class PlayerDialogueEntry {
        public int currentIndex;
        public final List<DialogueTree> trees;
        public int lastTickSeen;

        public PlayerDialogueEntry(int index, List<DialogueTree> trees, int tickComputed){
            this.trees = trees;
            this.currentIndex = index;
            this.lastTickSeen = tickComputed;
        }

        public boolean shouldRecalc(int ticksExisted){
            return lastTickSeen + TICK_TIMEOUT < ticksExisted;
        }

        public void cycleIndex(){
            this.currentIndex++;
            if (currentIndex >= trees.size()){
                currentIndex = 0;
            }
        }
    }

    private LivingEntity entity;
    public static final String NO_THANKS = "Talk to me about something else.";
    public static final DialoguePrompt ADDITIONAL_DIALOGUE = new DialoguePrompt("addTrees", NO_THANKS, NO_THANKS, "More");
    static {
        ADDITIONAL_DIALOGUE.compileMessage();
    }
    private ResourceLocation dialogueName;
    private Map<UUID, PlayerDialogueEntry> playerDialogues;
    private List<DialogueTree> dialogueTreeNames = new ArrayList<>();

    public NpcDialogueHandler(){
        playerDialogues = new HashMap<>();
    }

    @Override
    public void attach(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean hasDialogue() {
        return dialogueName != null || dialogueTreeNames.size() > 0 || playerDialogues.size() > 0;
    }

    @Override
    public void addAdditionalDialogueTree(DialogueTree tree){
        dialogueTreeNames.add(tree);
    }

    public void setupDialogueForPlayer(ServerPlayerEntity player){
        List<DialogueTree> playerQue = new ArrayList<>();
        DialogueTree tree = DialogueManager.getDialogueTree(dialogueName);
        if (tree != null){
            playerQue.add(tree);
        }
        playerQue.addAll(dialogueTreeNames);
        MinecraftForge.EVENT_BUS.post(new PlayerNpcDialogueTreeGatherEvent(player, getEntity(), playerQue));
        if (!playerQue.isEmpty()){
            playerDialogues.put(player.getUniqueID(), new PlayerDialogueEntry(0, playerQue, entity.ticksExisted));
        }
    }

    public PlayerDialogueEntry getTreesForPlayer(ServerPlayerEntity player){
        if (!playerDialogues.containsKey(player.getUniqueID()) || playerDialogues.get(player.getUniqueID()).shouldRecalc(entity.ticksExisted)){
            setupDialogueForPlayer(player);
        }
        return playerDialogues.get(player.getUniqueID());
    }


    @Override
    public void receiveMessage(ServerPlayerEntity player, String message) {
        PlayerDialogueEntry entry = getTreesForPlayer(player);
        if (hasDialogue()){
            if (message.equals(NO_THANKS)){
                entry.cycleIndex();
                startDialogue(player, true);
            } else {
                for (int i = entry.trees.size() - 1; i >= 0; i--) {
                    DialogueTree tree = entry.trees.get(i);
                    if (tree.handlePlayerMessage(player, message, entity)) {
                        return;
                    }
                }
            }

        }
    }

    @Override
    public void startDialogue(ServerPlayerEntity player, boolean suppressHail) {
        PlayerDialogueEntry entry = getTreesForPlayer(player);
        if (hasDialogue()) {
            if (player.getServer() != null && !suppressHail){
                player.getServer().getPlayerList().sendToAllNearExcept(null,
                        player.getPosX(), player.getPosY(), player.getPosZ(), ChatConstants.CHAT_RADIUS,
                        player.getServerWorld().getDimensionKey(),
                        new SChatPacket(new StringTextComponent(String.format("<%s> Hail, %s",
                                player.getName().getString(), this.getEntity().getName().getString())),
                                ChatType.CHAT, player.getUniqueID()));
            }


            for (int i = entry.trees.size() - 1 - entry.currentIndex; i >= 0; i--) {
                DialogueTree tree = entry.trees.get(i);
                if (tree.getHailPrompt() != null) {
                    DialoguePrompt addPrompt = entry.trees.size() > 1 ? ADDITIONAL_DIALOGUE : null;

                    if (tree.getHailPrompt().handlePrompt(player, entity, tree, addPrompt)) {
                        return;
                    } else {
                        entry.cycleIndex();
                    }

                }
            }
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
