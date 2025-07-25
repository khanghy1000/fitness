import { db } from '@lib/db.ts';
import { message } from '../db/schema/tables.ts';
import { eq, and, or, desc } from 'drizzle-orm';

export class MessageService {
    static async sendMessage(data: {
        senderId: string;
        recipientId: string;
        content: string;
        type?: 'text' | 'image' | 'video' | 'audio' | 'file';
        replyToId?: string;
    }) {
        const result = await db.insert(message).values(data).returning();
        return result[0];
    }

    static async getConversation(
        userId1: string,
        userId2: string,
        limit?: number,
        offset?: number
    ) {
        return await db.query.message.findMany({
            where: or(
                and(
                    eq(message.senderId, userId1),
                    eq(message.recipientId, userId2)
                ),
                and(
                    eq(message.senderId, userId2),
                    eq(message.recipientId, userId1)
                )
            ),
            orderBy: desc(message.createdAt),
            ...(limit && { limit }),
            ...(offset && { offset }),
            with: {
                sender: {
                    columns: {
                        id: true,
                        name: true,
                        image: true,
                    },
                },
                recipient: {
                    columns: {
                        id: true,
                        name: true,
                        image: true,
                    },
                },
            },
        });
    }

    static async markAsRead(messageIds: number[], userId: string) {
        return await db
            .update(message)
            .set({
                isRead: true,
                readAt: new Date(),
            })
            .where(
                and(
                    eq(message.recipientId, userId)
                    // Add condition to only update specific message IDs
                )
            )
            .returning();
    }

    static async getUnreadCount(userId: string) {
        const result = await db
            .select()
            .from(message)
            .where(
                and(eq(message.recipientId, userId), eq(message.isRead, false))
            );
        return result.length;
    }

    static async getConversationList(userId: string) {
        // Get latest message for each conversation
        return await db.query.message.findMany({
            where: or(
                eq(message.senderId, userId),
                eq(message.recipientId, userId)
            ),
            orderBy: desc(message.createdAt),
            with: {
                sender: {
                    columns: {
                        id: true,
                        name: true,
                        image: true,
                    },
                },
                recipient: {
                    columns: {
                        id: true,
                        name: true,
                        image: true,
                    },
                },
            },
        });
    }

    static async getMessageById(messageId: number) {
        return await db.query.message.findFirst({
            where: eq(message.id, messageId),
            with: {
                sender: {
                    columns: {
                        id: true,
                        name: true,
                        image: true,
                    },
                },
                recipient: {
                    columns: {
                        id: true,
                        name: true,
                        image: true,
                    },
                },
            },
        });
    }

    static async markConversationAsRead(
        userId: string,
        conversationUserId: string
    ) {
        return await db
            .update(message)
            .set({
                isRead: true,
                readAt: new Date(),
            })
            .where(
                and(
                    eq(message.recipientId, userId),
                    eq(message.senderId, conversationUserId),
                    eq(message.isRead, false)
                )
            )
            .returning();
    }
}
