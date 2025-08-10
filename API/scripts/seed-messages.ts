import { db } from '../src/lib/db.js';
import { message, user } from '../src/db/schema/tables.ts';
import { eq, and } from 'drizzle-orm';

type MessageData = {
    senderEmail: string;
    recipientEmail: string;
    content: string;
    isRead?: boolean;
    createdAt?: Date;
};

const messages: MessageData[] = [
    // Coach initiating conversation
    {
        senderEmail: 'coach@a.com',
        recipientEmail: 'trainee@a.com',
        content: 'Hi! Welcome to your fitness journey. I\'m excited to work with you!',
        isRead: true,
        createdAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000), // 7 days ago
    },
    {
        senderEmail: 'trainee@a.com',
        recipientEmail: 'coach@a.com',
        content: 'Thank you! I\'m really motivated to get started. What should I focus on first?',
        isRead: true,
        createdAt: new Date(Date.now() - 6 * 24 * 60 * 60 * 1000), // 6 days ago
    },
    {
        senderEmail: 'coach@a.com',
        recipientEmail: 'trainee@a.com',
        content: 'Great attitude! Let\'s start with a proper assessment. I\'ll create a workout plan for you based on your goals.',
        isRead: true,
        createdAt: new Date(Date.now() - 6 * 24 * 60 * 60 * 1000 + 30 * 60 * 1000), // 6 days ago + 30 min
    },
    {
        senderEmail: 'coach@a.com',
        recipientEmail: 'trainee@a.com',
        content: 'I\'ve assigned you a beginner-friendly workout plan. Remember to stay hydrated and listen to your body!',
        isRead: true,
        createdAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000), // 5 days ago
    },
    {
        senderEmail: 'trainee@a.com',
        recipientEmail: 'coach@a.com',
        content: 'Just completed my first workout! It was challenging but I feel great. Is it normal to feel this sore?',
        isRead: true,
        createdAt: new Date(Date.now() - 4 * 24 * 60 * 60 * 1000), // 4 days ago
    },
    {
        senderEmail: 'coach@a.com',
        recipientEmail: 'trainee@a.com',
        content: 'Absolutely! That\'s called DOMS (Delayed Onset Muscle Soreness). It\'s completely normal for beginners. Make sure to do some light stretching.',
        isRead: true,
        createdAt: new Date(Date.now() - 4 * 24 * 60 * 60 * 1000 + 45 * 60 * 1000), // 4 days ago + 45 min
    },
    {
        senderEmail: 'trainee@a.com',
        recipientEmail: 'coach@a.com',
        content: 'Thanks for the advice! Should I continue with the same intensity or take it easier tomorrow?',
        isRead: true,
        createdAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000), // 3 days ago
    },
    {
        senderEmail: 'coach@a.com',
        recipientEmail: 'trainee@a.com',
        content: 'Take it a bit easier tomorrow. Focus on form over intensity. Your body needs time to adapt. How\'s your nutrition going?',
        isRead: true,
        createdAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000 + 20 * 60 * 1000), // 3 days ago + 20 min
    },
    {
        senderEmail: 'trainee@a.com',
        recipientEmail: 'coach@a.com',
        content: 'I\'m trying to follow the nutrition plan you gave me, but I\'m struggling with meal prep. Any tips?',
        isRead: true,
        createdAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000), // 2 days ago
    },
    {
        senderEmail: 'coach@a.com',
        recipientEmail: 'trainee@a.com',
        content: 'Meal prep can be tough at first! Try preparing just 2-3 days worth at a time. Start with simple recipes and gradually add variety.',
        isRead: true,
        createdAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000 + 90 * 60 * 1000), // 2 days ago + 90 min
    },
    {
        senderEmail: 'trainee@a.com',
        recipientEmail: 'coach@a.com',
        content: 'That makes sense! I\'ll try that approach. By the way, I feel much stronger already!',
        isRead: false,
        createdAt: new Date(Date.now() - 24 * 60 * 60 * 1000), // 1 day ago
    },
    {
        senderEmail: 'coach@a.com',
        recipientEmail: 'trainee@a.com',
        content: 'That\'s fantastic progress! Keep up the great work. How do you feel about increasing the intensity slightly next week?',
        isRead: false,
        createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000), // 2 hours ago
    },
    // New unread messages
    {
        senderEmail: 'trainee@a.com',
        recipientEmail: 'coach@a.com',
        content: 'I think I\'m ready for the next level! Should I add more weight or increase reps?',
        isRead: false,
        createdAt: new Date(Date.now() - 30 * 60 * 1000), // 30 minutes ago
    },
    {
        senderEmail: 'coach@a.com',
        recipientEmail: 'trainee@a.com',
        content: 'Perfect timing! Let\'s focus on increasing reps first, then we\'ll add weight. Also, I\'ve updated your nutrition plan with some new recipes.',
        isRead: false,
        createdAt: new Date(Date.now() - 15 * 60 * 1000), // 15 minutes ago
    },
    {
        senderEmail: 'trainee@a.com',
        recipientEmail: 'coach@a.com',
        content: 'Awesome! I\'m excited to try the new recipes. Quick question - is it okay to workout on an empty stomach in the morning?',
        isRead: false,
        createdAt: new Date(Date.now() - 5 * 60 * 1000), // 5 minutes ago
    },
];

async function seedMessages() {
    try {
        // Get user IDs by email
        const coach = await db.select().from(user).where(eq(user.email, 'coach@a.com')).limit(1);
        const trainee = await db.select().from(user).where(eq(user.email, 'trainee@a.com')).limit(1);

        if (!coach.length || !trainee.length) {
            console.log('❌ Users not found. Please run seed-users script first.');
            return;
        }

        const coachId = coach[0].id;
        const traineeId = trainee[0].id;

        console.log('📨 Seeding messages...');

        let newMessages = 0;
        let skippedMessages = 0;

        for (const messageData of messages) {
            const senderId = messageData.senderEmail === 'coach@a.com' ? coachId : traineeId;
            const recipientId = messageData.recipientEmail === 'coach@a.com' ? coachId : traineeId;

            // Check if message already exists
            const existingMessage = await db
                .select()
                .from(message)
                .where(
                    and(
                        eq(message.senderId, senderId),
                        eq(message.recipientId, recipientId),
                        eq(message.content, messageData.content)
                    )
                )
                .limit(1);

            if (existingMessage.length > 0) {
                console.log(`⚠ Skipped (already exists): "${messageData.content.substring(0, 50)}..."`);
                skippedMessages++;
                continue;
            }

            await db.insert(message).values({
                senderId,
                recipientId,
                content: messageData.content,
                isRead: messageData.isRead || false,
                readAt: messageData.isRead ? messageData.createdAt : null,
                createdAt: messageData.createdAt || new Date(),
            });

            console.log(`✅ Message sent from ${messageData.senderEmail} to ${messageData.recipientEmail}`);
            newMessages++;
        }

        console.log(`\n🎉 Seeding completed!`);
        console.log(`📊 New messages: ${newMessages}`);
        console.log(`⏭ Skipped messages: ${skippedMessages}`);
        console.log(`💬 Total conversation between coach@a.com and trainee@a.com`);

    } catch (error) {
        console.error('❌ Error seeding messages:', error);
    }
}

seedMessages();