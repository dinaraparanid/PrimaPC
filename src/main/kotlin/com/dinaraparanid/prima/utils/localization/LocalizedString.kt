package com.dinaraparanid.prima.utils.localization

sealed class LocalizedString(val resource: String)
class English(resource: String) : LocalizedString(resource)
class Russian(resource: String) : LocalizedString(resource)
class Belarusian(resource: String) : LocalizedString(resource)
class Chinese(resource: String) : LocalizedString(resource)
