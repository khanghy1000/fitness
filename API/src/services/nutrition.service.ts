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
import { eq, and, inArray } from 'drizzle-orm';

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
        const plans = await db
            .select()
            .from(nutritionPlan)
            .where(eq(nutritionPlan.id, id));
        return plans[0] || null;
    }

    static async getNutritionPlanWithDetails(id: number) {
        // Get the nutrition plan
        const plan = await db
            .select()
            .from(nutritionPlan)
            .where(eq(nutritionPlan.id, id));

        if (!plan[0]) return null;

        // Get all days for this plan
        const days = await db
            .select()
            .from(nutritionPlanDay)
            .where(eq(nutritionPlanDay.nutritionPlanId, id));

        // Get all meals for these days
        const dayIds = days.map((d) => d.id);
        const meals =
            dayIds.length > 0
                ? await db
                      .select()
                      .from(nutritionPlanMeal)
                      .where(
                          inArray(nutritionPlanMeal.nutritionPlanDayId, dayIds)
                      )
                : [];

        // Get all foods for these meals
        const mealIds = meals.map((m) => m.id);
        const foods =
            mealIds.length > 0
                ? await db
                      .select()
                      .from(nutritionPlanFood)
                      .where(
                          inArray(
                              nutritionPlanFood.nutritionPlanMealId,
                              mealIds
                          )
                      )
                : [];

        return {
            ...plan[0],
            days: days.map((day) => ({
                ...day,
                meals: meals
                    .filter((meal) => meal.nutritionPlanDayId === day.id)
                    .map((meal) => ({
                        ...meal,
                        foods: foods.filter(
                            (food) => food.nutritionPlanMealId === meal.id
                        ),
                    })),
            })),
        };
    }

    static async createNutritionPlan(data: {
        name: string;
        description?: string;
        createdBy: string;
        weekdayPlans: WeekdayPlanType[];
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

            // Create nutrition plan days, meals, and foods
            for (const weekdayPlan of data.weekdayPlans) {
                const [planDay] = await tx
                    .insert(nutritionPlanDay)
                    .values({
                        nutritionPlanId: plan.id,
                        weekday: weekdayPlan.weekday,
                        totalCalories: weekdayPlan.totalCalories,
                        protein: weekdayPlan.protein,
                        carbs: weekdayPlan.carbs,
                        fat: weekdayPlan.fat,
                        fiber: weekdayPlan.fiber,
                    })
                    .returning();

                for (const meal of weekdayPlan.meals) {
                    const [planMeal] = await tx
                        .insert(nutritionPlanMeal)
                        .values({
                            nutritionPlanDayId: planDay.id,
                            name: meal.name,
                            time: meal.time,
                            calories: meal.calories,
                            protein: meal.protein,
                            carbs: meal.carbs,
                            fat: meal.fat,
                            fiber: meal.fiber,
                        })
                        .returning();

                    if (meal.foods && meal.foods.length > 0) {
                        const foodsData = meal.foods.map((food) => ({
                            nutritionPlanMealId: planMeal.id,
                            name: food.name,
                            quantity: food.quantity,
                            calories: food.calories,
                            protein: food.protein,
                            carbs: food.carbs,
                            fat: food.fat,
                            fiber: food.fiber,
                        }));

                        await tx.insert(nutritionPlanFood).values(foodsData);
                    }
                }
            }

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
            weekdayPlans?: WeekdayPlanType[];
        }
    ) {
        return await db.transaction(async (tx) => {
            // Update the main nutrition plan
            const [plan] = await tx
                .update(nutritionPlan)
                .set({
                    name: data.name,
                    description: data.description,
                    isActive: data.isActive,
                    updatedAt: new Date(),
                })
                .where(eq(nutritionPlan.id, id))
                .returning();

            if (!plan) return null;

            // If weekdayPlans are provided, update the structure
            if (data.weekdayPlans) {
                // Delete existing days (cascade will handle meals and foods)
                await tx
                    .delete(nutritionPlanDay)
                    .where(eq(nutritionPlanDay.nutritionPlanId, id));

                // Recreate the structure
                for (const weekdayPlan of data.weekdayPlans) {
                    const [planDay] = await tx
                        .insert(nutritionPlanDay)
                        .values({
                            nutritionPlanId: plan.id,
                            weekday: weekdayPlan.weekday,
                            totalCalories: weekdayPlan.totalCalories,
                            protein: weekdayPlan.protein,
                            carbs: weekdayPlan.carbs,
                            fat: weekdayPlan.fat,
                            fiber: weekdayPlan.fiber,
                        })
                        .returning();

                    for (const meal of weekdayPlan.meals) {
                        const [planMeal] = await tx
                            .insert(nutritionPlanMeal)
                            .values({
                                nutritionPlanDayId: planDay.id,
                                name: meal.name,
                                time: meal.time,
                                calories: meal.calories,
                                protein: meal.protein,
                                carbs: meal.carbs,
                                fat: meal.fat,
                                fiber: meal.fiber,
                            })
                            .returning();

                        if (meal.foods && meal.foods.length > 0) {
                            const foodsData = meal.foods.map((food) => ({
                                nutritionPlanMealId: planMeal.id,
                                name: food.name,
                                quantity: food.quantity,
                                calories: food.calories,
                                protein: food.protein,
                                carbs: food.carbs,
                                fat: food.fat,
                                fiber: food.fiber,
                            }));

                            await tx
                                .insert(nutritionPlanFood)
                                .values(foodsData);
                        }
                    }
                }
            }

            return plan;
        });
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
        endDate?: Date;
    }) {
        const result = await db
            .insert(userNutritionPlan)
            .values(data)
            .returning();
        return result[0];
    }

    static async getUserNutritionPlans(userId: string) {
        // Get user nutrition plans
        const userPlans = await db
            .select()
            .from(userNutritionPlan)
            .where(eq(userNutritionPlan.userId, userId));

        // Get the detailed plans and assigned by info
        const result = [];
        for (const userPlan of userPlans) {
            if (!userPlan.nutritionPlanId) continue;

            const plan = await this.getNutritionPlanById(
                userPlan.nutritionPlanId
            );
            const assignedBy = userPlan.assignedBy
                ? await db
                      .select({
                          id: user.id,
                          name: user.name,
                          image: user.image,
                      })
                      .from(user)
                      .where(eq(user.id, userPlan.assignedBy))
                : null;

            result.push({
                ...userPlan,
                nutritionPlan: plan,
                assignedBy: assignedBy?.[0] || null,
            });
        }

        return result;
    }

    // Adherence tracking methods
    static async createDailyAdherence(data: {
        userNutritionPlanId: number;
        userId: string;
        date: Date;
        weekday: 'sun' | 'mon' | 'tue' | 'wed' | 'thu' | 'fri' | 'sat';
        totalMeals: number;
    }) {
        const result = await db
            .insert(nutritionAdherence)
            .values({
                ...data,
                mealsCompleted: 0,
                adherencePercentage: 0,
                totalCaloriesConsumed: 0,
                totalCaloriesPlanned: 0,
            })
            .returning();
        return result[0];
    }

    static async updateDailyAdherence(
        id: number,
        data: {
            mealsCompleted?: number;
            totalMeals?: number;
            adherencePercentage?: number;
            totalCaloriesConsumed?: number;
            totalCaloriesPlanned?: number;
            notes?: string;
        }
    ) {
        const result = await db
            .update(nutritionAdherence)
            .set({ ...data, updatedAt: new Date() })
            .where(eq(nutritionAdherence.id, id))
            .returning();
        return result[0] || null;
    }

    static async getDailyAdherence(userNutritionPlanId: number, date: Date) {
        const result = await db
            .select()
            .from(nutritionAdherence)
            .where(
                and(
                    eq(
                        nutritionAdherence.userNutritionPlanId,
                        userNutritionPlanId
                    ),
                    eq(nutritionAdherence.date, date)
                )
            );
        return result[0] || null;
    }

    static async completeMeal(data: {
        nutritionAdherenceId: number;
        nutritionPlanMealId: number;
        userId: string;
        caloriesConsumed?: number;
        proteinConsumed?: number;
        carbsConsumed?: number;
        fatConsumed?: number;
        fiberConsumed?: number;
        notes?: string;
    }) {
        const result = await db
            .insert(mealCompletion)
            .values({
                ...data,
                isCompleted: true,
                completedAt: new Date(),
            })
            .returning();
        return result[0];
    }

    static async getMealCompletions(nutritionAdherenceId: number) {
        return await db
            .select()
            .from(mealCompletion)
            .where(
                eq(mealCompletion.nutritionAdherenceId, nutritionAdherenceId)
            );
    }

    static async getUserAdherenceHistory(
        userId: string,
        startDate: Date,
        endDate: Date
    ) {
        return await db
            .select()
            .from(nutritionAdherence)
            .where(
                and(
                    eq(nutritionAdherence.userId, userId),
                    eq(nutritionAdherence.date, startDate) // This needs proper date range query
                )
            );
    }
}
