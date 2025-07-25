import { db } from '@lib/db.ts';
import { plannedWorkout } from '../db/schema/tables.ts';
import { eq, and, desc } from 'drizzle-orm';

export class PlannedWorkoutService {
    // Create a new planned workout
    static async createPlannedWorkout(data: {
        userId: string;
        userWorkoutPlanId: number;
        weekdays: ('sun' | 'mon' | 'tue' | 'wed' | 'thu' | 'fri' | 'sat')[];
        time: string; // HH:MM format
        isActive?: boolean;
    }) {
        const result = await db.insert(plannedWorkout).values(data).returning();
        return result[0];
    }

    // Get planned workouts for a user
    static async getUserPlannedWorkouts(userId: string) {
        return await db.query.plannedWorkout.findMany({
            where: eq(plannedWorkout.userId, userId),
            orderBy: desc(plannedWorkout.createdAt),
            with: {
                userWorkoutPlan: {
                    with: {
                        workoutPlan: {
                            columns: {
                                name: true,
                                description: true,
                                difficulty: true,
                            },
                        },
                    },
                },
            },
        });
    }

    // Get planned workout by ID
    static async getPlannedWorkoutById(id: number) {
        return await db.query.plannedWorkout.findFirst({
            where: eq(plannedWorkout.id, id),
            with: {
                userWorkoutPlan: {
                    with: {
                        workoutPlan: {
                            columns: {
                                name: true,
                                description: true,
                                difficulty: true,
                            },
                        },
                    },
                },
            },
        });
    }

    // Update planned workout
    static async updatePlannedWorkout(
        id: number,
        data: Partial<{
            weekdays: ('sun' | 'mon' | 'tue' | 'wed' | 'thu' | 'fri' | 'sat')[];
            time: string;
            isActive: boolean;
        }>
    ) {
        const result = await db
            .update(plannedWorkout)
            .set({
                ...data,
                updatedAt: new Date(),
            })
            .where(eq(plannedWorkout.id, id))
            .returning();
        return result[0] || null;
    }

    // Delete planned workout
    static async deletePlannedWorkout(id: number) {
        const result = await db
            .delete(plannedWorkout)
            .where(eq(plannedWorkout.id, id))
            .returning();
        return result[0] || null;
    }

    // Toggle planned workout active status
    static async togglePlannedWorkout(id: number, isActive: boolean) {
        const result = await db
            .update(plannedWorkout)
            .set({
                isActive,
                updatedAt: new Date(),
            })
            .where(eq(plannedWorkout.id, id))
            .returning();
        return result[0] || null;
    }

    // Get planned workouts for a specific weekday and time range (for notifications)
    static async getPlannedWorkoutsForWeekday(
        weekday: 'sun' | 'mon' | 'tue' | 'wed' | 'thu' | 'fri' | 'sat',
        timeRange?: { start: string; end: string }
    ) {
        // Get all active planned workouts and filter in memory
        const allActive = await db.query.plannedWorkout.findMany({
            where: eq(plannedWorkout.isActive, true),
            with: {
                userWorkoutPlan: {
                    with: {
                        workoutPlan: {
                            columns: {
                                name: true,
                            },
                        },
                    },
                },
            },
        });

        // Filter by weekday in memory
        const filtered = allActive.filter(
            (pw) => pw.weekdays && pw.weekdays.includes(weekday)
        );

        // Filter by time range if provided
        if (timeRange) {
            return filtered.filter(
                (pw) => pw.time >= timeRange.start && pw.time <= timeRange.end
            );
        }

        return filtered;
    }

    // Get all planned workouts for a user for a specific weekday
    static async getUserPlannedWorkoutsForWeekday(
        userId: string,
        weekday: 'sun' | 'mon' | 'tue' | 'wed' | 'thu' | 'fri' | 'sat'
    ) {
        const userPlannedWorkouts = await db.query.plannedWorkout.findMany({
            where: and(
                eq(plannedWorkout.userId, userId),
                eq(plannedWorkout.isActive, true)
            ),
            with: {
                userWorkoutPlan: {
                    with: {
                        workoutPlan: {
                            columns: {
                                name: true,
                                description: true,
                            },
                        },
                    },
                },
            },
        });

        // Filter by weekday in memory
        return userPlannedWorkouts.filter(
            (pw) => pw.weekdays && pw.weekdays.includes(weekday)
        );
    }

    // Get upcoming planned workouts for today
    static async getTodaysPlannedWorkouts(userId: string) {
        const today = new Date();
        const dayNumber = today.getDay(); // 0 = Sunday, 1 = Monday, etc.

        // Convert JavaScript day number to weekday string
        const weekdays = [
            'sun',
            'mon',
            'tue',
            'wed',
            'thu',
            'fri',
            'sat',
        ] as const;
        const weekday = weekdays[dayNumber];

        return this.getUserPlannedWorkoutsForWeekday(userId, weekday);
    }
}
