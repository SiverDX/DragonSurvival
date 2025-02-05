package by.dragonsurvivalteam.dragonsurvival.common.items.food;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.FoodProperties.Builder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class DragonFoodItem extends Item{
	private Supplier<MobEffectInstance>[] effects;
	private AbstractDragonType dragonType;
	private Consumer<LivingEntity> onEat;

	public DragonFoodItem(Properties p_i48487_1_){
		super(p_i48487_1_.food(genFoodProperties(null, null)));
	}

	public DragonFoodItem(Properties p_i48487_1_, Supplier<MobEffectInstance>... effectInstances){
		super(p_i48487_1_.food(genFoodProperties(null, effectInstances)));
		effects = effectInstances;
	}


	public DragonFoodItem(Properties p_i48487_1_, AbstractDragonType dragonType, Supplier<MobEffectInstance>... effectInstances){
		super(p_i48487_1_.food(genFoodProperties(dragonType, effectInstances)));
		this.dragonType = dragonType;
		effects = effectInstances;
	}

	public DragonFoodItem(Properties p_i48487_1_, AbstractDragonType dragonType, Consumer<LivingEntity> onEat, Supplier<MobEffectInstance>... effectInstances){
		super(p_i48487_1_.food(genFoodProperties(dragonType, effectInstances)));
		this.dragonType = dragonType;
		effects = effectInstances;
		this.onEat = onEat;
	}

	public DragonFoodItem(Properties p_i48487_1_, AbstractDragonType dragonType, Consumer<LivingEntity> onEat){
		super(p_i48487_1_.food(genFoodProperties(dragonType, null)));
		this.dragonType = dragonType;
		this.onEat = onEat;
	}

	@NotNull
	private static FoodProperties genFoodProperties(AbstractDragonType dragonType, Supplier<MobEffectInstance>... effectInstances){
		Builder builder = new Builder();
		builder.nutrition(1);
		builder.saturationMod(0.4F);
        builder.effect(() -> new MobEffectInstance(MobEffects.HUNGER, 20 * 15, 0), 1.0F);

		if(effectInstances != null){
			builder.alwaysEat();
		}
		
		return builder.build();
	}
	
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag tooltipFlag){
		super.appendHoverText(stack, world, list, tooltipFlag);
		String langKey = "ds.description." + ResourceHelper.getKey(this).getPath();
		if(I18n.exists(langKey)){
			list.add(Component.translatable(langKey));
		}
	}

	@Override
	public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity){
		ItemStack stack = super.finishUsingItem(pStack, pLevel, pLivingEntity);

		if(onEat != null){
			if(DragonUtils.isDragon(pLivingEntity) && (dragonType == null || DragonUtils.isDragonType(pLivingEntity, dragonType))){
				onEat.accept(pLivingEntity);
			}
		}
		if(effects != null && effects.length > 0){
			if(DragonUtils.isDragon(pLivingEntity) && (dragonType == null || DragonUtils.isDragonType(pLivingEntity, dragonType))){
				for(Supplier<MobEffectInstance> effect : effects){
					pLivingEntity.addEffect(effect.get());
				}
			}
		}

		return stack;
	}

}