import {
    pgTable,
    text,
    timestamp,
    boolean,
    integer,
    real,
    json,
    uuid,
    serial,
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
    role: text('role', { enum: ['admin', 'trainer', 'user'] })
        .default('user')
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

// Exercise Categories
export const exerciseCategory = pgTable('exercise_category', {
    id: text('id').primaryKey(),
    name: text('name').notNull(),
    description: text('description'),
    type: text('type', {
        enum: [
            'running',
            'jumping',
            'upper_body',
            'lower_body',
            'full_body',
            'cardio',
            'strength',
            'flexibility',
        ],
    }).notNull(),
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
    updatedAt: timestamp('updated_at')
        .$defaultFn(() => new Date())
        .notNull(),
});

// Exercises
export const exercise = pgTable('exercise', {
    id: text('id').primaryKey(),
    name: text('name').notNull(),
    // description: text('description'),
    // instructions: text('instructions'),
    categoryId: text('category_id').references(() => exerciseCategory.id, {
        onDelete: 'cascade',
    }),
    difficulty: text('difficulty', {
        enum: ['beginner', 'intermediate', 'advanced'],
    }).default('beginner'),
    sets: integer('sets'), // number of sets
    reps: integer('rep'), // number of repetitions
    restTime: integer('rest_time'), // in seconds
    duration: integer('duration'), // in seconds
    calories: integer('calories'), // estimated calories burned
    // muscleGroups: json('muscle_groups').$type<string[]>(), // array of muscle groups
    // equipment: json('equipment').$type<string[]>(), // array of required equipment
    // videoUrl: text('video_url'),
    // imageUrl: text('image_url'),
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

// Nutrition Plans
export const nutritionPlan = pgTable('nutrition_plan', {
    id: text('id').primaryKey(),
    name: text('name').notNull(),
    description: text('description'),
    totalCalories: integer('total_calories'),
    protein: real('protein'), // in grams
    carbs: real('carbs'), // in grams
    fat: real('fat'), // in grams
    fiber: real('fiber'), // in grams
    meals: json('meals').$type<
        Array<{
            name: string;
            time: string;
            foods: Array<{
                name: string;
                quantity: string;
                calories: number;
            }>;
        }>
    >(),
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

// Workout Plans
export const workoutPlan = pgTable('workout_plan', {
    id: text('id').primaryKey(),
    name: text('name').notNull(),
    description: text('description'),
    goal: text('goal', {
        enum: [
            'weight_loss',
            'muscle_gain',
            'endurance',
            'strength',
            'flexibility',
            'general_fitness',
        ],
    }),
    difficulty: text('difficulty', {
        enum: ['beginner', 'intermediate', 'advanced'],
    }).default('beginner'),
    duration: integer('duration'), // in weeks
    daysPerWeek: integer('days_per_week'),
    estimatedCalories: integer('estimated_calories'),
    createdBy: text('created_by').references(() => user.id, {
        onDelete: 'cascade',
    }),
    nutritionPlanId: text('nutrition_plan_id').references(
        () => nutritionPlan.id
    ),
    isActive: boolean('is_active').default(true),
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
    updatedAt: timestamp('updated_at')
        .$defaultFn(() => new Date())
        .notNull(),
});

// Workout Plan Exercises (junction table for exercises in a workout plan)
export const workoutPlanExercise = pgTable('workout_plan_exercise', {
    id: text('id').primaryKey(),
    workoutPlanId: text('workout_plan_id').references(() => workoutPlan.id, {
        onDelete: 'cascade',
    }),
    exerciseId: text('exercise_id').references(() => exercise.id, {
        onDelete: 'cascade',
    }),
    dayOfWeek: integer('day_of_week'), // 1-7
    order: integer('order'), // order in the day
    sets: integer('sets'),
    reps: integer('reps'),
    duration: integer('duration'), // in seconds
    restTime: integer('rest_time'), // rest between sets in seconds
    weight: real('weight'), // in kg
    distance: real('distance'), // in meters/km
    targetReps: integer('target_reps'),
    targetSets: integer('target_sets'),
    targetDuration: integer('target_duration'),
    targetWeight: real('target_weight'),
    targetDistance: real('target_distance'),
    notes: text('notes'),
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
    updatedAt: timestamp('updated_at')
        .$defaultFn(() => new Date())
        .notNull(),
});

// Trainer-User Relationships
export const trainerUser = pgTable('trainer_user', {
    id: text('id').primaryKey(),
    trainerId: text('trainer_id').references(() => user.id, {
        onDelete: 'cascade',
    }),
    userId: text('user_id').references(() => user.id, { onDelete: 'cascade' }),
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
});

// User Workout Plans (assigned plans)
export const userWorkoutPlan = pgTable('user_workout_plan', {
    id: text('id').primaryKey(),
    userId: text('user_id').references(() => user.id, { onDelete: 'cascade' }),
    workoutPlanId: text('workout_plan_id').references(() => workoutPlan.id, {
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

// Workout Sessions
export const workoutSession = pgTable('workout_session', {
    id: text('id').primaryKey(),
    userId: text('user_id').references(() => user.id, { onDelete: 'cascade' }),
    userWorkoutPlanId: text('user_workout_plan_id').references(
        () => userWorkoutPlan.id
    ),
    date: timestamp('date').notNull(),
    startTime: timestamp('start_time'),
    endTime: timestamp('end_time'),
    totalDuration: integer('total_duration'), // in seconds
    totalCalories: integer('total_calories'),
    status: text('status', {
        enum: ['planned', 'in_progress', 'completed', 'skipped'],
    }).default('planned'),
    // notes: text('notes'),
    // rating: integer('rating'), // 1-5 stars
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
    updatedAt: timestamp('updated_at')
        .$defaultFn(() => new Date())
        .notNull(),
});

// Exercise Results/Logs
export const exerciseResult = pgTable('exercise_result', {
    id: text('id').primaryKey(),
    workoutSessionId: text('workout_session_id').references(
        () => workoutSession.id,
        { onDelete: 'cascade' }
    ),
    exerciseId: text('exercise_id').references(() => exercise.id, {
        onDelete: 'cascade',
    }),
    userId: text('user_id').references(() => user.id, { onDelete: 'cascade' }),
    sets: integer('sets'),
    reps: integer('reps'),
    duration: integer('duration'), // in seconds
    // weight: real('weight'), // in kg
    // distance: real('distance'), // in meters/km
    // calories: integer('calories'),
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
    id: text('id').primaryKey(),
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
    recordedBy: text('recorded_by').references(() => user.id), // who recorded it (user or trainer)
    notes: text('notes'),
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
});

// Messages between trainers and users
export const message = pgTable('message', {
    id: text('id').primaryKey(),
    senderId: text('sender_id').references(() => user.id, {
        onDelete: 'cascade',
    }),
    recipientId: text('recipient_id').references(() => user.id, {
        onDelete: 'cascade',
    }),
    content: text('content').notNull(),
    type: text('type', {
        enum: ['text', 'image', 'video', 'audio', 'file'],
    }).default('text'),
    // fileUrl: text('file_url'),
    readAt: timestamp('read_at'),
    isRead: boolean('is_read').default(false),
    replyToId: text('reply_to_id'), // references message.id
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
});

// User Goals
export const userGoal = pgTable('user_goal', {
    id: text('id').primaryKey(),
    userId: text('user_id').references(() => user.id, { onDelete: 'cascade' }),
    type: text('type', {
        enum: [
            'weight_loss',
            'weight_gain',
            'muscle_gain',
            'endurance',
            'strength',
            'flexibility',
            'custom',
        ],
    }).notNull(),
    title: text('title').notNull(),
    description: text('description'),
    targetValue: real('target_value'),
    currentValue: real('current_value').default(0),
    unit: text('unit'), // kg, cm, minutes, etc.
    targetDate: timestamp('target_date'),
    status: text('status', {
        enum: ['active', 'completed', 'paused', 'cancelled'],
    }).default('active'),
    priority: text('priority', { enum: ['low', 'medium', 'high'] }).default(
        'medium'
    ),
    createdAt: timestamp('created_at')
        .$defaultFn(() => new Date())
        .notNull(),
    updatedAt: timestamp('updated_at')
        .$defaultFn(() => new Date())
        .notNull(),
});
