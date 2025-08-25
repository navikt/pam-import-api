package no.nav.arbeidsplassen.importapi.provider

class ProviderCache {

    val simpleCache = mutableMapOf<Long, ProviderDTO>()

    fun invalidate(id: Long?) {
        if (id != null) {
            simpleCache.remove(id)
        }
    }

    fun get(id: Long): ProviderDTO? = simpleCache[id]

    fun set(id: Long, it: ProviderDTO) {
        simpleCache[id] = it
    }
}
