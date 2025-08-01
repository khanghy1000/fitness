import { extendZodWithOpenApi } from '@asteasolutions/zod-to-openapi';
import { z } from 'zod';

extendZodWithOpenApi(z);

// Common schemas with OpenAPI metadata
export const idParamSchema = z
    .object({
        id: z
            .string()
            .regex(/^\d+$/, 'ID must be a valid number')
            .transform(Number)
            .openapi({ description: 'Unique identifier', example: '123' }),
    })
    .openapi('IdParam');

export const dayIdParamSchema = z
    .object({
        dayId: z
            .string()
            .regex(/^\d+$/, 'Day ID must be a valid number')
            .transform(Number)
            .openapi({
                description: 'Workout plan day identifier',
                example: '456',
            }),
    })
    .openapi('DayIdParam');

export const nutritionPlanIdParamSchema = z
    .object({
        nutritionPlanId: z
            .string()
            .regex(/^\d+$/, 'Nutrition Plan ID must be a valid number')
            .transform(Number)
            .openapi({
                description: 'Nutrition plan identifier',
                example: '123',
            }),
    })
    .openapi('NutritionPlanIdParam');

export const myQuerySchema = z
    .object({
        my: z.enum(['true', 'false']).optional().openapi({
            description: 'Filter by current user resources',
            example: 'true',
        }),
    })
    .openapi('MyQuery');

export const paginationQuerySchema = z
    .object({
        limit: z
            .string()
            .optional()
            .transform((val) => (val ? parseInt(val) : undefined))
            .openapi({
                description: 'Number of items to return',
                example: '10',
            }),
        offset: z
            .string()
            .optional()
            .transform((val) => (val ? parseInt(val) : undefined))
            .openapi({ description: 'Number of items to skip', example: '0' }),
    })
    .openapi('PaginationQuery');

// Connection schemas
export const connectRequestSchema = z
    .object({
        coachId: z.string().min(1, 'Coach ID is required').openapi({
            description: 'ID of the coach to connect with',
            example: 'coach123',
        }),
        notes: z.string().optional().openapi({
            description: 'Optional notes for connection request',
            example: 'Looking for strength training guidance',
        }),
    })
    .openapi('ConnectRequest');

export const connectionRequestTypeSchema = z
    .object({
        type: z
            .enum(['sent', 'received'], {
                message: 'Type must be "sent" or "received"',
            })
            .openapi({
                description: 'Type of connection requests to retrieve',
                example: 'sent',
            }),
    })
    .openapi('ConnectionRequestType');

export const traineeIdSchema = z
    .object({
        traineeId: z.string().min(1, 'Trainee ID is required').openapi({
            description: 'ID of the trainee',
            example: 'trainee456',
        }),
    })
    .openapi('TraineeId');

export const traineeParamSchema = z
    .object({
        traineeId: z.string().min(1, 'Trainee ID is required').openapi({
            description: 'Trainee identifier from URL parameter',
            example: 'trainee789',
        }),
    })
    .openapi('TraineeParam');

// Nutrition schemas
const foodSchema = z
    .object({
        name: z.string().min(1, 'Food name is required').openapi({
            description: 'Name of the food item',
            example: 'Grilled Chicken Breast',
        }),
        quantity: z
            .string()
            .min(1, 'Quantity is required')
            .openapi({ description: 'Quantity of the food', example: '150g' }),
        calories: z
            .number()
            .min(0, 'Calories must be non-negative')
            .openapi({ description: 'Calories per serving', example: 231 }),
        protein: z.number().min(0).optional().openapi({
            description: 'Protein content in grams',
            example: 43.5,
        }),
        carbs: z.number().min(0).optional().openapi({
            description: 'Carbohydrate content in grams',
            example: 0,
        }),
        fat: z
            .number()
            .min(0)
            .optional()
            .openapi({ description: 'Fat content in grams', example: 5.0 }),
        fiber: z
            .number()
            .min(0)
            .optional()
            .openapi({ description: 'Fiber content in grams', example: 0 }),
    })
    .openapi('Food');

const mealSchema = z
    .object({
        name: z
            .string()
            .min(1, 'Meal name is required')
            .openapi({ description: 'Name of the meal', example: 'Breakfast' }),
        time: z
            .string()
            .regex(
                /^([0-1]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$/,
                'Time must be in HH:MM:SS format'
            )
            .openapi({
                description: 'Time when meal should be consumed',
                example: '08:00:00',
            }),
        calories: z.number().min(0).optional().openapi({
            description: 'Total calories for the meal',
            example: 450,
        }),
        protein: z.number().min(0).optional().openapi({
            description: 'Total protein for the meal',
            example: 25.5,
        }),
        carbs: z.number().min(0).optional().openapi({
            description: 'Total carbs for the meal',
            example: 45.0,
        }),
        fat: z
            .number()
            .min(0)
            .optional()
            .openapi({ description: 'Total fat for the meal', example: 12.0 }),
        fiber: z
            .number()
            .min(0)
            .optional()
            .openapi({ description: 'Total fiber for the meal', example: 8.0 }),
        foods: z
            .array(foodSchema)
            .min(1, 'At least one food is required')
            .openapi({ description: 'List of foods in this meal' }),
    })
    .openapi('Meal');

const weekdayPlanSchema = z
    .object({
        weekday: z
            .enum(['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'], {
                message:
                    'Weekday must be one of: sun, mon, tue, wed, thu, fri, sat',
            })
            .openapi({ description: 'Day of the week', example: 'mon' }),
        totalCalories: z.number().min(0).optional().openapi({
            description: 'Total calories for the day',
            example: 2000,
        }),
        protein: z.number().min(0).optional().openapi({
            description: 'Total protein for the day',
            example: 150,
        }),
        carbs: z
            .number()
            .min(0)
            .optional()
            .openapi({ description: 'Total carbs for the day', example: 200 }),
        fat: z
            .number()
            .min(0)
            .optional()
            .openapi({ description: 'Total fat for the day', example: 66 }),
        fiber: z
            .number()
            .min(0)
            .optional()
            .openapi({ description: 'Total fiber for the day', example: 25 }),
        meals: z
            .array(mealSchema)
            .min(1, 'At least one meal is required')
            .openapi({ description: 'List of meals for this day' }),
    })
    .openapi('WeekdayPlan');

export const createNutritionPlanSchema = z
    .object({
        name: z.string().min(1, 'Name is required').max(255).openapi({
            description: 'Name of the nutrition plan',
            example: 'High Protein Diet',
        }),
        description: z.string().optional().openapi({
            description: 'Description of the nutrition plan',
            example: 'A diet plan focused on muscle building',
        }),
    })
    .openapi('CreateNutritionPlan');

export const updateNutritionPlanSchema = z
    .object({
        name: z
            .string()
            .min(1, 'Name is required')
            .max(255)
            .optional()
            .openapi({ description: 'Updated name of the nutrition plan' }),
        description: z.string().optional().openapi({
            description: 'Updated description of the nutrition plan',
        }),
        isActive: z.boolean().optional().openapi({
            description: 'Whether the nutrition plan is active',
            example: true,
        }),
    })
    .openapi('UpdateNutritionPlan');

export const assignNutritionPlanSchema = z
    .object({
        userId: z.string().min(1, 'User ID is required').openapi({
            description: 'ID of the user to assign the plan to',
            example: 'user123',
        }),
        startDate: z
            .string()
            .refine((date) => !isNaN(Date.parse(date)), {
                message: 'Invalid start date format',
            })
            .openapi({
                description: 'Start date for the nutrition plan',
                example: '2025-01-01',
            }),
        endDate: z
            .string()
            .refine((date) => !isNaN(Date.parse(date)), {
                message: 'Invalid end date format',
            })
            .optional()
            .openapi({
                description: 'End date for the nutrition plan',
                example: '2025-03-01',
            }),
    })
    .openapi('AssignNutritionPlan');

export const mealCompletionSchema = z
    .object({
        date: z
            .string()
            .regex(/^\d{4}-\d{2}-\d{2}$/, 'Date must be in YYYY-MM-DD format')
            .refine((date) => !isNaN(Date.parse(date)), {
                message: 'Invalid date format',
            })
            .optional()
            .openapi({
                description:
                    'Date when the meal was completed (YYYY-MM-DD format, defaults to current date). Only the date part is used for adherence tracking.',
                example: '2025-01-15',
            }),
        caloriesConsumed: z
            .number()
            .min(0)
            .optional()
            .openapi({ description: 'Actual calories consumed', example: 420 }),
        proteinConsumed: z
            .number()
            .min(0)
            .optional()
            .openapi({ description: 'Actual protein consumed', example: 23.5 }),
        carbsConsumed: z
            .number()
            .min(0)
            .optional()
            .openapi({ description: 'Actual carbs consumed', example: 40.0 }),
        fatConsumed: z
            .number()
            .min(0)
            .optional()
            .openapi({ description: 'Actual fat consumed', example: 11.0 }),
        fiberConsumed: z
            .number()
            .min(0)
            .optional()
            .openapi({ description: 'Actual fiber consumed', example: 7.5 }),
        notes: z.string().optional().openapi({
            description: 'Notes about the meal completion',
            example: 'Felt satisfied after this meal',
        }),
    })
    .openapi('MealCompletion');

// Workout schemas
export const createWorkoutPlanSchema = z
    .object({
        name: z.string().min(1, 'Plan name is required').trim().openapi({
            description: 'Name of the workout plan',
            example: 'Push Pull Legs',
        }),
        description: z.string().optional().openapi({
            description: 'Description of the workout plan',
            example: 'A 6-day split focusing on push, pull, and leg movements',
        }),
        difficulty: z
            .enum(['beginner', 'intermediate', 'advanced'], {
                message: 'Invalid difficulty level',
            })
            .optional()
            .openapi({
                description: 'Difficulty level of the workout',
                example: 'intermediate',
            }),
        estimatedCalories: z.number().positive().optional().openapi({
            description: 'Estimated calories burned per session',
            example: 350,
        }),
    })
    .openapi('CreateWorkoutPlan');

export const updateWorkoutPlanSchema = z
    .object({
        name: z
            .string()
            .min(1, 'Invalid plan name')
            .trim()
            .optional()
            .openapi({ description: 'Updated name of the workout plan' }),
        description: z.string().optional().openapi({
            description: 'Updated description of the workout plan',
        }),
        difficulty: z
            .enum(['beginner', 'intermediate', 'advanced'], {
                message: 'Invalid difficulty level',
            })
            .optional()
            .openapi({ description: 'Updated difficulty level' }),
        estimatedCalories: z
            .number()
            .positive()
            .optional()
            .openapi({ description: 'Updated estimated calories burned' }),
        isActive: z.boolean().optional().openapi({
            description: 'Whether the workout plan is active',
            example: true,
        }),
    })
    .openapi('UpdateWorkoutPlan');

export const assignWorkoutPlanSchema = z
    .object({
        userId: z.string().min(1, 'User ID is required').openapi({
            description: 'ID of the user to assign the plan to',
            example: 'user456',
        }),
        startDate: z
            .string()
            .refine((date) => !isNaN(Date.parse(date)), {
                message: 'Start date is required and must be valid',
            })
            .openapi({
                description: 'Start date for the workout plan',
                example: '2025-01-01',
            }),
        endDate: z
            .string()
            .refine((date) => !isNaN(Date.parse(date)), {
                message: 'Invalid end date format',
            })
            .optional()
            .openapi({
                description: 'End date for the workout plan',
                example: '2025-06-01',
            }),
    })
    .openapi('AssignWorkoutPlan');

export const addDayToWorkoutPlanSchema = z
    .object({
        day: z.number().positive('Day must be a positive number').openapi({
            description: 'Day number in the workout plan',
            example: 1,
        }),
        isRestDay: z.boolean().default(false).openapi({
            description: 'Whether this is a rest day',
            example: false,
        }),
        estimatedCalories: z.number().positive().optional().openapi({
            description: 'Estimated calories burned for this day',
            example: 400,
        }),
        duration: z.number().positive().optional().openapi({
            description: 'Estimated duration in minutes',
            example: 60,
        }),
    })
    .openapi('AddDayToWorkoutPlan');

export const addExerciseToPlanDaySchema = z
    .object({
        exerciseTypeId: z
            .number()
            .positive('Exercise type ID is required')
            .openapi({ description: 'ID of the exercise type', example: 15 }),
        order: z.number().positive().optional().openapi({
            description: 'Order of exercise in the workout',
            example: 1,
        }),
        targetReps: z.number().positive().optional().openapi({
            description: 'Target number of repetitions',
            example: 12,
        }),
        targetDuration: z.number().positive().optional().openapi({
            description: 'Target duration in seconds',
            example: 300,
        }),
        estimatedCalories: z.number().positive().optional().openapi({
            description: 'Estimated calories burned for this exercise',
            example: 50,
        }),
        notes: z.string().optional().openapi({
            description: 'Additional notes for the exercise',
            example: 'Focus on form over speed',
        }),
    })
    .openapi('AddExerciseToPlanDay');

// Planned Workout schemas
export const createPlannedWorkoutSchema = z
    .object({
        userWorkoutPlanId: z
            .number()
            .positive()
            .or(z.string().transform(Number))
            .openapi({
                description: 'ID of the user workout plan',
                example: 123,
            }),
        weekdays: z
            .array(z.enum(['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat']))
            .min(1, 'At least one weekday is required')
            .openapi({
                description: 'Days of the week when workout is scheduled',
                example: ['mon', 'wed', 'fri'],
            }),
        time: z
            .string()
            .regex(/^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$/, {
                message: 'Time must be in HH:MM or HH:MM:SS format',
            })
            .openapi({
                description: 'Time when workout is scheduled',
                example: '07:00',
            }),
        isActive: z.boolean().optional().default(true).openapi({
            description: 'Whether the planned workout is active',
            example: true,
        }),
    })
    .openapi('CreatePlannedWorkout');

export const updatePlannedWorkoutSchema = z
    .object({
        weekdays: z
            .array(z.enum(['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat']))
            .min(1, 'At least one weekday is required')
            .optional()
            .openapi({ description: 'Updated days of the week' }),
        time: z
            .string()
            .regex(/^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$/, {
                message: 'Time must be in HH:MM or HH:MM:SS format',
            })
            .optional()
            .openapi({ description: 'Updated time for the workout' }),
        isActive: z
            .boolean()
            .optional()
            .openapi({ description: 'Updated active status' }),
    })
    .openapi('UpdatePlannedWorkout');

export const recordExerciseResultSchema = z
    .object({
        workoutPlanDayExerciseId: z
            .number()
            .positive()
            .or(z.string().transform(Number))
            .openapi({
                description: 'ID of the workout plan day exercise',
                example: 789,
            }),
        userWorkoutPlanId: z
            .number()
            .positive()
            .or(z.string().transform(Number))
            .openapi({
                description: 'ID of the user workout plan',
                example: 123,
            }),
        reps: z.number().positive().optional().openapi({
            description: 'Number of repetitions completed',
            example: 15,
        }),
        duration: z.number().positive().optional().openapi({
            description: 'Duration of exercise in seconds',
            example: 180,
        }),
        calories: z.number().positive().optional().openapi({
            description: 'Calories burned during exercise',
            example: 45,
        }),
    })
    .openapi('RecordExerciseResult');

export const availableSlotsQuerySchema = z
    .object({
        date: z
            .string()
            .refine((date) => !isNaN(Date.parse(date)), {
                message: 'Date is required and must be valid',
            })
            .openapi({
                description: 'Date to check available slots',
                example: '2025-01-15',
            }),
        userWorkoutPlanId: z
            .string()
            .transform((val) => parseInt(val))
            .openapi({ description: 'User workout plan ID', example: '123' }),
    })
    .openapi('AvailableSlotsQuery');

// User schemas
export const goalTypes = [
    'weight_loss',
    'weight_gain',
    'muscle_gain',
    'endurance',
    'strength',
    'flexibility',
    'custom',
] as const;
export const goalStatuses = [
    'active',
    'completed',
    'paused',
    'cancelled',
] as const;
export const goalPriorities = ['low', 'medium', 'high'] as const;

export const createUserGoalSchema = z
    .object({
        type: z
            .enum(goalTypes, {
                message: 'Invalid goal type',
            })
            .openapi({
                description: 'Type of fitness goal',
                example: 'muscle_gain',
            }),
        title: z.string().min(1, 'Title is required').trim().openapi({
            description: 'Title of the goal',
            example: 'Gain 10kg muscle mass',
        }),
        description: z.string().optional().openapi({
            description: 'Detailed description of the goal',
            example: 'Focus on upper body strength and mass',
        }),
        targetValue: z
            .number()
            .positive()
            .optional()
            .openapi({ description: 'Target value for the goal', example: 10 }),
        unit: z
            .string()
            .optional()
            .openapi({ description: 'Unit of measurement', example: 'kg' }),
        targetDate: z
            .string()
            .refine((date) => !isNaN(Date.parse(date)), {
                message: 'Invalid target date format',
            })
            .optional()
            .openapi({
                description: 'Target completion date',
                example: '2025-12-31',
            }),
        priority: z.enum(goalPriorities).optional().openapi({
            description: 'Priority level of the goal',
            example: 'high',
        }),
    })
    .openapi('CreateUserGoal');

export const updateUserGoalSchema = z
    .object({
        title: z
            .string()
            .min(1, 'Invalid title')
            .trim()
            .optional()
            .openapi({ description: 'Updated title of the goal' }),
        description: z
            .string()
            .optional()
            .openapi({ description: 'Updated description' }),
        targetValue: z
            .number()
            .positive()
            .optional()
            .openapi({ description: 'Updated target value' }),
        currentValue: z
            .number()
            .min(0)
            .optional()
            .openapi({ description: 'Current progress value', example: 3.5 }),
        unit: z
            .string()
            .optional()
            .openapi({ description: 'Updated unit of measurement' }),
        targetDate: z
            .string()
            .refine((date) => !isNaN(Date.parse(date)), {
                message: 'Invalid target date format',
            })
            .optional()
            .openapi({ description: 'Updated target date' }),
        status: z
            .enum(goalStatuses, {
                message: 'Invalid status',
            })
            .optional()
            .openapi({
                description: 'Updated status of the goal',
                example: 'active',
            }),
        priority: z
            .enum(goalPriorities, {
                message: 'Invalid priority',
            })
            .optional()
            .openapi({ description: 'Updated priority level' }),
    })
    .openapi('UpdateUserGoal');

export const recordUserStatsSchema = z
    .object({
        weight: z
            .number()
            .positive()
            .optional()
            .openapi({ description: 'Body weight in kg', example: 75.5 }),
        height: z
            .number()
            .positive()
            .optional()
            .openapi({ description: 'Height in cm', example: 175 }),
        bodyFat: z
            .number()
            .min(0)
            .max(100)
            .optional()
            .openapi({ description: 'Body fat percentage', example: 15.2 }),
        muscleMass: z
            .number()
            .positive()
            .optional()
            .openapi({ description: 'Muscle mass in kg', example: 35.8 }),
        chest: z
            .number()
            .positive()
            .optional()
            .openapi({ description: 'Chest measurement in cm', example: 102 }),
        waist: z
            .number()
            .positive()
            .optional()
            .openapi({ description: 'Waist measurement in cm', example: 85 }),
        hips: z
            .number()
            .positive()
            .optional()
            .openapi({ description: 'Hip measurement in cm', example: 95 }),
        arms: z
            .number()
            .positive()
            .optional()
            .openapi({ description: 'Arm measurement in cm', example: 38 }),
        thighs: z
            .number()
            .positive()
            .optional()
            .openapi({ description: 'Thigh measurement in cm', example: 58 }),
        notes: z.string().optional().openapi({
            description: 'Additional notes about measurements',
            example: 'Measured in the morning',
        }),
    })
    .openapi('RecordUserStats');

export const nutritionAdherenceSchema = z
    .object({
        date: z
            .string()
            .regex(/^\d{4}-\d{2}-\d{2}$/, 'Date must be in YYYY-MM-DD format')
            .refine((date) => !isNaN(Date.parse(date)), {
                message: 'Invalid date format',
            })
            .optional()
            .openapi({
                description: 'Date of adherence record (YYYY-MM-DD format)',
                example: '2025-01-15',
            }),
        weekday: z
            .enum(['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'])
            .optional()
            .openapi({ description: 'Day of the week', example: 'mon' }),
        totalMeals: z.number().positive().optional().openapi({
            description: 'Total number of meals planned',
            example: 5,
        }),
        adherencePercentage: z
            .number()
            .min(0)
            .max(100, 'Adherence percentage must be between 0 and 100')
            .optional()
            .openapi({
                description: 'Percentage of plan followed',
                example: 85.5,
            }),
        notes: z.string().optional().openapi({
            description: 'Notes about adherence',
            example: 'Skipped afternoon snack due to meeting',
        }),
    })
    .openapi('NutritionAdherence');

export const userSearchQuerySchema = z
    .object({
        query: z.string().min(1, 'Search query is required').trim().openapi({
            description: 'Search term for finding users',
            example: 'john',
        }),
        role: z.enum(['coach', 'trainee']).optional().openapi({
            description: 'Filter by user role',
            example: 'trainee',
        }),
    })
    .openapi('UserSearchQuery');

// Response schemas
export const errorResponseSchema = z
    .object({
        error: z.string().openapi({
            description: 'Error message',
            example: 'Resource not found',
        }),
        status: z
            .number()
            .optional()
            .openapi({ description: 'HTTP status code', example: 404 }),
    })
    .openapi('ErrorResponse');

export const successMessageSchema = z
    .object({
        message: z.string().openapi({
            description: 'Success message',
            example: 'Operation completed successfully',
        }),
    })
    .openapi('SuccessMessage');

export const userIdNameEmailSchema = z
    .object({
        id: z.string().openapi({ description: 'Trainee user ID' }),
        name: z.string().openapi({
            description: 'Trainee full name',
            example: 'John Doe',
        }),
        email: z.string().email().openapi({
            description: 'Trainee email',
            example: 'john@example.com',
        }),
    })
    .openapi('UserIdNameEmail');

export const userSchema = z
    .object({
        id: z.string().openapi({ description: 'User ID', example: 'user123' }),
        email: z.string().email().openapi({
            description: 'User email',
            example: 'john.doe@example.com',
        }),
        name: z
            .string()
            .openapi({ description: 'User full name', example: 'John Doe' }),
        role: z
            .enum(['coach', 'trainee'])
            .openapi({ description: 'User role', example: 'trainee' }),
    })
    .openapi('User');

export const exerciseTypeSchema = z
    .object({
        id: z.number().openapi({ description: 'Exercise type ID', example: 1 }),
        name: z
            .string()
            .openapi({ description: 'Exercise name', example: 'Push-ups' }),
        devicePosition: z.enum(['thigh', 'arm', 'none']).openapi({
            description: 'Device position for tracking',
            example: 'thigh',
        }),
        logType: z.enum(['reps', 'duration']).openapi({
            description: 'Type of logging for this exercise',
            example: 'reps',
        }),
    })
    .openapi('ExerciseType');

export const workoutPlanSchema = z
    .object({
        id: z.number().openapi({ description: 'Workout plan ID', example: 1 }),
        name: z
            .string()
            .openapi({ description: 'Plan name', example: 'Push Pull Legs' }),
        description: z
            .string()
            .optional()
            .openapi({ description: 'Plan description' }),
        difficulty: z.enum(['beginner', 'intermediate', 'advanced']).optional(),
        estimatedCalories: z
            .number()
            .optional()
            .openapi({ description: 'Estimated calories per session' }),
        createdBy: z.string().openapi({ description: 'Creator user ID' }),
        isActive: z
            .boolean()
            .openapi({ description: 'Whether plan is active' }),
        createdAt: z.string().openapi({ description: 'Creation date' }),
        updatedAt: z.string().openapi({ description: 'Last update date' }),
    })
    .openapi('WorkoutPlan');

export const nutritionPlanSchema = z
    .object({
        id: z
            .number()
            .openapi({ description: 'Nutrition plan ID', example: 1 }),
        name: z.string().openapi({
            description: 'Plan name',
            example: 'High Protein Diet',
        }),
        description: z
            .string()
            .optional()
            .openapi({ description: 'Plan description' }),
        createdBy: z.string().openapi({ description: 'Creator user ID' }),
        isActive: z
            .boolean()
            .openapi({ description: 'Whether plan is active' }),
        createdAt: z.string().openapi({ description: 'Creation date' }),
        updatedAt: z.string().openapi({ description: 'Last update date' }),
    })
    .openapi('NutritionPlan');

export const connectionWithoutCoachTraineeSchema = z
    .object({
        id: z.number().openapi({ description: 'Connection ID', example: 1 }),
        coachId: z.string().openapi({ description: 'Coach user ID' }),
        traineeId: z.string().openapi({ description: 'Trainee user ID' }),
        status: z
            .enum(['pending', 'active', 'inactive', 'blocked'])
            .openapi({ description: 'Connection status' }),
        startDate: z
            .string()
            .nullable()
            .optional()
            .openapi({ description: 'Connection start date' }),
        endDate: z
            .string()
            .nullable()
            .optional()
            .openapi({ description: 'Connection end date' }),
        notes: z
            .string()
            .nullable()
            .optional()
            .openapi({ description: 'Connection notes' }),
        createdAt: z.string().openapi({ description: 'Creation date' }),
        updatedAt: z.string().openapi({ description: 'Last update date' }),
    })
    .openapi('ConnectionWithoutCoachTrainee');

export const connectionSchema = z
    .object({
        id: z.number().openapi({ description: 'Connection ID', example: 1 }),
        coachId: z.string().openapi({ description: 'Coach user ID' }),
        traineeId: z.string().openapi({ description: 'Trainee user ID' }),
        status: z
            .enum(['pending', 'active', 'inactive', 'blocked'])
            .openapi({ description: 'Connection status' }),
        startDate: z
            .string()
            .nullable()
            .optional()
            .openapi({ description: 'Connection start date' }),
        endDate: z
            .string()
            .nullable()
            .optional()
            .openapi({ description: 'Connection end date' }),
        notes: z
            .string()
            .nullable()
            .optional()
            .openapi({ description: 'Connection notes' }),
        createdAt: z.string().openapi({ description: 'Creation date' }),
        updatedAt: z.string().openapi({ description: 'Last update date' }),
        coach: userIdNameEmailSchema,
        trainee: userIdNameEmailSchema,
    })
    .openapi('Connection');

// Nutrition Plan Day schemas
export const createNutritionPlanDaySchema = z
    .object({
        weekday: z
            .enum(['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'], {
                message:
                    'Weekday must be one of: sun, mon, tue, wed, thu, fri, sat',
            })
            .openapi({ description: 'Day of the week', example: 'mon' }),
        totalCalories: z.number().min(0).optional().openapi({
            description: 'Total calories for the day',
            example: 2000,
        }),
        protein: z.number().min(0).optional().openapi({
            description: 'Total protein for the day',
            example: 150,
        }),
        carbs: z.number().min(0).optional().openapi({
            description: 'Total carbs for the day',
            example: 200,
        }),
        fat: z.number().min(0).optional().openapi({
            description: 'Total fat for the day',
            example: 66,
        }),
        fiber: z.number().min(0).optional().openapi({
            description: 'Total fiber for the day',
            example: 25,
        }),
    })
    .openapi('CreateNutritionPlanDay');

export const updateNutritionPlanDaySchema = z
    .object({
        weekday: z
            .enum(['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'], {
                message:
                    'Weekday must be one of: sun, mon, tue, wed, thu, fri, sat',
            })
            .optional()
            .openapi({ description: 'Day of the week', example: 'mon' }),
        totalCalories: z.number().min(0).optional().openapi({
            description: 'Total calories for the day',
            example: 2000,
        }),
        protein: z.number().min(0).optional().openapi({
            description: 'Total protein for the day',
            example: 150,
        }),
        carbs: z.number().min(0).optional().openapi({
            description: 'Total carbs for the day',
            example: 200,
        }),
        fat: z.number().min(0).optional().openapi({
            description: 'Total fat for the day',
            example: 66,
        }),
        fiber: z.number().min(0).optional().openapi({
            description: 'Total fiber for the day',
            example: 25,
        }),
    })
    .openapi('UpdateNutritionPlanDay');

// Nutrition Plan Meal schemas
export const createNutritionPlanMealSchema = z
    .object({
        name: z.string().min(1, 'Meal name is required').openapi({
            description: 'Name of the meal',
            example: 'Breakfast',
        }),
        time: z
            .string()
            .regex(/^([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$/, {
                message: 'Time must be in HH:MM:SS format',
            })
            .openapi({
                description: 'Time when meal should be consumed',
                example: '08:00:00',
            }),
        calories: z.number().min(0).optional().openapi({
            description: 'Total calories for the meal',
            example: 450,
        }),
        protein: z.number().min(0).optional().openapi({
            description: 'Total protein for the meal',
            example: 25.5,
        }),
        carbs: z.number().min(0).optional().openapi({
            description: 'Total carbs for the meal',
            example: 45.0,
        }),
        fat: z.number().min(0).optional().openapi({
            description: 'Total fat for the meal',
            example: 12.0,
        }),
        fiber: z.number().min(0).optional().openapi({
            description: 'Total fiber for the meal',
            example: 8.0,
        }),
    })
    .openapi('CreateNutritionPlanMeal');

export const updateNutritionPlanMealSchema = z
    .object({
        name: z.string().min(1, 'Meal name is required').optional().openapi({
            description: 'Name of the meal',
            example: 'Breakfast',
        }),
        time: z
            .string()
            .regex(/^([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$/, {
                message: 'Time must be in HH:MM:SS format',
            })
            .optional()
            .openapi({
                description: 'Time when meal should be consumed',
                example: '08:00:00',
            }),
        calories: z.number().min(0).optional().openapi({
            description: 'Total calories for the meal',
            example: 450,
        }),
        protein: z.number().min(0).optional().openapi({
            description: 'Total protein for the meal',
            example: 25.5,
        }),
        carbs: z.number().min(0).optional().openapi({
            description: 'Total carbs for the meal',
            example: 45.0,
        }),
        fat: z.number().min(0).optional().openapi({
            description: 'Total fat for the meal',
            example: 12.0,
        }),
        fiber: z.number().min(0).optional().openapi({
            description: 'Total fiber for the meal',
            example: 8.0,
        }),
    })
    .openapi('UpdateNutritionPlanMeal');

// Nutrition Plan Food schemas
export const createNutritionPlanFoodSchema = z
    .object({
        name: z.string().min(1, 'Food name is required').openapi({
            description: 'Name of the food',
            example: 'Greek Yogurt',
        }),
        quantity: z.string().min(1, 'Quantity is required').openapi({
            description: 'Quantity of the food',
            example: '1 cup',
        }),
        calories: z.number().min(0, 'Calories cannot be negative').openapi({
            description: 'Calories in the food',
            example: 130,
        }),
        protein: z.number().min(0).optional().openapi({
            description: 'Protein content',
            example: 20.0,
        }),
        carbs: z.number().min(0).optional().openapi({
            description: 'Carbs content',
            example: 9.0,
        }),
        fat: z.number().min(0).optional().openapi({
            description: 'Fat content',
            example: 0.7,
        }),
        fiber: z.number().min(0).optional().openapi({
            description: 'Fiber content',
            example: 0.0,
        }),
    })
    .openapi('CreateNutritionPlanFood');

export const updateNutritionPlanFoodSchema = z
    .object({
        name: z.string().min(1, 'Food name is required').optional().openapi({
            description: 'Name of the food',
            example: 'Greek Yogurt',
        }),
        quantity: z.string().min(1, 'Quantity is required').optional().openapi({
            description: 'Quantity of the food',
            example: '1 cup',
        }),
        calories: z
            .number()
            .min(0, 'Calories cannot be negative')
            .optional()
            .openapi({
                description: 'Calories in the food',
                example: 130,
            }),
        protein: z.number().min(0).optional().openapi({
            description: 'Protein content',
            example: 20.0,
        }),
        carbs: z.number().min(0).optional().openapi({
            description: 'Carbs content',
            example: 9.0,
        }),
        fat: z.number().min(0).optional().openapi({
            description: 'Fat content',
            example: 0.7,
        }),
        fiber: z.number().min(0).optional().openapi({
            description: 'Fiber content',
            example: 0.0,
        }),
    })
    .openapi('UpdateNutritionPlanFood');

// Workout Plan Day schemas
export const updateWorkoutPlanDaySchema = z
    .object({
        day: z
            .number()
            .positive('Day must be a positive number')
            .optional()
            .openapi({
                description: 'Day number in the workout plan',
                example: 1,
            }),
        isRestDay: z.boolean().optional().openapi({
            description: 'Whether this is a rest day',
            example: false,
        }),
        estimatedCalories: z.number().positive().optional().openapi({
            description: 'Estimated calories burned for this day',
            example: 400,
        }),
        duration: z.number().positive().optional().openapi({
            description: 'Estimated duration in minutes',
            example: 60,
        }),
    })
    .openapi('UpdateWorkoutPlanDay');

// Workout Plan Day Exercise schemas
export const updateExerciseInPlanDaySchema = z
    .object({
        exerciseTypeId: z
            .number()
            .positive('Exercise type ID is required')
            .optional()
            .openapi({ description: 'ID of the exercise type', example: 15 }),
        order: z.number().positive().optional().openapi({
            description: 'Order of exercise in the workout',
            example: 1,
        }),
        targetReps: z.number().positive().optional().openapi({
            description: 'Target number of repetitions',
            example: 12,
        }),
        targetDuration: z.number().positive().optional().openapi({
            description: 'Target duration in seconds',
            example: 300,
        }),
        estimatedCalories: z.number().positive().optional().openapi({
            description: 'Estimated calories burned for this exercise',
            example: 50,
        }),
        notes: z.string().optional().openapi({
            description: 'Additional notes for the exercise',
            example: 'Focus on form over speed',
        }),
    })
    .openapi('UpdateExerciseInPlanDay');

// Planned Workout ID parameter schemas
export const userNutritionPlanIdParamSchema = z
    .object({
        userNutritionPlanId: z
            .string()
            .regex(/^\d+$/, 'User Nutrition Plan ID must be a valid number')
            .transform(Number)
            .openapi({
                description: 'User nutrition plan identifier',
                example: '456',
            }),
    })
    .openapi('UserNutritionPlanIdParam');

export const userWorkoutPlanIdParamSchema = z
    .object({
        userWorkoutPlanId: z
            .string()
            .regex(/^\d+$/, 'User Workout Plan ID must be a valid number')
            .transform(Number)
            .openapi({
                description: 'User workout plan identifier',
                example: '789',
            }),
    })
    .openapi('UserWorkoutPlanIdParam');

// Workout Plan ID parameter schema
export const workoutPlanIdParamSchema = z
    .object({
        workoutPlanId: z
            .string()
            .regex(/^\d+$/, 'Workout Plan ID must be a valid number')
            .transform(Number)
            .openapi({
                description: 'Workout plan identifier',
                example: '123',
            }),
    })
    .openapi('WorkoutPlanIdParam');

export const mealIdParamSchema = z
    .object({
        mealId: z
            .string()
            .regex(/^\d+$/, 'Meal ID must be a valid number')
            .transform(Number)
            .openapi({
                description: 'Nutrition plan meal identifier',
                example: '789',
            }),
    })
    .openapi('MealIdParam');
