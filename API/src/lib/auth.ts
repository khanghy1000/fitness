import { betterAuth } from 'better-auth';
import { drizzleAdapter } from 'better-auth/adapters/drizzle';
import { db } from '@lib/db.ts';
import { bearer, openAPI } from 'better-auth/plugins';

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
                type: ['admin', 'coach', 'trainee'],
                required: true,
                defaultValue: 'trainee',
                input: false,
            },
        },
    },
    plugins: [openAPI(), bearer()],
});
