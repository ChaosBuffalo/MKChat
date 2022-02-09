package com.chaosbuffalo.mkchat.capabilities;

import com.chaosbuffalo.mkchat.ChatConstants;
import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.DialogueManager;
import com.chaosbuffalo.mkchat.dialogue.DialoguePrompt;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mkchat.event.PlayerNpcDialogueTreeGatherEvent;
import com.chaosbuffalo.mkcore.GameConstants;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class NpcDialogueHandler implements INpcDialogue {
    protected static final int TICK_TIMEOUT = 60 * GameConstants.TICKS_PER_SECOND;

    public static class Conversation {
        public final List<DialogueTree> trees;
        public int currentIndex;
        public int lastTickSeen;
        public List<DialogueTree> relevantTrees;

        public Conversation(ServerPlayerEntity player, LivingEntity speaker, List<DialogueTree> speakerTrees) {
            this.trees = speakerTrees;
            this.currentIndex = 0;
            this.lastTickSeen = speaker.ticksExisted;
            updateRelevantTrees(player, speaker);
        }

        public boolean isExpired(int ticksExisted) {
            return lastTickSeen + TICK_TIMEOUT < ticksExisted;
        }

        public void nextTree() {
            currentIndex++;
            if (currentIndex >= relevantTrees.size()) {
                currentIndex = 0;
            }
        }

        private void updateRelevantTrees(ServerPlayerEntity player, LivingEntity speaker) {
            relevantTrees = trees.stream().filter(tree -> {
                MKChat.LOGGER.debug("Checking Dialogue Tree {} relevance for player {}", tree.getDialogueName(), player);
                DialoguePrompt hailPrompt = tree.getHailPrompt();
                return hailPrompt != null && hailPrompt.willHandle(player, speaker);
            }).collect(Collectors.toList());

            MKChat.LOGGER.debug("Calculated relevant trees for player: {}", player);
            for (DialogueTree tree : relevantTrees) {
                MKChat.LOGGER.debug("Added: {}", tree.getDialogueName());
            }
        }

        public void handlePlayerMessage(ServerPlayerEntity player, LivingEntity speaker, String message) {
            for (int i = relevantTrees.size() - 1; i >= 0; i--) {
                DialogueTree tree = relevantTrees.get(i);
                if (tree.handlePlayerMessage(player, message, speaker)) {
                    updateRelevantTrees(player, speaker);
                    return;
                }
            }
        }

        public void converse(ServerPlayerEntity player, LivingEntity speaker) {
            for (int i = relevantTrees.size() - 1 - currentIndex; i >= 0; i--) {
                DialogueTree tree = relevantTrees.get(i);
                DialoguePrompt hail = tree.getHailPrompt();
                if (hail != null) {
                    DialoguePrompt addPrompt = relevantTrees.size() > 1 ? ADDITIONAL_DIALOGUE : null;
                    if (hail.handlePrompt(player, speaker, tree, addPrompt)) {
                        updateRelevantTrees(player, speaker);
                        return;
                    }
                }
            }
        }
    }


    public static final String NO_THANKS = "Talk to me about something else.";
    private static final DialogueTree moveNextTree = new DialogueTree(new ResourceLocation(MKChat.MODID, "move_next_tree"));
    public static final DialoguePrompt ADDITIONAL_DIALOGUE =
            Util.make(new DialoguePrompt("nextTree", NO_THANKS, NO_THANKS, "More"), p -> p.setDialogueTree(moveNextTree));

    private final LivingEntity entity;
    private final Map<UUID, Conversation> conversations;
    private final List<DialogueTree> secondaryTrees = new ArrayList<>();
    private ResourceLocation primaryDialogueTreeName;

    public NpcDialogueHandler(LivingEntity attached) {
        this.entity = attached;
        conversations = new HashMap<>();
    }

    @Override
    public boolean hasDialogue() {
        return primaryDialogueTreeName != null || secondaryTrees.size() > 0 || conversations.size() > 0;
    }

    @Override
    public void addAdditionalDialogueTree(DialogueTree tree) {
        conversations.clear();
        secondaryTrees.add(tree);
    }

    @Nullable
    public Conversation createConversation(ServerPlayerEntity player) {
        List<DialogueTree> trees = new ArrayList<>();
        DialogueTree primaryTree = DialogueManager.getDialogueTree(primaryDialogueTreeName);
        if (primaryTree != null) {
            trees.add(primaryTree);
        }
        trees.addAll(secondaryTrees);
        MinecraftForge.EVENT_BUS.post(new PlayerNpcDialogueTreeGatherEvent(player, getEntity(), trees));
        if (!trees.isEmpty()) {
            Conversation entry = new Conversation(player, entity, trees);
            conversations.put(player.getUniqueID(), entry);
            return entry;
        }
        return null;
    }

    @Nullable
    public Conversation getConversation(ServerPlayerEntity player) {
        Conversation entry = conversations.get(player.getUniqueID());
        if (entry == null || entry.isExpired(entity.ticksExisted)) {
            entry = createConversation(player);
        }
        return entry;
    }


    @Override
    public void receiveMessage(ServerPlayerEntity player, String message) {
        Conversation entry = getConversation(player);
        if (entry == null || !hasDialogue())
            return;

        if (message.contains(NO_THANKS)) {
            entry.nextTree();
            entry.converse(player, entity);
        } else {
            entry.handlePlayerMessage(player, entity, message);
        }
    }

    public void startDialogue(ServerPlayerEntity player) {
        Conversation entry = createConversation(player);
        if (entry == null || !hasDialogue())
            return;

        sendHailMessage(player);
        entry.converse(player, entity);
    }

    // Use single-argument version
    @Deprecated
    @Override
    public void startDialogue(ServerPlayerEntity player, boolean suppressHail) {
        startDialogue(player);
    }

    private void sendHailMessage(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null)
            return;

        ITextComponent hail = new StringTextComponent("<")
                .appendSibling(player.getDisplayName())
                .appendString("> Hail, ")
                .appendSibling(getEntity().getDisplayName());

        server.getPlayerList().sendToAllNearExcept(null,
                player.getPosX(), player.getPosY(), player.getPosZ(), ChatConstants.CHAT_RADIUS,
                player.getServerWorld().getDimensionKey(),
                new SChatPacket(hail, ChatType.CHAT, player.getUniqueID()));
    }

    @Override
    public void setDialogueTree(ResourceLocation treeName) {
        primaryDialogueTreeName = treeName;
        conversations.clear();
    }

    @Override
    public LivingEntity getEntity() {
        return entity;
    }

    @Nullable
    @Override
    public ResourceLocation getDialogueTreeName() {
        return primaryDialogueTreeName;
    }

    @Override
    public CompoundNBT serializeNBT() {
        return new CompoundNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {

    }
}
