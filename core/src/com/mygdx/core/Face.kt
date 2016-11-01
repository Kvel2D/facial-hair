package com.mygdx.core

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils

var minFaceWidth = 150f
var maxFaceWidth = 300f
var minFaceHeight = 200f
var maxFaceHeight = 270f
val skinColorMin = Color(200 / 255f, 180 / 255f, 150 / 255f, 1f)
val skinColorMax = Color(254 / 255f, 211 / 255f, 186 / 255f, 1f)

class Face(var x: Float, var y: Float, var lookingDirection: Int) {
    val width = MathUtils.random(minFaceWidth, maxFaceWidth)
    val height = MathUtils.random(minFaceHeight, maxFaceHeight)
    val eyes = Eyes(this)
    val mouth = Mouth(this)
    val brows = Brows(this)
    val skinColor = Color(
            MathUtils.random(skinColorMin.r, skinColorMax.r),
            MathUtils.random(skinColorMin.g, skinColorMax.g),
            MathUtils.random(skinColorMin.b, skinColorMax.b),
            1f)
    var sleeping = false

    fun drawShapes(shapeRenderer: ShapeRenderer) {
        // face, eyes, pupils
        shapeRenderer.color = skinColor
        shapeRenderer.ellipse(x - width / 2, y, width, height)
        if (!sleeping) {
            shapeRenderer.color = Color.WHITE
            shapeRenderer.circle(x - eyes.spacing / 2f, y + eyes.y, eyes.whiteRadius)
            shapeRenderer.circle(x + eyes.spacing / 2f, y + eyes.y, eyes.whiteRadius)
            shapeRenderer.color = Color.BLACK
            shapeRenderer.circle(x - eyes.spacing / 2f + lookingDirection * eyes.whiteRadius / 4f, y + eyes.y, eyes.pupilRadius)
            shapeRenderer.circle(x + eyes.spacing / 2f + lookingDirection * eyes.whiteRadius / 4f, y + eyes.y, eyes.pupilRadius)
        } else {
            shapeRenderer.color = Color.BLACK
            shapeRenderer.line(x - eyes.spacing / 2f - eyes.whiteRadius / 2f, y + eyes.y, x - eyes.spacing / 2f + eyes.whiteRadius / 2f, y + eyes.y)
            shapeRenderer.line(x + eyes.spacing / 2f - eyes.whiteRadius / 2f, y + eyes.y, x + eyes.spacing / 2f + eyes.whiteRadius / 2f, y + eyes.y)
        }
    }

    fun drawTextures(spriteBatch: SpriteBatch) {
        // mouth and brows
        var dy = (brows.y[1] - brows.y[0]).toDouble()
        var angle = (Math.atan2(dy, brows.height.toDouble()) / Constants.DEGTORAD).toFloat()
        spriteBatch.draw(brows.texture, x - brows.spacing / 2f - brows.width / 2f, y + brows.yBase + (brows.y[0] + brows.y[1]) / 2f, brows.width / 2f, brows.height / 2f, brows.width, brows.height, 1f, 1f, angle)
        dy = (brows.y[3] - brows.y[2]).toDouble()
        angle = (Math.atan2(dy, brows.height.toDouble()) / Constants.DEGTORAD).toFloat()
        spriteBatch.draw(brows.textureFlipped, x + brows.spacing / 2f - brows.width / 2f, y + brows.yBase + (brows.y[2] + brows.y[3]) / 2f, brows.width / 2f, brows.height / 2f, brows.width, brows.height, 1f, 1f, angle)
        spriteBatch.draw(mouth.texture, x - mouth.texture.regionWidth / 2f, y + mouth.y)
    }
}

val minEyeHeight = 0.55f
val maxEyeHeight = 0.7f
val minEyeSpacing = 0.2f
val maxEyeSpacing = 0.6f
val minEyeRadius = 0.05f
val maxEyeRadius = 0.07f
val minPupilRadius = 0.01f
val maxPupilRadius = 0.03f

class Eyes {
    val spacing: Float
    val y: Float
    val whiteRadius: Float
    val pupilRadius: Float

    constructor(face: Face) {
        spacing = MathUtils.random(face.width * minEyeSpacing, face.width * maxEyeSpacing)
        y = MathUtils.random(face.height * minEyeHeight, face.height * maxEyeHeight)
        whiteRadius = MathUtils.random(face.width * minEyeRadius, face.width * maxEyeRadius)
        pupilRadius = MathUtils.random(face.width * minPupilRadius, face.width * maxPupilRadius)
    }
}

val minMouthY = 0.1f
val maxMouthY = 0.4f

class Mouth {
    val y: Float
    val texture: TextureRegion

    constructor(face: Face) {
        val k = MathUtils.random(1, 8)
        texture = TextureRegion(assets.getTexture("mouth$k.png"))
        y = MathUtils.random(face.height * minMouthY, face.height * maxMouthY)
    }
}

val minBrowHeight = 0.01f
val maxBrowHeight = 0.1f
val minBrowSpacing = -0.02f
val maxBrowSpacing = 0.05f
val raiseMax = 20f
val raiseSpeed = 0.5f
val relaxSpeed = 1f
val low = 0
val high = 1
val neither = 2
val superHigh = 4

class Brows {
    val spacing: Float
    val y = FloatArray(4, { i -> 0f }) // 0-1 left brow, 2-3 right brow
    val yBase: Float
    val texture: TextureRegion
    val textureFlipped: TextureRegion
    val width: Float
    val height: Float
    var emotionBits = 0

    constructor(face: Face) {
        val k = MathUtils.random(1, 4)
        texture = TextureRegion(assets.getTexture("brow$k.png"))
        textureFlipped = TextureRegion(assets.getTexture("brow$k.png"))
        textureFlipped.flip(true, false)
        width = texture.regionWidth.toFloat()
        height = texture.regionHeight.toFloat()
        yBase = face.eyes.y + MathUtils.random(face.height * minBrowHeight, face.height * maxBrowHeight)
        spacing = face.eyes.spacing + MathUtils.random(face.height * minBrowSpacing, face.height * maxBrowSpacing)
    }

    // 1st 4 bits - left point of left brow, etc.
    fun calculateEmotionBits() {
        emotionBits = 0
        for (i in 0..3) {
            if (y[i] == 0f) {
                continue
            } else if (y[i] == raiseMax) {
                emotionBits = emotionBits or (high shl (4 * (3 - i)))
            } else {
                emotionBits = emotionBits or (neither shl (4 * (3 - i)))
            }
        }
    }

    fun emote(bits: Int) {
        for (i in 0..3) {
            emotionBits = (bits shr (4 * (3 - i))) and 0x000F
            if (emotionBits == low) {
                lower(i)
            } else if (emotionBits == high) {
                raise(i)
            } else if (emotionBits == superHigh) {
                y[i] = Math.min(y[i] + 2 * raiseSpeed, 10 * raiseMax)
            }
        }
    }

    fun raise(i: Int) {
        y[i] = Math.min(y[i] + raiseSpeed, raiseMax)
    }

    fun lower(i: Int) {
        y[i] = Math.max(y[i] - relaxSpeed, 0f)
    }
}
