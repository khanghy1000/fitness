import {
    pgTable,
    text,
    timestamp,
    date,
    boolean,
    integer,
    real,
    json,
    uuid,
    serial,
    unique,
    primaryKey,
    time,
} from 'drizzle-orm/pg-core';

export const user = pgTable('user', {
    id: text('id').primaryKey(),
    name: text('name').notNull(),
    email: text('email').notNull().unique(),
    emailVerified: boolean('email_verified')
        .$defaultFn(() => false)
        .notNull(),
    image: text('image'),
    createdAt: timestamp('created_at')
        .$defaultFn(() => /* @__PURE__ */ new Date())
        .notNull(),
    updatedAt: timestamp('updated_at')
        .$defaultFn(() => /* @__PURE__ */ new Date())
        .notNull(),
    role: text('role', { enum: ['coach', 'trainee'] })
        .default('trainee')
        .notNull(),
});

export const session = pgTable('session', {
    id: text('id').primaryKey(),
    expiresAt: timestamp('expires_at').notNull(),
    token: text('token').notNull().unique(),
    createdAt: timestamp('created_at').notNull(),
    updatedAt: timestamp('updated_at').notNull(),
    ipAddress: text('ip_address'),
    userAgent: text('user_agent'),
    userId: text('user_id')
        .notNull()
        .references(() => user.id, { onDelete: 'cascade' }),
});

export const account = pgTable('account', {
    id: text('id').primaryKey(),
    accountId: text('account_id').notNull(),
    providerId: text('provider_id').notNull(),
    userId: text('user_id')
        .notNull()
        .references(() => user.id, { onDelete: 'cascade' }),
    accessToken: text('access_token'),
    refreshToken: text('refresh_token'),
    idToken: text('id_token'),
    accessTokenExpiresAt: timestamp('access_token_expires_at'),
    refreshTokenExpiresAt: timestamp('refresh_token_expires_at'),
    scope: text('scope'),
    password: text('password'),
    createdAt: timestamp('created_at').notNull(),
    updatedAt: timestamp('updated_at').notNull(),
});

export const verification = pgTable('verification', {
    id: text('id').primaryKey(),
    identifier: text('identifier').notNull(),
    value: text('value').notNull(),
    expiresAt: timestamp('expires_at').notNull(),
    createdAt: timestamp('created_at').$defaultFn(
        () => /* @__PURE__ */ new Date()
    ),
    updatedAt: timestamp('updated_at').$defaultFn(
        () => /* @__PURE__ */ new Date()
    ),
});

// Trainer-User Relationships
export const coachTrainee = pgTable(
    'coach_trainee',
    {
        coachId: text('coach_id').references(() => user.id, {
            onDelete: 'cascade',
        }),
        traineeId: text('trainee_id').references(() => user.id, {
            onDelete: 'cascade',
        }),
        status: text('status', {
            enum: ['pending', 'active', 'inactive', 'blocked'],
        }).default('pending'),
        startDate: timestamp('start_date'),
        endDate: timestamp('end_date'),
        notes: text('notes'),
        createdAt: timestamp('created_at')
            .$defaultFn(() => new Date())
            .notNull(),
        updatedAt: timestamp('updated_at')
            .$defaultFn(() => new Date())
            .notNull(),
    },
    (t) => [primaryKey({ columns: [t.coachId, t.traineeId] })]
);

// Exercise Categories
export const exerciseType = pgTable('exercise_type', {
    id: integer('id').primaryKey().generatedAlwaysAsIdentity(),
    name: text('name').notNull().unique(),
    devicePosition: text('device_position', {
        enum: ['thigh', 'arm', 'none'],
    }).default('thigh'),
    logType: text('log_type', {
        enum: ['reps', 'duration'],
    })
        .default('reps')
        .notNull(),
    // imageUrl: text('image_url'),
});

// Nutrition Plans
export const nutritionPlan = pgTable('nutrition_plan', {
    id: integer('id').primaryKey().generatedAlwaysAsIdentity(),
    name: text('name').notNull(),
    description: text('description'),
    createdBy: text('created_by').references(() => user.id, {
        onDelete: 'cascade',
    }),
    isActive: boolean('is_active').default(true),
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
    updatedAt: timestamp('updated_at')
        .$defaultFn(() => new Date())
        .notNull(),
});

// Nutrition Plan Days (for each weekday)
export const nutritionPlanDay = pgTable(
    'nutrition_plan_day',
    {
        id: integer('id').primaryKey().generatedAlwaysAsIdentity(),
        nutritionPlanId: integer('nutrition_plan_id').references(
            () => nutritionPlan.id,
            { onDelete: 'cascade' }
        ),
        weekday: text('weekday', {
            enum: ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'],
        }).notNull(),
        totalCalories: integer('total_calories'),
        protein: real('protein'), // in grams
        carbs: real('carbs'), // in grams
        fat: real('fat'), // in grams
        fiber: real('fiber'), // in grams
        createdAt: timestamp('created_at')
            .$defaultFn(() => new Date())
            .notNull(),
        updatedAt: timestamp('updated_at')
            .$defaultFn(() => new Date())
            .notNull(),
    },
    (t) => [unique().on(t.nutritionPlanId, t.weekday)]
);

// Meals for each nutrition plan day
export const nutritionPlanMeal = pgTable('nutrition_plan_meal', {
    id: integer('id').primaryKey().generatedAlwaysAsIdentity(),
    nutritionPlanDayId: integer('nutrition_plan_day_id').references(
        () => nutritionPlanDay.id,
        { onDelete: 'cascade' }
    ),
    name: text('name').notNull(), // e.g., "Breakfast", "Lunch", "Dinner", "Snack"
    time: time('time').notNull(), // Time of day (e.g., "07:30:00", "12:00:00", "18:30:00")
    calories: integer('calories'),
    protein: real('protein'), // in grams
    carbs: real('carbs'), // in grams
    fat: real('fat'), // in grams
    fiber: real('fiber'), // in grams
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
    updatedAt: timestamp('updated_at')
        .$defaultFn(() => new Date())
        .notNull(),
});

// Foods in each meal
export const nutritionPlanFood = pgTable('nutrition_plan_food', {
    id: integer('id').primaryKey().generatedAlwaysAsIdentity(),
    nutritionPlanMealId: integer('nutrition_plan_meal_id').references(
        () => nutritionPlanMeal.id,
        { onDelete: 'cascade' }
    ),
    name: text('name').notNull(),
    quantity: text('quantity').notNull(), // e.g., "1 cup", "100g", "2 pieces"
    calories: integer('calories').notNull(),
    protein: real('protein'), // in grams
    carbs: real('carbs'), // in grams
    fat: real('fat'), // in grams
    fiber: real('fiber'), // in grams
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
    updatedAt: timestamp('updated_at')
        .$defaultFn(() => new Date())
        .notNull(),
});

// Workout Plans
export const workoutPlan = pgTable('workout_plan', {
    id: integer('id').primaryKey().generatedAlwaysAsIdentity(),
    name: text('name').notNull(),
    description: text('description'),
    // goal: text('goal', {
    //     enum: [
    //         'weight_loss',
    //         'muscle_gain',
    //         'endurance',
    //         'strength',
    //         'flexibility',
    //         'general_fitness',
    //     ],
    // }),
    difficulty: text('difficulty', {
        enum: ['beginner', 'intermediate', 'advanced'],
    }).default('beginner'),
    // duration: integer('duration'), // in weeks
    estimatedCalories: integer('estimated_calories'),
    createdBy: text('created_by').references(() => user.id, {
        onDelete: 'cascade',
    }),
    isActive: boolean('is_active').default(true),
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
    updatedAt: timestamp('updated_at')
        .$defaultFn(() => new Date())
        .notNull(),
});

export const workoutPlanDay = pgTable(
    'workout_plan_day',
    {
        id: integer('id').primaryKey().generatedAlwaysAsIdentity(),
        workoutPlanId: integer('workout_plan_id').references(
            () => workoutPlan.id,
            {
                onDelete: 'cascade',
            }
        ),
        day: integer('day').notNull(),
        isRestDay: boolean('is_rest_day').default(false),
        estimatedCalories: integer('estimated_calories'),
        duration: integer('duration'), // in seconds
    },
    (t) => [unique().on(t.workoutPlanId, t.day)]
);

// Workout Plan Day Exercises (junction table for exercises in a workout plan day)
export const workoutPlanDayExercise = pgTable('workout_plan_day_exercise', {
    id: integer('id').primaryKey().generatedAlwaysAsIdentity(),
    workoutPlanDayId: integer('workout_plan_day_id').references(
        () => workoutPlanDay.id,
        {
            onDelete: 'cascade',
        }
    ),
    exerciseTypeId: integer('exercise_type_id').references(
        () => exerciseType.id,
        {
            onDelete: 'cascade',
        }
    ),
    order: integer('order'), // order in the day
    targetReps: integer('target_reps'),
    targetDuration: integer('target_duration'),
    estimatedCalories: integer('estimated_calories'),
    // targetWeight: real('target_weight'),
    // targetDistance: real('target_distance'),
    notes: text('notes'),
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
    updatedAt: timestamp('updated_at')
        .$defaultFn(() => new Date())
        .notNull(),
});

// User Workout Plans (assigned plans)
export const userWorkoutPlan = pgTable('user_workout_plan', {
    id: integer('id').primaryKey().generatedAlwaysAsIdentity(),
    userId: text('user_id').references(() => user.id, {
        onDelete: 'cascade',
    }),
    workoutPlanId: integer('workout_plan_id').references(() => workoutPlan.id, {
        onDelete: 'cascade',
    }),
    assignedBy: text('assigned_by').references(() => user.id), // trainer who assigned
    startDate: timestamp('start_date').notNull(),
    endDate: timestamp('end_date'),
    status: text('status', {
        enum: ['active', 'completed', 'paused', 'cancelled'],
    }).default('active'),
    progress: real('progress').default(0), // percentage 0-100
    notes: text('notes'),
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
    updatedAt: timestamp('updated_at')
        .$defaultFn(() => new Date())
        .notNull(),
});

// User Nutrition Plans (assigned nutrition plans)
export const userNutritionPlan = pgTable('user_nutrition_plan', {
    id: integer('id').primaryKey().generatedAlwaysAsIdentity(),
    userId: text('user_id').references(() => user.id, {
        onDelete: 'cascade',
    }),
    nutritionPlanId: integer('nutrition_plan_id').references(
        () => nutritionPlan.id,
        {
            onDelete: 'cascade',
        }
    ),
    assignedBy: text('assigned_by').references(() => user.id), // trainer who assigned
    startDate: timestamp('start_date').notNull(),
    endDate: timestamp('end_date'),
    status: text('status', {
        enum: ['active', 'completed', 'paused', 'cancelled'],
    }).default('active'),
    notes: text('notes'),
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
    updatedAt: timestamp('updated_at')
        .$defaultFn(() => new Date())
        .notNull(),
});

// Daily nutrition adherence tracking
export const nutritionAdherence = pgTable('nutrition_adherence', {
    id: integer('id').primaryKey().generatedAlwaysAsIdentity(),
    userNutritionPlanId: integer('user_nutrition_plan_id').references(
        () => userNutritionPlan.id,
        { onDelete: 'cascade' }
    ),
    userId: text('user_id').references(() => user.id, { onDelete: 'cascade' }),
    date: date('date').notNull(), // Date for which adherence is tracked
    weekday: text('weekday', {
        enum: ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'],
    }).notNull(),
    mealsCompleted: integer('meals_completed').default(0), // Number of meals completed
    totalMeals: integer('total_meals').default(0), // Total meals planned for the day
    adherencePercentage: real('adherence_percentage').default(0), // 0-100%
    totalCaloriesConsumed: integer('total_calories_consumed').default(0),
    totalCaloriesPlanned: integer('total_calories_planned').default(0),
    notes: text('notes'), // User notes about the day
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
    updatedAt: timestamp('updated_at')
        .$defaultFn(() => new Date())
        .notNull(),
});

// Meal completion tracking
export const mealCompletion = pgTable('meal_completion', {
    id: integer('id').primaryKey().generatedAlwaysAsIdentity(),
    nutritionAdherenceId: integer('nutrition_adherence_id').references(
        () => nutritionAdherence.id,
        { onDelete: 'cascade' }
    ),
    nutritionPlanMealId: integer('nutrition_plan_meal_id').references(
        () => nutritionPlanMeal.id,
        { onDelete: 'cascade' }
    ),
    userId: text('user_id').references(() => user.id, { onDelete: 'cascade' }),
    isCompleted: boolean('is_completed').default(false),
    completedAt: timestamp('completed_at'),
    caloriesConsumed: integer('calories_consumed'),
    proteinConsumed: real('protein_consumed'),
    carbsConsumed: real('carbs_consumed'),
    fatConsumed: real('fat_consumed'),
    fiberConsumed: real('fiber_consumed'),
    notes: text('notes'), // User notes about the specific meal
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
    updatedAt: timestamp('updated_at')
        .$defaultFn(() => new Date())
        .notNull(),
});

// Planned Workouts (for recurring workout notifications) (May not needed - can be local data in app)
export const plannedWorkout = pgTable('planned_workout', {
    id: integer('id').primaryKey().generatedAlwaysAsIdentity(),
    userId: text('user_id').references(() => user.id, { onDelete: 'cascade' }),
    userWorkoutPlanId: integer('user_workout_plan_id').references(
        () => userWorkoutPlan.id,
        { onDelete: 'cascade' }
    ),
    weekdays: json('weekdays')
        .$type<('sun' | 'mon' | 'tue' | 'wed' | 'thu' | 'fri' | 'sat')[]>()
        .notNull(), // Array of weekdays: ['sun', 'mon', 'tue']
    time: time('time').notNull(), // Time of day without timezone (e.g., "07:30:00", "18:00:00")
    isActive: boolean('is_active').default(true), // can be enabled/disabled
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
    updatedAt: timestamp('updated_at')
        .$defaultFn(() => new Date())
        .notNull(),
});

// Workout Sessions
// Used only when trainee actually starts/completes a workout
// export const workoutSession = pgTable('workout_session', {
//     id: integer('id').primaryKey().generatedAlwaysAsIdentity(),
//     userId: text('user_id').references(() => user.id, { onDelete: 'cascade' }),
//     userWorkoutPlanId: integer('user_workout_plan_id').references(
//         () => userWorkoutPlan.id,
//         { onDelete: 'cascade' }
//     ),
//     workoutPlanDayId: integer('workout_plan_day_id').references(
//         () => workoutPlanDay.id,
//         { onDelete: 'cascade' }
//     ),
//     startTime: timestamp('start_time'),
//     endTime: timestamp('end_time'),
//     totalDuration: integer('total_duration'), // in seconds
//     totalCalories: integer('total_calories'),
//     status: text('status', {
//         enum: ['in_progress', 'done'],
//     }).default('in_progress'),
//     // rating: integer('rating'), // 1-5 stars
//     // notes: text('notes'),
//     createdAt: timestamp('created_at')
//         .$defaultFn(() => new Date())
//         .notNull(),
//     updatedAt: timestamp('updated_at')
//         .$defaultFn(() => new Date())
//         .notNull(),
// });

// Exercise Results/Logs
export const exerciseResult = pgTable('exercise_result', {
    id: integer('id').primaryKey().generatedAlwaysAsIdentity(),
    // workoutSessionId: integer('workout_session_id').references(
    //     () => workoutSession.id,
    //     { onDelete: 'cascade' }
    // ),
    workoutPlanDayExerciseId: integer(
        'workout_plan_day_exercise_id'
    ).references(() => workoutPlanDayExercise.id, {
        onDelete: 'cascade',
    }),
    userWorkoutPlanId: integer('user_workout_plan_id').references(
        () => userWorkoutPlan.id,
        { onDelete: 'cascade' }
    ),
    userId: text('user_id').references(() => user.id, { onDelete: 'cascade' }),
    reps: integer('reps'),
    duration: integer('duration'), // in seconds
    calories: integer('calories'),
    // weight: real('weight'), // in kg
    // distance: real('distance'), // in meters/km
    // heartRate: integer('heart_rate'), // average heart rate
    // maxHeartRate: integer('max_heart_rate'),
    // difficulty: integer('difficulty'), // 1-10 perceived exertion
    // notes: text('notes'),
    completedAt: timestamp('completed_at').notNull(),
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
});

// User Stats/Measurements
export const userStats = pgTable('user_stats', {
    id: integer('id').primaryKey().generatedAlwaysAsIdentity(),
    userId: text('user_id').references(() => user.id, { onDelete: 'cascade' }),
    weight: real('weight'), // in kg
    height: real('height'), // in cm
    bodyFat: real('body_fat'), // percentage
    muscleMass: real('muscle_mass'), // in kg
    bmi: real('bmi'),
    chest: real('chest'), // in cm
    waist: real('waist'), // in cm
    hips: real('hips'), // in cm
    arms: real('arms'), // in cm
    thighs: real('thighs'), // in cm
    recordedAt: timestamp('recorded_at').notNull(),
    recordedBy: text('recorded_by').references(() => user.id), // who recorded it (coach or trainee)
    notes: text('notes'),
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
});

// User Goals
// export const userGoal = pgTable('user_goal', {
//     id: integer('id').primaryKey().generatedAlwaysAsIdentity(),
//     userId: text('user_id').references(() => user.id, { onDelete: 'cascade' }),
//     type: text('type', {
//         enum: [
//             'weight_loss',
//             'weight_gain',
//             'muscle_gain',
//             'endurance',
//             'strength',
//             'flexibility',
//             'custom',
//         ],
//     }).notNull(),
//     title: text('title').notNull(),
//     description: text('description'),
//     targetValue: real('target_value'),
//     currentValue: real('current_value').default(0),
//     unit: text('unit'), // kg, cm, minutes, etc.
//     targetDate: timestamp('target_date'),
//     status: text('status', {
//         enum: ['active', 'completed', 'paused', 'cancelled'],
//     }).default('active'),
//     priority: text('priority', { enum: ['low', 'medium', 'high'] }).default(
//         'medium'
//     ),
//     createdAt: timestamp('created_at')
//         .$defaultFn(() => new Date())
//         .notNull(),
//     updatedAt: timestamp('updated_at')
//         .$defaultFn(() => new Date())
//         .notNull(),
// });

// Messages between coach and trainee
export const message = pgTable('message', {
    id: integer('id').primaryKey().generatedAlwaysAsIdentity(),
    senderId: text('sender_id').references(() => user.id, {
        onDelete: 'cascade',
    }),
    recipientId: text('recipient_id').references(() => user.id, {
        onDelete: 'cascade',
    }),
    content: text('content').notNull(),
    // type: text('type', {
    //     enum: ['text', 'image', 'video', 'audio', 'file'],
    // }).default('text'),
    // fileUrl: text('file_url'),
    readAt: timestamp('read_at'),
    isRead: boolean('is_read').default(false),
    replyToId: text('reply_to_id'), // references message.id
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
});
