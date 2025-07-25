import 'dotenv/config';
import { db } from '../src/lib/db.js';
import {
    nutritionPlan,
    nutritionPlanDay,
    nutritionPlanMeal,
    nutritionPlanFood,
    user,
} from '../src/db/schema/tables.js';

async function seedNutritionPlans() {
    try {
        console.log('Starting to seed nutrition plans...');

        // First, let's create a sample nutrition plan
        const [plan] = await db
            .insert(nutritionPlan)
            .values({
                name: 'Balanced Weekly Meal Plan',
                description:
                    'A balanced nutrition plan for general fitness with meals for all 7 days',
                createdBy: 'sample-coach-id', // You'll need to replace this with an actual coach ID
            })
            .returning();

        console.log(`Created nutrition plan: ${plan.name}`);

        // Sample meal structure for each weekday
        const weekdayMeals = [
            // Sunday
            {
                weekday: 'sun' as const,
                totalCalories: 2000,
                protein: 120,
                carbs: 250,
                fat: 65,
                fiber: 25,
                meals: [
                    {
                        name: 'Breakfast',
                        time: '08:00:00',
                        calories: 500,
                        protein: 25,
                        carbs: 60,
                        fat: 20,
                        foods: [
                            {
                                name: 'Oatmeal',
                                quantity: '1 cup',
                                calories: 150,
                                protein: 5,
                                carbs: 27,
                                fat: 3,
                            },
                            {
                                name: 'Banana',
                                quantity: '1 medium',
                                calories: 105,
                                protein: 1,
                                carbs: 27,
                                fat: 0,
                            },
                            {
                                name: 'Almond Milk',
                                quantity: '1 cup',
                                calories: 60,
                                protein: 1,
                                carbs: 8,
                                fat: 3,
                            },
                            {
                                name: 'Almonds',
                                quantity: '1 oz',
                                calories: 160,
                                protein: 6,
                                carbs: 6,
                                fat: 14,
                            },
                        ],
                    },
                    {
                        name: 'Lunch',
                        time: '12:30:00',
                        calories: 600,
                        protein: 35,
                        carbs: 70,
                        fat: 20,
                        foods: [
                            {
                                name: 'Grilled Chicken Breast',
                                quantity: '4 oz',
                                calories: 185,
                                protein: 35,
                                carbs: 0,
                                fat: 4,
                            },
                            {
                                name: 'Brown Rice',
                                quantity: '1 cup',
                                calories: 220,
                                protein: 5,
                                carbs: 45,
                                fat: 2,
                            },
                            {
                                name: 'Mixed Vegetables',
                                quantity: '1 cup',
                                calories: 50,
                                protein: 2,
                                carbs: 10,
                                fat: 0,
                            },
                            {
                                name: 'Olive Oil',
                                quantity: '1 tbsp',
                                calories: 120,
                                protein: 0,
                                carbs: 0,
                                fat: 14,
                            },
                        ],
                    },
                    {
                        name: 'Snack',
                        time: '15:30:00',
                        calories: 200,
                        protein: 15,
                        carbs: 20,
                        fat: 8,
                        foods: [
                            {
                                name: 'Greek Yogurt',
                                quantity: '1 cup',
                                calories: 130,
                                protein: 15,
                                carbs: 20,
                                fat: 0,
                            },
                            {
                                name: 'Walnuts',
                                quantity: '0.5 oz',
                                calories: 90,
                                protein: 2,
                                carbs: 2,
                                fat: 8,
                            },
                        ],
                    },
                    {
                        name: 'Dinner',
                        time: '19:00:00',
                        calories: 700,
                        protein: 45,
                        carbs: 80,
                        fat: 25,
                        foods: [
                            {
                                name: 'Salmon Fillet',
                                quantity: '5 oz',
                                calories: 280,
                                protein: 40,
                                carbs: 0,
                                fat: 12,
                            },
                            {
                                name: 'Sweet Potato',
                                quantity: '1 medium',
                                calories: 180,
                                protein: 4,
                                carbs: 41,
                                fat: 0,
                            },
                            {
                                name: 'Broccoli',
                                quantity: '1 cup',
                                calories: 55,
                                protein: 4,
                                carbs: 11,
                                fat: 1,
                            },
                            {
                                name: 'Avocado',
                                quantity: '0.5 medium',
                                calories: 120,
                                protein: 2,
                                carbs: 6,
                                fat: 11,
                            },
                        ],
                    },
                ],
            },
            // Monday
            {
                weekday: 'mon' as const,
                totalCalories: 2100,
                protein: 125,
                carbs: 260,
                fat: 70,
                fiber: 28,
                meals: [
                    {
                        name: 'Breakfast',
                        time: '07:30:00',
                        calories: 550,
                        protein: 30,
                        carbs: 65,
                        fat: 18,
                        foods: [
                            {
                                name: 'Scrambled Eggs',
                                quantity: '3 large',
                                calories: 210,
                                protein: 18,
                                carbs: 2,
                                fat: 15,
                            },
                            {
                                name: 'Whole Grain Toast',
                                quantity: '2 slices',
                                calories: 160,
                                protein: 8,
                                carbs: 30,
                                fat: 2,
                            },
                            {
                                name: 'Spinach',
                                quantity: '1 cup',
                                calories: 7,
                                protein: 1,
                                carbs: 1,
                                fat: 0,
                            },
                            {
                                name: 'Orange Juice',
                                quantity: '1 cup',
                                calories: 110,
                                protein: 2,
                                carbs: 26,
                                fat: 0,
                            },
                        ],
                    },
                    {
                        name: 'Lunch',
                        time: '12:00:00',
                        calories: 650,
                        protein: 40,
                        carbs: 75,
                        fat: 22,
                        foods: [
                            {
                                name: 'Turkey Sandwich',
                                quantity: '1 sandwich',
                                calories: 350,
                                protein: 25,
                                carbs: 35,
                                fat: 12,
                            },
                            {
                                name: 'Apple',
                                quantity: '1 medium',
                                calories: 95,
                                protein: 0,
                                carbs: 25,
                                fat: 0,
                            },
                            {
                                name: 'Hummus',
                                quantity: '2 tbsp',
                                calories: 70,
                                protein: 3,
                                carbs: 6,
                                fat: 5,
                            },
                            {
                                name: 'Baby Carrots',
                                quantity: '1 cup',
                                calories: 50,
                                protein: 1,
                                carbs: 12,
                                fat: 0,
                            },
                        ],
                    },
                    {
                        name: 'Snack',
                        time: '16:00:00',
                        calories: 180,
                        protein: 12,
                        carbs: 25,
                        fat: 6,
                        foods: [
                            {
                                name: 'Protein Smoothie',
                                quantity: '1 cup',
                                calories: 180,
                                protein: 12,
                                carbs: 25,
                                fat: 6,
                            },
                        ],
                    },
                    {
                        name: 'Dinner',
                        time: '18:30:00',
                        calories: 720,
                        protein: 43,
                        carbs: 85,
                        fat: 24,
                        foods: [
                            {
                                name: 'Lean Beef',
                                quantity: '4 oz',
                                calories: 220,
                                protein: 32,
                                carbs: 0,
                                fat: 10,
                            },
                            {
                                name: 'Quinoa',
                                quantity: '1 cup',
                                calories: 220,
                                protein: 8,
                                carbs: 39,
                                fat: 4,
                            },
                            {
                                name: 'Roasted Vegetables',
                                quantity: '1.5 cups',
                                calories: 120,
                                protein: 3,
                                carbs: 25,
                                fat: 3,
                            },
                            {
                                name: 'Olive Oil',
                                quantity: '1 tbsp',
                                calories: 120,
                                protein: 0,
                                carbs: 0,
                                fat: 14,
                            },
                        ],
                    },
                ],
            },
        ];

        // Insert the weekday plans
        for (const weekdayData of weekdayMeals) {
            const [planDay] = await db
                .insert(nutritionPlanDay)
                .values({
                    nutritionPlanId: plan.id,
                    weekday: weekdayData.weekday,
                    totalCalories: weekdayData.totalCalories,
                    protein: weekdayData.protein,
                    carbs: weekdayData.carbs,
                    fat: weekdayData.fat,
                    fiber: weekdayData.fiber,
                })
                .returning();

            console.log(`Created plan day for weekday ${weekdayData.weekday}`);

            // Insert meals for this day
            for (const mealData of weekdayData.meals) {
                const [planMeal] = await db
                    .insert(nutritionPlanMeal)
                    .values({
                        nutritionPlanDayId: planDay.id,
                        name: mealData.name,
                        time: mealData.time,
                        calories: mealData.calories,
                        protein: mealData.protein,
                        carbs: mealData.carbs,
                        fat: mealData.fat,
                    })
                    .returning();

                console.log(`  Created meal: ${mealData.name}`);

                // Insert foods for this meal
                for (const foodData of mealData.foods) {
                    await db.insert(nutritionPlanFood).values({
                        nutritionPlanMealId: planMeal.id,
                        name: foodData.name,
                        quantity: foodData.quantity,
                        calories: foodData.calories,
                        protein: foodData.protein,
                        carbs: foodData.carbs,
                        fat: foodData.fat,
                    });
                }

                console.log(
                    `    Added ${mealData.foods.length} foods to ${mealData.name}`
                );
            }
        }

        console.log('✅ Nutrition plan seeding completed successfully!');
        console.log(`Created plan with ID: ${plan.id}`);
    } catch (error) {
        console.error('❌ Error seeding nutrition plans:', error);
        throw error;
    }
}

// Run the seeding
seedNutritionPlans()
    .then(() => {
        console.log('Seeding completed');
        process.exit(0);
    })
    .catch((error) => {
        console.error('Seeding failed:', error);
        process.exit(1);
    });
