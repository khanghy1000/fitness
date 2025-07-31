import { Server, Socket } from 'socket.io';
import { auth } from '@lib/auth.ts';
import { MessageService } from '@services/message.service.ts';

interface AuthenticatedSocket extends Socket {
    session?: typeof auth.$Infer.Session;
}

// Store active user connections
const activeUsers = new Map<string, AuthenticatedSocket>();

export function initializeSocketIO(io: Server) {
    // Authentication middleware
    io.use(async (socket: AuthenticatedSocket, next) => {
        try {
            const token = socket.handshake.auth.token;

            if (!token) {
                return next(new Error('Authentication token required'));
            }

            // Simple token verification for better-auth
            const session = await auth.api.getSession({
                headers: {
                    cookie: `better-auth.session_token=${token}`,
                } as any,
            });

            if (!session?.user) {
                return next(new Error('Invalid authentication token'));
            }

            socket.session = session;

            next();
        } catch (error) {
            next(new Error('Authentication failed'));
        }
    });

    io.on('connection', (socket: AuthenticatedSocket) => {
        console.log(
            `User ${socket.session!.user.name} (${socket.session!.user.id}) connected`
        );

        // Store active user
        if (socket.session!.user.id) {
            activeUsers.set(socket.session!.user.id, socket);
        }

        // Join user to their personal room
        socket.join(`user:${socket.session!.user.id}`);

        // Handle sending messages
        socket.on('send_message', async (data) => {
            try {
                const { recipientId, content, replyToId } = data;

                if (!recipientId || !content) {
                    socket.emit('error', {
                        message: 'Recipient ID and content are required',
                    });
                    return;
                }

                // Save message to database
                const message = await MessageService.sendMessage({
                    senderId: socket.session!.user.id!,
                    recipientId,
                    content: content.trim(),
                    replyToId,
                });

                // Get full message data with sender/recipient info
                const fullMessage = await MessageService.getMessageById(
                    message.id
                );

                // Send to recipient if they're online
                io.to(`user:${recipientId}`).emit('new_message', fullMessage);

                // Send confirmation back to sender
                socket.emit('message_sent', fullMessage);
            } catch (error) {
                console.error('Error sending message:', error);
                socket.emit('error', { message: 'Failed to send message' });
            }
        });

        // Handle getting conversation history
        socket.on('get_conversation', async (data) => {
            try {
                const { userId, limit, offset } = data;

                if (!userId) {
                    socket.emit('error', { message: 'User ID is required' });
                    return;
                }

                const messages = await MessageService.getConversation(
                    socket.session!.user.id,
                    userId,
                    limit,
                    offset
                );

                socket.emit('conversation_history', { userId, messages });
            } catch (error) {
                console.error('Error getting conversation:', error);
                socket.emit('error', { message: 'Failed to get conversation' });
            }
        });

        // Handle marking messages as read
        socket.on('mark_messages_read', async (data) => {
            try {
                const { conversationUserId } = data;

                if (!conversationUserId) {
                    socket.emit('error', {
                        message: 'Conversation user ID is required',
                    });
                    return;
                }

                await MessageService.markConversationAsRead(
                    socket.session!.user.id,
                    conversationUserId
                );

                // Notify the sender that their messages were read
                io.to(`user:${conversationUserId}`).emit('messages_read', {
                    readBy: socket.session!.user.id,
                    readAt: new Date().toISOString(),
                });

                socket.emit('messages_marked_read', { conversationUserId });
            } catch (error) {
                console.error('Error marking messages as read:', error);
                socket.emit('error', {
                    message: 'Failed to mark messages as read',
                });
            }
        });

        // Handle getting conversation list
        socket.on('get_conversations', async () => {
            try {
                const conversations = await MessageService.getConversationList(
                    socket.session!.user.id
                );
                socket.emit('conversations_list', conversations);
            } catch (error) {
                console.error('Error getting conversations:', error);
                socket.emit('error', {
                    message: 'Failed to get conversations',
                });
            }
        });

        // Handle getting unread count
        socket.on('get_unread_count', async () => {
            try {
                const count = await MessageService.getUnreadCount(
                    socket.session!.user.id
                );
                socket.emit('unread_count', { count });
            } catch (error) {
                console.error('Error getting unread count:', error);
                socket.emit('error', { message: 'Failed to get unread count' });
            }
        });

        // Handle user typing
        socket.on('typing_start', (data) => {
            const { recipientId } = data;
            if (recipientId) {
                io.to(`user:${recipientId}`).emit('user_typing', {
                    userId: socket.session!.user.id,
                    userName: socket.session!.user.name,
                });
            }
        });

        socket.on('typing_stop', (data) => {
            const { recipientId } = data;
            if (recipientId) {
                io.to(`user:${recipientId}`).emit('user_stopped_typing', {
                    userId: socket.session!.user.id,
                });
            }
        });

        // Handle user going online/offline
        socket.on('user_online', () => {
            socket.broadcast.emit('user_status_changed', {
                userId: socket.session!.user.id,
                status: 'online',
            });
        });

        // Handle disconnection
        socket.on('disconnect', () => {
            console.log(
                `User ${socket.session!.user.name} (${socket.session!.user.id}) disconnected`
            );

            if (socket.session!.user.id) {
                activeUsers.delete(socket.session!.user.id);

                // Notify others that user is offline
                socket.broadcast.emit('user_status_changed', {
                    userId: socket.session!.user.id,
                    status: 'offline',
                });
            }
        });
    });

    // Helper function to get online users
    io.engine.on('connection_error', (err) => {
        console.log(
            'Socket.IO connection error:',
            err.req,
            err.code,
            err.message,
            err.context
        );
    });
}

export function getActiveUsers() {
    return Array.from(activeUsers.keys());
}

export function isUserOnline(userId: string) {
    return activeUsers.has(userId);
}
