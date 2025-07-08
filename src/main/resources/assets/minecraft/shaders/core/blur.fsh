#version 150

uniform sampler2D InputSampler;
uniform vec2 InputResolution;
uniform vec2 uSize;
uniform vec2 uLocation;
uniform float topLeftRadius;
uniform float topRightRadius;
uniform float bottomRightRadius;
uniform float bottomLeftRadius;
uniform float Brightness;
uniform float Quality;
uniform vec4 color1;
in vec2 texCoord;

out vec4 fragColor;

float roundedBoxSDF(vec2 center, vec2 size, float topLeftRadius, float topRightRadius, float bottomRightRadius, float bottomLeftRadius) {
    vec2 q = abs(center) - size;
    vec2 corner = center.xy;
    float radius;
    if (corner.x > 0.0 && corner.y > 0.0) {
        radius = topRightRadius;
    } else if (corner.x > 0.0 && corner.y < 0.0) {
        radius = bottomRightRadius;
    } else if (corner.x < 0.0 && corner.y < 0.0) {
        radius = bottomLeftRadius;
    } else {
        radius = topLeftRadius;
    }

    return length(max(q + radius, 0.0)) - radius;
}

vec4 blur() {
    #define TAU 6.28318530718

    vec2 Radius = Quality / InputResolution.xy;

    vec2 uv = gl_FragCoord.xy / InputResolution.xy;
    vec4 Color = texture(InputSampler, uv);

    float step = TAU / 16.0;

    for (float d = 0.0; d < TAU; d += step) {
        for (float i = 0.2; i <= 1.0; i += 0.2) {
            Color += texture(InputSampler, uv + vec2(cos(d), sin(d)) * Radius * i);
        }
    }

    Color /= 80;
    return (Color + color1) * Brightness;
}

void main() {
    vec2 halfSize = uSize / 2.0;
    float smoothedAlpha = (1.0 - smoothstep(0.0, 1.0, roundedBoxSDF(gl_FragCoord.xy - uLocation - halfSize, halfSize, topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius)));
    fragColor = vec4(blur().rgb, smoothedAlpha * color1.a);
}