package com.dsp.main.Utils.Render.Blur;

import com.dsp.main.Utils.Render.ColorUtil;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

public class Outline2Shader extends Shader {

    private final Uniform color;

    public Outline2Shader() {
        super("outline2", DefaultVertexFormat.POSITION);
        color = uniform("color");
    }

    public void setupBind(RenderTarget target, int c) {
        setSample("sampler", target.getColorTextureId());
        color.set(ColorUtil.r(c), ColorUtil.g(c), ColorUtil.b(c), ColorUtil.a(c));
        super.bind();
    }
}
