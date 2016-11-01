package com.mygdx.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import java.util.*

class IntRect(var x: Int, var y: Int, var w: Int, var h: Int)

class Game {
    enum class STATE {
        CONTROLS, NORMAL
    }
    var state = STATE.CONTROLS
    var stateTimer = 0f

    val spriteBatch: SpriteBatch = SpriteBatch()
    val shapeRenderer = ShapeRenderer()
    val viewport = IntRect(0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT)
    val camera: OrthographicCamera
    val font = assets.getFont("noto.fnt")
    val layout = GlyphLayout()

    enum class PHRASE_TYPE {
        STATEMENT,
        QUESTION,
        INFINITE,
        EXIT
    }

    enum class EMOTION {
        ANGRY, SYMPATHY, NEUTRAL, SURPRISED, QUESTIONING, ANGRY_QUESTIONING, IMPOSSIBLE, SLANTED_LEFT, SLANTED_RIGHT, NONE
    }

    val emotionBits = mutableMapOf(
            EMOTION.ANGRY to 0x1001,
            EMOTION.SYMPATHY to 0x0110,
            EMOTION.NEUTRAL to 0x0000,
            EMOTION.SURPRISED to 0x1111,
            EMOTION.QUESTIONING to 0x0010,
            EMOTION.ANGRY_QUESTIONING to 0x0001,
            EMOTION.SLANTED_LEFT to 0x0101,
            EMOTION.SLANTED_RIGHT to 0x1010,
            EMOTION.IMPOSSIBLE to 0x4444,
            EMOTION.NONE to 0xFFFF // unreachable state
    )

    class Phrase {
        val type: PHRASE_TYPE
        var text: String
        val botEmotion: EMOTION
        val goodResponse: EMOTION
        val badResponse: EMOTION
        val goodNext: Phrase?
        val badNext: Phrase?
        var timer = 0f

        constructor(text: String = "", type: PHRASE_TYPE = PHRASE_TYPE.STATEMENT) {
            this.type = type
            this.text = text
            this.botEmotion = EMOTION.NEUTRAL
            this.goodResponse = EMOTION.NONE
            this.badResponse = EMOTION.NONE
            goodNext = null
            badNext = null
        }

        constructor(text: String, botEmotion: EMOTION, duration: Float, next: Phrase) {
            this.type = PHRASE_TYPE.STATEMENT
            this.text = text
            this.timer = duration
            this.botEmotion = botEmotion
            this.goodResponse = EMOTION.NONE
            this.badResponse = EMOTION.NONE
            goodNext = next
            badNext = null
        }

        constructor(text: String, botEmotion: EMOTION, goodResponse: EMOTION, goodNext: Phrase) {
            this.type = PHRASE_TYPE.QUESTION
            this.text = text
            this.botEmotion = botEmotion
            this.goodResponse = goodResponse
            this.badResponse = EMOTION.NONE
            this.goodNext = goodNext
            this.badNext = null
        }

        constructor(text: String, botEmotion: EMOTION, goodResponse: EMOTION, goodNext: Phrase,
                    badResponse: EMOTION, badNext: Phrase) {
            this.type = PHRASE_TYPE.QUESTION
            this.text = text
            this.botEmotion = botEmotion
            this.goodResponse = goodResponse
            this.badResponse = badResponse
            this.goodNext = goodNext
            this.badNext = badNext
        }
    }

    val goaway = Phrase("ok, I'll go away", PHRASE_TYPE.EXIT)
    val leave = Phrase("gotta go, see you later", PHRASE_TYPE.EXIT)
    val friendlyLeave = Phrase("gotta go, see you later, friend", PHRASE_TYPE.EXIT)

    val impressed = Phrase("impressed you, huh?", EMOTION.NEUTRAL, 5f, leave) // path end
    val didntlike = Phrase("aww, you didn't like it?", EMOTION.SYMPATHY, 3f, goaway)
    val perfomance7 = Phrase("how was that?", EMOTION.QUESTIONING, EMOTION.SURPRISED, impressed, EMOTION.ANGRY, didntlike)
    val perfomance6 = Phrase("...", EMOTION.SYMPATHY, 0.4f, perfomance7)
    val perfomance5 = Phrase("...", EMOTION.ANGRY, 0.5f, perfomance6)
    val perfomance4 = Phrase("..", EMOTION.SLANTED_LEFT, 0.8f, perfomance5)
    val perfomance3 = Phrase(".", EMOTION.SURPRISED, 0.7f, perfomance4)
    val perfomance2 = Phrase("check it", EMOTION.QUESTIONING, 1f, perfomance3)
    val perfomance1 = Phrase("check it", EMOTION.ANGRY_QUESTIONING, 1f, perfomance2)
    val cantoo = Phrase("i can do this too, look", EMOTION.NEUTRAL, 2f, perfomance1)
    val active = Phrase("your eyebrows are very active!", EMOTION.SURPRISED, 2f, cantoo)
    val backtrack = Phrase("what are you trying to say?", EMOTION.QUESTIONING, 3f, active)

    val bye = Phrase("bye, then", PHRASE_TYPE.EXIT) // path end
    val hate = Phrase("okay, i guess you hate me", EMOTION.SYMPATHY, 3f, bye)
    val wrong = Phrase("what? is something wrong?", EMOTION.QUESTIONING, EMOTION.SYMPATHY, backtrack, EMOTION.ANGRY, hate)

    val wasfun = Phrase("that was fun", EMOTION.SLANTED_LEFT, 4f, friendlyLeave) // path end
    val cant = Phrase("got you!", EMOTION.NEUTRAL, 5f, wasfun)
    val impossible = Phrase("but can you do this?", EMOTION.IMPOSSIBLE, 6f, cant)
    val butcan = Phrase("but can you do this?", EMOTION.NEUTRAL, 2f, impossible)
    val yougood1 = Phrase("you're good...", EMOTION.NEUTRAL, 2.5f, butcan)
    val game6 = Phrase("", EMOTION.QUESTIONING, EMOTION.QUESTIONING, yougood1)
    val game55 = Phrase("", EMOTION.NEUTRAL, 1.5f, game6)
    val game5 = Phrase("", EMOTION.SURPRISED, EMOTION.SURPRISED, game55)
    val game45 = Phrase("", EMOTION.NEUTRAL, 1.5f, game5)
    val game4 = Phrase("", EMOTION.ANGRY, EMOTION.ANGRY, game45)
    val game35 = Phrase("", EMOTION.NEUTRAL, 1.5f, game4)
    val game3 = Phrase("", EMOTION.SLANTED_RIGHT, EMOTION.SLANTED_RIGHT, game35)
    val game25 = Phrase("", EMOTION.NEUTRAL, 1.5f, game3)
    val game2 = Phrase("", EMOTION.SLANTED_LEFT, EMOTION.SLANTED_LEFT, game25)
    val game15 = Phrase("", EMOTION.NEUTRAL, 1.5f, game2)
    val game1 = Phrase("", EMOTION.SURPRISED, EMOTION.SURPRISED, game15)
    val explain2 = Phrase("and you match it with yours!", EMOTION.NEUTRAL, 2f, game1)
    val explain = Phrase("i'll move my eyebrows", EMOTION.NEUTRAL, 2f, explain2)
    val gameintro = Phrase("want to play a game?", EMOTION.QUESTIONING, 3f, explain)
    val becomefriends = Phrase("yay, you're my first friend!", EMOTION.SYMPATHY, 3f, gameintro)

    val friends = Phrase("want to be friends?", EMOTION.QUESTIONING, EMOTION.SYMPATHY, becomefriends, EMOTION.ANGRY, wrong)
    val hello = Phrase("hello", EMOTION.NEUTRAL, 3f, friends)
    val sleep = Phrase("zzz...", PHRASE_TYPE.INFINITE)
    var currentPhrase = hello

    val browTimers = mutableMapOf(
            EMOTION.ANGRY to 0f,
            EMOTION.SYMPATHY to 0f,
            EMOTION.NEUTRAL to 0f,
            EMOTION.SURPRISED to 0f,
            EMOTION.QUESTIONING to 0f,
            EMOTION.ANGRY_QUESTIONING to 0f,
            EMOTION.SLANTED_LEFT to 0f,
            EMOTION.SLANTED_RIGHT to 0f,
            EMOTION.IMPOSSIBLE to 0f,
            EMOTION.NONE to 0f
    )

    var player = Face(400f, 200f, 1)
    var bot = Face(800f, 200f, -1)

    init {
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f)
        Texture.setAssetManager(assets)

        if (Gdx.graphics.isFullscreen) {
            val width = Gdx.graphics.width
            val height = Gdx.graphics.height
            val aspectRatio = width.toFloat() / height.toFloat()
            var scale: Float
            val crop = Vector2()

            if (aspectRatio > Constants.ASPECT_RATIO) {
                scale = height.toFloat() / Constants.VIEWPORT_HEIGHT.toFloat()
                crop.x = (width - Constants.VIEWPORT_WIDTH * scale) / 2f
            } else if (aspectRatio < Constants.ASPECT_RATIO) {
                scale = width.toFloat() / Constants.VIEWPORT_WIDTH.toFloat()
                crop.y = (height - Constants.VIEWPORT_HEIGHT * scale) / 2f;
            } else {
                scale = width.toFloat() / Constants.VIEWPORT_WIDTH.toFloat()
            }

            viewport.x = crop.x.toInt()
            viewport.y = crop.y.toInt()
            viewport.w = (Constants.VIEWPORT_WIDTH * scale).toInt()
            viewport.h = (Constants.VIEWPORT_WIDTH * scale).toInt()

            spriteBatch.projectionMatrix.setToOrtho2D(0f, 0f, viewport.w.toFloat(), viewport.w.toFloat())
        }

        camera = OrthographicCamera(viewport.w.toFloat(), viewport.h.toFloat())
        camera.translate(viewport.w / 2f, viewport.h / 2f)
        camera.update()

        val greetingVariants = mutableListOf("good ", "hello", "hi", "hey", "greetings")
        val calendar = Calendar.getInstance();
        val hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour < 4) {
            currentPhrase = sleep
            bot.sleeping = true
        } else if (hour < 12) {
            greetingVariants[0] += "morning"
        } else if (hour < 16) {
            greetingVariants[0] += "afternoon"
        } else {
            greetingVariants[0] += "evening"
        }
        if (hour > 4) { // not night
            val k = MathUtils.random(greetingVariants.size - 1)
            currentPhrase.text = greetingVariants[k]
        }
    }

    fun dispose() {
        spriteBatch.dispose()
        shapeRenderer.dispose()
    }

    fun updateControls(deltaTime: Float) {
        stateTimer += deltaTime

        Gdx.gl.glViewport(viewport.x, viewport.y, viewport.w, viewport.h)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        spriteBatch.begin()
        font.color = Color.BLACK
        font.draw(spriteBatch, "Move left brow: A/D", 10f, 400f)
        font.draw(spriteBatch, "Move right brow: left/right arrow keys", 10f, 300f)
        spriteBatch.end()

        if (stateTimer > 0.5f && Gdx.input.isKeyPressed(Input.Keys.ANY_KEY)) {
            state = STATE.NORMAL
            stateTimer = 0f
            val xScale = Math.abs((bot.width - minFaceWidth) / (maxFaceWidth - minFaceWidth)) + 0.9f
            val yScale = Math.abs((bot.height - minFaceHeight) / (maxFaceHeight - minFaceHeight)) + 0.9f
            font.data.setScale(xScale, yScale)
        }
    }

    fun updateNormal(deltaTime: Float) {

        val playerBrows = player.brows
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            playerBrows.raise(0)
        } else {
            playerBrows.lower(0)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            playerBrows.raise(1)
        } else {
            playerBrows.lower(1)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            playerBrows.raise(2)
        } else {
            playerBrows.lower(2)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            playerBrows.raise(3)
        } else {
            playerBrows.lower(3)
        }

        playerBrows.calculateEmotionBits()
        EMOTION.values().forEach {
            if (playerBrows.emotionBits == emotionBits[it]) {
                browTimers[it] = browTimers[it]!! + deltaTime
                if (browTimers[it]!! > 100f) {
                    browTimers[it] = 100f
                }
            } else {
                browTimers[it] = 0f
            }
        }

        val botBrows = bot.brows
        botBrows.emote(emotionBits[currentPhrase.botEmotion]!!)

        when (currentPhrase.type) {
            PHRASE_TYPE.STATEMENT -> {
                currentPhrase.timer -= deltaTime
                if (currentPhrase.timer <= 0f) {
                    currentPhrase = currentPhrase.goodNext!!
                }
            }
            PHRASE_TYPE.QUESTION -> {
                currentPhrase.timer += deltaTime
                if (currentPhrase.timer > 100f) {
                    currentPhrase.timer = 100f
                }
                if (currentPhrase.timer > 1.5f) {
                    if (browTimers[currentPhrase.goodResponse]!! > 0.1f) {
                        currentPhrase = currentPhrase.goodNext!!
                        browTimers[currentPhrase.goodResponse] = 0f
                    } else if (browTimers[currentPhrase.badResponse]!! > 0.1f) {
                        currentPhrase = currentPhrase.badNext!!
                        browTimers[currentPhrase.badResponse] = 0f
                    }
                }
            }
            PHRASE_TYPE.EXIT -> {
                if (bot.x < 3000f) {
                    bot.x += 3f
                }
            }
            PHRASE_TYPE.INFINITE -> {
                // do nothing
            }
        }

        Gdx.gl.glViewport(viewport.x, viewport.y, viewport.w, viewport.h)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        player.drawShapes(shapeRenderer)
        bot.drawShapes(shapeRenderer)
        shapeRenderer.end()

        spriteBatch.begin()
        player.drawTextures(spriteBatch)
        bot.drawTextures(spriteBatch)
        font.color = Color.BLACK
//        font.draw(spriteBatch, "ANGRY: ${browTimers[EMOTION.ANGRY]}", 10f, 100f)
//        font.draw(spriteBatch, "NEUTRAL: ${browTimers[EMOTION.NEUTRAL]}", 10f, 200f)
//        font.draw(spriteBatch, "SURPRISED: ${browTimers[EMOTION.SURPRISED]}", 10f, 300f)
//        font.draw(spriteBatch, "SYMPATHY: ${browTimers[EMOTION.SYMPATHY]}", 10f, 400f)
//        font.draw(spriteBatch, "LEFT: ${browTimers[EMOTION.SLANTED_LEFT]}", 10f, 500f)
//        font.draw(spriteBatch, "RIGHT: ${browTimers[EMOTION.SLANTED_RIGHT]}", 10f, 600f)
//        font.draw(spriteBatch, "CONFUSED: ${browTimers[EMOTION.CONFUSED]}", 10f, 700f)
//        font.draw(spriteBatch, "ANGRY_CONFUSED: ${browTimers[EMOTION.ANGRY_CONFUSED]}", 210f, 100f)

        layout.setText(font, "${currentPhrase.text}")
        font.draw(spriteBatch, "${currentPhrase.text}", bot.x - layout.width / 2f, 550f)
        spriteBatch.end()

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit()
        }
    }

    fun update(deltaTime: Float) {
        when (state) {
            STATE.CONTROLS -> {
                updateControls(deltaTime)
            }
            STATE.NORMAL -> {
                updateNormal(deltaTime)
            }
        }
    }
}
