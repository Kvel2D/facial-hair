package com.mygdx.core

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx

class Main : ApplicationAdapter() {
    lateinit var game: Game

    override fun create() {
        game = Game()
    }

    override fun render() {
        game.update(Gdx.graphics.deltaTime)
    }

    override fun dispose() {
        game.dispose()
        assets.dispose()
    }
}
