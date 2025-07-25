// This service has been deprecated after removing workoutSession table
// The functionality has been moved to ExerciseService for direct exercise result recording

/*
import { db } from '@lib/db.ts';
import {
    workoutSession,
    exerciseResult,
    userWorkoutPlan,
} from '../db/schema/tables.ts';
import { eq, and, desc, gte, lte } from 'drizzle-orm';

export class WorkoutSessionService {
    // Create a new workout session (when user actually starts working out)
    static async createWorkoutSession(data: {
        userId: string;
        userWorkoutPlanId: number;
        workoutPlanDayId: number;
        notes?: string;
    }) {
        const result = await db
            .insert(workoutSession)
            .values({
                ...data,
                startTime: new Date(),
            })
            .returning();
        return result[0];
    }

    static async getUserWorkoutSessions(userId: string, limit = 20) {
        return await db.query.workoutSession.findMany({
            where: eq(workoutSession.userId, userId),
            orderBy: desc(workoutSession.createdAt),
            limit,
            with: {
                workoutPlanDay: {
                    with: {
                        exercises: {
                            with: {
                                exerciseType: true,
                            },
                        },
                    },
                },
                exerciseResults: true,
            },
        });
    }

    static async startWorkoutSession(sessionId: number) {
        const result = await db
            .update(workoutSession)
            .set({
                status: 'in_progress',
                startTime: new Date(),
                updatedAt: new Date(),
            })
            .where(eq(workoutSession.id, sessionId))
            .returning();
        return result[0] || null;
    }

    static async completeWorkoutSession(
        sessionId: number,
        data: {
            totalDuration?: number;
            totalCalories?: number;
        }
    ) {
        const result = await db
            .update(workoutSession)
            .set({
                status: 'done',
                endTime: new Date(),
                ...data,
                updatedAt: new Date(),
            })
            .where(eq(workoutSession.id, sessionId))
            .returning();
        return result[0] || null;
    }

    static async recordExerciseResult(data: {
        workoutSessionId: number;
        workoutPlanExerciseId: number;
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

    static async getWorkoutSessionById(sessionId: number) {
        return await db.query.workoutSession.findFirst({
            where: eq(workoutSession.id, sessionId),
            with: {
                workoutPlanDay: {
                    with: {
                        exercises: {
                            with: {
                                exerciseType: true,
                            },
                        },
                    },
                },
                exerciseResults: true,
            },
        });
    }

    static async getWorkoutSessionProgress(sessionId: number) {
        const session = await db.query.workoutSession.findFirst({
            where: eq(workoutSession.id, sessionId),
            with: {
                workoutPlanDay: {
                    with: {
                        exercises: {
                            with: {
                                exerciseType: true,
                            },
                        },
                    },
                },
                exerciseResults: {
                    with: {
                        workoutPlanExercise: {
                            with: {
                                exerciseType: true,
                            },
                        },
                    },
                },
            },
        });

        if (!session) return null;

        // Calculate progress percentage
        const totalExercises = session.workoutPlanDay?.exercises?.length || 0;
        const completedExercises = session.exerciseResults?.length || 0;
        const progressPercentage =
            totalExercises > 0
                ? Math.round((completedExercises / totalExercises) * 100)
                : 0;

        return {
            ...session,
            progressPercentage,
            totalExercises,
            completedExercises,
        };
    }

    // Get workout sessions for a specific date
    static async getWorkoutSessionsForDate(userId: string, date: Date) {
        const startOfDay = new Date(date);
        startOfDay.setHours(0, 0, 0, 0);

        const endOfDay = new Date(date);
        endOfDay.setHours(23, 59, 59, 999);

        return await db.query.workoutSession.findMany({
            where: and(
                eq(workoutSession.userId, userId),
                gte(workoutSession.createdAt, startOfDay),
                lte(workoutSession.createdAt, endOfDay)
            ),
            with: {
                workoutPlanDay: {
                    with: {
                        exercises: {
                            with: {
                                exerciseType: true,
                            },
                        },
                    },
                },
                exerciseResults: true,
            },
        });
    }
}
*/
