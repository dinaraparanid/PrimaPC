package com.dinaraparanid.prima.utils.localization

import com.dinaraparanid.prima.utils.Params

object Localization {

    @Suppress("NOTHING_TO_INLINE")
    private inline fun resource(en: String, ru: String, be: String, zh: String) = when (Params.language) {
        Params.Language.EN -> English(en)
        Params.Language.RU -> Russian(ru)
        Params.Language.BE -> Belarusian(be)
        Params.Language.ZH -> Chinese(zh)
    }

    @JvmField
    val unknownTrack = resource(
        en = "Unknown Track",
        ru = "Неизвестный Трек",
        be = "Невядомы Трэк",
        zh = "未知轨道"
    )

    @JvmField
    val unknownArtist = resource(
        en = "Unknown Artist",
        ru = "Неизвестный Артист",
        be = "Невядомы Артыст",
        zh = "未知艺术家"
    )

    @JvmField
    val unknownAlbum = resource(
        en = "Unknown Album",
        ru = "Неизвестный Альбом",
        be = "Невядомы Альбом",
        zh = "未知专辑"
    )

    @JvmField
    val trackCover = resource(
        en = "Track's cover",
        ru = "Обложка трека",
        be = "Вокладка трэка",
        zh = "轨道盖"
    )

    @JvmField
    val tracks = resource(
        en = "Tracks",
        ru = "Треки",
        be = "Трэкі",
        zh = "音乐曲目"
    )

    @JvmField
    val search = resource(
        en = "Search",
        ru = "Поиск",
        be = "Пошук",
        zh = "搜索"
    )

    @JvmField
    val cancel = resource(
        en = "Cancel",
        ru = "Отмена",
        be = "Адмена",
        zh = "取消"
    )
}