import 'dotenv/config';
import { db } from '../src/lib/db.js';
import { exerciseType as exerciseTypeTable } from '../src/db/schema/tables.js';

const exerciseTypes = [
    { name: 'High Stepping', devicePosition: 'thigh', logType: 'reps' },
    { name: 'Push Up', devicePosition: 'thigh', logType: 'reps' },
    { name: 'Squat', devicePosition: 'thigh', logType: 'reps' },
    { name: 'Jumping Jack', devicePosition: 'thigh', logType: 'reps' },
    { name: 'Skipping', devicePosition: 'thigh', logType: 'duration' },
    { name: 'Mountain Climber', devicePosition: 'thigh', logType: 'reps' },
    { name: 'Plank Jack', devicePosition: 'thigh', logType: 'reps' },
    { name: 'Sit Up', devicePosition: 'thigh', logType: 'reps' },
    { name: 'Lunge', devicePosition: 'thigh', logType: 'reps' },
    { name: 'Abdominal Crunch', devicePosition: 'thigh', logType: 'reps' },
    { name: 'Bicycle Crunch', devicePosition: 'thigh', logType: 'reps' },
    { name: 'Leg Raise', devicePosition: 'thigh', logType: 'reps' },
    { name: 'Heel Touch', devicePosition: 'thigh', logType: 'reps' },
    { name: 'Flutter Kick', devicePosition: 'thigh', logType: 'reps' },
    { name: 'Plank', devicePosition: 'none', logType: 'duration' },
    { name: 'Cobra Stretch', devicePosition: 'none', logType: 'duration' },
    // { name: 'Custom', devicePosition: 'none', logType: 'duration' },
];

async function seedExerciseTypes() {
    try {
        console.log('Starting to seed exercise types...');

        for (const exerciseType of exerciseTypes) {
            try {
                await db
                    .insert(exerciseTypeTable)
                    .values({
                        name: exerciseType.name,
                        devicePosition: exerciseType.devicePosition as
                            | 'thigh'
                            | 'arm'
                            | 'none',
                        logType: exerciseType.logType as 'reps' | 'duration',
                    })
                    .onConflictDoNothing();
                console.log(
                    `✓ Added: ${exerciseType.name} (${exerciseType.devicePosition}, ${exerciseType.logType})`
                );
            } catch (error) {
                console.log(
                    `⚠ Skipped (already exists): ${exerciseType.name}`
                );
            }
        }

        console.log('\n✅ Exercise types seeding completed!');

        // Verify the data
        const allExerciseTypes = await db.select().from(exerciseTypeTable);
        console.log(
            `\n📊 Total exercise types in database: ${allExerciseTypes.length}`
        );
        console.log('Exercise types:');
        allExerciseTypes.forEach((type) => {
            console.log(
                `  - ${type.name} (ID: ${type.id}, Device: ${type.devicePosition}, Log: ${type.logType})`
            );
        });
    } catch (error) {
        console.error('❌ Error seeding exercise types:', error);
    } finally {
        process.exit(0);
    }
}

seedExerciseTypes();
