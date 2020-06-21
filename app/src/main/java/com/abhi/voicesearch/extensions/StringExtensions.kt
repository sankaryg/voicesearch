package com.abhi.voicesearch.extensions

import java.text.Normalizer

internal fun String.normalizeString() =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .toLowerCase()
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")