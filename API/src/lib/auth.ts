import { betterAuth } from 'better-auth';
import { drizzleAdapter } from 'better-auth/adapters/drizzle';
import { db } from '@lib/db.ts';
import { openAPI } from 'better-auth/plugins';

export const auth = betterAuth({
    database: drizzleAdapter(db, {
        provider: 'pg',
    }),
    emailAndPassword: {
        enabled: true,
    },
    user: {
        additionalFields: {
            role: {
                type: ['admin', 'trainer', 'user'],
                required: true,
                defaultValue: 'user',
                input: false,
            },
        },
    },
    plugins: [openAPI()],
});
