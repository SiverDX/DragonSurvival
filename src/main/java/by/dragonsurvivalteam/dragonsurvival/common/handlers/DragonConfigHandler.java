package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.active.NetherBreathAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.ForestDragon.active.ForestBreathAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.active.StormBreathAbility;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DragonConfigHandler{
    public static HashSet<Item> DRAGON_BLACKLISTED_ITEMS = new HashSet<>();
    public static HashSet<Block> SEA_DRAGON_HYDRATION_BLOCKS = new HashSet<>();
    public static HashSet<Item> SEA_DRAGON_HYDRATION_USE_ALTERNATIVES = new HashSet<>();
    public static HashSet<Block> FOREST_DRAGON_BREATH_GROW_BLACKLIST = new HashSet<>();

    public static Map<String, HashSet<Block>> DRAGON_BREATH_BLOCKS;
    public static Map<String, HashSet<Block>> DRAGON_MANA_BLOCKS;

    public static void rebuildBreathBlocks(){
        HashMap<String, HashSet<Block>> breathMap = new HashMap<>();
        breathMap.put(DragonTypes.CAVE.getTypeName(), ConfigHandler.getResourceElements(Block.class, NetherBreathAbility.fireBreathBlockBreaks));
        breathMap.put(DragonTypes.FOREST.getTypeName(), ConfigHandler.getResourceElements(Block.class, ForestBreathAbility.forestBreathBlockBreaks));
        breathMap.put(DragonTypes.SEA.getTypeName(), ConfigHandler.getResourceElements(Block.class, StormBreathAbility.stormBreathBlockBreaks));
        DRAGON_BREATH_BLOCKS = breathMap;
    }

    public static void rebuildManaBlocks() {
        HashMap<String, HashSet<Block>> map = new HashMap<>();
        map.put(DragonTypes.CAVE.getTypeName(), ConfigHandler.getResourceElements(Block.class, ServerConfig.caveDragonManaBlocks));
        map.put(DragonTypes.FOREST.getTypeName(), ConfigHandler.getResourceElements(Block.class, ServerConfig.forestDragonManaBlocks));
        map.put(DragonTypes.SEA.getTypeName(), ConfigHandler.getResourceElements(Block.class, ServerConfig.seaDragonManaBlocks));
        DRAGON_MANA_BLOCKS = map;
    }

    public static void rebuildSeaDragonConfigs() {
        SEA_DRAGON_HYDRATION_BLOCKS = ConfigHandler.getResourceElements(Block.class, ServerConfig.seaHydrationBlocks);
        SEA_DRAGON_HYDRATION_USE_ALTERNATIVES = ConfigHandler.getResourceElements(Item.class, ServerConfig.seaAdditionalWaterUseables);
    }

    public static void rebuildForestDragonConfigs() {
        FOREST_DRAGON_BREATH_GROW_BLACKLIST = ConfigHandler.getResourceElements(Block.class, ForestBreathAbility.forestBreathGrowBlacklist);
    }

    public static void rebuildBlacklistedItems(){
        DRAGON_BLACKLISTED_ITEMS = ConfigHandler.getResourceElements(Item.class, ServerConfig.blacklistedItems);
    }
}