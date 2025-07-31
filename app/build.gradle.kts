plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose) // Certifique-se que este plugin está definido no seu libs.versions.toml
}

android {
    namespace = "com.example.gymlog"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gymlog"
        minSdk = 33
        targetSdk = 35 // targetSdk geralmente acompanha compileSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    // Se o plugin kotlin.compose não estiver habilitando isso, você pode precisar de:
    // composeOptions {
    //     kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get() // ou a versão explícita
    // }
}

dependencies {

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.androidx.core.ktx) // Apenas uma vez, via catalog
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) // Importante: Gerencia versões do Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview) // Para previews
    implementation(libs.androidx.material3)          // Apenas uma vez, via catalog
    implementation(libs.androidx.navigation.compose)  // Adicione alias para navigation-compose no libs.versions.toml
    // Ex: no [libraries] do toml:
    // androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
    // E no [versions]:
    // navigationCompose = "2.7.3" (ou versão mais recente)


    // Outras dependências que você tinha e que podem ser mantidas se não forem cobertas pelo BOM
    // ou se você precisar de versões específicas fora do gerenciamento do BOM (menos comum para Compose).
    implementation ("androidx.compose.runtime:runtime-livedata:1.5.3") // Verifique se o BOM cobre isso ou adicione ao catalog
    implementation ("androidx.core:core-splashscreen:1.0.1")           // Adicione ao catalog
    implementation("androidx.compose.material:material-icons-extended-android:1.6.7") // Adicione ao catalog (esta é Material 2 icons, mas ok)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom)) // BOM para testes também
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling) // Para o Inspetor de Layout, etc.
    debugImplementation(libs.androidx.ui.test.manifest)
}