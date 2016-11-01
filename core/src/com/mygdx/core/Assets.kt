package com.mygdx.core

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont

val assets = AssetManager()

fun AssetManager.getTexture(path: String): Texture {
    if (!assets.isLoaded(path)) {
        assets.load(path, Texture::class.java)
        assets.finishLoading()
    }
    return assets.get(path)
}

fun AssetManager.getFont(path: String): BitmapFont {
    if (!assets.isLoaded(path)) {
        assets.load(path, BitmapFont::class.java)
        assets.finishLoading()
    }
    return assets.get(path)
}

fun AssetManager.getSound(path: String): Sound {
    if (!assets.isLoaded(path)) {
        assets.load(path, Sound::class.java)
        assets.finishLoading()
    }
    return assets.get(path)
}
