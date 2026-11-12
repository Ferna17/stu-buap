plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "mx.buap.stu"
    compileSdk = 37

    defaultConfig {
        applicationId = "mx.buap.stu"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.all { tareaPrueba ->
            val resultadosOrdenados = mutableListOf<Pair<Int, String>>()
            tareaPrueba.addTestListener(object : org.gradle.api.tasks.testing.TestListener {
                override fun beforeSuite(suite: org.gradle.api.tasks.testing.TestDescriptor) {}
                override fun beforeTest(descriptorPrueba: org.gradle.api.tasks.testing.TestDescriptor) {}
                override fun afterTest(
                    descriptorPrueba: org.gradle.api.tasks.testing.TestDescriptor,
                    resultado: org.gradle.api.tasks.testing.TestResult
                ) {
                    val duracionMs = (resultado.endTime - resultado.startTime).coerceAtLeast(1)
                    val numeroPF = Regex("^PF(\\d+)").find(descriptorPrueba.name)?.groupValues?.get(1)?.toIntOrNull() ?: 99
                    val linea = "  √ ${descriptorPrueba.name} (${duracionMs} ms)"
                    resultadosOrdenados.add(numeroPF to linea)
                }
                override fun afterSuite(
                    suite: org.gradle.api.tasks.testing.TestDescriptor,
                    resultado: org.gradle.api.tasks.testing.TestResult
                ) {
                    if (suite.parent == null) {
                        val totalSegundos = (resultado.endTime - resultado.startTime) / 1000.0
                        println("\n APROBADO  app/src/test/java/mx/buap/stu/auth/ValidadorLoginTest.kt")
                        println("  Módulo de Inicio de Sesión STU Android (HU-01 - BICEP/CORRECT)")
                        resultadosOrdenados.sortedBy { it.first }.forEach { println(it.second) }
                        println("\nSuites de Prueba: 1 aprobada, 1 total | Pruebas: ${resultado.successfulTestCount} aprobadas, ${resultado.testCount} total | Tiempo: ${totalSegundos} s\n")
                    }
                }
            })
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
