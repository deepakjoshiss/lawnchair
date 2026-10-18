package app.lawnchair.allapps

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import androidx.annotation.RequiresApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ShaderBrush

/**
 * DJ(drawer-glass): Liquid glass light effects for the drawer search pill (Android 13+, AGSL).
 * Light only: the wallpaper behind the pill is another window, so there is nothing to refract.
 * Bright rim on the edges facing the top-left light, dark rim opposite, soft inner glow and a
 * diagonal sheen. Drawn over the pill's own tint; the shape is computed here, so no clip needed.
 */
private const val LIQUID_GLASS_AGSL = """
uniform float2 size;
uniform float radius;
uniform float highlight;
uniform float shade;
uniform float sheen;

float sdRound(float2 p, float2 b, float r) {
    float2 q = abs(p) - b + r;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

half4 main(float2 xy) {
    float2 c = size * 0.5;
    float2 p = xy - c;
    float d = sdRound(p, c, radius);
    if (d > 0.5) return half4(0.0);

    float2 n = normalize(float2(
        sdRound(p + float2(1.0, 0.0), c, radius) - sdRound(p - float2(1.0, 0.0), c, radius),
        sdRound(p + float2(0.0, 1.0), c, radius) - sdRound(p - float2(0.0, 1.0), c, radius)) + 0.00001);
    float facing = dot(n, normalize(float2(-0.6, -0.8)));
    float inside = max(-d, 0.0);
    float rim = exp(-inside / 1.4);
    float glow = exp(-inside / 10.0);

    float lit = clamp(facing, 0.0, 1.0);
    float unlit = clamp(-facing, 0.0, 1.0);
    float hl = highlight * (rim * mix(0.25, 1.0, lit) + glow * 0.12);
    float sh = shade * unlit * (rim + glow * 0.3);

    float t = (xy.x / size.x) * 0.85 + (xy.y / size.y) * 0.15;
    float band = smoothstep(0.0, 0.12, t) * (1.0 - smoothstep(0.12, 0.45, t));
    float sn = sheen * band * (1.0 - 0.6 * xy.y / size.y);

    float aa = clamp(0.5 - d, 0.0, 1.0);
    float white = clamp(hl + sn, 0.0, 1.0) * aa;
    float black = clamp(sh, 0.0, 1.0) * aa;
    return half4(half3(white), half(white + black * (1.0 - white)));
}
"""

@RequiresApi(33)
fun Modifier.liquidGlass(cornerRadiusPx: Float, highlight: Float, shade: Float, sheen: Float): Modifier = drawWithCache {
    val shader = RuntimeShader(LIQUID_GLASS_AGSL).apply {
        setFloatUniform("size", size.width, size.height)
        setFloatUniform("radius", minOf(cornerRadiusPx, size.height / 2f))
        setFloatUniform("highlight", highlight)
        setFloatUniform("shade", shade)
        setFloatUniform("sheen", sheen)
    }
    val brush = ShaderBrush(shader)
    onDrawWithContent {
        drawContent()
        drawRect(brush)
    }
}

/**
 * Refraction for scroll-under (step 4): within [band] px of the pill's edge, content is sampled
 * further inside, so apps behind the rim bend like light through a lens, with a slight colour
 * split. Applied after the blur. Coordinates are the backdrop layer's, the pill at [x], [y].
 */
private const val LIQUID_REFRACT_AGSL = """
uniform shader content;
uniform float2 origin;
uniform float2 size;
uniform float radius;
uniform float band;
uniform float strength;

float sdRound(float2 p, float2 b, float r) {
    float2 q = abs(p) - b + r;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

half4 main(float2 xy) {
    float2 h = size * 0.5;
    float2 p = xy - origin - h;
    float d = sdRound(p, h, radius);
    if (d > 0.0) return content.eval(xy);

    float2 n = normalize(float2(
        sdRound(p + float2(1.0, 0.0), h, radius) - sdRound(p - float2(1.0, 0.0), h, radius),
        sdRound(p + float2(0.0, 1.0), h, radius) - sdRound(p - float2(0.0, 1.0), h, radius)) + 0.00001);
    float t = clamp(1.0 + d / band, 0.0, 1.0);
    float2 off = -n * strength * band * t * t;

    half4 g = content.eval(xy + off);
    return half4(content.eval(xy + off * 1.08).r, g.g, content.eval(xy + off * 0.92).b, g.a);
}
"""

@RequiresApi(33)
fun liquidRefraction(blurPx: Float, x: Float, y: Float, width: Float, height: Float, band: Float): RenderEffect {
    val shader = RuntimeShader(LIQUID_REFRACT_AGSL).apply {
        setFloatUniform("origin", x, y)
        setFloatUniform("size", width, height)
        setFloatUniform("radius", height / 2f)
        setFloatUniform("band", band)
        setFloatUniform("strength", 1.2f)
    }
    return RenderEffect.createChainEffect(
        RenderEffect.createRuntimeShaderEffect(shader, "content"),
        RenderEffect.createBlurEffect(blurPx, blurPx, Shader.TileMode.DECAL),
    )
}
