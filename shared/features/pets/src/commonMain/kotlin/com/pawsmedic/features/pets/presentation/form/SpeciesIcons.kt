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
internal enum class SpeciesIcon { DOG, CAT, RODENT, HAMSTER, BIRD, FISH, REPTIL, OTHER }

/** Mapea el nombre de la especie (viene de la BD) al icono correspondiente. */
internal fun speciesIconFor(name: String): SpeciesIcon {
    val n = name.lowercase().trim()
    return when {
        "canino" in n || "perro" in n || "dog" in n -> SpeciesIcon.DOG
        "felino" in n || "gato" in n || "cat" in n -> SpeciesIcon.CAT
        "roedor" in n || "conejo" in n || "peque" in n || "rodent" in n -> SpeciesIcon.RODENT
        "hamster" in n || "hámster" in n -> SpeciesIcon.HAMSTER
        n == "ave" || n == "aves" || "pájaro" in n || "pajaro" in n || "bird" in n -> SpeciesIcon.BIRD
        "pez" in n || "peces" in n || "fish" in n -> SpeciesIcon.FISH
        "reptil" in n || "reptiles" in n || "reptil" in n -> SpeciesIcon.REPTIL
        else -> SpeciesIcon.OTHER
    }
}

internal fun SpeciesIcon.vector(): ImageVector = when (this) {
    SpeciesIcon.DOG -> DogIcon
    SpeciesIcon.CAT -> CatIcon
    SpeciesIcon.RODENT -> RodentIcon
    SpeciesIcon.HAMSTER -> HamsterIcon
    SpeciesIcon.BIRD -> BirdIcon
    SpeciesIcon.FISH -> FishIcon
    SpeciesIcon.REPTIL -> ReptilIcon
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

private val HamsterIcon: ImageVector by lazy {
    val builder = ImageVector.Builder(
        name = "Hamster",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 512f,
        viewportHeight = 512f
    )
    builder.addPath(
        pathData = PathParser().parsePathString(
            """M501.402,256.835c-2.899-8.855-12.425-13.679-21.278-10.783l-22.611,7.402
c-2.537-13.514-6.207-27.196-10.939-40.814c38.858-15.047,65.428-52.611,65.428-95.734
c0-102.231-133.848-141.156-188.92-55.851c-42.269-20.808-88.704-22.307-134.165-0.01
C133.742-24.395,0,14.808,0,116.906c0,43.124,26.57,80.689,65.43,95.735
c-4.729,13.621-8.405,27.304-10.941,40.813l-22.613-7.403c-8.849-2.892-18.379,1.93-21.278,10.783
c-2.898,8.853,1.93,18.382,10.783,21.28l29.11,9.529c-0.984,22.212,1.244,44.653,8.87,65.832
l-37.411,10.8c-8.95,2.583-14.112,11.934-11.528,20.884c2.585,8.955,11.94,14.112,20.885,11.528
l43.879-12.667c29.499,41.8,80.002,59.535,130.597,65.773v31.091c0,9.316,7.552,16.868,16.868,16.868
h66.699c9.316,0,16.868-7.552,16.868-16.868v-31.092c50.57-6.234,101.089-23.958,130.597-65.772
l43.879,12.667c8.951,2.585,18.302-2.58,20.885-11.528c2.583-8.95-2.577-18.301-11.528-20.884
l-37.411-10.8c7.529-20.904,9.87-43.235,8.87-65.832l29.11-9.529
C499.473,275.215,504.3,265.688,501.402,256.835z
M478.264,116.906c0,29.174-18.087,54.572-44.479,64.525
c-18.837-39.64-47.173-77.188-81.902-102.783C389.403,22.158,478.264,48.777,478.264,116.906z
M33.736,116.906c0-68.249,88.916-94.587,126.357-38.294
c-34.931,25.692-63.198,63.451-81.883,102.816C51.822,171.475,33.736,146.077,33.736,116.906z
M256,370.876l-35.938-37.664h71.875L256,370.876z
M239.519,464.016V452.4c10.962,0.387,21.988,0.388,32.963,0v11.615H239.519z
M420.154,344.098l-41.628-12.017c-8.946-2.583-18.3,2.577-20.885,11.528
c-2.583,8.95,2.577,18.301,11.528,20.884l32.543,9.394c-30.699,33.326-83.727,42.974-128.845,44.738
v-16.559l51.383-53.853c17.465-18.304,4.473-48.738-20.86-48.738h-94.784
c-25.299,0-38.349,30.41-20.861,48.737l51.384,53.854v16.559
c-45.077-1.762-98.132-11.396-128.845-44.738l32.543-9.394c8.95-2.583,14.112-11.934,11.528-20.884
c-2.584-8.951-11.939-14.114-20.885-11.528l-41.628,12.017
c-5.113-13.263-7.706-28.376-7.84-45.483l48.898,16.008
c8.869,2.9,18.384-1.943,21.278-10.783c2.898-8.853-1.93-18.382-10.783-21.28
l-56.533-18.506c2.941-17.557,8.172-36.165,16.262-55.681
c82.111-182.786,238.191-161.598,305.56-0.418c8.141,19.477,13.485,38.393,16.45,56.097
l-56.539,18.509c-8.855,2.898-13.682,12.426-10.783,21.28
c2.898,8.849,12.418,13.681,21.278,10.783l48.898-16.008C427.86,315.721,425.267,330.835,420.154,344.098z
M192.717,223.134c0.011-12.324-9.97-22.323-22.294-22.334
c-12.324-0.011-22.323,9.97-22.334,22.294c-0.011,12.324,9.97,22.323,22.294,22.334
c12.323,0.011,22.323-9.97,22.334-22.294z
M363.035,223.281c0.011-12.324-9.97-22.323-22.294-22.334
c-12.324-0.011-22.323,9.97-22.334,22.294c-0.011,12.324,9.97,22.323,22.294,22.334
c12.324,0.011,22.323-9.97,22.334-22.294z""".trimIndent()
        ).toNodes(),
        fill = SolidColor(Color.Black)
    )
    builder.build()
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

private val ReptilIcon: ImageVector by lazy {
    lucide(
        "Turtle",
        "m12 10 2 4v3a1 1 0 0 0 1 1h2a1 1 0 0 0 1-1v-3a8 8 0 1 0-16 0v3a1 1 0 0 0 1 1h2a1 1 0 0 0 1-1v-3l2-4h4Z",
        "M4.82 7.9 8 10",
        "M15.18 7.9 12 10",
        "M16.93 10H20a2 2 0 0 1 0 4H2"
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
