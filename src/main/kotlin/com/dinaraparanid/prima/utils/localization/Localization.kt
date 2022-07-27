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

    @JvmField
    val byTitle = resource(
        en = "By title",
        ru = "По названию",
        be = "Па назве",
        zh = "按标题分类"
    )

    @JvmField
    val byArtist = resource(
        en = "By artist",
        ru = "По артисту",
        be = "Па артысту",
        zh = "艺术家"
    )

    @JvmField
    val byAlbum = resource(
        en = "By album",
        ru = "По альбому",
        be = "Па альбоме",
        zh = "按专辑分类"
    )

    @JvmField
    val byDate = resource(
        en = "By date",
        ru = "По дате",
        be = "Па даце",
        zh = "按日期计算"
    )

    @JvmField
    val byNumberInAlbum = resource(
        en = "By № in album",
        ru = "По № в альбоме",
        be = "Па № ў альбоме",
        zh = "在专辑中"
    )

    @JvmField
    val artists = resource(
        en = "Artists",
        ru = "Артисты",
        be = "Артысты",
        zh = "艺术家"
    )

    @JvmField
    val trackCollections = resource(
        en = "Track Collections",
        ru = "Сборники Треков",
        be = "Зборнікі Трэкаў",
        zh = "曲目集合"
    )

    @JvmField
    val favourites = resource(
        en = "Favourites",
        ru = "Любимое",
        be = "Любімы",
        zh = "最喜欢的"
    )

    @JvmField
    val mp3Converter = resource(
        en = "MP3 Converter",
        ru = "MP3 Конвертер",
        be = "MP3 Канвэртар",
        zh = "MP3转换器"
    )

    @JvmField
    val gtm = resource(
        en = "Guess The Melody",
        ru = "Угадай Мелодию",
        be = "Угадай Мелодыю",
        zh = "猜旋律"
    )

    @JvmField
    val statistics = resource(
        en = "Statistics",
        ru = "Статистика",
        be = "Статыстыка",
        zh = "统计数字"
    )

    @JvmField
    val settings = resource(
        en = "Settings",
        ru = "Настройки",
        be = "Наладжваньне",
        zh = "参数"
    )

    @JvmField
    val aboutApp = resource(
        en = "About App",
        ru = "О Приложении",
        be = "Аб Дадатку",
        zh = "关于应用程序"
    )

    @JvmField
    val todo = resource(
        en = "Not yet implemented",
        ru = "Еще не реализовано",
        be = "Яшчэ не рэалізавана",
        zh = "尚未实施"
    )

    @JvmField
    val ascending = resource(
        en = "ascending",
        ru = "по возрастанию",
        be = "па ўзрастанні",
        zh = "上升"
    )

    @JvmField
    val descending = resource(
        en = "descending",
        ru = "по убыванию",
        be = "па змяншэнні",
        zh = "降序排列"
    )

    @JvmField
    val trackOrder = resource(
        en = "Tracks' order",
        ru = "Порядок треков",
        be = "Парадак трэкаў",
        zh = "追踪订单"
    )

    @JvmField
    val changeTrackInfo = resource(
        en = "Change track's information",
        ru = "Изменить информацию о треке",
        be = "Змяніць інфармацыю аб трэку",
        zh = "更改轨道信息"
    )

    @JvmField
    val addToQueue = resource(
        en = "Add to queue (or remove)",
        ru = "Добавить в очередь (или убрать)",
        be = "Дадаць у чаргу (або прыбраць)",
        zh = "添加到队列（或删除）"
    )

    @JvmField
    val addToFavourites = resource(
        en = "Add to favourites (or remove)",
        ru = "Добавить в любимое (или убрать)",
        be = "Дадаць у любімае (або прыбраць)",
        zh = "添加到收藏夹（或删除）"
    )

    @JvmField
    val removeTrack = resource(
        en = "Remove track",
        ru = "Удалить трек",
        be = "Выдаліць трэк",
        zh = "删除曲目"
    )

    @JvmField
    val lyrics = resource(
        en = "Lyrics",
        ru = "Текст трека",
        be = "Тэкст трэка",
        zh = "歌词"
    )

    @JvmField
    val trackInformation = resource(
        en = "Track's information",
        ru = "Информация о треке",
        be = "Інфармацыя аб трэку",
        zh = "追踪资料"
    )

    @JvmField
    val trimTrack = resource(
        en = "Trim track",
        ru = "Обрезать трек",
        be = "Абрэзаць трэк",
        zh = "裁剪轨道"
    )

    @JvmField
    val hideTrack = resource(
        en = "Hide track",
        ru = "Спрятать трек",
        be = "Схаваць трэк",
        zh = "隐藏轨道"
    )

    @JvmField
    val currentPlaylist = resource(
        en = "Current Playlist",
        ru = "Текущий плейлист",
        be = "Бягучы плэйліст",
        zh = "当前播放列表"
    )
}