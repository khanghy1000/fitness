import { relations } from 'drizzle-orm';
import {
    user,
    session,
    account,
    verification,
    exerciseCategory,
    exercise,
    nutritionPlan,
    workoutPlan,
    workoutPlanExercise,
    trainerUser,
    userWorkoutPlan,
    workoutSession,
    exerciseResult,
    userStats,
    message,
    userGoal,
} from './tables.ts';

// User relations
export const userRelations = relations(user, ({ many }) => ({
    sessions: many(session),
    accounts: many(account),
    createdExercises: many(exercise),
    createdNutritionPlans: many(nutritionPlan),
    createdWorkoutPlans: many(workoutPlan),
    trainerRelationships: many(trainerUser, { relationName: 'trainer' }),
    userRelationships: many(trainerUser, { relationName: 'trainee' }),
    assignedWorkoutPlans: many(userWorkoutPlan),
    workoutSessions: many(workoutSession),
    exerciseResults: many(exerciseResult),
    stats: many(userStats),
    sentMessages: many(message, { relationName: 'sender' }),
    receivedMessages: many(message, { relationName: 'recipient' }),
    goals: many(userGoal),
}));

// Session relations
export const sessionRelations = relations(session, ({ one }) => ({
    user: one(user, {
        fields: [session.userId],
        references: [user.id],
    }),
}));

// Account relations
export const accountRelations = relations(account, ({ one }) => ({
    user: one(user, {
        fields: [account.userId],
        references: [user.id],
    }),
}));

// Exercise Category relations
export const exerciseCategoryRelations = relations(
    exerciseCategory,
    ({ many }) => ({
        exercises: many(exercise),
    })
);

// Exercise relations
export const exerciseRelations = relations(exercise, ({ one, many }) => ({
    category: one(exerciseCategory, {
        fields: [exercise.categoryId],
        references: [exerciseCategory.id],
    }),
    creator: one(user, {
        fields: [exercise.createdBy],
        references: [user.id],
    }),
    workoutPlanExercises: many(workoutPlanExercise),
    results: many(exerciseResult),
}));

// Nutrition Plan relations
export const nutritionPlanRelations = relations(
    nutritionPlan,
    ({ one, many }) => ({
        creator: one(user, {
            fields: [nutritionPlan.createdBy],
            references: [user.id],
        }),
        workoutPlans: many(workoutPlan),
    })
);

// Workout Plan relations
export const workoutPlanRelations = relations(workoutPlan, ({ one, many }) => ({
    creator: one(user, {
        fields: [workoutPlan.createdBy],
        references: [user.id],
    }),
    nutritionPlan: one(nutritionPlan, {
        fields: [workoutPlan.nutritionPlanId],
        references: [nutritionPlan.id],
    }),
    exercises: many(workoutPlanExercise),
    userPlans: many(userWorkoutPlan),
}));

// Workout Plan Exercise relations
export const workoutPlanExerciseRelations = relations(
    workoutPlanExercise,
    ({ one }) => ({
        workoutPlan: one(workoutPlan, {
            fields: [workoutPlanExercise.workoutPlanId],
            references: [workoutPlan.id],
        }),
        exercise: one(exercise, {
            fields: [workoutPlanExercise.exerciseId],
            references: [exercise.id],
        }),
    })
);

// Trainer User relations
export const trainerUserRelations = relations(trainerUser, ({ one }) => ({
    trainer: one(user, {
        fields: [trainerUser.trainerId],
        references: [user.id],
        relationName: 'trainer',
    }),
    trainee: one(user, {
        fields: [trainerUser.userId],
        references: [user.id],
        relationName: 'trainee',
    }),
}));

// User Workout Plan relations
export const userWorkoutPlanRelations = relations(
    userWorkoutPlan,
    ({ one, many }) => ({
        user: one(user, {
            fields: [userWorkoutPlan.userId],
            references: [user.id],
        }),
        workoutPlan: one(workoutPlan, {
            fields: [userWorkoutPlan.workoutPlanId],
            references: [workoutPlan.id],
        }),
        assignedBy: one(user, {
            fields: [userWorkoutPlan.assignedBy],
            references: [user.id],
        }),
        sessions: many(workoutSession),
    })
);

// Workout Session relations
export const workoutSessionRelations = relations(
    workoutSession,
    ({ one, many }) => ({
        user: one(user, {
            fields: [workoutSession.userId],
            references: [user.id],
        }),
        userWorkoutPlan: one(userWorkoutPlan, {
            fields: [workoutSession.userWorkoutPlanId],
            references: [userWorkoutPlan.id],
        }),
        exerciseResults: many(exerciseResult),
    })
);

// Exercise Result relations
export const exerciseResultRelations = relations(exerciseResult, ({ one }) => ({
    workoutSession: one(workoutSession, {
        fields: [exerciseResult.workoutSessionId],
        references: [workoutSession.id],
    }),
    exercise: one(exercise, {
        fields: [exerciseResult.exerciseId],
        references: [exercise.id],
    }),
    user: one(user, {
        fields: [exerciseResult.userId],
        references: [user.id],
    }),
}));

// User Stats relations
export const userStatsRelations = relations(userStats, ({ one }) => ({
    user: one(user, {
        fields: [userStats.userId],
        references: [user.id],
    }),
    recordedBy: one(user, {
        fields: [userStats.recordedBy],
        references: [user.id],
    }),
}));

// Message relations
export const messageRelations = relations(message, ({ one }) => ({
    sender: one(user, {
        fields: [message.senderId],
        references: [user.id],
        relationName: 'sender',
    }),
    recipient: one(user, {
        fields: [message.recipientId],
        references: [user.id],
        relationName: 'recipient',
    }),
}));

// User Goal relations
export const userGoalRelations = relations(userGoal, ({ one }) => ({
    user: one(user, {
        fields: [userGoal.userId],
        references: [user.id],
    }),
}));
