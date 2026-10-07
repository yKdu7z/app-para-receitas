plugins {
    alias(libs.plugins.android.application) apply false
    // O AGP 9 já compila Kotlin sozinho; declarar o plugin aqui só fixa a versão do Kotlin usada
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}
