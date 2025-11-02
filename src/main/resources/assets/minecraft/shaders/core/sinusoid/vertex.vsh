#version 150
in vec3 Position;
in vec2 UV;
in vec4 Color;

uniform mat4 ProjectionMatrix;
uniform mat4 ViewModelMatrix;
uniform float Time;

out vec2 vUv;
out vec4 vColor;
out float vGlow;

void main() {
    vec3 pos = Position;
    float phase = Time * 2.0 + pos.x * 0.5;
    pos.y += sin(phase + pos.z * 1.5) * 0.25;
    vUv = UV;
    vColor = Color;
    vGlow = 0.5 + 0.5 * sin(phase);
    gl_Position = ProjectionMatrix * ViewModelMatrix * vec4(pos, 1.0);
}
