import 'dotenv/config';
import { db } from '../src/lib/db.js';
import { user, userStats } from '../src/db/schema/tables.js';
import { eq } from 'drizzle-orm';

async function getUserIdByEmail(email: string): Promise<string> {
    const result = await db
        .select({ id: user.id })
        .from(user)
        .where(eq(user.email, email));
    if (!result.length) throw new Error(`User with email ${email} not found`);
    return result[0].id;
}

function getStatsForDay(dayOffset: number) {
    // Simulate some realistic but slightly varying stats
    const baseWeight = 70; // kg
    const baseBodyFat = 18; // %
    const baseMuscle = 35; // kg
    const baseHeight = 175; // cm
    const baseChest = 95; // cm
    const baseWaist = 80; // cm
    const baseHips = 95; // cm
    const baseArms = 32; // cm
    const baseThighs = 55; // cm
    const weight = baseWeight + dayOffset * 0.1;
    const bodyFat = baseBodyFat - dayOffset * 0.05;
    const muscleMass = baseMuscle + dayOffset * 0.08;
    const height = baseHeight;
    const chest = baseChest + dayOffset * 0.05;
    const waist = baseWaist - dayOffset * 0.03;
    const hips = baseHips + dayOffset * 0.02;
    const arms = baseArms + dayOffset * 0.01;
    const thighs = baseThighs + dayOffset * 0.02;
    const bmi = weight / (height / 100) ** 2;
    const recordedAt = new Date();
    recordedAt.setDate(recordedAt.getDate() - dayOffset);
    return {
        weight,
        height,
        bodyFat,
        muscleMass,
        bmi,
        chest,
        waist,
        hips,
        arms,
        thighs,
        recordedAt,
        notes: `Auto-generated stat for day ${7 - dayOffset}`,
    };
}

export async function seedUserBodyStats() {
    try {
        console.log('Starting to seed user body stats...');
        const email = 'trainee@a.com';
        const userId = await getUserIdByEmail(email);
        const statsRecords = [];
        for (let i = 6; i >= 0; i--) {
            const stats = getStatsForDay(i);
            statsRecords.push({ ...stats, userId, recordedBy: userId });
        }
        await db.insert(userStats).values(statsRecords);
        console.log('✅ Seeded 7 days of body stats for', email);
        // process.exit(0); // Removed to allow main script to continue
    } catch (error) {
        console.error('❌ Error seeding user body stats:', error);
        // process.exit(1); // Removed to allow main script to continue
    }
}
