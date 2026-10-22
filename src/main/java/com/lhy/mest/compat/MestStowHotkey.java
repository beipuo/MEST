package com.lhy.mest.compat;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.features.HotkeyAction;
import appeng.api.networking.IGrid;
import appeng.api.stacks.AEItemKey;
import appeng.me.helpers.PlayerSource;

import de.mari_023.ae2wtlib.wct.CraftingTerminalHandler;

/**
 * {@code ae2wtlib_stow} (the "store held stack into the ME network" key) only resolves
 * {@code WTDefinitions.CRAFTING} through {@link CraftingTerminalHandler}, so a player carrying
 * nothing but a spliced terminal gets no action at all. This action performs the same insertion
 * against the spliced terminal's grid.
 * <p>
 * If a wireless crafting terminal is worn or carried, wtlib's own action keeps priority and this
 * one bows out.
 */
public final class MestStowHotkey implements HotkeyAction {
    public static final String HOTKEY_ID = "ae2wtlib_stow";

    @Override
    public boolean run(Player player) {
        if (craftingTerminalHandles(player)) {
            return false;
        }
        ItemStack mest = MestWtlibSupport.mestStack(player);
        if (mest.isEmpty()) {
            return false;
        }
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (held.isEmpty()) {
            return false;
        }
        if (held.isNotReplaceableByPickAction(player, player.getInventory().selected)) {
            return false;
        }
        IGrid grid = MestWtlibSupport.mestGrid(player);
        if (grid == null || grid.getStorageService() == null) {
            return false;
        }
        AEItemKey key = AEItemKey.of(held);
        if (key == null) {
            return false;
        }
        long inserted = grid.getStorageService().getInventory()
                .insert(key, held.getCount(), Actionable.MODULATE, new PlayerSource(player));
        if (inserted <= 0L) {
            return false;
        }
        held.setCount(held.getCount() - (int) inserted);
        return true;
    }

    /** Mirrors the guard inside wtlib's action so a real crafting terminal is never shadowed. */
    private static boolean craftingTerminalHandles(Player player) {
        CraftingTerminalHandler handler = CraftingTerminalHandler.getCraftingTerminalHandler(player);
        return handler.inRange() && handler.getTargetGrid() != null;
    }
}
