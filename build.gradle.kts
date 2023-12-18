import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.compose") version "1.4.0"
}

group = "top.myrest"
version = "1.0.0"
val entry = "$name.jar"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    compileOnly(compose.desktop.currentOs)
    compileOnly("top.myrest:myflow-kit:1.0.0")
    implementation("com.belerweb:pinyin4j:2.5.1")
    implementation("com.github.houbb:opencc4j:1.8.1")
    testImplementation("top.myrest:myflow-baseimpl:1.0.0")
    testImplementation(kotlin("test"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.jar {
    archiveFileName.set(entry)
    from(
        configurations.runtimeClasspath.get().allDependencies.flatMap { dependency ->
            configurations.runtimeClasspath.get().files(dependency).map { file ->
                if (file.isDirectory) file else zipTree(file)
            }
        },
    )
}

tasks.build {
    doLast {
        copy {
            from("./build/libs/$entry")
            into(".")
        }
        val specFile = file("./plugin-spec.yml")
        val specContent = specFile.readLines(Charsets.UTF_8).joinToString(separator = System.lineSeparator()) {
            if (it.startsWith("version:")) {
                "version: $version"
            } else if (it.startsWith("entry:")) {
                "entry: ./$entry"
            } else it
        }
        specFile.writeText(specContent, Charsets.UTF_8)
        specFile.appendText(System.lineSeparator())
    }
}

tasks.register("packagePlugin") {
    group = "build"
    description = "Package as runflow plugin zip file."
    dependsOn("build")
    doLast {
        val zipFile = file("./runflow-plugin.zip")
        val zip = ZipOutputStream(FileOutputStream(zipFile))
        val files = mutableListOf(
            file(entry) to entry,
            file("plugin-spec.yml") to "plugin-spec.yml",
            file("description_en_us.md") to "description_en_us.md",
            file("description_zh_cn.md") to "description_zh_cn.md",
        )
        file("language").listFiles()?.forEach {
            files.add(it to "language/${it.name}")
        }
        file("logos").listFiles()?.forEach {
            files.add(it to "logos/${it.name}")
        }
        files.filter {
            it.first.exists()
        }.forEach {
            zip.putNextEntry(ZipEntry(it.second))
            zip.write(it.first.readBytes())
        }
        zip.closeEntry()
        zip.close()
    }
}
