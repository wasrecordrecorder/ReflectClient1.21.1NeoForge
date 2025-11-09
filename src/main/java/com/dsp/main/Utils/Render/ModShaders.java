package com.dsp.main.Utils.Render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@EventBusSubscriber(modid = "dsp", value = Dist.CLIENT)
public class ModShaders {
    private static final Logger LOGGER = LoggerFactory.getLogger("DSP-ModShaders");

    public static ShaderProgram RECTANGLE_SHADER;
    public static ShaderProgram BORDER_SHADER;
    public static ShaderProgram MSDF_FONT_SHADER;
    public static ShaderProgram BLUR_SHADER;
    public static ShaderProgram POSITION_SHADER;
    public static ShaderProgram POSITION_TEX_SHADER;

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        LOGGER.info("=== DSP SHADER REGISTRATION START ===");

        try {
            RECTANGLE_SHADER = new ShaderProgram(
                    ResourceLocation.fromNamespaceAndPath("dsp", "rectangle"),
                    DefaultVertexFormat.POSITION,
                    ShaderDefines.EMPTY
            );
            event.registerShader(RECTANGLE_SHADER);
            LOGGER.info("Registered shader: dsp:rectangle");
        } catch (Exception e) {
            LOGGER.error("Failed to register dsp:rectangle", e);
        }

        try {
            BORDER_SHADER = new ShaderProgram(
                    ResourceLocation.fromNamespaceAndPath("dsp", "border"),
                    DefaultVertexFormat.POSITION_COLOR,
                    ShaderDefines.EMPTY
            );
            event.registerShader(BORDER_SHADER);
            LOGGER.info("Registered shader: dsp:border");
        } catch (Exception e) {
            LOGGER.error("Failed to register dsp:border", e);
        }

        try {
            MSDF_FONT_SHADER = new ShaderProgram(
                    ResourceLocation.fromNamespaceAndPath("dsp", "msdf_font"),
                    DefaultVertexFormat.POSITION_TEX_COLOR,
                    ShaderDefines.EMPTY
            );
            event.registerShader(MSDF_FONT_SHADER);
            LOGGER.info("Registered shader: dsp:msdf_font");
        } catch (Exception e) {
            LOGGER.error("Failed to register dsp:msdf_font", e);
        }

        try {
            BLUR_SHADER = new ShaderProgram(
                    ResourceLocation.fromNamespaceAndPath("dsp", "blur"),
                    DefaultVertexFormat.POSITION,
                    ShaderDefines.EMPTY
            );
            event.registerShader(BLUR_SHADER);
            LOGGER.info("Registered shader: dsp:blur");
        } catch (Exception e) {
            LOGGER.error("Failed to register dsp:blur", e);
        }

        try {
            POSITION_SHADER = new ShaderProgram(
                    ResourceLocation.fromNamespaceAndPath("dsp", "position"),
                    DefaultVertexFormat.POSITION,
                    ShaderDefines.EMPTY
            );
            event.registerShader(POSITION_SHADER);
            LOGGER.info("Registered shader: dsp:position");
        } catch (Exception e) {
            LOGGER.error("Failed to register dsp:position", e);
        }

        try {
            POSITION_TEX_SHADER = new ShaderProgram(
                    ResourceLocation.fromNamespaceAndPath("dsp", "position_tex"),
                    DefaultVertexFormat.POSITION_TEX,
                    ShaderDefines.EMPTY
            );
            event.registerShader(POSITION_TEX_SHADER);
            LOGGER.info("Registered shader: dsp:position_tex");
        } catch (Exception e) {
            LOGGER.error("Failed to register dsp:position_tex", e);
        }

        LOGGER.info("=== DSP SHADER REGISTRATION END ===");
    }
}