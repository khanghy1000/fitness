import { relations } from 'drizzle-orm';
import {
    user,
    session,
    account,
    verification,
    coachTrainee,
    exerciseType,
    nutritionPlan,
    workoutPlan,
    workoutPlanDay,
    workoutPlanDayExercise,
    userWorkoutPlan,
    userNutritionPlan,
    workoutSession,
    exerciseResult,
    userStats,
    userGoal,
    message,
} from './tables.ts';

export const userRelations = relations(user, ({ many }) => ({
    sessions: many(session),
    accounts: many(account),

    // Coach-Trainee relationships
    coachRelationships: many(coachTrainee, { relationName: 'coach' }),
    traineeRelationships: many(coachTrainee, { relationName: 'trainee' }),

    // Created content
    createdNutritionPlans: many(nutritionPlan),
    createdWorkoutPlans: many(workoutPlan),

    // Assigned workout plans
    userWorkoutPlans: many(userWorkoutPlan, { relationName: 'assignedUser' }),
    assignedWorkoutPlans: many(userWorkoutPlan, { relationName: 'assignedBy' }),

    // Assigned nutrition plans
    userNutritionPlans: many(userNutritionPlan, {
        relationName: 'assignedUser',
    }),
    assignedNutritionPlans: many(userNutritionPlan, {
        relationName: 'assignedBy',
    }),

    // Workout sessions and results
    workoutSessions: many(workoutSession),
    exerciseResults: many(exerciseResult),

    // Stats and goals
    userStats: many(userStats, { relationName: 'statsUser' }),
    recordedStats: many(userStats, { relationName: 'recordedBy' }),
    userGoals: many(userGoal),

    // Messages
    sentMessages: many(message, { relationName: 'sender' }),
    receivedMessages: many(message, { relationName: 'recipient' }),
}));

// Session relations
export const sessionRelations = relations(session, ({ one }) => ({
    user: one(user, {
        fields: [session.userId],
        references: [user.id],
    }),
}));

export const accountRelations = relations(account, ({ one }) => ({
    user: one(user, {
        fields: [account.userId],
        references: [user.id],
    }),
}));

export const coachTraineeRelations = relations(coachTrainee, ({ one }) => ({
    coach: one(user, {
        fields: [coachTrainee.coachId],
        references: [user.id],
        relationName: 'coach',
    }),
    trainee: one(user, {
        fields: [coachTrainee.traineeId],
        references: [user.id],
        relationName: 'trainee',
    }),
}));

export const exerciseTypeRelations = relations(exerciseType, ({ many }) => ({
    workoutPlanDayExercises: many(workoutPlanDayExercise),
}));

export const nutritionPlanRelations = relations(
    nutritionPlan,
    ({ one, many }) => ({
        createdBy: one(user, {
            fields: [nutritionPlan.createdBy],
            references: [user.id],
        }),
        userNutritionPlans: many(userNutritionPlan),
    })
);

export const workoutPlanRelations = relations(workoutPlan, ({ one, many }) => ({
    createdBy: one(user, {
        fields: [workoutPlan.createdBy],
        references: [user.id],
    }),
    workoutPlanDays: many(workoutPlanDay),
    userWorkoutPlans: many(userWorkoutPlan),
}));

export const workoutPlanDayRelations = relations(
    workoutPlanDay,
    ({ one, many }) => ({
        workoutPlan: one(workoutPlan, {
            fields: [workoutPlanDay.workoutPlanId],
            references: [workoutPlan.id],
        }),
        exercises: many(workoutPlanDayExercise),
        workoutSessions: many(workoutSession),
    })
);

export const workoutPlanDayExerciseRelations = relations(
    workoutPlanDayExercise,
    ({ one, many }) => ({
        workoutPlanDay: one(workoutPlanDay, {
            fields: [workoutPlanDayExercise.workoutPlanDayId],
            references: [workoutPlanDay.id],
        }),
        exerciseType: one(exerciseType, {
            fields: [workoutPlanDayExercise.exerciseTypeId],
            references: [exerciseType.id],
        }),
        exerciseResults: many(exerciseResult),
    })
);

export const userWorkoutPlanRelations = relations(
    userWorkoutPlan,
    ({ one, many }) => ({
        user: one(user, {
            fields: [userWorkoutPlan.userId],
            references: [user.id],
            relationName: 'assignedUser',
        }),
        workoutPlan: one(workoutPlan, {
            fields: [userWorkoutPlan.workoutPlanId],
            references: [workoutPlan.id],
        }),
        assignedBy: one(user, {
            fields: [userWorkoutPlan.assignedBy],
            references: [user.id],
            relationName: 'assignedBy',
        }),
        workoutSessions: many(workoutSession),
    })
);

export const userNutritionPlanRelations = relations(
    userNutritionPlan,
    ({ one }) => ({
        user: one(user, {
            fields: [userNutritionPlan.userId],
            references: [user.id],
            relationName: 'assignedUser',
        }),
        nutritionPlan: one(nutritionPlan, {
            fields: [userNutritionPlan.nutritionPlanId],
            references: [nutritionPlan.id],
        }),
        assignedBy: one(user, {
            fields: [userNutritionPlan.assignedBy],
            references: [user.id],
            relationName: 'assignedBy',
        }),
    })
);

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
        workoutPlanDay: one(workoutPlanDay, {
            fields: [workoutSession.workoutPlanDayId],
            references: [workoutPlanDay.id],
        }),
        exerciseResults: many(exerciseResult),
    })
);

export const exerciseResultRelations = relations(exerciseResult, ({ one }) => ({
    workoutSession: one(workoutSession, {
        fields: [exerciseResult.workoutSessionId],
        references: [workoutSession.id],
    }),
    workoutPlanExercise: one(workoutPlanDayExercise, {
        fields: [exerciseResult.workoutPlanExerciseId],
        references: [workoutPlanDayExercise.id],
    }),
    user: one(user, {
        fields: [exerciseResult.userId],
        references: [user.id],
    }),
}));

export const userStatsRelations = relations(userStats, ({ one }) => ({
    user: one(user, {
        fields: [userStats.userId],
        references: [user.id],
        relationName: 'statsUser',
    }),
    recordedBy: one(user, {
        fields: [userStats.recordedBy],
        references: [user.id],
        relationName: 'recordedBy',
    }),
}));

export const userGoalRelations = relations(userGoal, ({ one }) => ({
    user: one(user, {
        fields: [userGoal.userId],
        references: [user.id],
    }),
}));

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
