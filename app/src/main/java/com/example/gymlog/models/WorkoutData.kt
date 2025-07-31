package com.example.gymlog.models

import com.example.gymlog.R
import java.util.Date
import java.util.UUID // Import UUID

// --- Existing Models (Keep as is or adapt if needed) ---
data class ProfileData(
    val id: Int,
    val name: String,
    val height: Double,
    val weight: Double,
    val profilePicture: Int
)

val profileData = ProfileData(
    id = 1,
    name = "Julia Oliveira",
    height = 170.0,
    weight = 75.0,
    profilePicture = R.drawable.profile
)

data class Exercise(
    val id: Int, // Added ID for easier reference
    val name: String,
    val description: String,
    val sets: Int, // Target sets (for routines)
    val reps: Int, // Target reps (for routines)
    val weight: Double, // Target weight (for routines)
    val exercisePicture: Int
)

val exerciseList = listOf(
    Exercise(
        id = 1,
        name = "Supino Reto",
        description = "Exercício de supino reto com barra.",
        sets = 3,
        reps = 12,
        weight = 100.0,
        exercisePicture = R.drawable.supino
    ),
    Exercise(
        id = 2,
        name = "Rosca Direta",
        description = "Exercício de rosca direta com barra.",
        sets = 3,
        reps = 12,
        weight = 20.0,
        exercisePicture = R.drawable.rosca_direta
    ),
    Exercise(
        id = 3,
        name = "Agachamento",
        description = "Exercício de agachamento com barra.",
        sets = 3,
        reps = 12,
        weight = 50.0,
        exercisePicture = R.drawable.agachamento
    ),
    Exercise(
        id = 4,
        name = "Barra fixa",
        description = "Exercício de costas com barra fixa.",
        sets = 3,
        reps = 12,
        weight = profileData.weight,
        // Corrected: Replaced R.drawable.pull_up (assuming it's a GIF) with a static placeholder
        exercisePicture = R.drawable.supino // Placeholder: Replace with a static pull-up image (PNG/JPG)
    ),
    Exercise(
        id = 5,
        name = "Levantamento terra",
        description = "Exercício de levantamento terra com barra.",
        sets = 3,
        reps = 12,
        weight = 100.0,
        exercisePicture = R.drawable.deadlift
    ),
    Exercise(
        id = 6,
        name = "Tríceps Corda",
        description = "Exercício de tríceps com corda na polia.",
        sets = 3,
        reps = 15,
        weight = 25.0,
        exercisePicture = R.drawable.rosca_direta // Placeholder image
    ),
    Exercise(
        id = 7,
        name = "Rosca Martelo",
        description = "Exercício de bíceps com halteres.",
        sets = 3,
        reps = 12,
        weight = 15.0,
        exercisePicture = R.drawable.rosca_direta // Placeholder image
    ),
    Exercise(
        id = 8,
        name = "Leg Press",
        description = "Exercício de pernas no aparelho leg press.",
        sets = 4,
        reps = 10,
        weight = 200.0,
        exercisePicture = R.drawable.agachamento // Placeholder image
    ),
    Exercise(
        id = 9,
        name = "Remada Curvada",
        description = "Exercício de costas com barra.",
        sets = 3,
        reps = 12,
        weight = 60.0,
        // Corrected: Replaced R.drawable.pull_up (assuming it's a GIF) with a static placeholder
        exercisePicture = R.drawable.supino // Placeholder: Replace with a static rowing image (PNG/JPG)
    ),
    Exercise(
        id = 10,
        name = "Puxada Frontal",
        description = "Exercício de costas na polia alta.",
        sets = 3,
        reps = 12,
        weight = 70.0,
        // Corrected: Replaced R.drawable.pull_up (assuming it's a GIF) with a static placeholder
        exercisePicture = R.drawable.supino // Placeholder: Replace with a static pull-down image (PNG/JPG)
    ),
    Exercise(
        id = 11,
        name = "Burpees",
        description = "Exercício funcional de corpo inteiro.",
        sets = 5,
        reps = 20,
        weight = 0.0,
        exercisePicture = R.drawable.deadlift // Placeholder image
    ),
    Exercise(
        id = 12,
        name = "Mountain Climbers",
        description = "Exercício cardiovascular.",
        sets = 5,
        reps = 30,
        weight = 0.0,
        exercisePicture = R.drawable.deadlift // Placeholder image
    )
)

/**
 * Modelo de dados para os treinos (rotinas pré-definidas)
 */
data class WorkoutRoutine(
    val id: Int,
    val name: String,
    val description: String,
    val duration: Int, // Estimated duration in minutes
    val difficulty: String,
    val category: String,
    val image: Int,
    val exercises: List<Exercise>, // List of exercises in the routine
    val videoUrl: String? = null,
    val audioUrl: String? = null,
    val isFavorite: Boolean = false,
    val rating: Float = 0f,
    val caloriesBurned: Int = 0 // Estimated calories
)

val mockWorkoutRoutines = listOf(
    WorkoutRoutine(
        id = 1,
        name = "Treino Full Body",
        description = "Um treino completo que trabalha todos os principais grupos musculares em uma única sessão. Ideal para quem tem pouco tempo disponível e quer maximizar resultados.",
        duration = 60,
        difficulty = "Intermediário",
        category = "Força",
        image = R.drawable.supino,
        exercises = listOf(exerciseList[0], exerciseList[2], exerciseList[3], exerciseList[4]),
        videoUrl = "https://example.com/videos/fullbody.mp4",
        caloriesBurned = 450
    ),
    WorkoutRoutine(
        id = 2,
        name = "Treino de Braços",
        description = "Foco intenso em bíceps, tríceps e antebraços para desenvolver força e definição nos membros superiores.",
        duration = 45,
        difficulty = "Iniciante",
        category = "Força",
        image = R.drawable.rosca_direta,
        exercises = listOf(exerciseList[1], exerciseList[5], exerciseList[6]),
        audioUrl = "https://example.com/audio/arms_guidance.mp3",
        caloriesBurned = 300
    ),
    WorkoutRoutine(
        id = 3,
        name = "Treino de Pernas",
        description = "Treino focado em quadríceps, posteriores, glúteos e panturrilhas para desenvolver força e potência nos membros inferiores.",
        duration = 50,
        difficulty = "Avançado",
        category = "Força",
        image = R.drawable.agachamento,
        exercises = listOf(exerciseList[2], exerciseList[4], exerciseList[7]),
        videoUrl = "https://example.com/videos/legs.mp4",
        isFavorite = true,
        caloriesBurned = 500
    ),
    WorkoutRoutine(
        id = 4,
        name = "Treino de Costas",
        description = "Foco em desenvolver os músculos das costas, incluindo latíssimo do dorso, trapézio e romboides.",
        duration = 40,
        difficulty = "Intermediário",
        category = "Força",
        // Corrected: Replaced R.drawable.pull_up (assuming it's a GIF) with a static placeholder
        image = R.drawable.supino, // Placeholder: Replace with a static back workout image (PNG/JPG)
        exercises = listOf(exerciseList[3], exerciseList[8], exerciseList[9]),
        caloriesBurned = 380
    ),
    WorkoutRoutine(
        id = 5,
        name = "Treino HIIT",
        description = "Treino intervalado de alta intensidade para queima de gordura e condicionamento cardiovascular.",
        duration = 30,
        difficulty = "Avançado",
        category = "Cardio",
        image = R.drawable.deadlift,
        exercises = listOf(exerciseList[10], exerciseList[11]),
        videoUrl = "https://example.com/videos/hiit.mp4",
        isFavorite = true,
        caloriesBurned = 400
    )
)

/**
 * Perguntas frequentes para a tela de Ajuda
 */
data class FAQ(
    val id: Int,
    val question: String,
    val answer: String
)

val faqList = listOf(
    FAQ(1, "Como registrar um novo treino?", "Na tela 'Log', clique no botão '+' e selecione uma rotina ou crie um treino personalizado."),
    FAQ(2, "Como marcar uma rotina como favorita?", "Na tela de detalhes da rotina (acessível pela tela inicial), clique no ícone de estrela."),
    FAQ(3, "Como usar o timer de descanso?", "Durante o registro de um treino, após completar uma série, clique no ícone de cronômetro para iniciar o descanso."),
    FAQ(4, "Como editar ou excluir um treino do histórico?", "Na tela 'Log', encontre o registro desejado e use os ícones de lápis (editar) ou lixeira (excluir)."),
    FAQ(5, "Como criar uma rotina personalizada?", "Atualmente, você pode registrar um treino personalizado na hora. A funcionalidade de salvar rotinas personalizadas será adicionada em breve.")
)

// --- New Models for Workout Logging ---

/**
 * Represents a single set performed during a workout log.
 */
data class PerformedSet(
    val id: String = UUID.randomUUID().toString(), // Unique ID for the set
    var reps: Int,
    var weight: Double,
    var isCompleted: Boolean = false
)

/**
 * Represents an exercise performed during a workout log, including its sets.
 */
data class PerformedExercise(
    val id: String = UUID.randomUUID().toString(), // Unique ID for this instance
    val exerciseId: Int, // Reference to the base Exercise
    val exerciseName: String,
    val sets: MutableList<PerformedSet> = mutableListOf(),
    val targetSets: Int, // From the original routine/exercise
    val targetReps: Int,
    val targetWeight: Double
)

/**
 * Represents a completed workout session saved in the log.
 */
data class WorkoutLogEntry(
    val id: String = UUID.randomUUID().toString(), // Unique ID for the log entry
    val routineId: Int? = null, // ID of the WorkoutRoutine if based on one
    val workoutName: String, // Name of the routine or "Treino Personalizado"
    val startTime: Date,
    var endTime: Date? = null,
    var durationMillis: Long = 0,
    val performedExercises: MutableList<PerformedExercise> = mutableListOf(),
    var notes: String? = null,
    var caloriesBurned: Int? = null // Can be calculated later
)
