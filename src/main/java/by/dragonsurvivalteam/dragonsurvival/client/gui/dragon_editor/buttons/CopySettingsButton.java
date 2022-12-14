package by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.buttons;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.CopyEditorSettingsComponent;
import by.dragonsurvivalteam.dragonsurvival.client.util.TooltipRendering;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class CopySettingsButton extends ExtendedButton{

	private final DragonEditorScreen screen;
	public boolean toggled;
	private CopyEditorSettingsComponent component;
	private Widget renderButton;

	public CopySettingsButton(DragonEditorScreen screen, int xPos, int yPos, int width, int height, Component displayString, OnPress handler){
		super(xPos, yPos, width, height, displayString, handler);
		this.screen = screen;
	}


	@Override
	public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
		this.active = this.visible = screen.showUi;
		super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);

		if(visible){
			RenderSystem.setShaderTexture(0, new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/copy_icon.png"));
			blit(pMatrixStack, x, y, 0, 0, 16, 16, 16, 16);
		}

		if(toggled && (!visible || !isMouseOver(pMouseX, pMouseY) && (component == null || !component.isMouseOver(pMouseX, pMouseY)))){
			toggled = false;
			Screen screen = Minecraft.getInstance().screen;
			screen.children.removeIf(s -> s == component);
			screen.renderables.removeIf(s -> s == renderButton);
		}

		if(this.isHoveredOrFocused() && (component != null && !component.isMouseOver(pMouseX, pMouseY) || !toggled))
			this.renderToolTip(pMatrixStack, pMouseX, pMouseY);
	}

	@Override
	public void renderToolTip(PoseStack p_230443_1_, int p_230443_2_, int p_230443_3_){
		TooltipRendering.drawHoveringText(p_230443_1_, new TranslatableComponent("ds.gui.dragon_editor.copy"), p_230443_2_, p_230443_3_);
	}

	@Override
	public void renderButton(PoseStack mStack, int mouseX, int mouseY, float partial){}

	@Override
	public void onPress(){
		if(!toggled){
			renderButton = new ExtendedButton(0, 0, 0, 0, TextComponent.EMPTY, null){
				@Override
				public void render(PoseStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_){
					this.active = this.visible = false;

					if(component != null){
						component.visible = CopySettingsButton.this.visible;

						if(component.visible)
							component.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
					}
				}
			};

			int offset = screen.height - (y + 80);
			component = new CopyEditorSettingsComponent(screen, this, x + width - 80, y + Math.min(offset, 0), 80, 70);
			screen.children.add(0, component);
			screen.children.add(component);
			screen.renderables.add(renderButton);
		}else{
			screen.children.removeIf(s -> s == component);
			screen.renderables.removeIf(s -> s == renderButton);
		}

		toggled = !toggled;
	}
}