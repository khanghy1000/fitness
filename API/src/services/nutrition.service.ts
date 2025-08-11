import { db } from '@lib/db.ts';
import {
    user,
    nutritionPlan,
    nutritionPlanDay,
    nutritionPlanMeal,
    nutritionPlanFood,
    userNutritionPlan,
    nutritionAdherence,
    mealCompletion,
} from '../db/schema/tables.ts';
import { eq, and, inArray, desc, sql } from 'drizzle-orm';

type FoodType = {
    name: string;
    quantity: string;
    calories: number;
    protein?: number;
    carbs?: number;
    fat?: number;
    fiber?: number;
};

type MealType = {
    name: string;
    time: string; // HH:MM:SS format
    calories?: number;
    protein?: number;
    carbs?: number;
    fat?: number;
    fiber?: number;
    foods: FoodType[];
};

type WeekdayPlanType = {
    weekday: 'sun' | 'mon' | 'tue' | 'wed' | 'thu' | 'fri' | 'sat';
    totalCalories?: number;
    protein?: number;
    carbs?: number;
    fat?: number;
    fiber?: number;
    meals: MealType[];
};

export class NutritionService {
    static async getAllNutritionPlans(userId: string, userRole: string) {
        if (userRole === 'coach') {
            // Coaches get only their created plans
            return await db
                .select()
                .from(nutritionPlan)
                .where(eq(nutritionPlan.createdBy, userId));
        } else if (userRole === 'trainee') {
            // Trainees get both created plans and assigned plans
            const createdPlans = await db
                .select()
                .from(nutritionPlan)
                .where(eq(nutritionPlan.createdBy, userId));

            const assignedPlans = await db
                .select({
                    id: nutritionPlan.id,
                    name: nutritionPlan.name,
                    description: nutritionPlan.description,
                    createdBy: nutritionPlan.createdBy,
                    isActive: nutritionPlan.isActive,
                    createdAt: nutritionPlan.createdAt,
                    updatedAt: nutritionPlan.updatedAt,
                })
                .from(nutritionPlan)
                .innerJoin(
                    userNutritionPlan,
                    eq(userNutritionPlan.nutritionPlanId, nutritionPlan.id)
                )
                .where(eq(userNutritionPlan.userId, userId));

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
        return await db.select().from(nutritionPlan);
    }

    static async getNutritionPlanById(id: number) {
        const plan = await db.query.nutritionPlan.findFirst({
            where: eq(nutritionPlan.id, id),
            with: {
                days: {
                    with: {
                        meals: {
                            with: {
                                foods: true,
                            },
                        },
                    },
                },
            },
        });
        return plan || null;
    }

    static async createNutritionPlan(data: {
        name: string;
        description?: string;
        createdBy: string;
        userRole?: string;
    }) {
        return await db.transaction(async (tx) => {
            // Create the main nutrition plan
            const [plan] = await tx
                .insert(nutritionPlan)
                .values({
                    name: data.name,
                    description: data.description,
                    createdBy: data.createdBy,
                })
                .returning();

            // Auto-assign to trainee if they created it
            if (data.userRole === 'trainee') {
                await tx.insert(userNutritionPlan).values({
                    userId: data.createdBy,
                    nutritionPlanId: plan.id,
                    assignedBy: data.createdBy,
                    startDate: new Date(),
                });
            }

            return plan;
        });
    }

    static async updateNutritionPlan(
        id: number,
        data: {
            name?: string;
            description?: string;
            isActive?: boolean;
        }
    ) {
        const result = await db
            .update(nutritionPlan)
            .set({ ...data, updatedAt: new Date() })
            .where(eq(nutritionPlan.id, id))
            .returning();
        return result[0] || null;
    }

    static async deleteNutritionPlan(id: number) {
        const result = await db
            .delete(nutritionPlan)
            .where(eq(nutritionPlan.id, id))
            .returning();
        return result[0] || null;
    }

    static async assignNutritionPlanToUser(data: {
        userId: string;
        nutritionPlanId: number;
        assignedBy: string;
        startDate: Date;
    }) {
        const result = await db
            .insert(userNutritionPlan)
            .values(data)
            .returning();
        return result[0];
    }

    static async completeNutritionPlan(
        userNutritionPlanId: number,
        userId: string,
    ) {
        return await db.transaction(async (tx) => {
            // First verify the plan belongs to the user
            const existingPlan = await tx.query.userNutritionPlan.findFirst({
                where: and(
                    eq(userNutritionPlan.id, userNutritionPlanId),
                    eq(userNutritionPlan.userId, userId)
                ),
            });

            if (!existingPlan) {
                throw new Error('Nutrition plan not found or access denied');
            }

            if (existingPlan.status === 'completed') {
                throw new Error('Nutrition plan is already completed');
            }

            // Update the plan status to completed and set end date
            const result = await tx
                .update(userNutritionPlan)
                .set({
                    status: 'completed',
                    endDate: new Date(),
                    updatedAt: new Date(),
                })
                .where(eq(userNutritionPlan.id, userNutritionPlanId))
                .returning();

            return result[0];
        });
    }

    static async getUserAssignedNutritionPlans(userId: string) {
        return await db.query.userNutritionPlan.findMany({
            where: eq(userNutritionPlan.userId, userId),
            with: {
                nutritionPlan: {
                    with: {
                        days: {
                            with: {
                                meals: {
                                    with: {
                                        foods: true,
                                    },
                                },
                            },
                        },
                    },
                },
                nutritionAdherences: {
                    orderBy: (adherence) => desc(adherence.date),
                },
            },
        });
    }

    // Adherence tracking methods

    // Helper method to get userNutritionPlanId from nutritionPlanId and userId
    static async getUserNutritionPlanId(
        userId: string,
        nutritionPlanId: number
    ) {
        const result = await db
            .select({ id: userNutritionPlan.id })
            .from(userNutritionPlan)
            .where(
                and(
                    eq(userNutritionPlan.userId, userId),
                    eq(userNutritionPlan.nutritionPlanId, nutritionPlanId)
                )
            );
        return result[0]?.id || null;
    }

    static async getDailyAdherence(
        userId: string,
        userNutritionPlanId: number,
        date: Date
    ) {
        // Convert to date string in YYYY-MM-DD format for date column
        const dateStr = date.toISOString().split('T')[0];

        const result = await db
            .select()
            .from(nutritionAdherence)
            .where(
                and(
                    eq(
                        nutritionAdherence.userNutritionPlanId,
                        userNutritionPlanId
                    ),
                    eq(nutritionAdherence.userId, userId),
                    eq(nutritionAdherence.date, dateStr)
                )
            );
        return result[0] || null;
    }

    static async completeMeal(data: {
        userNutritionPlanId: number;
        nutritionPlanMealId: number;
        userId: string;
        notes?: string;
    }) {
        return await db.transaction(async (tx) => {
            const completionDate = new Date();
            // Convert to date string in YYYY-MM-DD format for date column
            const dateStr = completionDate.toISOString().split('T')[0];
            const weekday = this.getWeekdayEnum(completionDate.getDay());

            // Get the nutrition plan meal with its foods to calculate nutrition values
            const mealWithFoods = await tx.query.nutritionPlanMeal.findFirst({
                where: eq(nutritionPlanMeal.id, data.nutritionPlanMealId),
                with: {
                    foods: true,
                },
            });

            if (!mealWithFoods) {
                throw new Error('Nutrition plan meal not found');
            }

            // Calculate total nutrition values from all foods in the meal
            const calculatedNutrition = mealWithFoods.foods.reduce(
                (totals, food) => ({
                    calories: totals.calories + (food.calories || 0),
                    protein: totals.protein + (food.protein || 0),
                    carbs: totals.carbs + (food.carbs || 0),
                    fat: totals.fat + (food.fat || 0),
                    fiber: totals.fiber + (food.fiber || 0),
                }),
                { calories: 0, protein: 0, carbs: 0, fat: 0, fiber: 0 }
            );

            // Check if nutrition adherence record exists for this date
            let adherence = await tx.query.nutritionAdherence.findFirst({
                where: and(
                    eq(
                        nutritionAdherence.userNutritionPlanId,
                        data.userNutritionPlanId
                    ),
                    eq(nutritionAdherence.userId, data.userId),
                    eq(nutritionAdherence.date, dateStr)
                ),
            });

            // If no adherence record exists, create one
            if (!adherence) {
                // Get the nutrition plan to calculate total meals for the day
                const nutritionPlan =
                    await tx.query.userNutritionPlan.findFirst({
                        where: eq(
                            userNutritionPlan.id,
                            data.userNutritionPlanId
                        ),
                        with: {
                            nutritionPlan: {
                                with: {
                                    days: {
                                        where: eq(
                                            nutritionPlanDay.weekday,
                                            weekday
                                        ),
                                        with: {
                                            meals: true,
                                        },
                                    },
                                },
                            },
                        },
                    });

                // Get the total meals for this specific weekday
                const dayPlan = nutritionPlan?.nutritionPlan?.days?.find(
                    (day) => day.weekday === weekday
                );
                const totalMeals = dayPlan?.meals?.length || 0;

                const adherenceResult = await tx
                    .insert(nutritionAdherence)
                    .values({
                        userNutritionPlanId: data.userNutritionPlanId,
                        userId: data.userId,
                        date: dateStr,
                        weekday,
                        totalMeals,
                        mealsCompleted: 0,
                        adherencePercentage: 0,
                        totalCaloriesConsumed: 0,
                        totalCaloriesPlanned: 0,
                    })
                    .returning();
                adherence = adherenceResult[0];
            }

            // Check if this meal was already completed
            const existingCompletion = await tx.query.mealCompletion.findFirst({
                where: and(
                    eq(mealCompletion.nutritionAdherenceId, adherence.id),
                    eq(
                        mealCompletion.nutritionPlanMealId,
                        data.nutritionPlanMealId
                    )
                ),
            });

            let completion;
            if (existingCompletion) {
                // Update existing completion with calculated nutrition values
                const updateResult = await tx
                    .update(mealCompletion)
                    .set({
                        isCompleted: true,
                        completedAt: new Date(),
                        caloriesConsumed: calculatedNutrition.calories,
                        proteinConsumed: calculatedNutrition.protein,
                        carbsConsumed: calculatedNutrition.carbs,
                        fatConsumed: calculatedNutrition.fat,
                        fiberConsumed: calculatedNutrition.fiber,
                        notes: data.notes,
                        updatedAt: new Date(),
                    })
                    .where(eq(mealCompletion.id, existingCompletion.id))
                    .returning();
                completion = updateResult[0];
            } else {
                // Create new completion with calculated nutrition values
                const completionResult = await tx
                    .insert(mealCompletion)
                    .values({
                        nutritionAdherenceId: adherence.id,
                        nutritionPlanMealId: data.nutritionPlanMealId,
                        userId: data.userId,
                        isCompleted: true,
                        completedAt: new Date(),
                        caloriesConsumed: calculatedNutrition.calories,
                        proteinConsumed: calculatedNutrition.protein,
                        carbsConsumed: calculatedNutrition.carbs,
                        fatConsumed: calculatedNutrition.fat,
                        fiberConsumed: calculatedNutrition.fiber,
                        notes: data.notes,
                    })
                    .returning();
                completion = completionResult[0];
            }

            // Update adherence statistics
            await this.updateNutritionAdherenceStats(adherence.id, tx);

            return completion;
        });
    }

    // Helper method to update nutrition adherence statistics
    static async updateNutritionAdherenceStats(
        adherenceId: number,
        tx: any = db
    ) {
        // Get all meal completions for this adherence record
        const completions = await tx.query.mealCompletion.findMany({
            where: eq(mealCompletion.nutritionAdherenceId, adherenceId),
            with: {
                nutritionPlanMeal: true,
            },
        });

        // Get adherence record with total meals and associated nutrition plan
        const adherence = await tx.query.nutritionAdherence.findFirst({
            where: eq(nutritionAdherence.id, adherenceId),
            with: {
                userNutritionPlan: {
                    with: {
                        nutritionPlan: {
                            with: {
                                days: {
                                    with: {
                                        meals: true,
                                    },
                                },
                            },
                        },
                    },
                },
            },
        });

        if (!adherence) return;

        // Get the meals planned for this specific weekday
        const dayPlan = adherence.userNutritionPlan?.nutritionPlan?.days?.find(
            (day: any) => day.weekday === adherence.weekday
        );
        const plannedMeals = dayPlan?.meals || [];

        // Update total meals if it's different from what's currently stored
        const actualTotalMeals = plannedMeals.length;

        const completedMeals = completions.filter(
            (c: any) => c.isCompleted
        ).length;

        const totalCaloriesConsumed = completions
            .filter((c: any) => c.isCompleted)
            .reduce(
                (sum: number, c: any) => sum + (c.caloriesConsumed || 0),
                0
            );

        // Calculate total planned calories from the nutrition plan meals for this day
        const totalCaloriesPlanned = plannedMeals.reduce(
            (sum: number, meal: any) => sum + (meal.calories || 0),
            0
        );

        const adherencePercentage =
            actualTotalMeals > 0
                ? (completedMeals / actualTotalMeals) * 100
                : 0;

        await tx
            .update(nutritionAdherence)
            .set({
                totalMeals: actualTotalMeals,
                mealsCompleted: completedMeals,
                adherencePercentage:
                    Math.round(adherencePercentage * 100) / 100,
                totalCaloriesConsumed,
                totalCaloriesPlanned,
                updatedAt: new Date(),
            })
            .where(eq(nutritionAdherence.id, adherenceId));
    }

    // Helper method to convert day number to weekday enum
    static getWeekdayEnum(
        dayNumber: number
    ): 'sun' | 'mon' | 'tue' | 'wed' | 'thu' | 'fri' | 'sat' {
        const weekdays = [
            'sun',
            'mon',
            'tue',
            'wed',
            'thu',
            'fri',
            'sat',
        ] as const;
        return weekdays[dayNumber];
    }

    static async getMealCompletions(nutritionAdherenceId: number) {
        return await db
            .select()
            .from(mealCompletion)
            .where(
                eq(mealCompletion.nutritionAdherenceId, nutritionAdherenceId)
            );
    }

    // Get user adherence history by user nutrition plan ID
    static async getUserAdherenceHistoryByPlan(
        userId: string,
        userNutritionPlanId: number,
        startDate?: Date,
        endDate?: Date
    ) {
        const conditions = [
            eq(nutritionAdherence.userNutritionPlanId, userNutritionPlanId),
            eq(nutritionAdherence.userId, userId),
        ];

        if (startDate) {
            const startDateStr = startDate.toISOString().split('T')[0];
            conditions.push(sql`${nutritionAdherence.date} >= ${startDateStr}`);
        }
        if (endDate) {
            const endDateStr = endDate.toISOString().split('T')[0];
            conditions.push(sql`${nutritionAdherence.date} <= ${endDateStr}`);
        }

        return await db.query.nutritionAdherence.findMany({
            where: and(...conditions),
            orderBy: (adherence) => desc(adherence.date),
            with: {
                mealCompletions: true,
            },
        });
    }

    static async bulkUpdateNutritionPlan(
        id: number,
        data: {
            name?: string;
            description?: string;
            isActive?: boolean;
            days?: Array<{
                id?: number;
                weekday: 'sun' | 'mon' | 'tue' | 'wed' | 'thu' | 'fri' | 'sat';
                meals?: Array<{
                    id?: number;
                    name: string;
                    time: string;
                    foods?: Array<{
                        id?: number;
                        name: string;
                        quantity: string;
                        calories: number;
                        protein?: number;
                        carbs?: number;
                        fat?: number;
                        fiber?: number;
                    }>;
                }>;
            }>;
        }
    ) {
        return await db.transaction(async (tx) => {
            // Helper function to calculate meal totals from foods
            const calculateMealTotals = (
                foods: Array<{
                    calories: number;
                    protein?: number;
                    carbs?: number;
                    fat?: number;
                    fiber?: number;
                }>
            ) => {
                return foods.reduce(
                    (totals, food) => ({
                        calories: totals.calories + food.calories,
                        protein: (totals.protein || 0) + (food.protein || 0),
                        carbs: (totals.carbs || 0) + (food.carbs || 0),
                        fat: (totals.fat || 0) + (food.fat || 0),
                        fiber: (totals.fiber || 0) + (food.fiber || 0),
                    }),
                    { calories: 0, protein: 0, carbs: 0, fat: 0, fiber: 0 }
                );
            };

            // Helper function to calculate day totals from meals
            const calculateDayTotals = (
                meals: Array<{
                    foods?: Array<{
                        calories: number;
                        protein?: number;
                        carbs?: number;
                        fat?: number;
                        fiber?: number;
                    }>;
                }>
            ) => {
                return meals.reduce(
                    (totals, meal) => {
                        if (meal.foods) {
                            const mealTotals = calculateMealTotals(meal.foods);
                            return {
                                totalCalories:
                                    totals.totalCalories + mealTotals.calories,
                                protein:
                                    (totals.protein || 0) +
                                    (mealTotals.protein || 0),
                                carbs:
                                    (totals.carbs || 0) +
                                    (mealTotals.carbs || 0),
                                fat: (totals.fat || 0) + (mealTotals.fat || 0),
                                fiber:
                                    (totals.fiber || 0) +
                                    (mealTotals.fiber || 0),
                            };
                        }
                        return totals;
                    },
                    { totalCalories: 0, protein: 0, carbs: 0, fat: 0, fiber: 0 }
                );
            };

            // Update nutrition plan basic info
            const { days, ...planData } = data;

            const updatedPlan = await tx
                .update(nutritionPlan)
                .set({ ...planData, updatedAt: new Date() })
                .where(eq(nutritionPlan.id, id))
                .returning();

            if (!updatedPlan[0]) {
                throw new Error('Nutrition plan not found');
            }

            if (days) {
                // Get existing days, meals, and foods
                const existingDays = await tx.query.nutritionPlanDay.findMany({
                    where: eq(nutritionPlanDay.nutritionPlanId, id),
                    with: {
                        meals: {
                            with: {
                                foods: true,
                            },
                        },
                    },
                });
                const existingDayIds = new Set(existingDays.map((d) => d.id));
                const existingMealIds = new Set(
                    existingDays.flatMap((d) => d.meals.map((m) => m.id))
                );
                const existingFoodIds = new Set(
                    existingDays.flatMap((d) =>
                        d.meals.flatMap((m) => m.foods.map((f) => f.id))
                    )
                );

                const providedDayIds = new Set(
                    days.filter((d) => d.id).map((d) => d.id!)
                );
                const providedMealIds = new Set(
                    days.flatMap(
                        (d) =>
                            d.meals?.filter((m) => m.id).map((m) => m.id!) || []
                    )
                );
                const providedFoodIds = new Set(
                    days.flatMap(
                        (d) =>
                            d.meals?.flatMap(
                                (m) =>
                                    m.foods
                                        ?.filter((f) => f.id)
                                        .map((f) => f.id!) || []
                            ) || []
                    )
                );

                // Delete days that are not in the provided list
                const daysToDelete = existingDays.filter(
                    (d) => !providedDayIds.has(d.id)
                );
                for (const dayToDelete of daysToDelete) {
                    await tx
                        .delete(nutritionPlanDay)
                        .where(eq(nutritionPlanDay.id, dayToDelete.id));
                }

                // Delete meals and foods that are not in the provided list for existing days
                for (const existingDay of existingDays) {
                    if (providedDayIds.has(existingDay.id)) {
                        const mealsToDelete = existingDay.meals.filter(
                            (m) => !providedMealIds.has(m.id)
                        );
                        for (const mealToDelete of mealsToDelete) {
                            await tx
                                .delete(nutritionPlanMeal)
                                .where(
                                    eq(nutritionPlanMeal.id, mealToDelete.id)
                                );
                        }

                        // Delete foods that are not in the provided list for existing meals
                        for (const existingMeal of existingDay.meals) {
                            if (providedMealIds.has(existingMeal.id)) {
                                const foodsToDelete = existingMeal.foods.filter(
                                    (f) => !providedFoodIds.has(f.id)
                                );
                                for (const foodToDelete of foodsToDelete) {
                                    await tx
                                        .delete(nutritionPlanFood)
                                        .where(
                                            eq(
                                                nutritionPlanFood.id,
                                                foodToDelete.id
                                            )
                                        );
                                }
                            }
                        }
                    }
                }

                // Process each day
                for (const dayData of days) {
                    const { meals, ...dayInfoRaw } = dayData;

                    // Calculate day totals from meals
                    const dayTotals = meals
                        ? calculateDayTotals(meals)
                        : {
                              totalCalories: 0,
                              protein: 0,
                              carbs: 0,
                              fat: 0,
                              fiber: 0,
                          };

                    // Remove id from update data and add calculated values
                    const { id: dayIdForUpdate, ...dayInfo } = dayInfoRaw;
                    const dayInfoWithCalculated = {
                        ...dayInfo,
                        ...dayTotals,
                        updatedAt: new Date(),
                    };

                    let dayId: number;

                    if (dayData.id) {
                        // Only update if the day exists
                        if (existingDayIds.has(dayData.id)) {
                            const updatedDay = await tx
                                .update(nutritionPlanDay)
                                .set(dayInfoWithCalculated)
                                .where(eq(nutritionPlanDay.id, dayData.id))
                                .returning();
                            dayId = updatedDay[0].id;
                        } else {
                            // Skip update for non-existent day
                            continue;
                        }
                    } else {
                        // Create new day
                        const newDay = await tx
                            .insert(nutritionPlanDay)
                            .values({
                                ...dayInfoWithCalculated,
                                nutritionPlanId: id,
                            })
                            .returning();
                        dayId = newDay[0].id;
                    }

                    // Process meals for this day
                    if (meals) {
                        for (const mealData of meals) {
                            const { foods, ...mealInfoRaw } = mealData;

                            // Calculate meal totals from foods
                            const mealTotals = foods
                                ? calculateMealTotals(foods)
                                : {
                                      calories: 0,
                                      protein: 0,
                                      carbs: 0,
                                      fat: 0,
                                      fiber: 0,
                                  };

                            const { id: mealIdForUpdate, ...mealInfo } =
                                mealInfoRaw;
                            const mealInfoWithCalculated = {
                                ...mealInfo,
                                calories: mealTotals.calories,
                                protein: mealTotals.protein,
                                carbs: mealTotals.carbs,
                                fat: mealTotals.fat,
                                fiber: mealTotals.fiber,
                                updatedAt: new Date(),
                            };

                            let mealId: number;

                            if (mealData.id) {
                                // Only update if the meal exists
                                if (existingMealIds.has(mealData.id)) {
                                    const updatedMeal = await tx
                                        .update(nutritionPlanMeal)
                                        .set(mealInfoWithCalculated)
                                        .where(
                                            eq(
                                                nutritionPlanMeal.id,
                                                mealData.id
                                            )
                                        )
                                        .returning();
                                    mealId = updatedMeal[0].id;
                                } else {
                                    // Skip update for non-existent meal
                                    continue;
                                }
                            } else {
                                // Create new meal
                                const newMeal = await tx
                                    .insert(nutritionPlanMeal)
                                    .values({
                                        ...mealInfoWithCalculated,
                                        nutritionPlanDayId: dayId,
                                    })
                                    .returning();
                                mealId = newMeal[0].id;
                            }

                            // Process foods for this meal
                            if (foods) {
                                for (const foodData of foods) {
                                    const { id: foodIdForUpdate, ...foodInfo } =
                                        foodData;
                                    if (foodData.id) {
                                        // Only update if the food exists
                                        if (existingFoodIds.has(foodData.id)) {
                                            await tx
                                                .update(nutritionPlanFood)
                                                .set({
                                                    ...foodInfo,
                                                    updatedAt: new Date(),
                                                })
                                                .where(
                                                    eq(
                                                        nutritionPlanFood.id,
                                                        foodData.id
                                                    )
                                                );
                                        } else {
                                            // Skip update for non-existent food
                                            continue;
                                        }
                                    } else {
                                        // Create new food
                                        await tx
                                            .insert(nutritionPlanFood)
                                            .values({
                                                ...foodInfo,
                                                nutritionPlanMealId: mealId,
                                            });
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recalculate nutrition adherence for all users with this plan assigned
            await this.recalculateNutritionAdherenceForPlan(id, tx);

            // Return the updated plan with full details
            return await tx.query.nutritionPlan.findFirst({
                where: eq(nutritionPlan.id, id),
                with: {
                    days: {
                        with: {
                            meals: {
                                with: {
                                    foods: true,
                                },
                            },
                        },
                    },
                },
            });
        });
    }

    // Helper method to recalculate nutrition adherence for all users with a specific nutrition plan
    static async recalculateNutritionAdherenceForPlan(
        nutritionPlanId: number,
        tx: any = db
    ) {
        // Get all user nutrition plans for this nutrition plan
        const userNutritionPlans = await tx.query.userNutritionPlan.findMany({
            where: eq(userNutritionPlan.nutritionPlanId, nutritionPlanId),
        });

        // Get all nutrition adherence records for these user nutrition plans
        for (const userPlan of userNutritionPlans) {
            const adherenceRecords = await tx.query.nutritionAdherence.findMany(
                {
                    where: eq(
                        nutritionAdherence.userNutritionPlanId,
                        userPlan.id
                    ),
                }
            );

            // Recalculate each adherence record
            for (const adherence of adherenceRecords) {
                await this.updateNutritionAdherenceStats(adherence.id, tx);
            }
        }
    }
}
