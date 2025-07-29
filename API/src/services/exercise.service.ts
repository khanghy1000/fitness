import { db } from '@lib/db.ts';
import { exerciseType } from '../db/schema/tables.ts';
import { eq } from 'drizzle-orm';

export class ExerciseService {
    static async getAllExerciseTypes() {
        return await db.select().from(exerciseType);
    }

    static async getExerciseTypeById(id: number) {
        const exerciseTypes = await db
            .select()
            .from(exerciseType)
            .where(eq(exerciseType.id, id));
        return exerciseTypes[0] || null;
    }

    static async getExerciseTypeByName(name: string) {
        const exerciseTypes = await db
            .select()
            .from(exerciseType)
            .where(eq(exerciseType.name, name));
        return exerciseTypes[0] || null;
    }
}
