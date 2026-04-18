package com.example.rooming.quality

import com.lemonappdev.konsist.api.Konsist
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchitectureKonsistTest {
    private val rootDir: Path = generateSequence(Path.of("").toAbsolutePath()) { current ->
        current.parent
    }.first { candidate ->
        Files.exists(candidate.resolve("settings.gradle.kts"))
    }

    @Test
    fun `use cases should live in domain usecase module`() {
        val useCases = Konsist.scopeFromDirectory("domain/usecase/src/main/kotlin")
            .classes()
            .filter { declaration -> declaration.hasNameEndingWith("UseCase") }

        assertTrue(
            "All use cases must reside in domain.usecase package",
            useCases.all { declaration ->
                declaration.packagee?.name == "com.example.rooming.domain.usecase"
            },
        )
    }

    @Test
    fun `repository interfaces should live in domain and implementations in data`() {
        val repositoryInterfaces = Konsist.scopeFromDirectory("domain/repository/src/main/kotlin")
            .interfaces()
            .filter { declaration -> declaration.hasNameEndingWith("Repository") }
        val repositoryImplementations = Konsist.scopeFromDirectories(
            listOf(
                "data/rooms/src/main/kotlin",
                "data/favorites/src/main/kotlin",
                "data/bookings/src/main/kotlin",
            ),
        )
            .classes()
            .filter { declaration -> declaration.hasNameEndingWith("Repository") }

        assertTrue(
            "Repository interfaces must reside in domain.repository package",
            repositoryInterfaces.all { declaration ->
                declaration.packagee?.name == "com.example.rooming.domain.repository"
            },
        )
        assertTrue(
            "Repository implementations must reside in data packages",
            repositoryImplementations.all { declaration ->
                declaration.packagee?.name?.startsWith("com.example.rooming.data.") == true
            },
        )
    }

    @Test
    fun `domain should not depend on Android framework`() {
        assertNoForbiddenImports(
            sourceRoot = rootDir.resolve("domain"),
            forbiddenPrefixes = listOf("android.", "androidx."),
        )
    }

    @Test
    fun `data should not depend on ui packages`() {
        assertNoForbiddenImports(
            sourceRoot = rootDir.resolve("data"),
            forbiddenPrefixes = listOf(
                "androidx.compose.",
                "com.example.rooming.feature.",
                "com.example.rooming.core.ui",
            ),
        )
    }

    @Test
    fun `feature impl modules may depend on other features only through api`() {
        val implFiles = Files.walk(rootDir.resolve("feature"))
            .filter { path -> path.toString().endsWith(".kt") && path.toString().contains("/impl/") }
            .toList()

        implFiles.forEach { file ->
            val currentFeature = file.toString().substringAfter("/feature/").substringBefore('/')
            val forbiddenImport = Files.readAllLines(file)
                .firstOrNull { line ->
                    line.startsWith("import com.example.rooming.feature.") &&
                        ".impl." in line &&
                        !line.contains(".feature.$currentFeature.impl.")
                }

            assertTrue(
                "Feature impl module should not import another feature impl directly: ${file.name} -> $forbiddenImport",
                forbiddenImport == null,
            )
        }
    }

    private fun assertNoForbiddenImports(
        sourceRoot: Path,
        forbiddenPrefixes: List<String>,
    ) {
        val kotlinFiles = Files.walk(sourceRoot)
            .filter { path -> path.toString().endsWith(".kt") }
            .toList()

        kotlinFiles.forEach { file ->
            val invalidImport = Files.readAllLines(file)
                .firstOrNull { line ->
                    line.startsWith("import ") &&
                        forbiddenPrefixes.any { prefix -> line.contains(prefix) }
                }

            assertTrue(
                "Forbidden import found in $file: $invalidImport",
                invalidImport == null,
            )
        }
    }
}
