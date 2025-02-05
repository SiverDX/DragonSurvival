package by.dragonsurvivalteam.dragonsurvival.common.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.client.particles.DSParticles;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.MagicCap;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.passive.BurnAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.ForestDragon.active.HunterAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.active.RevealingTheSoulAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.active.SeaEyesAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.active.StormBreathAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.passive.SpectralImpactAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ActiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.BreathAbility.BreathDamage;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.TargetingFunctions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingVisibilityEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.Objects;
import java.util.UUID;

@EventBusSubscriber
public class MagicHandler{
	private static final UUID DRAGON_PASSIVE_MOVEMENT_SPEED = UUID.fromString("cdc3be6e-e17d-4efa-90f4-9dd838e9b000");

	@SubscribeEvent
	public static void magicUpdate(PlayerTickEvent event){
		if(event.phase == Phase.START){
			return;
		}

		Player player = event.player;

		AttributeInstance moveSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);

		DragonStateProvider.getCap(player).ifPresent(cap -> {
			if(!cap.isDragon() || cap.getLevel() != DragonLevel.ADULT){
				if(moveSpeed.getModifier(DRAGON_PASSIVE_MOVEMENT_SPEED) != null){
					moveSpeed.removeModifier(DRAGON_PASSIVE_MOVEMENT_SPEED);
				}
			}

			if(cap.getLevel() == DragonLevel.ADULT){
				AttributeModifier move_speed = new AttributeModifier(DRAGON_PASSIVE_MOVEMENT_SPEED, "DRAGON_MOVE_SPEED", 0.2F, AttributeModifier.Operation.MULTIPLY_TOTAL);

				if(moveSpeed.getModifier(DRAGON_PASSIVE_MOVEMENT_SPEED) == null){
					moveSpeed.addTransientModifier(move_speed);
				}
			}


			if(cap.getMagicData().abilities.isEmpty() || cap.getMagicData().innateDragonAbilities.isEmpty() || cap.getMagicData().activeDragonAbilities.isEmpty()){
				cap.getMagicData().initAbilities(cap.getType());
			}

			for(int i = 0; i < MagicCap.activeAbilitySlots; i++){
				ActiveDragonAbility ability = cap.getMagicData().getAbilityFromSlot(i);

				if(ability != null){
					ability.tickCooldown();
				}
			}
		});
	}

	@SubscribeEvent
	public static void playerTick(PlayerTickEvent event){
		if(event.phase == Phase.START){
			return;
		}

		Player player = event.player;

		DragonStateProvider.getCap(player).ifPresent(cap -> {
			if(!cap.isDragon()){
				return;
			}

			for(DragonAbility ability : cap.getMagicData().abilities.values()){
				ability.player = player;
			}

			if(player.hasEffect(DragonEffects.WATER_VISION) && (player.isEyeInFluid(FluidTags.WATER) || SeaEyesAbility.seaEyesOutOfWater)){
				player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 10, 0, false, false));
			}

			if(player.hasEffect(DragonEffects.HUNTER)){
				BlockState bl = player.getFeetBlockState();
				BlockState below = player.level.getBlockState(player.blockPosition().below());

				if(bl.getMaterial() == Material.PLANT || bl.getMaterial() == Material.REPLACEABLE_PLANT || bl.getMaterial() == Material.GRASS || below.getMaterial() == Material.PLANT || below.getMaterial() == Material.REPLACEABLE_PLANT){
					player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 10, 0, false, false));
				}

				player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 2, false, false));
			}
		});
	}

	@SubscribeEvent
	public static void livingVisibility(LivingVisibilityEvent event){
		if(event.getEntity() instanceof Player player){
			DragonStateProvider.getCap(player).ifPresent(cap -> {
				if(!cap.isDragon()){
					return;
				}

				if(player.hasEffect(DragonEffects.HUNTER)){
					event.modifyVisibility(0);
				}
			});
		}
	}

	@SubscribeEvent
	public static void livingTick(LivingEvent.LivingTickEvent event){
		LivingEntity entity = event.getEntity();
		EntityStateHandler cap = DragonUtils.getEntityHandler(entity);

		if(entity.hasEffect(DragonEffects.BURN)){
			if(entity.isEyeInFluid(FluidTags.WATER) || entity.isInWaterRainOrBubble()){
				entity.removeEffect(DragonEffects.BURN);
			}
		}

		if(entity.tickCount % 20 == 0){
			if(entity.hasEffect(DragonEffects.DRAIN)){
				if(!DragonUtils.isDragonType(entity, DragonTypes.FOREST)){
					Player player = cap.lastAfflicted != -1 && entity.level.getEntity(cap.lastAfflicted) instanceof Player ? (Player)entity.level.getEntity(cap.lastAfflicted) : null;
					if(player != null){
						TargetingFunctions.attackTargets(player, ent -> ent.hurt(new EntityDamageSource("magic", player).bypassArmor().setMagic(), 1f), entity);
					}else{
						entity.hurt(DamageSource.MAGIC, 1.0F);
					}
				}
			}

			if(entity.hasEffect(DragonEffects.CHARGED)){
				Player player = cap.lastAfflicted != -1 && entity.level.getEntity(cap.lastAfflicted) instanceof Player ? (Player)entity.level.getEntity(cap.lastAfflicted) : null;
				if(!DragonUtils.isDragonType(entity, DragonTypes.SEA)){
					StormBreathAbility.chargedEffectSparkle(player, entity, StormBreathAbility.chargedChainRange, StormBreathAbility.chargedEffectChainCount, StormBreathAbility.chargedEffectDamage);
				}
			}

			if(entity.hasEffect(DragonEffects.BURN)){
				if(!entity.fireImmune()){
					if(cap.lastPos != null){
						double distance = entity.distanceToSqr(cap.lastPos);
						float damage = Mth.clamp((float)distance, 0, 10);

						if(damage > 0){
							if(!entity.isOnFire()){
								// Short enough fire duration to not cause fire damage but still drop cooked items
								entity.setRemainingFireTicks(1);
							}
							Player player = cap.lastAfflicted != -1 && entity.level.getEntity(cap.lastAfflicted) instanceof Player ? (Player)entity.level.getEntity(cap.lastAfflicted) : null;
							if(player != null){
								TargetingFunctions.attackTargets(player, ent -> ent.hurt(new EntityDamageSource("onFire", player).bypassArmor().setIsFire(), damage), entity);
							}else{
								entity.hurt(DamageSource.ON_FIRE, damage);
							}
						}
					}
				}
			}

			cap.lastPos = entity.position();
		}

	}

	@SubscribeEvent
	public static void playerStruckByLightning(EntityStruckByLightningEvent event){
		if(event.getEntity() instanceof Player player){
			
			DragonStateProvider.getCap(player).ifPresent(cap -> {
				if(!cap.isDragon()){
					return;
				}

				if(Objects.equals(cap.getType(), DragonTypes.SEA)){
					event.setCanceled(true);
				}
			});
		}
	}

	@SubscribeEvent
	public static void playerDamaged(LivingDamageEvent event){
		if(event.getEntity() instanceof Player player){
			DragonStateProvider.getCap(player).ifPresent(cap -> {
				if(!cap.isDragon()){
					return;
				}

				if(player.hasEffect(DragonEffects.HUNTER)){
					player.removeEffect(DragonEffects.HUNTER);
				}
			});
		}
	}

	@SubscribeEvent
	public static void playerHitEntity(CriticalHitEvent event){
		Player player = event.getEntity();
		DragonStateProvider.getCap(player).ifPresent(cap -> {
			if(!cap.isDragon()){
				return;
			}

			if(player.hasEffect(DragonEffects.HUNTER)){
				MobEffectInstance hunter = player.getEffect(DragonEffects.HUNTER);
				player.removeEffect(DragonEffects.HUNTER);
				event.setDamageModifier((float)((hunter.getAmplifier() + 1) * HunterAbility.hunterDamageBonus));
				event.setResult(Result.ALLOW);
			}
		});
	}

	@SubscribeEvent
	public static void livingHurt(LivingAttackEvent event){
		if(event.getSource() instanceof EntityDamageSource && !(event.getSource() instanceof IndirectEntityDamageSource) && !(event.getSource() instanceof BreathDamage)){
			if(event.getEntity() != null){
				if(event.getSource() != null && event.getSource().getEntity() != null){
					if(event.getSource().getEntity() instanceof Player player){
						DragonStateProvider.getCap(player).ifPresent(cap -> {
							if(!cap.isDragon()){
								return;
							}

							if(Objects.equals(cap.getType(), DragonTypes.SEA)){
								SpectralImpactAbility spectralImpact = DragonAbilities.getSelfAbility(player, SpectralImpactAbility.class);
								boolean hit = player.getRandom().nextInt(100) <= spectralImpact.getChance();

								if(hit){
									event.getSource().bypassArmor();
									double d0 = -Mth.sin(player.yRot * ((float)Math.PI / 180F));
									double d1 = Mth.cos(player.yRot * ((float)Math.PI / 180F));

									if(player.level instanceof ServerLevel){
										((ServerLevel)player.level).sendParticles(DSParticles.seaSweep, player.getX() + d0, player.getY(0.5D), player.getZ() + d1, 0, d0, 0.0D, d1, 0.0D);
									}
								}
							}else if(Objects.equals(cap.getType(), DragonTypes.CAVE)){
								BurnAbility burnAbility = DragonAbilities.getSelfAbility(player, BurnAbility.class);
								boolean hit = player.getRandom().nextInt(100) < burnAbility.getChance();

								if(hit){
									EntityStateHandler entityCap = DragonUtils.getEntityHandler(event.getEntity());

									if(entityCap != null){
										entityCap.lastAfflicted = player.getId();
									}

									if(!player.level.isClientSide){
										event.getEntity().addEffect(new MobEffectInstance(DragonEffects.BURN, Functions.secondsToTicks(30)));
									}
								}
							}
						});
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void experienceDrop(LivingExperienceDropEvent event){
		Player player = event.getAttackingPlayer();

		if(player != null){
			DragonStateProvider.getCap(player).ifPresent(cap -> {
				if(!cap.isDragon()){
					return;
				}

				if(player.hasEffect(DragonEffects.REVEALING_THE_SOUL)){
					int extra = (int)Math.min(RevealingTheSoulAbility.revealingTheSoulMaxEXP, event.getDroppedExperience() * RevealingTheSoulAbility.revealingTheSoulMultiplier);
					event.setDroppedExperience(event.getDroppedExperience() + extra);
				}
			});
		}
	}
}