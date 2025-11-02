#version 150
in vec2 vUv;
in vec4 vColor;
in float vGlow;

uniform float GlowIntensity;

out vec4 outColor;

void main() {
    float alpha = vColor.a;
    float glow = clamp(vGlow * GlowIntensity, 0.0, 1.0);
    vec3 base = vColor.rgb;

    // Базовый цвет куба (как сейчас)
    vec3 bloom = base * (0.6 + glow * 0.8);

    // Для свечения вокруг: альфа по расстоянию к краям UV
    float edgeDist = min(min(vUv.x, 1.0 - vUv.x), min(vUv.y, 1.0 - vUv.y));
    float halo = smoothstep(0.0, 0.3, edgeDist) * glow;

    // Цвет свечения — яркий, полупрозрачный
    vec3 glowColor = base * 1.5;

    // Если это "внешняя" часть — рисуем только свечение
    outColor = vec4(base + bloom, alpha);
    outColor.rgb += glowColor * (1.0 - halo) * 0.5;
}
