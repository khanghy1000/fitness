import { db } from '@lib/db.ts';
import {
    workoutPlan,
    workoutPlanDay,
    workoutPlanDayExercise,
    userWorkoutPlan,
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
    }) {
        const result = await db.insert(workoutPlan).values(data).returning();
        return result[0];
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

    static async getUserWorkoutPlans(userId: string) {
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
}
