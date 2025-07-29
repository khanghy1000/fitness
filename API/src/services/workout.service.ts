import { db } from '@lib/db.ts';
import {
    workoutPlan,
    workoutPlanDay,
    workoutPlanDayExercise,
    userWorkoutPlan,
    exerciseResult,
} from '../db/schema/tables.ts';
import { eq, and, or } from 'drizzle-orm';

export class WorkoutService {
    static async getAllWorkoutPlans(userId: string, userRole: string) {
        if (userRole === 'coach') {
            // Coaches get only their created plans
            return await db
                .select()
                .from(workoutPlan)
                .where(eq(workoutPlan.createdBy, userId));
        } else if (userRole === 'trainee') {
            // Trainees get both created plans and assigned plans
            const createdPlans = await db
                .select()
                .from(workoutPlan)
                .where(eq(workoutPlan.createdBy, userId));

            const assignedPlans = await db
                .select({
                    id: workoutPlan.id,
                    name: workoutPlan.name,
                    description: workoutPlan.description,
                    difficulty: workoutPlan.difficulty,
                    estimatedCalories: workoutPlan.estimatedCalories,
                    createdBy: workoutPlan.createdBy,
                    isActive: workoutPlan.isActive,
                    createdAt: workoutPlan.createdAt,
                    updatedAt: workoutPlan.updatedAt,
                })
                .from(workoutPlan)
                .innerJoin(
                    userWorkoutPlan,
                    eq(userWorkoutPlan.workoutPlanId, workoutPlan.id)
                )
                .where(eq(userWorkoutPlan.userId, userId));

            // Combine and deduplicate plans
            const allPlans = [...createdPlans];
            const createdPlanIds = new Set(createdPlans.map((p) => p.id));

            for (const assignedPlan of assignedPlans) {
                if (!createdPlanIds.has(assignedPlan.id)) {
                    allPlans.push(assignedPlan);
                }
            }

            return allPlans;
        }

        // Default fallback
        return await db.select().from(workoutPlan);
    }

    static async getWorkoutPlanById(id: number) {
        const plans = await db.query.workoutPlan.findFirst({
            where: eq(workoutPlan.id, id),
            with: {
                workoutPlanDays: {
                    with: {
                        exercises: {
                            with: {
                                exerciseType: true,
                            },
                        },
                    },
                },
            },
        });
        return plans || null;
    }

    static async createWorkoutPlan(data: {
        name: string;
        description?: string;
        difficulty?: 'beginner' | 'intermediate' | 'advanced';
        estimatedCalories?: number;
        createdBy: string;
        userRole?: string;
    }) {
        return await db.transaction(async (tx) => {
            // Create the workout plan
            const result = await tx
                .insert(workoutPlan)
                .values({
                    name: data.name,
                    description: data.description,
                    difficulty: data.difficulty,
                    estimatedCalories: data.estimatedCalories,
                    createdBy: data.createdBy,
                })
                .returning();

            const createdPlan = result[0];

            // Auto-assign to trainee if they created it
            if (data.userRole === 'trainee') {
                await tx.insert(userWorkoutPlan).values({
                    userId: data.createdBy,
                    workoutPlanId: createdPlan.id,
                    assignedBy: data.createdBy,
                    startDate: new Date(),
                });
            }

            return createdPlan;
        });
    }

    static async updateWorkoutPlan(
        id: number,
        data: {
            name?: string;
            description?: string;
            difficulty?: 'beginner' | 'intermediate' | 'advanced';
            estimatedCalories?: number;
            isActive?: boolean;
        }
    ) {
        const result = await db
            .update(workoutPlan)
            .set({ ...data, updatedAt: new Date() })
            .where(eq(workoutPlan.id, id))
            .returning();
        return result[0] || null;
    }

    static async deleteWorkoutPlan(id: number) {
        const result = await db
            .delete(workoutPlan)
            .where(eq(workoutPlan.id, id))
            .returning();
        return result[0] || null;
    }

    static async addDayToWorkoutPlan(
        workoutPlanId: number,
        data: {
            day: number;
            isRestDay?: boolean;
            estimatedCalories?: number;
            duration?: number;
        }
    ) {
        const result = await db
            .insert(workoutPlanDay)
            .values({ ...data, workoutPlanId })
            .returning();
        return result[0];
    }

    static async addExerciseToPlanDay(
        workoutPlanDayId: number,
        data: {
            exerciseTypeId: number;
            order?: number;
            targetReps?: number;
            targetDuration?: number;
            estimatedCalories?: number;
            notes?: string;
        }
    ) {
        const result = await db
            .insert(workoutPlanDayExercise)
            .values({ ...data, workoutPlanDayId })
            .returning();
        return result[0];
    }

    static async assignWorkoutPlanToUser(data: {
        userId: string;
        workoutPlanId: number;
        assignedBy: string;
        startDate: Date;
        endDate?: Date;
    }) {
        const result = await db
            .insert(userWorkoutPlan)
            .values(data)
            .returning();
        return result[0];
    }

    static async getUserAssignedWorkoutPlans(userId: string) {
        return await db.query.userWorkoutPlan.findMany({
            where: eq(userWorkoutPlan.userId, userId),
            with: {
                workoutPlan: {
                    with: {
                        workoutPlanDays: {
                            with: {
                                exercises: {
                                    with: {
                                        exerciseType: true,
                                    },
                                },
                            },
                        },
                    },
                },
            },
        });
    }

    // Workout Plan Day methods
    static async getWorkoutPlanDays(workoutPlanId: number) {
        return await db.query.workoutPlanDay.findMany({
            where: eq(workoutPlanDay.workoutPlanId, workoutPlanId),
            with: {
                exercises: {
                    with: {
                        exerciseType: true,
                    },
                },
            },
        });
    }

    static async getWorkoutPlanDayById(id: number) {
        return await db.query.workoutPlanDay.findFirst({
            where: eq(workoutPlanDay.id, id),
            with: {
                exercises: {
                    with: {
                        exerciseType: true,
                    },
                },
            },
        });
    }

    static async updateWorkoutPlanDay(
        id: number,
        data: {
            day?: number;
            isRestDay?: boolean;
            estimatedCalories?: number;
            duration?: number;
        }
    ) {
        const result = await db
            .update(workoutPlanDay)
            .set(data)
            .where(eq(workoutPlanDay.id, id))
            .returning();
        return result[0] || null;
    }

    static async deleteWorkoutPlanDay(id: number) {
        const result = await db
            .delete(workoutPlanDay)
            .where(eq(workoutPlanDay.id, id))
            .returning();
        return result[0] || null;
    }

    // Workout Plan Day Exercise methods
    static async getWorkoutPlanDayExercises(workoutPlanDayId: number) {
        return await db.query.workoutPlanDayExercise.findMany({
            where: eq(
                workoutPlanDayExercise.workoutPlanDayId,
                workoutPlanDayId
            ),
            with: {
                exerciseType: true,
            },
        });
    }

    static async getWorkoutPlanDayExerciseById(id: number) {
        return await db.query.workoutPlanDayExercise.findFirst({
            where: eq(workoutPlanDayExercise.id, id),
            with: {
                exerciseType: true,
            },
        });
    }

    static async updateExerciseInPlanDay(
        id: number,
        data: {
            exerciseTypeId?: number;
            order?: number;
            targetReps?: number;
            targetDuration?: number;
            estimatedCalories?: number;
            notes?: string;
        }
    ) {
        const result = await db
            .update(workoutPlanDayExercise)
            .set({ ...data, updatedAt: new Date() })
            .where(eq(workoutPlanDayExercise.id, id))
            .returning();
        return result[0] || null;
    }

    static async deleteExerciseFromPlanDay(id: number) {
        const result = await db
            .delete(workoutPlanDayExercise)
            .where(eq(workoutPlanDayExercise.id, id))
            .returning();
        return result[0] || null;
    }

    // Record exercise result
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
}
