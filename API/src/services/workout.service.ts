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

    static async assignWorkoutPlanToUser(data: {
        userId: string;
        workoutPlanId: number;
        assignedBy: string;
        startDate: Date;
    }) {
        const result = await db
            .insert(userWorkoutPlan)
            .values(data)
            .returning();
        return result[0];
    }

    static async cancelWorkoutPlan(
        userWorkoutPlanId: number,
        userId: string,
    ) {
        return await db.transaction(async (tx) => {
            // First verify the plan belongs to the user
            const existingPlan = await tx.query.userWorkoutPlan.findFirst({
                where: and(
                    eq(userWorkoutPlan.id, userWorkoutPlanId),
                    eq(userWorkoutPlan.userId, userId)
                ),
            });

            if (!existingPlan) {
                throw new Error('Workout plan not found or access denied');
            }

            if (existingPlan.status === 'cancelled') {
                throw new Error('Workout plan is already cancelled');
            }

            if (existingPlan.status === 'completed') {
                throw new Error('Cannot cancel a completed workout plan');
            }

            // Update the plan status to cancelled and set end date
            const result = await tx
                .update(userWorkoutPlan)
                .set({
                    status: 'cancelled',
                    endDate: new Date(),
                    updatedAt: new Date(),
                })
                .where(eq(userWorkoutPlan.id, userWorkoutPlanId))
                .returning();

            return result[0];
        });
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

    // Record exercise result
    static async recordExerciseResult(data: {
        workoutPlanDayExerciseId: number;
        userWorkoutPlanId: number;
        userId: string;
        reps?: number;
        duration?: number;
        calories?: number;
    }) {
        return await db.transaction(async (tx) => {
            // Check if an exercise result already exists for this exercise, user workout plan, and user
            const existingResult = await tx
                .select()
                .from(exerciseResult)
                .where(
                    and(
                        eq(
                            exerciseResult.workoutPlanDayExerciseId,
                            data.workoutPlanDayExerciseId
                        ),
                        eq(
                            exerciseResult.userWorkoutPlanId,
                            data.userWorkoutPlanId
                        ),
                        eq(exerciseResult.userId, data.userId)
                    )
                )
                .limit(1);

            let result;
            if (existingResult.length > 0) {
                // Update existing record
                const updated = await tx
                    .update(exerciseResult)
                    .set({
                        reps: data.reps,
                        duration: data.duration,
                        calories: data.calories,
                        completedAt: new Date(),
                    })
                    .where(eq(exerciseResult.id, existingResult[0].id))
                    .returning();
                result = updated[0];
            } else {
                // Insert new exercise result
                const inserted = await tx
                    .insert(exerciseResult)
                    .values({
                        ...data,
                        completedAt: new Date(),
                    })
                    .returning();
                result = inserted[0];
            }

            // Update progress for the user workout plan
            await this.updateWorkoutPlanProgress(
                data.userWorkoutPlanId,
                data.userId,
                tx
            );

            return result;
        });
    }

    // Reset exercise results for a workout day
    static async resetWorkoutDayResults(data: {
        userWorkoutPlanId: number;
        workoutPlanDayId: number;
        userId: string;
    }) {
        return await db.transaction(async (tx) => {
            // Get all exercises for this workout plan day
            const dayExercises = await tx
                .select({ id: workoutPlanDayExercise.id })
                .from(workoutPlanDayExercise)
                .where(
                    eq(
                        workoutPlanDayExercise.workoutPlanDayId,
                        data.workoutPlanDayId
                    )
                );

            if (dayExercises.length > 0) {
                const exerciseIds = dayExercises.map((e) => e.id);

                // Delete all exercise results for this day, user workout plan, and user
                const deletedResults = await tx
                    .delete(exerciseResult)
                    .where(
                        and(
                            eq(
                                exerciseResult.userWorkoutPlanId,
                                data.userWorkoutPlanId
                            ),
                            eq(exerciseResult.userId, data.userId),
                            or(
                                ...exerciseIds.map((id) =>
                                    eq(
                                        exerciseResult.workoutPlanDayExerciseId,
                                        id
                                    )
                                )
                            )
                        )
                    )
                    .returning();

                // Update progress for the user workout plan
                await this.updateWorkoutPlanProgress(
                    data.userWorkoutPlanId,
                    data.userId,
                    tx
                );
            }
        });
    }

    // Helper method to calculate and update workout plan progress
    static async updateWorkoutPlanProgress(
        userWorkoutPlanId: number,
        userId: string,
        tx: any = db
    ) {
        // Get user workout plan with all workout plan days and exercises
        const userPlan = await tx.query.userWorkoutPlan.findFirst({
            where: and(
                eq(userWorkoutPlan.id, userWorkoutPlanId),
                eq(userWorkoutPlan.userId, userId)
            ),
            with: {
                workoutPlan: {
                    with: {
                        workoutPlanDays: {
                            with: {
                                exercises: {
                                    with: {
                                        exerciseResults: {
                                            where: and(
                                                eq(
                                                    exerciseResult.userId,
                                                    userId
                                                ),
                                                eq(
                                                    exerciseResult.userWorkoutPlanId,
                                                    userWorkoutPlanId
                                                )
                                            ),
                                        },
                                    },
                                },
                            },
                        },
                    },
                },
            },
        });

        if (!userPlan || !userPlan.workoutPlan) {
            return;
        }

        const workoutPlanDays = userPlan.workoutPlan.workoutPlanDays;
        let completedDays = 0;
        let totalNonRestDays = 0;

        for (const day of workoutPlanDays) {
            // Skip rest days
            if (day.isRestDay) {
                continue;
            }

            totalNonRestDays++;

            // Check if all exercises for this day are completed
            const allExercisesCompleted = day.exercises.every(
                (exercise: any) => exercise.exerciseResults.length > 0
            );

            if (allExercisesCompleted) {
                completedDays++;
            }
        }

        // Calculate progress percentage
        const progress =
            totalNonRestDays > 0 ? (completedDays / totalNonRestDays) * 100 : 0;

        // Prepare update data
        const updateData: any = {
            progress: Math.round(progress * 100) / 100, // Round to 2 decimal places
            updatedAt: new Date(),
        };

        // If progress is 100% and status is still active, mark as completed
        if (progress >= 100 && userPlan.status === 'active') {
            updateData.status = 'completed';
            updateData.endDate = new Date();
        }

        // Update user workout plan progress and potentially status
        await tx
            .update(userWorkoutPlan)
            .set(updateData)
            .where(eq(userWorkoutPlan.id, userWorkoutPlanId));
    }

    // Get workout plan results with exercise results
    static async getWorkoutPlanResults(
        userWorkoutPlanId: number,
        userId: string
    ) {
        // First get the userWorkoutPlan for this user
        const userPlan = await db.query.userWorkoutPlan.findFirst({
            where: and(
                eq(userWorkoutPlan.id, userWorkoutPlanId),
                eq(userWorkoutPlan.userId, userId)
            ),
        });

        if (!userPlan || !userPlan.workoutPlanId) {
            return null;
        }

        // Get the workout plan with all days, exercises, and exercise results
        const workoutPlanWithResults = await db.query.workoutPlan.findFirst({
            where: eq(workoutPlan.id, userPlan.workoutPlanId),
            with: {
                workoutPlanDays: {
                    with: {
                        exercises: {
                            with: {
                                exerciseType: true,
                                exerciseResults: {
                                    where: and(
                                        eq(exerciseResult.userId, userId),
                                        eq(
                                            exerciseResult.userWorkoutPlanId,
                                            userWorkoutPlanId
                                        )
                                    ),
                                },
                            },
                        },
                    },
                },
            },
        });

        return {
            ...workoutPlanWithResults,
            userWorkoutPlan: userPlan,
        };
    }

    static async bulkUpdateWorkoutPlan(
        id: number,
        data: {
            name?: string;
            description?: string;
            difficulty?: 'beginner' | 'intermediate' | 'advanced';
            isActive?: boolean;
            days?: Array<{
                id?: number;
                day: number;
                isRestDay?: boolean;
                exercises?: Array<{
                    id?: number;
                    exerciseTypeId: number;
                    order?: number;
                    targetReps?: number;
                    targetDuration?: number;
                    notes?: string;
                }>;
            }>;
        }
    ) {
        return await db.transaction(async (tx) => {
            // Helper function to calculate exercise estimated calories (base formula)
            const calculateExerciseCalories = (
                targetReps?: number,
                targetDuration?: number
            ): number => {
                // Base calories per rep (3 seconds per rep) or per second
                const caloriesPerSecond = 0.1; // Rough estimate
                if (targetDuration) {
                    return Math.round(targetDuration * caloriesPerSecond);
                } else if (targetReps) {
                    return Math.round(targetReps * 3 * caloriesPerSecond); // 3 seconds per rep
                }
                return 0;
            };

            // Helper function to calculate exercise duration
            const calculateExerciseDuration = (
                targetReps?: number,
                targetDuration?: number
            ): number => {
                if (targetDuration) {
                    return targetDuration;
                } else if (targetReps) {
                    return targetReps * 3; // 3 seconds per rep
                }
                return 0;
            };

            // Calculate total plan estimated calories if days are provided
            let totalPlanCalories = 0;
            if (data.days) {
                for (const dayData of data.days) {
                    // Auto-determine if day should be rest day based on exercises
                    const hasExercises =
                        dayData.exercises && dayData.exercises.length > 0;
                    const isRestDay = dayData.isRestDay || !hasExercises;

                    if (!isRestDay && dayData.exercises) {
                        for (const exercise of dayData.exercises) {
                            totalPlanCalories += calculateExerciseCalories(
                                exercise.targetReps,
                                exercise.targetDuration
                            );
                        }
                    }
                }
            }

            // Update workout plan basic info with auto-calculated calories
            const { days, ...planData } = data;
            const planUpdateData = {
                ...planData,
                ...(data.days && { estimatedCalories: totalPlanCalories }),
                updatedAt: new Date(),
            };

            const updatedPlan = await tx
                .update(workoutPlan)
                .set(planUpdateData)
                .where(eq(workoutPlan.id, id))
                .returning();

            if (!updatedPlan[0]) {
                throw new Error('Workout plan not found');
            }

            if (days) {
                // Get existing days and exercises
                const existingDays = await tx.query.workoutPlanDay.findMany({
                    where: eq(workoutPlanDay.workoutPlanId, id),
                    with: {
                        exercises: true,
                    },
                });
                const existingDayIds = new Set(existingDays.map((d) => d.id));
                const existingExerciseIds = new Set(
                    existingDays.flatMap((d) => d.exercises.map((e) => e.id))
                );

                const providedDayIds = new Set(
                    days.filter((d) => d.id).map((d) => d.id!)
                );
                const providedExerciseIds = new Set(
                    days.flatMap(
                        (d) =>
                            d.exercises
                                ?.filter((e) => e.id)
                                .map((e) => e.id!) || []
                    )
                );

                // Delete days that are not in the provided list
                const daysToDelete = existingDays.filter(
                    (d) => !providedDayIds.has(d.id)
                );
                for (const dayToDelete of daysToDelete) {
                    await tx
                        .delete(workoutPlanDay)
                        .where(eq(workoutPlanDay.id, dayToDelete.id));
                }

                // Delete exercises that are not in the provided list for existing days
                for (const existingDay of existingDays) {
                    if (providedDayIds.has(existingDay.id)) {
                        const exercisesToDelete = existingDay.exercises.filter(
                            (e) => !providedExerciseIds.has(e.id)
                        );
                        for (const exerciseToDelete of exercisesToDelete) {
                            await tx
                                .delete(workoutPlanDayExercise)
                                .where(
                                    eq(
                                        workoutPlanDayExercise.id,
                                        exerciseToDelete.id
                                    )
                                );
                        }
                    }
                }

                // Process each day
                for (const dayData of days) {
                    const { exercises, ...dayInfoRaw } = dayData;

                    // Auto-set as rest day if no exercises are provided or exercises array is empty
                    const hasExercises = exercises && exercises.length > 0;
                    const isRestDay = dayData.isRestDay || !hasExercises;

                    // Calculate day-level totals
                    let dayCalories = 0;
                    let dayDuration = 0;

                    if (!isRestDay && exercises) {
                        for (const exercise of exercises) {
                            dayCalories += calculateExerciseCalories(
                                exercise.targetReps,
                                exercise.targetDuration
                            );
                            dayDuration += calculateExerciseDuration(
                                exercise.targetReps,
                                exercise.targetDuration
                            );
                        }
                    }

                    // Remove id from update data and add calculated values
                    const { id: dayIdForUpdate, ...dayInfo } = dayInfoRaw;
                    const dayInfoWithCalculated = {
                        ...dayInfo,
                        isRestDay: isRestDay,
                        estimatedCalories: dayCalories,
                        duration: dayDuration,
                    };

                    let dayId: number;

                    if (dayData.id) {
                        // Only update if the day exists
                        if (existingDayIds.has(dayData.id)) {
                            const updatedDay = await tx
                                .update(workoutPlanDay)
                                .set(dayInfoWithCalculated)
                                .where(eq(workoutPlanDay.id, dayData.id))
                                .returning();
                            dayId = updatedDay[0].id;
                        } else {
                            // Skip update for non-existent day
                            continue;
                        }
                    } else {
                        // Create new day
                        const newDay = await tx
                            .insert(workoutPlanDay)
                            .values({
                                ...dayInfoWithCalculated,
                                workoutPlanId: id,
                            })
                            .returning();
                        dayId = newDay[0].id;
                    }

                    // Process exercises for this day (only if it's not a rest day)
                    if (exercises && !isRestDay) {
                        for (const exerciseData of exercises) {
                            const {
                                id: exerciseIdForUpdate,
                                ...exerciseInfoRaw
                            } = exerciseData;

                            // Calculate exercise estimated calories
                            const exerciseCalories = calculateExerciseCalories(
                                exerciseData.targetReps,
                                exerciseData.targetDuration
                            );

                            const exerciseInfo = {
                                ...exerciseInfoRaw,
                                estimatedCalories: exerciseCalories,
                            };

                            if (exerciseData.id) {
                                // Only update if the exercise exists
                                if (existingExerciseIds.has(exerciseData.id)) {
                                    await tx
                                        .update(workoutPlanDayExercise)
                                        .set({
                                            ...exerciseInfo,
                                            updatedAt: new Date(),
                                        })
                                        .where(
                                            eq(
                                                workoutPlanDayExercise.id,
                                                exerciseData.id
                                            )
                                        );
                                } else {
                                    // Skip update for non-existent exercise
                                    continue;
                                }
                            } else {
                                // Create new exercise
                                await tx.insert(workoutPlanDayExercise).values({
                                    ...exerciseInfo,
                                    workoutPlanDayId: dayId,
                                });
                            }
                        }
                    }
                }
            }

            // Recalculate workout progress for all users with this plan assigned
            await this.recalculateWorkoutProgressForPlan(id, tx);

            // Return the updated plan with full details
            return await tx.query.workoutPlan.findFirst({
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
        });
    }

    // Helper method to recalculate workout progress for all users with a specific workout plan
    static async recalculateWorkoutProgressForPlan(
        workoutPlanId: number,
        tx: any = db
    ) {
        // Get all user workout plans for this workout plan
        const userWorkoutPlans = await tx.query.userWorkoutPlan.findMany({
            where: eq(userWorkoutPlan.workoutPlanId, workoutPlanId),
        });

        // Recalculate progress for each user workout plan
        for (const userPlan of userWorkoutPlans) {
            await this.updateWorkoutPlanProgress(
                userPlan.id,
                userPlan.userId,
                tx
            );
        }
    }
}
