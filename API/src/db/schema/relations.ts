import { relations } from 'drizzle-orm';
import {
    user,
    session,
    account,
    verification,
    coachTrainee,
    exerciseType,
    nutritionPlan,
    nutritionPlanDay,
    nutritionPlanMeal,
    nutritionPlanFood,
    workoutPlan,
    workoutPlanDay,
    workoutPlanDayExercise,
    userWorkoutPlan,
    userNutritionPlan,
    nutritionAdherence,
    mealCompletion,
    plannedWorkout,
    exerciseResult,
    userStats,
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

    // Nutrition adherence tracking
    nutritionAdherences: many(nutritionAdherence),
    mealCompletions: many(mealCompletion),

    // Workout sessions and results
    plannedWorkouts: many(plannedWorkout),
    // workoutSessions: many(workoutSession),
    exerciseResults: many(exerciseResult),

    // Stats and goals
    userStats: many(userStats, { relationName: 'statsUser' }),
    recordedStats: many(userStats, { relationName: 'recordedBy' }),
    // userGoals: many(userGoal),

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
        days: many(nutritionPlanDay),
        userNutritionPlans: many(userNutritionPlan),
    })
);

export const nutritionPlanDayRelations = relations(
    nutritionPlanDay,
    ({ one, many }) => ({
        nutritionPlan: one(nutritionPlan, {
            fields: [nutritionPlanDay.nutritionPlanId],
            references: [nutritionPlan.id],
        }),
        meals: many(nutritionPlanMeal),
    })
);

export const nutritionPlanMealRelations = relations(
    nutritionPlanMeal,
    ({ one, many }) => ({
        nutritionPlanDay: one(nutritionPlanDay, {
            fields: [nutritionPlanMeal.nutritionPlanDayId],
            references: [nutritionPlanDay.id],
        }),
        foods: many(nutritionPlanFood),
        mealCompletions: many(mealCompletion),
    })
);

export const nutritionPlanFoodRelations = relations(
    nutritionPlanFood,
    ({ one }) => ({
        nutritionPlanMeal: one(nutritionPlanMeal, {
            fields: [nutritionPlanFood.nutritionPlanMealId],
            references: [nutritionPlanMeal.id],
        }),
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
        // workoutSessions: many(workoutSession),
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
        exerciseResults: many(exerciseResult),
        // workoutSessions: many(workoutSession),
    })
);

export const userNutritionPlanRelations = relations(
    userNutritionPlan,
    ({ one, many }) => ({
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
        nutritionAdherences: many(nutritionAdherence),
    })
);

export const nutritionAdherenceRelations = relations(
    nutritionAdherence,
    ({ one, many }) => ({
        userNutritionPlan: one(userNutritionPlan, {
            fields: [nutritionAdherence.userNutritionPlanId],
            references: [userNutritionPlan.id],
        }),
        user: one(user, {
            fields: [nutritionAdherence.userId],
            references: [user.id],
        }),
        mealCompletions: many(mealCompletion),
    })
);

export const mealCompletionRelations = relations(mealCompletion, ({ one }) => ({
    nutritionAdherence: one(nutritionAdherence, {
        fields: [mealCompletion.nutritionAdherenceId],
        references: [nutritionAdherence.id],
    }),
    nutritionPlanMeal: one(nutritionPlanMeal, {
        fields: [mealCompletion.nutritionPlanMealId],
        references: [nutritionPlanMeal.id],
    }),
    user: one(user, {
        fields: [mealCompletion.userId],
        references: [user.id],
    }),
}));

// export const workoutSessionRelations = relations(
//     workoutSession,
//     ({ one, many }) => ({
//         user: one(user, {
//             fields: [workoutSession.userId],
//             references: [user.id],
//         }),
//         userWorkoutPlan: one(userWorkoutPlan, {
//             fields: [workoutSession.userWorkoutPlanId],
//             references: [userWorkoutPlan.id],
//         }),
//         workoutPlanDay: one(workoutPlanDay, {
//             fields: [workoutSession.workoutPlanDayId],
//             references: [workoutPlanDay.id],
//         }),
//         exerciseResults: many(exerciseResult),
//     })
// );

export const exerciseResultRelations = relations(exerciseResult, ({ one }) => ({
    // workoutSession: one(workoutSession, {
    //     fields: [exerciseResult.workoutSessionId],
    //     references: [workoutSession.id],
    // }),
    workoutPlanExercise: one(workoutPlanDayExercise, {
        fields: [exerciseResult.workoutPlanDayExerciseId],
        references: [workoutPlanDayExercise.id],
    }),
    userWorkoutPlan: one(userWorkoutPlan, {
        fields: [exerciseResult.userWorkoutPlanId],
        references: [userWorkoutPlan.id],
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

// export const userGoalRelations = relations(userGoal, ({ one }) => ({
//     user: one(user, {
//         fields: [userGoal.userId],
//         references: [user.id],
//     }),
// }));

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

export const plannedWorkoutRelations = relations(plannedWorkout, ({ one }) => ({
    user: one(user, {
        fields: [plannedWorkout.userId],
        references: [user.id],
    }),
    userWorkoutPlan: one(userWorkoutPlan, {
        fields: [plannedWorkout.userWorkoutPlanId],
        references: [userWorkoutPlan.id],
    }),
}));
