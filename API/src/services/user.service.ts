import { db } from '@lib/db.ts';
import { userStats, user } from '../db/schema/tables.ts';
import { eq, desc, and, sql } from 'drizzle-orm';

export class UserService {
    // static async createUserGoal(data: {
    //     userId: string;
    //     type:
    //         | 'weight_loss'
    //         | 'weight_gain'
    //         | 'muscle_gain'
    //         | 'endurance'
    //         | 'strength'
    //         | 'flexibility'
    //         | 'custom';
    //     title: string;
    //     description?: string;
    //     targetValue?: number;
    //     unit?: string;
    //     targetDate?: Date;
    //     priority?: 'low' | 'medium' | 'high';
    // }) {
    //     const result = await db.insert(userGoal).values(data).returning();
    //     return result[0];
    // }

    // static async getUserGoals(userId: string) {
    //     return await db
    //         .select()
    //         .from(userGoal)
    //         .where(eq(userGoal.userId, userId))
    //         .orderBy(desc(userGoal.createdAt));
    // }

    // static async updateUserGoal(
    //     id: number,
    //     data: {
    //         title?: string;
    //         description?: string;
    //         targetValue?: number;
    //         currentValue?: number;
    //         unit?: string;
    //         targetDate?: Date;
    //         status?: 'active' | 'completed' | 'paused' | 'cancelled';
    //         priority?: 'low' | 'medium' | 'high';
    //     }
    // ) {
    //     const result = await db
    //         .update(userGoal)
    //         .set({ ...data, updatedAt: new Date() })
    //         .where(eq(userGoal.id, id))
    //         .returning();
    //     return result[0] || null;
    // }

    static async recordUserStats(data: {
        userId: string;
        weight?: number;
        height?: number;
        bodyFat?: number;
        muscleMass?: number;
        bmi?: number;
        chest?: number;
        waist?: number;
        hips?: number;
        arms?: number;
        thighs?: number;
        recordedBy: string;
        recordedAt: Date;
        notes?: string;
    }) {
        // Calculate BMI if weight and height are provided
        if (data.weight && data.height && !data.bmi) {
            const heightInMeters = data.height / 100;
            data.bmi = data.weight / (heightInMeters * heightInMeters);
        }

        const result = await db.insert(userStats).values(data).returning();
        return result[0];
    }

    static async getUserStats(userId: string) {
        return await db
            .select()
            .from(userStats)
            .where(eq(userStats.userId, userId))
            .orderBy(desc(userStats.recordedAt));
    }

    static async getLatestUserStats(userId: string) {
        const stats = await db
            .select()
            .from(userStats)
            .where(eq(userStats.userId, userId))
            .orderBy(desc(userStats.recordedAt))
            .limit(1);
        return stats[0] || null;
    }

    static async searchUsers(query: string, role?: 'coach' | 'trainee') {
        const conditions = [];

        // Search by name or email
        conditions.push(
            sql`${user.name} ILIKE ${`%${query}%`} OR ${user.email} ILIKE ${`%${query}%`}`
        );

        // Filter by role if specified
        if (role) {
            conditions.push(eq(user.role, role));
        }

        return await db
            .select({
                id: user.id,
                name: user.name,
                email: user.email,
                image: user.image,
                role: user.role,
            })
            .from(user)
            .where(and(...conditions))
            .limit(20);
    }
}
