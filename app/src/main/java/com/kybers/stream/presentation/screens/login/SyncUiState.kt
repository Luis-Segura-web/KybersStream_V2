package com.kybers.stream.presentation.screens.login

data class SyncUiState(
    val isVisible: Boolean = false,
    val isLoading: Boolean = false,
    val currentStep: SyncStep = SyncStep.STARTING,
    val progress: Float = 0f,
    val errorMessage: String? = null,
    val totalSteps: Int = 4
) {
    val progressText: String
        get() = when (currentStep) {
            SyncStep.STARTING -> "Iniciando sincronización..."
            SyncStep.CATEGORIES -> "Cargando categorías..."
            SyncStep.CHANNELS -> "Cargando canales..."
            SyncStep.MOVIES -> "Cargando películas..."
            SyncStep.SERIES -> "Cargando series..."
            SyncStep.FINISHING -> "Finalizando..."
            SyncStep.COMPLETED -> "Completado"
            SyncStep.ERROR -> "Error en sincronización"
        }
}

enum class SyncStep(val stepNumber: Int) {
    STARTING(0),
    CATEGORIES(1),
    CHANNELS(2),
    MOVIES(3),
    SERIES(4),
    FINISHING(5),
    COMPLETED(6),
    ERROR(-1)
}
