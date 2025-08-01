import { db } from '@lib/db.ts';
import { coachTrainee } from '../db/schema/tables.ts';
import { eq, and, or } from 'drizzle-orm';

export class CoachTraineeService {
    static async sendConnectionRequest(
        coachId: string,
        traineeId: string,
        notes?: string
    ) {
        const result = await db
            .insert(coachTrainee)
            .values({
                coachId,
                traineeId,
                status: 'pending',
                notes,
            })
            .returning();
        return result[0];
    }

    static async getConnectionRequests(
        userId: string,
        type: 'sent' | 'received'
    ) {
        const condition =
            type === 'sent'
                ? eq(coachTrainee.traineeId, userId)
                : eq(coachTrainee.coachId, userId);

        return await db.query.coachTrainee.findMany({
            where: and(condition, eq(coachTrainee.status, 'pending')),
            with: {
                coach: {
                    columns: {
                        id: true,
                        name: true,
                        email: true,
                    },
                },
                trainee: {
                    columns: {
                        id: true,
                        name: true,
                        email: true,
                    },
                },
            },
        });
    }

    static async acceptConnectionRequest(coachId: string, traineeId: string) {
        const result = await db
            .update(coachTrainee)
            .set({
                status: 'active',
                startDate: new Date(),
                updatedAt: new Date(),
            })
            .where(
                and(
                    eq(coachTrainee.coachId, coachId),
                    eq(coachTrainee.traineeId, traineeId)
                )
            )
            .returning();
        return result[0] || null;
    }

    static async rejectConnectionRequest(coachId: string, traineeId: string) {
        const result = await db
            .delete(coachTrainee)
            .where(
                and(
                    eq(coachTrainee.coachId, coachId),
                    eq(coachTrainee.traineeId, traineeId)
                )
            )
            .returning();
        return result[0] || null;
    }

    static async getActiveConnections(
        userId: string,
        userRole: 'coach' | 'trainee'
    ) {
        const condition =
            userRole === 'coach'
                ? eq(coachTrainee.coachId, userId)
                : eq(coachTrainee.traineeId, userId);

        return await db.query.coachTrainee.findMany({
            where: and(condition, eq(coachTrainee.status, 'active')),
            with: {
                coach: {
                    columns: {
                        id: true,
                        name: true,
                        email: true,
                        image: true,
                    },
                },
                trainee: {
                    columns: {
                        id: true,
                        name: true,
                        email: true,
                        image: true,
                    },
                },
            },
        });
    }

    static async endConnection(coachId: string, traineeId: string) {
        const result = await db
            .update(coachTrainee)
            .set({
                status: 'inactive',
                endDate: new Date(),
                updatedAt: new Date(),
            })
            .where(
                and(
                    eq(coachTrainee.coachId, coachId),
                    eq(coachTrainee.traineeId, traineeId)
                )
            )
            .returning();
        return result[0] || null;
    }
}
