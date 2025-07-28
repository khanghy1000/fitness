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
        name: 'coach',
        email: 'coach@a.com',
        password: 'Test12345*',
        role: 'coach',
    },
];

async function seedUsers() {
    for (const user of users) {
        try {
            await auth.api.signUpEmail({ body: user });
        } catch (error) {
            console.log(`⚠ Skipped (already exists): ${user.name}`);
        }
    }
    console.log('\n✅ Users seeding completed!');
}
seedUsers();
