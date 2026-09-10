package com.bleachmod.common.quest.objectives;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import com.bleachmod.common.util.ResourceIds;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemObjective extends QuestObjective {
    private final String itemId;

    public ItemObjective(String itemId, int required) {
        super(ObjectiveType.ITEM, required);
        this.itemId = itemId;
    }

    public String getItemId() {
        return itemId;
    }

    public int countInInventory(Player player) {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceIds.parse(itemId));
        if (item == null) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    @Override
    public Component describe() {
        return Component.translatable("bleachmod.objective.item", getRequired(), itemId);
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "ITEM");
        json.addProperty("item", itemId);
        json.addProperty("count", getRequired());
        return json;
    }
}
