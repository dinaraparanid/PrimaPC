package com.dinaraparanid.prima.utils.localization

import com.dinaraparanid.prima.utils.Params

object Localization {
    val unknownTrack = when (Params.language) {
        Params.Language.EN -> English("Unknown Track")
        Params.Language.RU -> Russian("Неизвестный Трек")
        Params.Language.BE -> Belarusian("Невядомы Трэк")
        Params.Language.ZH -> Chinese("未知轨道")
    }

    val unknownArtist = when (Params.language) {
        Params.Language.EN -> English("Unknown Artist")
        Params.Language.RU -> Russian("Неизвестный Артист")
        Params.Language.BE -> Belarusian("Невядомы Артыст")
        Params.Language.ZH -> Chinese("未知艺术家")
    }

    val unknownAlbum = when (Params.language) {
        Params.Language.EN -> English("Unknown Album")
        Params.Language.RU -> Russian("Неизвестный Альбом")
        Params.Language.BE -> Belarusian("Невядомы Альбом")
        Params.Language.ZH -> Chinese("未知专辑")
    }

    val trackCover = when (Params.language) {
        Params.Language.EN -> English("Track's cover")
        Params.Language.RU -> Russian("Обложка трека")
        Params.Language.BE -> Belarusian("Вокладка трэка")
        Params.Language.ZH -> Chinese("轨道盖")
    }
}