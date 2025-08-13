import 'dotenv/config';
import { db } from '../src/lib/db.js';
import {
    workoutPlan,
    workoutPlanDay,
    workoutPlanDayExercise,
    exerciseType,
    user,
} from '../src/db/schema/tables.js';

import { eq } from 'drizzle-orm';

async function getCoachIdByEmail(email: string): Promise<string> {
    const coach = await db.select().from(user).where(eq(user.email, email));
    if (!coach.length) throw new Error(`Coach with email ${email} not found`);
    return coach[0].id;
}

async function getExerciseTypeIds(): Promise<
    Array<{ id: number; name: string }>
> {
    const types = await db
        .select({ id: exerciseType.id, name: exerciseType.name })
        .from(exerciseType);
    if (!types.length) throw new Error('No exercise types found');
    return types;
}

export async function seedWorkoutPlans() {
    try {
        console.log('Starting to seed workout plans...');
        const coachEmail = 'coach@a.com';
        const coachId = await getCoachIdByEmail(coachEmail);
        const exerciseTypes = await getExerciseTypeIds();

        // Helper to pick exerciseTypeId by name
        const getTypeId = (name: string): number => {
            const found = exerciseTypes.find((t) => t.name === name);
            if (!found) throw new Error(`Exercise type not found: ${name}`);
            return found.id;
        };

        const plans: Array<{
            name: string;
            description: string;
            difficulty: 'beginner' | 'intermediate' | 'advanced';
            estimatedCalories: number;
            days: Array<{
                day: number;
                isRestDay: boolean;
                exercises: Array<{
                    name: string;
                    reps?: number;
                    duration?: number;
                }>;
            }>;
        }> = [
            {
                name: 'Beginner Full Body',
                description: 'A simple 3-day full body plan for beginners.',
                difficulty: 'beginner',
                estimatedCalories: 1200,
                days: [
                    {
                        day: 1,
                        isRestDay: false,
                        exercises: [
                            { name: 'Squat', reps: 12 },
                            { name: 'Lunge', reps: 10 },
                            { name: 'Sit-up', reps: 15 },
                        ],
                    },
                    {
                        day: 2,
                        isRestDay: true,
                        exercises: [],
                    },
                    {
                        day: 3,
                        isRestDay: false,
                        exercises: [
                            { name: 'Plank', duration: 60 },
                            { name: 'Cobra', duration: 45 },
                        ],
                    },
                ],
            },
            {
                name: 'Intermediate Strength',
                description: 'A 4-day split for building strength.',
                difficulty: 'intermediate',
                estimatedCalories: 1800,
                days: [
                    {
                        day: 1,
                        isRestDay: false,
                        exercises: [
                            { name: 'Squat', reps: 15 },
                            { name: 'Plank', duration: 90 },
                        ],
                    },
                    {
                        day: 2,
                        isRestDay: false,
                        exercises: [
                            { name: 'Lunge', reps: 12 },
                            { name: 'Sit-up', reps: 20 },
                        ],
                    },
                    {
                        day: 3,
                        isRestDay: true,
                        exercises: [],
                    },
                    {
                        day: 4,
                        isRestDay: false,
                        exercises: [{ name: 'Cobra', duration: 60 }],
                    },
                ],
            },
            {
                name: 'Advanced Fat Burn',
                description: 'High intensity 5-day plan for advanced trainees.',
                difficulty: 'advanced',
                estimatedCalories: 2500,
                days: [
                    {
                        day: 1,
                        isRestDay: false,
                        exercises: [
                            { name: 'Squat', reps: 20 },
                            { name: 'Plank', duration: 120 },
                        ],
                    },
                    {
                        day: 2,
                        isRestDay: false,
                        exercises: [
                            { name: 'Lunge', reps: 20 },
                            { name: 'Sit-up', reps: 30 },
                        ],
                    },
                    {
                        day: 3,
                        isRestDay: false,
                        exercises: [{ name: 'Cobra', duration: 90 }],
                    },
                    {
                        day: 4,
                        isRestDay: true,
                        exercises: [],
                    },
                    {
                        day: 5,
                        isRestDay: false,
                        exercises: [
                            { name: 'Squat', reps: 25 },
                            { name: 'Plank', duration: 150 },
                        ],
                    },
                ],
            },
        ];

        for (const plan of plans) {
            const [planRow] = await db
                .insert(workoutPlan)
                .values({
                    name: plan.name,
                    description: plan.description,
                    difficulty: plan.difficulty,
                    estimatedCalories: plan.estimatedCalories,
                    createdBy: coachId,
                })
                .returning();
            console.log(`Created workout plan: ${plan.name}`);

            for (const day of plan.days) {
                const [dayRow] = await db
                    .insert(workoutPlanDay)
                    .values({
                        workoutPlanId: planRow.id,
                        day: day.day,
                        isRestDay: day.isRestDay,
                    })
                    .returning();
                if (day.isRestDay) {
                    console.log(`  Day ${day.day}: Rest Day`);
                    continue;
                }
                for (let i = 0; i < day.exercises.length; i++) {
                    const ex = day.exercises[i];
                    await db.insert(workoutPlanDayExercise).values({
                        workoutPlanDayId: dayRow.id,
                        exerciseTypeId: getTypeId(ex.name),
                        order: i + 1,
                        targetReps: ex.reps,
                        targetDuration: ex.duration,
                    });
                }
                console.log(
                    `  Day ${day.day}: Added ${day.exercises.length} exercises`
                );
            }
        }
        console.log('✅ Workout plans seeding completed successfully!');
    } catch (error) {
        console.error('❌ Error seeding workout plans:', error);
        // process.exit(1); // Removed to allow main script to continue
    }
}
