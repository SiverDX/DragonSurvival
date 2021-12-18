package by.jackraidenph.dragonsurvival.handlers.Magic;

import by.jackraidenph.dragonsurvival.DragonSurvivalMod;
import by.jackraidenph.dragonsurvival.capability.DragonStateHandler;
import by.jackraidenph.dragonsurvival.capability.DragonStateProvider;
import by.jackraidenph.dragonsurvival.config.ConfigHandler;
import by.jackraidenph.dragonsurvival.util.DragonLevel;
import by.jackraidenph.dragonsurvival.util.DragonType;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent.PickupXp;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.ArrayList;

@EventBusSubscriber
public class ClawToolHandler
{
	@SubscribeEvent
	public static void experiencePickup(PickupXp event){
		PlayerEntity player = event.getPlayer();
		
		DragonStateProvider.getCap(player).ifPresent(cap -> {
			ArrayList<ItemStack> stacks = new ArrayList<>();
			
			for(int i = 0; i < 4; i++){
				ItemStack clawStack = cap.getClawInventory().getClawsInventory().getItem(i);
				int mending = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, clawStack);
				
				if(mending > 0 && clawStack.isDamaged()){
					stacks.add(clawStack);
				}
			}
			
			if(stacks.size() > 0) {
				ItemStack repairTime = stacks.get(player.level.random.nextInt(stacks.size()));
				if (!repairTime.isEmpty() && repairTime.isDamaged()) {
					
					int i = Math.min((int)(event.getOrb().value * repairTime.getXpRepairRatio()), repairTime.getDamageValue());
					event.getOrb().value -= i * 2;
					repairTime.setDamageValue(repairTime.getDamageValue() - i);
				}
			}
			
			event.getOrb().value = Math.max(0, event.getOrb().value);
		});
	}
	
	@SubscribeEvent
	public static void playerDieEvent(LivingDropsEvent event){
		Entity ent = event.getEntity();
		
		if(ent instanceof PlayerEntity){
			PlayerEntity player = (PlayerEntity)ent;
			
			if(!player.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && !ConfigHandler.SERVER.keepClawItems.get()){
				DragonStateHandler handler = DragonStateProvider.getCap(player).orElse(null);
				
				if(handler != null){
					for(int i = 0; i < handler.getClawInventory().getClawsInventory().getContainerSize(); i++){
						ItemStack stack = handler.getClawInventory().getClawsInventory().getItem(i);
						
						if(!stack.isEmpty()) {
							event.getDrops().add(new ItemEntity(player.level, player.getX(), player.getY(), player.getZ(), stack));
							handler.getClawInventory().getClawsInventory().setItem(i, ItemStack.EMPTY);
						}
					}
				}
			}
		}
	}
	
	
	@Mod.EventBusSubscriber( modid = DragonSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class Event_busHandler{
		@SubscribeEvent
		public void modifyBreakSpeed(PlayerEvent.BreakSpeed breakSpeedEvent) {
			if (!ConfigHandler.SERVER.bonuses.get() || !ConfigHandler.SERVER.clawsAreTools.get())
				return;
			PlayerEntity playerEntity = breakSpeedEvent.getPlayer();
			
			ItemStack mainStack = playerEntity.getMainHandItem();
			DragonStateHandler dragonStateHandler = DragonStateProvider.getCap(playerEntity).orElse(null);
			if(mainStack.getItem() instanceof TieredItem || dragonStateHandler == null || !dragonStateHandler.isDragon()) return;
			
			BlockState blockState = breakSpeedEvent.getState();
			Item item = mainStack.getItem();
			
			float originalSpeed = breakSpeedEvent.getOriginalSpeed();
			
			if(!(item instanceof ToolItem)){
				for (int i = 1; i < 4; i++) {
					if (blockState.getHarvestTool() == DragonStateHandler.CLAW_TOOL_TYPES[i]) {
						ItemStack breakingItem = dragonStateHandler.getClawInventory().getClawsInventory().getItem(i);
						if (breakingItem != null && !breakingItem.isEmpty()) {
							return;
						}
					}
				}
			}
			
			if (!(item instanceof ToolItem || item instanceof SwordItem || item instanceof ShearsItem)) {
				float bonus = dragonStateHandler.getLevel() == DragonLevel.ADULT ? (
						blockState.getHarvestTool() == ToolType.AXE && dragonStateHandler.getType() == DragonType.FOREST ? 4 :
						blockState.getHarvestTool() == ToolType.PICKAXE && dragonStateHandler.getType() == DragonType.CAVE ? 4 :
						blockState.getHarvestTool() == ToolType.SHOVEL && dragonStateHandler.getType() == DragonType.SEA ? 4 : 2F
						) : dragonStateHandler.getLevel() == DragonLevel.BABY ? ConfigHandler.SERVER.bonusUnlockedAt.get() != DragonLevel.BABY ? 2F : 1F
						: dragonStateHandler.getLevel() == DragonLevel.YOUNG ? ConfigHandler.SERVER.bonusUnlockedAt.get() == DragonLevel.ADULT && dragonStateHandler.getLevel() != DragonLevel.BABY ? 2F : 1F
						: 2F;
				
				breakSpeedEvent.setNewSpeed((originalSpeed * bonus));
			}
		}
	}
}
