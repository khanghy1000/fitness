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
        endDate?: Date;
    }) {
        const result = await db
            .insert(userNutritionPlan)
            .values(data)
            .returning();
        return result[0];
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

    static async createDailyAdherence(data: {
        nutritionPlanId: number;
        userId: string;
        date: Date;
        weekday: 'sun' | 'mon' | 'tue' | 'wed' | 'thu' | 'fri' | 'sat';
        totalMeals?: number;
    }) {
        // Get the userNutritionPlanId from nutritionPlanId and userId
        const userNutritionPlanId = await this.getUserNutritionPlanId(
            data.userId,
            data.nutritionPlanId
        );

        if (!userNutritionPlanId) {
            throw new Error('User nutrition plan not found');
        }

        const result = await db
            .insert(nutritionAdherence)
            .values({
                userNutritionPlanId,
                userId: data.userId,
                date: data.date,
                weekday: data.weekday,
                totalMeals: data.totalMeals || 0,
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

    static async getDailyAdherence(
        userId: string,
        nutritionPlanId: number,
        date: Date
    ) {
        // Get the userNutritionPlanId from nutritionPlanId and userId
        const userNutritionPlanId = await this.getUserNutritionPlanId(
            userId,
            nutritionPlanId
        );

        if (!userNutritionPlanId) {
            return null;
        }

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
                    eq(nutritionAdherence.date, startDate)
                )
            );
    }

    // Get user adherence history by nutrition plan ID
    static async getUserAdherenceHistoryByPlan(
        userId: string,
        nutritionPlanId: number,
        startDate?: Date,
        endDate?: Date
    ) {
        const userNutritionPlanId = await this.getUserNutritionPlanId(
            userId,
            nutritionPlanId
        );

        if (!userNutritionPlanId) {
            return [];
        }

        const conditions = [
            eq(nutritionAdherence.userNutritionPlanId, userNutritionPlanId),
            eq(nutritionAdherence.userId, userId),
        ];

        if (startDate) {
            conditions.push(sql`${nutritionAdherence.date} >= ${startDate}`);
        }
        if (endDate) {
            conditions.push(sql`${nutritionAdherence.date} <= ${endDate}`);
        }

        return await db
            .select()
            .from(nutritionAdherence)
            .where(and(...conditions))
            .orderBy(desc(nutritionAdherence.date));
    }

    // Nutrition Plan Day methods
    static async getNutritionPlanDays(nutritionPlanId: number) {
        return await db.query.nutritionPlanDay.findMany({
            where: eq(nutritionPlanDay.nutritionPlanId, nutritionPlanId),
            with: {
                meals: {
                    with: {
                        foods: true,
                    },
                },
            },
        });
    }

    static async getNutritionPlanDayById(id: number) {
        return await db.query.nutritionPlanDay.findFirst({
            where: eq(nutritionPlanDay.id, id),
            with: {
                meals: {
                    with: {
                        foods: true,
                    },
                },
            },
        });
    }

    static async createNutritionPlanDay(data: {
        nutritionPlanId: number;
        weekday: 'sun' | 'mon' | 'tue' | 'wed' | 'thu' | 'fri' | 'sat';
        totalCalories?: number;
        protein?: number;
        carbs?: number;
        fat?: number;
        fiber?: number;
    }) {
        const result = await db
            .insert(nutritionPlanDay)
            .values(data)
            .returning();
        return result[0];
    }

    static async updateNutritionPlanDay(
        id: number,
        data: {
            weekday?: 'sun' | 'mon' | 'tue' | 'wed' | 'thu' | 'fri' | 'sat';
            totalCalories?: number;
            protein?: number;
            carbs?: number;
            fat?: number;
            fiber?: number;
        }
    ) {
        const result = await db
            .update(nutritionPlanDay)
            .set({ ...data, updatedAt: new Date() })
            .where(eq(nutritionPlanDay.id, id))
            .returning();
        return result[0] || null;
    }

    static async deleteNutritionPlanDay(id: number) {
        const result = await db
            .delete(nutritionPlanDay)
            .where(eq(nutritionPlanDay.id, id))
            .returning();
        return result[0] || null;
    }

    // Nutrition Plan Meal methods
    static async getNutritionPlanMeals(nutritionPlanDayId: number) {
        return await db.query.nutritionPlanMeal.findMany({
            where: eq(nutritionPlanMeal.nutritionPlanDayId, nutritionPlanDayId),
            with: {
                foods: true,
            },
        });
    }

    static async getNutritionPlanMealById(id: number) {
        return await db.query.nutritionPlanMeal.findFirst({
            where: eq(nutritionPlanMeal.id, id),
            with: {
                foods: true,
            },
        });
    }

    static async createNutritionPlanMeal(data: {
        nutritionPlanDayId: number;
        name: string;
        time: string;
        calories?: number;
        protein?: number;
        carbs?: number;
        fat?: number;
        fiber?: number;
    }) {
        const result = await db
            .insert(nutritionPlanMeal)
            .values(data)
            .returning();
        return result[0];
    }

    static async updateNutritionPlanMeal(
        id: number,
        data: {
            name?: string;
            time?: string;
            calories?: number;
            protein?: number;
            carbs?: number;
            fat?: number;
            fiber?: number;
        }
    ) {
        const result = await db
            .update(nutritionPlanMeal)
            .set({ ...data, updatedAt: new Date() })
            .where(eq(nutritionPlanMeal.id, id))
            .returning();
        return result[0] || null;
    }

    static async deleteNutritionPlanMeal(id: number) {
        const result = await db
            .delete(nutritionPlanMeal)
            .where(eq(nutritionPlanMeal.id, id))
            .returning();
        return result[0] || null;
    }

    // Nutrition Plan Food methods
    static async getNutritionPlanFoods(nutritionPlanMealId: number) {
        return await db
            .select()
            .from(nutritionPlanFood)
            .where(
                eq(nutritionPlanFood.nutritionPlanMealId, nutritionPlanMealId)
            );
    }

    static async getNutritionPlanFoodById(id: number) {
        const result = await db
            .select()
            .from(nutritionPlanFood)
            .where(eq(nutritionPlanFood.id, id));
        return result[0] || null;
    }

    static async createNutritionPlanFood(data: {
        nutritionPlanMealId: number;
        name: string;
        quantity: string;
        calories: number;
        protein?: number;
        carbs?: number;
        fat?: number;
        fiber?: number;
    }) {
        const result = await db
            .insert(nutritionPlanFood)
            .values(data)
            .returning();
        return result[0];
    }

    static async updateNutritionPlanFood(
        id: number,
        data: {
            name?: string;
            quantity?: string;
            calories?: number;
            protein?: number;
            carbs?: number;
            fat?: number;
            fiber?: number;
        }
    ) {
        const result = await db
            .update(nutritionPlanFood)
            .set({ ...data, updatedAt: new Date() })
            .where(eq(nutritionPlanFood.id, id))
            .returning();
        return result[0] || null;
    }

    static async deleteNutritionPlanFood(id: number) {
        const result = await db
            .delete(nutritionPlanFood)
            .where(eq(nutritionPlanFood.id, id))
            .returning();
        return result[0] || null;
    }
}
