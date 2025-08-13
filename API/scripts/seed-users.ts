import { auth } from '@lib/auth.ts';

type User = {
    name: string;
    email: string;
    password: string;
    role: 'coach' | 'trainee';
};

const users: User[] = [
    {
        name: 'trainee',
        email: 'trainee@a.com',
        password: 'Test12345*',
        role: 'trainee',
    },
    {
        name: 'trainee2',
        email: 'trainee2@a.com',
        password: 'Test12345*',
        role: 'trainee',
    },
    {
        name: 'trainee3',
        email: 'trainee3@a.com',
        password: 'Test12345*',
        role: 'trainee',
    },
    {
        name: 'trainee4',
        email: 'trainee4@a.com',
        password: 'Test12345*',
        role: 'trainee',
    },
    {
        name: 'coach',
        email: 'coach@a.com',
        password: 'Test12345*',
        role: 'coach',
    },
    {
        name: 'coach2',
        email: 'coach2@a.com',
        password: 'Test12345*',
        role: 'coach',
    },
    {
        name: 'coach3',
        email: 'coach3@a.com',
        password: 'Test12345*',
        role: 'coach',
    },
    {
        name: 'coach4',
        email: 'coach4@a.com',
        password: 'Test12345*',
        role: 'coach',
    },
];

export async function seedUsers() {
    for (const user of users) {
        try {
            await auth.api.signUpEmail({ body: user });
        } catch (error) {
            console.log(`⚠ Skipped (already exists): ${user.name}`);
        }
    }
    console.log('\n✅ Users seeding completed!');
}
