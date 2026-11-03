package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        StudyMaterialEntity::class, 
        SubjectEntity::class, 
        UserProfileEntity::class,
        QuizEntity::class,
        QuizQuestionEntity::class,
        FlashcardEntity::class,
        FlashcardSetEntity::class,
        StudyActivityEntity::class,
        StudyGoalEntity::class
    ], 
    version = 11, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studyMaterialDao(): StudyMaterialDao
    abstract fun subjectDao(): SubjectDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun quizDao(): QuizDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun studyActivityDao(): StudyActivityDao
    abstract fun studyGoalDao(): StudyGoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `flashcard_sets` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `userId` TEXT NOT NULL, 
                        `subjectId` INTEGER NOT NULL, 
                        `materialId` INTEGER NOT NULL, 
                        `totalCards` INTEGER NOT NULL, 
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("ALTER TABLE `flashcards` ADD COLUMN `setId` INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `study_activities` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `userId` TEXT NOT NULL, 
                        `activityType` TEXT NOT NULL,
                        `subjectId` INTEGER, 
                        `materialId` INTEGER, 
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `user_profiles` ADD COLUMN `profilePictureUri` TEXT")
                db.execSQL("ALTER TABLE `user_profiles` ADD COLUMN `studyGoal` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `user_profiles` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
            }
        }

        private fun migrateQuizTables(db: SupportSQLiteDatabase) {
            // Recreate quizzes table to match exact Room expected schema
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `quizzes_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `userId` TEXT NOT NULL,
                    `subjectId` INTEGER,
                    `subjectName` TEXT NOT NULL,
                    `materialId` INTEGER,
                    `materialTitle` TEXT NOT NULL,
                    `quizTitle` TEXT NOT NULL,
                    `difficulty` TEXT NOT NULL,
                    `questionType` TEXT NOT NULL,
                    `totalQuestions` INTEGER NOT NULL,
                    `score` INTEGER NOT NULL,
                    `percentage` REAL NOT NULL,
                    `correctCount` INTEGER NOT NULL,
                    `incorrectCount` INTEGER NOT NULL,
                    `createdAt` INTEGER NOT NULL
                )
            """.trimIndent())

            try {
                db.execSQL("""
                    INSERT INTO `quizzes_new` (`id`, `userId`, `subjectId`, `subjectName`, `materialId`, `materialTitle`, `quizTitle`, `difficulty`, `questionType`, `totalQuestions`, `score`, `percentage`, `correctCount`, `incorrectCount`, `createdAt`)
                    SELECT 
                        `id`, 
                        `userId`, 
                        NULL, 
                        '', 
                        `materialId`, 
                        '', 
                        'AI Quiz', 
                        'Medium', 
                        'Multiple Choice', 
                        `totalQuestions`, 
                        `score`, 
                        CASE WHEN `totalQuestions` > 0 THEN (`score` * 100.0 / `totalQuestions`) ELSE 0.0 END, 
                        `score`, 
                        MAX(0, `totalQuestions` - `score`), 
                        `createdAt`
                    FROM `quizzes`
                """.trimIndent())
            } catch (e: Exception) {
                // If source columns differ, handle gracefully
            }

            db.execSQL("DROP TABLE IF EXISTS `quizzes`")
            db.execSQL("ALTER TABLE `quizzes_new` RENAME TO `quizzes`")

            // Recreate quiz_questions table to match exact Room expected schema
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `quiz_questions_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `quizId` INTEGER NOT NULL,
                    `question` TEXT NOT NULL,
                    `questionType` TEXT NOT NULL,
                    `optionA` TEXT NOT NULL,
                    `optionB` TEXT NOT NULL,
                    `optionC` TEXT NOT NULL,
                    `optionD` TEXT NOT NULL,
                    `correctAnswer` TEXT NOT NULL,
                    `explanation` TEXT NOT NULL,
                    `studentAnswer` TEXT,
                    `isCorrect` INTEGER NOT NULL
                )
            """.trimIndent())

            try {
                db.execSQL("""
                    INSERT INTO `quiz_questions_new` (`id`, `quizId`, `question`, `questionType`, `optionA`, `optionB`, `optionC`, `optionD`, `correctAnswer`, `explanation`, `studentAnswer`, `isCorrect`)
                    SELECT 
                        `id`, 
                        `quizId`, 
                        `question`, 
                        'MCQ', 
                        `optionA`, 
                        `optionB`, 
                        `optionC`, 
                        `optionD`, 
                        `correctAnswer`, 
                        `explanation`, 
                        NULL, 
                        0
                    FROM `quiz_questions`
                """.trimIndent())
            } catch (e: Exception) {
                // If source columns differ, handle gracefully
            }

            db.execSQL("DROP TABLE IF EXISTS `quiz_questions`")
            db.execSQL("ALTER TABLE `quiz_questions_new` RENAME TO `quiz_questions`")
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateQuizTables(db)
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateQuizTables(db)
            }
        }

        private val MIGRATION_8_10 = object : Migration(8, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateQuizTables(db)
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `study_goals` (
                        `userId` TEXT NOT NULL,
                        `period` TEXT NOT NULL,
                        `dailyStudyTimeMinutes` INTEGER NOT NULL,
                        `dailyQuizAccuracy` INTEGER NOT NULL,
                        `weeklyStudyTimeMinutes` INTEGER NOT NULL,
                        `weeklyQuizAccuracy` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`userId`)
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_assist_database"
                )
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_8_10, MIGRATION_10_11)
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
