package by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.buttons;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.DropDownButton;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.client.skinPartSystem.EnumSkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.misc.DragonLevel;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@EventBusSubscriber
public class EditorPartButton extends ExtendedButton {
	public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/textbox.png");

	private EnumSkinLayer layer;
	public String value;
	public Consumer<String> setter;
	public DropDownButton source;
	private final DragonEditorScreen screen;
	private final StringTextComponent message;

	private final DragonStateHandler handler = new DragonStateHandler();
	private static final ConcurrentHashMap<String, ResourceLocation> textures = new ConcurrentHashMap<>();
	private ResourceLocation texture;
	public EditorPartButton(DragonEditorScreen screen, DropDownButton source, int xPos, int yPos, int width, int height, String value, Consumer<String> setter, EnumSkinLayer layer){
		super(xPos, yPos, width, height, StringTextComponent.EMPTY, (s) -> {});
		this.value = value;
		this.setter = setter;
		this.source = source;
		this.screen = screen;
		this.layer = layer;
		String val = (value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1).toLowerCase(Locale.ROOT)).replace("_", " ");

		if(val.length() >= 30){
			val = val.substring(0, 27) + "...";
		}

		message = new StringTextComponent(val);

		generateImage();
	}

	@OnlyIn( Dist.CLIENT )
	@SubscribeEvent
	public static void clientStart(FMLClientSetupEvent event){
		if(Minecraft.getInstance().getResourceManager() instanceof IReloadableResourceManager){
			((IReloadableResourceManager)Minecraft.getInstance().getResourceManager()).registerReloadListener((IResourceManagerReloadListener)manager -> {
				textures.clear();
			});
		}
	}
	public void generateImage(){
		String key = layer.name().toLowerCase(Locale.ROOT) + "_" + screen.type.name().toLowerCase(Locale.ROOT) + "_" + value.toLowerCase(Locale.ROOT);

		if(textures.containsKey(key)){
			texture = textures.get(key);
			return;
		}

		handler.getSkin().blankSkin = true;
		handler.setSize(DragonLevel.ADULT.size);
		handler.setType(screen.type);
		handler.setHasWings(true);
		handler.getSkin().skinPreset.skinAges.get(DragonLevel.ADULT).layerSettings.get(layer).selectedSkin = value;

		int width = 256;
		int height = 256;

		float zoom = 0;
		float xRot = -5;
		float yRot = -3;
		float xOffset = 0;
		float yOffset = 0;

		if(layer == EnumSkinLayer.EYES){
			handler.getSkin().skinPreset.skinAges.get(DragonLevel.ADULT).wings = false;
			zoom = 250;
			yOffset = -1.05f;
			xOffset = 1f;
			xRot = -10;
			yRot = 0;
		}else if(layer == EnumSkinLayer.HORNS){
			handler.getSkin().skinPreset.skinAges.get(DragonLevel.ADULT).wings = false;
			zoom = 40;
			yOffset = -1f;
			xOffset = 0.75f;
			xRot = -10;
			yRot = -5;
		}else if(layer == EnumSkinLayer.BOTTOM){
			handler.getSkin().skinPreset.skinAges.get(DragonLevel.ADULT).wings = true;
			zoom = 10;
			yOffset = -0.5f;
			xRot = 2;
			yRot = 6;
		}else if(layer == EnumSkinLayer.SPIKES){
			handler.getSkin().skinPreset.skinAges.get(DragonLevel.ADULT).wings = false;
			xRot = 6;
			yRot = -5;
			yOffset = 0.5f;
		}else{
			handler.getSkin().skinPreset.skinAges.get(DragonLevel.ADULT).wings = true;
			xRot = 6;
		}

		RenderSystem.pushMatrix();
		Framebuffer framebuffer = new Framebuffer(width, height, true, false);
		framebuffer.bindWrite(true);
		framebuffer.blitToScreen(width, height);

		ClientDragonRender.dragonModel.setCurrentTexture(null);
		FakeClientPlayerUtils.getFakePlayer(1, handler).animationSupplier = () -> "sit";
		ClientDragonRender.renderEntityInInventory(FakeClientPlayerUtils.getFakeDragon(1, handler), width / 2, height / 2, 80 + (zoom * 2), xRot, yRot, xOffset, yOffset);

		NativeImage nativeimage = new NativeImage(width, height, false);
		RenderSystem.bindTexture(framebuffer.getColorTextureId());
		nativeimage.downloadTexture(0, false);
		nativeimage.flipY();

		texture = new ResourceLocation(DragonSurvivalMod.MODID, "dynamic_preview_" + value.toLowerCase() + "_" + layer.name().toLowerCase());
		textures.put(key, texture);

		DynamicTexture dynamicTexture = new DynamicTexture(width, height, false);
		dynamicTexture.setPixels(nativeimage);
		dynamicTexture.upload();

		nativeimage.close();

		Minecraft.getInstance().getTextureManager().register(texture, dynamicTexture);

		framebuffer.unbindWrite();
		framebuffer.destroyBuffers();
		RenderSystem.popMatrix();

		Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
	}

	@Override
	public void renderButton(MatrixStack mStack, int mouseX, int mouseY, float partial){
		Minecraft.getInstance().textureManager.bind(BACKGROUND_TEXTURE);
		GuiUtils.drawContinuousTexturedBox(mStack, x, y, !active ? 32 : 0, isHovered() && active ? 32 : 0, width, height, 32, 32, 10, 0);

		if(texture != null){
			Minecraft.getInstance().textureManager.bind(texture);
			blit(mStack, x + 3, y + 3, 0, 0, width - 6, height - 6, width - 6, height - 6);
		}

		TextRenderUtil.drawScaledTextSplit(mStack , this.x + 4, this.y + (this.height - 10), 0.4f, message.getContents(), getFGColor(), width - 9, 200);
	}

	@Override
	public void onPress(){
		super.onPress();
		source.current = value;
		source.onPress();
		setter.accept(value);
	}
}