package com.dinaraparanid.prima.utils.localization

sealed class LocalizedString(val resource: String) : CharSequence {
    override val length = resource.length
    override fun get(index: Int) = resource[index]
    override fun subSequence(startIndex: Int, endIndex: Int) = resource.subSequence(startIndex, endIndex)
}

class English(resource: String) : LocalizedString(resource)
class Russian(resource: String) : LocalizedString(resource)
class Belarusian(resource: String) : LocalizedString(resource)
class Chinese(resource: String) : LocalizedString(resource)
