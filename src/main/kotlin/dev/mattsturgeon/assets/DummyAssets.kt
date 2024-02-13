package dev.mattsturgeon.assets

import java.util.function.Supplier

class DummyAssets(files: Map<String, String>) :
    IndexedAssets(files.mapValues { Supplier { it.value.reader() } })