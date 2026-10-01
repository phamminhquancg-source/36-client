#version 410 core
layout(std140) uniform Uniforms {
    mat4 uProjection;
    vec4 uColor;
    float uWidth;
    float uGlowWidth;
    float uZ;
    vec2 _pad;
};
in vec2 vUV;
in vec2 vPos;
out vec4 fragColor;
void main() {
    // vUV.x = along line 0-1, vUV.y = across -0.5 to 0.5
    float dist = abs(vUV.y);
    float coreHalf = uWidth * 0.5;
    float glowHalf = uGlowWidth * 0.5;
    float edge = fwidth(dist);
    // core alpha smooth
    float coreA = 1.0 - smoothstep(coreHalf - edge, coreHalf + edge, dist * 40.0);
    // glow falloff exponential
    float glowA = exp(-dist * dist * 90.0 / (glowHalf * glowHalf));
    glowA *= 0.6;
    float a = max(coreA, glowA);
    if (a < 0.01) discard;
    vec3 col = uColor.rgb * (1.0 + glowA * 0.8);
    fragColor = vec4(col, uColor.a * a);
}
