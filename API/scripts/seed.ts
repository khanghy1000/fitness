import { seedUsers } from './seed-users.js';
import { seedExerciseTypes } from './seed-exercise-types.js';
import { seedWorkoutPlans } from './seed-workout-plans.js';
import { seedNutritionPlans } from './seed-nutrition-plans.js';
import { seedUserBodyStats } from './seed-user-body-stats.js';

async function main() {
    try {
        console.log('\n--- Seeding users ---');
        await seedUsers();
        console.log('\n--- Seeding exercise types ---');
        await seedExerciseTypes();
        console.log('\n--- Seeding workout plans ---');
        await seedWorkoutPlans();
        console.log('\n--- Seeding nutrition plans ---');
        await seedNutritionPlans();
        console.log('\n--- Seeding user body stats ---');
        await seedUserBodyStats();
        console.log('\n✅ All seed scripts completed successfully!');
        process.exit(0);
    } catch (err) {
        console.error('\n❌ Error running seed scripts:', err);
        process.exit(1);
    }
}

main();
