package com.pawsmedic.features.pets.presentation.form

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Iconos de Lucide (https://lucide.dev) convertidos a ImageVector.
 * Son de solo trazo (stroke 2, round caps/joins, viewBox 24x24), así que se tiñen con `Icon(tint = ...)`.
 */
internal enum class SpeciesIcon { DOG, CAT, RODENT, BIRD, FISH, OTHER }

/** Mapea el nombre de la especie (viene de la BD) al icono correspondiente. */
internal fun speciesIconFor(name: String): SpeciesIcon {
    val n = name.lowercase().trim()
    return when {
        "canin" in n || "perro" in n || "dog" in n -> SpeciesIcon.DOG
        "felin" in n || "gato" in n || "cat" in n -> SpeciesIcon.CAT
        "roedor" in n || "conejo" in n || "peque" in n || "rodent" in n -> SpeciesIcon.RODENT
        n == "ave" || n == "aves" || "pájaro" in n || "pajaro" in n || "bird" in n -> SpeciesIcon.BIRD
        "pez" in n || "peces" in n || "fish" in n -> SpeciesIcon.FISH
        else -> SpeciesIcon.OTHER
    }
}

internal fun SpeciesIcon.vector(): ImageVector = when (this) {
    SpeciesIcon.DOG -> DogIcon
    SpeciesIcon.CAT -> CatIcon
    SpeciesIcon.RODENT -> RodentIcon
    SpeciesIcon.BIRD -> BirdIcon
    SpeciesIcon.FISH -> FishIcon
    SpeciesIcon.OTHER -> PawPrintIcon
}

// Círculo <circle cx cy r> expresado como path
private fun circle(cx: Float, cy: Float, r: Float): String =
    "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0"

private fun lucide(name: String, vararg paths: String): ImageVector {
    val builder = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    )
    paths.forEach { d ->
        builder.addPath(
            pathData = PathParser().parsePathString(d).toNodes(),
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }
    return builder.build()
}

private val DogIcon: ImageVector by lazy {
    lucide(
        "Dog",
        "M11.25 16.25h1.5L12 17z",
        "M16 14v.5",
        "M4.42 11.247A13.152 13.152 0 0 0 4 14.556C4 18.728 7.582 21 12 21s8-2.272 8-6.444a11.702 11.702 0 0 0-.493-3.309",
        "M8 14v.5",
        "M8.5 8.5c-.384 1.05-1.083 2.028-2.344 2.5-1.931.722-3.576-.297-3.656-1-.113-.994 1.177-6.53 4-7 1.923-.321 3.651.845 3.651 2.235A7.497 7.497 0 0 1 14 5.277c0-1.39 1.844-2.598 3.767-2.277 2.823.47 4.113 6.006 4 7-.08.703-1.725 1.722-3.656 1-1.261-.472-1.855-1.45-2.239-2.5"
    )
}

private val CatIcon: ImageVector by lazy {
    lucide(
        "Cat",
        "M12 5c.67 0 1.35.09 2 .26 1.78-2 5.03-2.84 6.42-2.26 1.4.58-.42 7-.42 7 .57 1.07 1 2.24 1 3.44C21 17.9 16.97 21 12 21s-9-3-9-7.56c0-1.25.5-2.4 1-3.44 0 0-1.89-6.42-.5-7 1.39-.58 4.72.23 6.5 2.23A9.04 9.04 0 0 1 12 5Z",
        "M8 14v.5",
        "M16 14v.5",
        "M11.25 16.25h1.5L12 17l-.75-.75Z"
    )
}

// Lucide "rabbit" → roedores / animales pequeños
private val RodentIcon: ImageVector by lazy {
    lucide(
        "Rabbit",
        "M13 16a3 3 0 0 1 2.24 5",
        "M18 12h.01",
        "M18 21h-8a4 4 0 0 1-4-4 7 7 0 0 1 7-7h.2L9.6 6.4a1 1 0 1 1 2.8-2.8L15.8 7h.2c3.3 0 6 2.7 6 6v1a2 2 0 0 1-2 2h-1a3 3 0 0 0-3 3",
        "M20 8.54V4a2 2 0 1 0-4 0v3",
        "M7.612 12.524a3 3 0 1 0-1.6 4.3"
    )
}

private val BirdIcon: ImageVector by lazy {
    lucide(
        "Bird",
        "M16 7h.01",
        "M3.4 18H12a8 8 0 0 0 8-8V7a4 4 0 0 0-7.28-2.3L2 20",
        "m20 7 2 .5-2 .5",
        "M10 18v3",
        "M14 17.75V21",
        "M7 18a6 6 0 0 0 3.84-10.61"
    )
}

private val FishIcon: ImageVector by lazy {
    lucide(
        "Fish",
        "M6.5 12c.94-3.46 4.94-6 8.5-6 3.56 0 6.06 2.54 7 6-.94 3.47-3.44 6-7 6s-7.56-2.53-8.5-6Z",
        "M18 12v.5",
        "M16 17.93a9.77 9.77 0 0 1 0-11.86",
        "M7 10.67C7 8 5.58 5.97 2.73 5.5c-1 1.5-1 5 .23 6.5-1.24 1.5-1.24 5-.23 6.5C5.58 18.03 7 16 7 13.33",
        "M10.46 7.26C10.2 5.88 9.17 4.24 8 3h5.8a2 2 0 0 1 1.98 1.67l.23 1.4",
        "m16.01 17.93-.23 1.4A2 2 0 0 1 13.8 21H9.5a5.96 5.96 0 0 0 1.49-3.98"
    )
}

private val PawPrintIcon: ImageVector by lazy {
    lucide(
        "PawPrint",
        circle(11f, 4f, 2f),
        circle(18f, 8f, 2f),
        circle(20f, 16f, 2f),
        "M9 10a5 5 0 0 1 5 5v3.5a3.5 3.5 0 0 1-6.84 1.045Q6.52 17.48 4.46 16.84A3.5 3.5 0 0 1 5.5 10Z"
    )
}
