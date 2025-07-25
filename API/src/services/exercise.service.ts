import { db } from '@lib/db.ts';
import { exerciseType, exerciseResult } from '../db/schema/tables.ts';
import { eq, and, desc, gte, lte } from 'drizzle-orm';

export class ExerciseService {
    static async getAllExerciseTypes() {
        return await db.select().from(exerciseType);
    }

    static async getExerciseTypeById(id: number) {
        const exerciseTypes = await db
            .select()
            .from(exerciseType)
            .where(eq(exerciseType.id, id));
        return exerciseTypes[0] || null;
    }

    static async getExerciseTypeByName(name: string) {
        const exerciseTypes = await db
            .select()
            .from(exerciseType)
            .where(eq(exerciseType.name, name));
        return exerciseTypes[0] || null;
    }

    // Record exercise result directly without workout session
    static async recordExerciseResult(data: {
        workoutPlanDayExerciseId: number;
        userId: string;
        reps?: number;
        duration?: number;
        calories?: number;
    }) {
        const result = await db
            .insert(exerciseResult)
            .values({
                ...data,
                completedAt: new Date(),
            })
            .returning();
        return result[0];
    }

    // Get exercise results for a user
    static async getUserExerciseResults(userId: string) {
        return await db.query.exerciseResult.findMany({
            where: eq(exerciseResult.userId, userId),
            orderBy: desc(exerciseResult.completedAt),
            with: {
                workoutPlanExercise: {
                    with: {
                        exerciseType: true,
                        workoutPlanDay: true,
                    },
                },
            },
        });
    }

    // Get exercise results for a specific date
    static async getExerciseResultsForDate(userId: string, date: Date) {
        const startOfDay = new Date(date);
        startOfDay.setHours(0, 0, 0, 0);

        const endOfDay = new Date(date);
        endOfDay.setHours(23, 59, 59, 999);

        return await db.query.exerciseResult.findMany({
            where: and(
                eq(exerciseResult.userId, userId),
                gte(exerciseResult.completedAt, startOfDay),
                lte(exerciseResult.completedAt, endOfDay)
            ),
            with: {
                workoutPlanExercise: {
                    with: {
                        exerciseType: true,
                        workoutPlanDay: true,
                    },
                },
            },
        });
    }

    // Get exercise results for a specific workout plan day exercise
    static async getExerciseResultsByWorkoutPlanDayExercise(
        workoutPlanDayExerciseId: number,
        userId: string
    ) {
        return await db.query.exerciseResult.findMany({
            where: and(
                eq(
                    exerciseResult.workoutPlanDayExerciseId,
                    workoutPlanDayExerciseId
                ),
                eq(exerciseResult.userId, userId)
            ),
            orderBy: desc(exerciseResult.completedAt),
            with: {
                workoutPlanExercise: {
                    with: {
                        exerciseType: true,
                    },
                },
            },
        });
    }
}
