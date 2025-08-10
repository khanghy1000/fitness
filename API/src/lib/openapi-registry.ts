import {
    OpenAPIRegistry,
    OpenApiGeneratorV3,
    OpenApiGeneratorV31,
} from '@asteasolutions/zod-to-openapi';
import { z } from 'zod';
import {
    // Common schemas
    idParamSchema,
    nutritionPlanIdParamSchema,
    userNutritionPlanIdParamSchema,
    workoutPlanIdParamSchema,
    userWorkoutPlanIdParamSchema,
    mealIdParamSchema,
    successMessageSchema,
    userIdQuerySchema,
    userIdParamSchema,

    // Connection schemas
    connectRequestSchema,
    connectionRequestTypeSchema,
    traineeIdSchema,
    connectionSchema,
    connectionWithoutCoachTraineeSchema,

    // Exercise schemas
    exerciseTypeSchema,
    recordExerciseResultSchema,

    // Nutrition schemas
    assignNutritionPlanSchema,
    mealCompletionSchema,
    nutritionAdherenceSchema,
    nutritionPlanSchema,
    createNutritionPlanSchema,
    updateNutritionPlanSchema,
    nutritionPlanDaySchema,
    createNutritionPlanDaySchema,
    updateNutritionPlanDaySchema,
    nutritionPlanMealSchema,
    createNutritionPlanMealSchema,
    updateNutritionPlanMealSchema,
    nutritionPlanFoodSchema,
    createNutritionPlanFoodSchema,
    updateNutritionPlanFoodSchema,
    detailedNutritionPlanSchema,

    // Workout schemas
    assignWorkoutPlanSchema,
    workoutPlanSchema,
    createWorkoutPlanSchema,
    updateWorkoutPlanSchema,
    workoutPlanDaySchema,
    addDayToWorkoutPlanSchema,
    updateWorkoutPlanDaySchema,
    workoutPlanDayExerciseSchema,
    addExerciseToPlanDaySchema,
    updateExerciseInPlanDaySchema,
    bulkUpdateWorkoutPlanSchema,
    bulkUpdateNutritionPlanSchema,
    detailedWorkoutPlanSchema,

    // Planned workout schemas
    plannedWorkoutSchema,
    createPlannedWorkoutSchema,
    updatePlannedWorkoutSchema,

    // User schemas
    recordUserStatsSchema,
    userSearchQuerySchema,
    userSchema,
    userIdNameEmailSchema,
    detailedUserSchema,
    dayIdParamSchema,
} from '../validation/schemas.ts';
import { create } from 'domain';

const registry = new OpenAPIRegistry();

// Register schemas

// Common schemas
registry.register('userIdQuery', userIdQuerySchema);
registry.register('IdParam', idParamSchema);
registry.register('NutritionPlanIdParam', nutritionPlanIdParamSchema);
registry.register('UserNutritionPlanIdParam', userNutritionPlanIdParamSchema);
registry.register('WorkoutPlanIdParam', workoutPlanIdParamSchema);
registry.register('UserWorkoutPlanIdParam', userWorkoutPlanIdParamSchema);
registry.register('MealIdParam', mealIdParamSchema);
registry.register('UserIdParam', userIdParamSchema);
registry.register('SuccessMessage', successMessageSchema);
registry.register('DayIdParam', dayIdParamSchema);

// Bulk update schemas
registry.register('BulkUpdateWorkoutPlan', bulkUpdateWorkoutPlanSchema);
registry.register('BulkUpdateNutritionPlan', bulkUpdateNutritionPlanSchema);

// Connection schemas
registry.register('ConnectRequest', connectRequestSchema);
registry.register('ConnectionRequestType', connectionRequestTypeSchema);
registry.register('TraineeId', traineeIdSchema);
registry.register('Connection', connectionSchema);
registry.register(
    'ConnectionWithoutCoachTrainee',
    connectionWithoutCoachTraineeSchema
);

// Exercise schemas
registry.register('ExerciseType', exerciseTypeSchema);
registry.register('RecordExerciseResult', recordExerciseResultSchema);

// Nutrition schemas
registry.register('AssignNutritionPlan', assignNutritionPlanSchema);
registry.register('MealCompletion', mealCompletionSchema);
registry.register('NutritionAdherence', nutritionAdherenceSchema);
registry.register('NutritionPlan', nutritionPlanSchema);
registry.register('CreateNutritionPlan', createNutritionPlanSchema);
registry.register('UpdateNutritionPlan', updateNutritionPlanSchema);
registry.register('NutritionPlanDay', nutritionPlanDaySchema);
registry.register('CreateNutritionPlanDay', createNutritionPlanDaySchema);
registry.register('UpdateNutritionPlanDay', updateNutritionPlanDaySchema);
registry.register('NutritionPlanMeal', nutritionPlanMealSchema);
registry.register('CreateNutritionPlanMeal', createNutritionPlanMealSchema);
registry.register('UpdateNutritionPlanMeal', updateNutritionPlanMealSchema);
registry.register('NutritionPlanFood', nutritionPlanFoodSchema);
registry.register('CreateNutritionPlanFood', createNutritionPlanFoodSchema);
registry.register('UpdateNutritionPlanFood', updateNutritionPlanFoodSchema);
registry.register('DetailedNutritionPlan', detailedNutritionPlanSchema);

// Workout schemas
registry.register('AssignWorkoutPlan', assignWorkoutPlanSchema);
registry.register('WorkoutPlan', workoutPlanSchema);
registry.register('CreateWorkoutPlan', createWorkoutPlanSchema);
registry.register('UpdateWorkoutPlan', updateWorkoutPlanSchema);
registry.register('WorkoutPlanDay', workoutPlanDaySchema);
registry.register('AddDayToWorkoutPlan', addDayToWorkoutPlanSchema);
registry.register('UpdateWorkoutPlanDay', updateWorkoutPlanDaySchema);
registry.register('WorkoutPlanDayExercise', workoutPlanDayExerciseSchema);
registry.register('AddExerciseToPlanDay', addExerciseToPlanDaySchema);
registry.register('UpdateExerciseInPlanDay', updateExerciseInPlanDaySchema);
registry.register('DetailedWorkoutPlan', detailedWorkoutPlanSchema);

// Planned workout schemas
registry.register('PlannedWorkout', plannedWorkoutSchema);
registry.register('CreatePlannedWorkout', createPlannedWorkoutSchema);
registry.register('UpdatePlannedWorkout', updatePlannedWorkoutSchema);

// User schemas
registry.register('RecordUserStats', recordUserStatsSchema);
registry.register('UserSearchQuery', userSearchQuerySchema);
registry.register('User', userSchema);
registry.register('UserIdNameEmail', userIdNameEmailSchema);
registry.register('DetailedUser', detailedUserSchema);

// Register API paths

// Ping routes
registry.registerPath({
    method: 'get',
    path: '/api/ping',
    tags: ['Ping'],
    summary: 'Health check endpoint',
    description: 'Simple health check that returns pong',
    responses: {
        200: {
            description: 'Successful ping response',
            content: {
                'application/json': {
                    schema: z
                        .object({
                            message: z.string().openapi({ example: 'pong' }),
                        })
                        .openapi('PingResponse'),
                },
            },
        },
    },
});

// Connections routes
registry.registerPath({
    method: 'post',
    path: '/api/connections/connect',
    tags: ['Connections'],
    summary: 'Send connection request',
    description: 'Send a connection request from trainee to coach',
    request: {
        body: {
            description: 'Connection request data',
            content: {
                'application/json': {
                    schema: connectRequestSchema,
                },
            },
        },
    },
    responses: {
        201: {
            description: 'Connection request sent successfully',
            content: {
                'application/json': {
                    schema: connectionWithoutCoachTraineeSchema,
                },
            },
        },
        400: {
            description: 'Invalid input data or connection already exists',
        },
        401: {
            description: 'Unauthorized - Trainee role required',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/connections/requests/{type}',
    tags: ['Connections'],
    summary: 'Get connection requests',
    description: 'Get connection requests (sent or received)',
    request: {
        params: connectionRequestTypeSchema,
    },
    responses: {
        200: {
            description: 'List of connection requests',
            content: {
                'application/json': {
                    schema: z.array(connectionSchema),
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
        403: {
            description: 'Access denied - Role mismatch',
        },
    },
});

registry.registerPath({
    method: 'post',
    path: '/api/connections/accept',
    tags: ['Connections'],
    summary: 'Accept connection request',
    description: 'Accept a connection request (coach only)',
    request: {
        body: {
            description: 'Trainee ID to accept',
            content: {
                'application/json': {
                    schema: traineeIdSchema,
                },
            },
        },
    },
    responses: {
        200: {
            description: 'Connection request accepted',
            content: {
                'application/json': {
                    schema: connectionWithoutCoachTraineeSchema,
                },
            },
        },
        400: {
            description: 'Invalid input data',
        },
        401: {
            description: 'Unauthorized - Coach role required',
        },
        404: {
            description: 'Connection request not found',
        },
    },
});

registry.registerPath({
    method: 'post',
    path: '/api/connections/reject',
    tags: ['Connections'],
    summary: 'Reject connection request',
    description: 'Reject a connection request (coach only)',
    request: {
        body: {
            description: 'Trainee ID to reject',
            content: {
                'application/json': {
                    schema: traineeIdSchema,
                },
            },
        },
    },
    responses: {
        200: {
            description: 'Connection request rejected',
            content: {
                'application/json': {
                    schema: successMessageSchema,
                },
            },
        },
        400: {
            description: 'Invalid input data',
        },
        401: {
            description: 'Unauthorized - Coach role required',
        },
        404: {
            description: 'Connection request not found',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/connections/active',
    tags: ['Connections'],
    summary: 'Get active connections',
    description: 'Get list of active connections for the current user',
    responses: {
        200: {
            description: 'List of active connections',
            content: {
                'application/json': {
                    schema: z.array(connectionSchema),
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
        403: {
            description: 'Access denied',
        },
    },
});

registry.registerPath({
    method: 'post',
    path: '/api/connections/disconnect',
    tags: ['Connections'],
    summary: 'End connection',
    description: 'End an active connection (coach only)',
    request: {
        body: {
            description: 'Trainee ID to disconnect',
            content: {
                'application/json': {
                    schema: traineeIdSchema,
                },
            },
        },
    },
    responses: {
        200: {
            description: 'Connection ended successfully',
            content: {
                'application/json': {
                    schema: successMessageSchema,
                },
            },
        },
        400: {
            description: 'Invalid input data',
        },
        401: {
            description: 'Unauthorized - Coach role required',
        },
        404: {
            description: 'Active connection not found',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/connections/all',
    tags: ['Connections'],
    summary: 'Get all connections',
    description:
        'Get list of all connections (all statuses) for the current user',
    responses: {
        200: {
            description: 'List of all connections',
            content: {
                'application/json': {
                    schema: z.array(connectionSchema),
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
        403: {
            description: 'Access denied',
        },
    },
});

// Users routes
registry.registerPath({
    method: 'get',
    path: '/api/users/{userId}',
    tags: ['Users'],
    summary: 'Get user information by ID',
    description:
        'Get user information by user ID. Users can view their own information, coaches can view any user.',
    request: {
        params: userIdParamSchema,
    },
    responses: {
        200: {
            description: 'User information',
            content: {
                'application/json': {
                    schema: detailedUserSchema,
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
        404: {
            description: 'User not found',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/users/stats',
    tags: ['Users'],
    summary: 'Get user body stats',
    description: 'Get all body measurement statistics for the current user',
    responses: {
        200: {
            description: 'User body stats history',
            content: {
                'application/json': {
                    schema: z.array(
                        z
                            .object({
                                id: z.int(),
                                userId: z.string(),
                                weight: z.number().optional(),
                                height: z.number().optional(),
                                bodyFat: z.number().optional(),
                                muscleMass: z.number().optional(),
                                chest: z.number().optional(),
                                waist: z.number().optional(),
                                hips: z.number().optional(),
                                arms: z.number().optional(),
                                thighs: z.number().optional(),
                                notes: z.string().optional(),
                                recordedAt: z.string(),
                                recordedBy: z.string(),
                            })
                            .openapi('UserStats')
                    ),
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/users/stats/latest',
    tags: ['Users'],
    summary: 'Get latest user body stats',
    description:
        'Get the most recent body measurement statistics for the current user',
    responses: {
        200: {
            description: 'Latest user body stats',
            content: {
                'application/json': {
                    schema: z
                        .object({
                            id: z.int(),
                            userId: z.string(),
                            weight: z.number().optional(),
                            height: z.number().optional(),
                            bodyFat: z.number().optional(),
                            muscleMass: z.number().optional(),
                            chest: z.number().optional(),
                            waist: z.number().optional(),
                            hips: z.number().optional(),
                            arms: z.number().optional(),
                            thighs: z.number().optional(),
                            notes: z.string().optional(),
                            recordedAt: z.string(),
                            recordedBy: z.string(),
                        })
                        .openapi('LatestUserStats'),
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
    },
});

registry.registerPath({
    method: 'post',
    path: '/api/users/stats',
    tags: ['Users'],
    summary: 'Record user body stats',
    description: 'Record new body measurements for the current user',
    request: {
        body: {
            description: 'Body measurement data',
            content: {
                'application/json': {
                    schema: recordUserStatsSchema,
                },
            },
        },
    },
    responses: {
        201: {
            description: 'Body stats recorded successfully',
            content: {
                'application/json': {
                    schema: z
                        .object({
                            id: z.int(),
                            userId: z.string(),
                            weight: z.number().optional(),
                            height: z.number().optional(),
                            bodyFat: z.number().optional(),
                            muscleMass: z.number().optional(),
                            chest: z.number().optional(),
                            waist: z.number().optional(),
                            hips: z.number().optional(),
                            arms: z.number().optional(),
                            thighs: z.number().optional(),
                            notes: z.string().optional(),
                            recordedAt: z.string(),
                            recordedBy: z.string(),
                        })
                        .openapi('UserStatsResponse'),
                },
            },
        },
        400: {
            description: 'Invalid input data',
        },
        401: {
            description: 'Unauthorized',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/users/workout-plans',
    tags: ['Users'],
    summary: 'Get user assigned workout plans',
    description: 'Get all workout plans assigned to the current user',
    responses: {
        200: {
            description: 'List of assigned workout plans',
            content: {
                'application/json': {
                    schema: z.array(
                        z
                            .object({
                                id: z.int(),
                                userId: z.string(),
                                workoutPlanId: z.int(),
                                assignedBy: z.string(),
                                startDate: z.string(),
                                endDate: z.string().optional(),
                                status: z.enum([
                                    'active',
                                    'completed',
                                    'paused',
                                    'cancelled',
                                ]),
                                progress: z.number(),
                                notes: z.string().optional(),
                                createdAt: z.string(),
                                updatedAt: z.string(),
                                workoutPlan: workoutPlanSchema,
                            })
                            .openapi('WorkoutPlanAssignment')
                    ),
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/users/nutrition-plans',
    tags: ['Users'],
    summary: 'Get user assigned nutrition plans',
    description: 'Get all nutrition plans assigned to the current user',
    responses: {
        200: {
            description: 'List of assigned nutrition plans',
            content: {
                'application/json': {
                    schema: z.array(
                        z
                            .object({
                                id: z.int(),
                                userId: z.string(),
                                nutritionPlanId: z.int(),
                                assignedBy: z.string(),
                                startDate: z.string(),
                                endDate: z.string().optional(),
                                status: z.enum([
                                    'active',
                                    'completed',
                                    'paused',
                                    'cancelled',
                                ]),
                                notes: z.string().optional(),
                                createdAt: z.string(),
                                updatedAt: z.string(),
                                nutritionPlan: nutritionPlanSchema.optional(),
                                nutritionAdherences: z.array(
                                    nutritionAdherenceSchema
                                ),
                            })
                            .openapi('NutritionPlanAssignment')
                    ),
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/users/search',
    tags: ['Users'],
    summary: 'Search users',
    description:
        'Search for users by name or email, optionally filtered by role',
    request: {
        query: userSearchQuerySchema,
    },
    responses: {
        200: {
            description: 'List of matching users',
            content: {
                'application/json': {
                    schema: z.array(userSchema),
                },
            },
        },
        400: {
            description: 'Invalid query parameters',
        },
        401: {
            description: 'Unauthorized',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/users/nutrition/{nutritionPlanId}/assign',
    tags: ['Users'],
    summary: 'Get nutrition plan assignments',
    description:
        'Get all nutrition plan assignment details for a specific plan',
    request: {
        params: nutritionPlanIdParamSchema,
        query: userIdQuerySchema,
    },
    responses: {
        200: {
            description: 'List of nutrition plan assignments',
            content: {
                'application/json': {
                    schema: z.array(
                        z
                            .object({
                                id: z.int(),
                                userId: z.string(),
                                nutritionPlanId: z.int(),
                                assignedBy: z.string(),
                                startDate: z.string(),
                                endDate: z.string().optional(),
                                status: z.enum([
                                    'active',
                                    'completed',
                                    'paused',
                                    'cancelled',
                                ]),
                                notes: z.string().optional(),
                                createdAt: z.string(),
                                updatedAt: z.string(),
                                nutritionPlan: nutritionPlanSchema.optional(),
                                nutritionAdherences: z.array(
                                    nutritionAdherenceSchema
                                ),
                            })
                            .openapi('NutritionPlanAssignment')
                    ),
                },
            },
        },
        400: {
            description: 'Missing userId parameter for coaches',
        },
        401: {
            description: 'Unauthorized',
        },
        403: {
            description: 'Access denied',
        },
    },
});

registry.registerPath({
    method: 'post',
    path: '/api/users/nutrition/{nutritionPlanId}/assign',
    tags: ['Users'],
    summary: 'Assign nutrition plan',
    description: 'Assign a nutrition plan to a trainee (coach only)',
    request: {
        params: nutritionPlanIdParamSchema,
        body: {
            description: 'Assignment data',
            content: {
                'application/json': {
                    schema: assignNutritionPlanSchema,
                },
            },
        },
    },
    responses: {
        201: {
            description: 'Nutrition plan assigned successfully',
            content: {
                'application/json': {
                    schema: z
                        .object({
                            id: z.int(),
                            userId: z.string(),
                            nutritionPlanId: z.int(),
                            assignedBy: z.string(),
                            startDate: z.string(),
                            endDate: z.string().optional(),
                            status: z.enum([
                                'active',
                                'completed',
                                'paused',
                                'cancelled',
                            ]),
                            notes: z.string().optional(),
                            createdAt: z.string(),
                            updatedAt: z.string(),
                        })
                        .openapi('NutritionPlanAssignmentResponse'),
                },
            },
        },
        400: {
            description: 'Invalid input data',
        },
        401: {
            description: 'Unauthorized - Coach role required',
        },
    },
});

registry.registerPath({
    method: 'post',
    path: '/api/users/nutrition/user-plans/{userNutritionPlanId}/meals/{mealId}/complete',
    tags: ['Users'],
    summary: 'Complete a meal',
    description:
        'Mark a meal as completed with actual consumption data. Automatically creates or updates nutrition adherence record.',
    request: {
        params: userNutritionPlanIdParamSchema.merge(mealIdParamSchema),
        body: {
            description: 'Meal completion data',
            content: {
                'application/json': {
                    schema: mealCompletionSchema,
                },
            },
        },
    },
    responses: {
        201: {
            description: 'Meal completed successfully',
            content: {
                'application/json': {
                    schema: z
                        .object({
                            id: z.int(),
                            nutritionAdherenceId: z.int(),
                            nutritionPlanMealId: z.int(),
                            userId: z.string(),
                            isCompleted: z.boolean(),
                            completedAt: z.string(),
                            caloriesConsumed: z.int().optional(),
                            proteinConsumed: z.number().optional(),
                            carbsConsumed: z.number().optional(),
                            fatConsumed: z.number().optional(),
                            fiberConsumed: z.number().optional(),
                            notes: z.string().optional(),
                            createdAt: z.string(),
                            updatedAt: z.string(),
                        })
                        .openapi('MealCompletionResponse'),
                },
            },
        },
        400: {
            description: 'Invalid input data',
        },
        401: {
            description: 'Unauthorized',
        },
        404: {
            description: 'User nutrition plan or meal not found',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/users/nutrition/user-plans/{userNutritionPlanId}/adherence',
    tags: ['Users'],
    summary: 'Get nutrition adherence history',
    description: 'Get adherence history for a user nutrition plan',
    request: {
        params: userNutritionPlanIdParamSchema,
        query: userIdQuerySchema,
    },
    responses: {
        200: {
            description: 'Nutrition adherence history',
            content: {
                'application/json': {
                    schema: z.array(
                        z
                            .object({
                                id: z.int(),
                                nutritionPlanId: z.int(),
                                userId: z.string(),
                                date: z.string(),
                                weekday: z.enum([
                                    'sun',
                                    'mon',
                                    'tue',
                                    'wed',
                                    'thu',
                                    'fri',
                                    'sat',
                                ]),
                                mealsCompleted: z.int().optional(),
                                totalMeals: z.int().optional(),
                                adherencePercentage: z.number().optional(),
                                totalCaloriesConsumed: z.int().optional(),
                                totalCaloriesPlanned: z.int().optional(),
                                notes: z.string().optional(),
                            })
                            .openapi('NutritionAdherenceHistory')
                    ),
                },
            },
        },
        400: {
            description: 'Missing userId parameter for coaches',
        },
        401: {
            description: 'Unauthorized',
        },
        403: {
            description: 'Access denied',
        },
    },
});

registry.registerPath({
    method: 'post',
    path: '/api/users/workout/exercise-results',
    tags: ['Users'],
    summary: 'Record exercise result',
    description: 'Record the result of an exercise performance',
    request: {
        body: {
            description: 'Exercise result data',
            content: {
                'application/json': {
                    schema: recordExerciseResultSchema,
                },
            },
        },
    },
    responses: {
        201: {
            description: 'Exercise result recorded successfully',
            content: {
                'application/json': {
                    schema: z
                        .object({
                            id: z.int(),
                            workoutPlanDayExerciseId: z.int(),
                            userWorkoutPlanId: z.int(),
                            userId: z.string(),
                            reps: z.int().optional(),
                            duration: z.int().optional(),
                            calories: z.int().optional(),
                            completedAt: z.string(),
                            createdAt: z.string(),
                        })
                        .openapi('ExerciseResult'),
                },
            },
        },
        400: {
            description: 'Invalid input data',
        },
        401: {
            description: 'Unauthorized',
        },
    },
});

registry.registerPath({
    method: 'delete',
    path: '/api/users/workout/user-plans/{userWorkoutPlanId}/days/{dayId}/results',
    tags: ['Users'],
    summary: 'Reset exercise results',
    description: 'Reset exercise results for a workout day',
    request: {
        params: userWorkoutPlanIdParamSchema.merge(dayIdParamSchema),
    },
    responses: {
        201: {
            description: 'Exercise result recorded successfully',
            content: {
                'application/json': {
                    schema: successMessageSchema,
                },
            },
        },
        400: {
            description: 'Invalid input data',
        },
        401: {
            description: 'Unauthorized',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/users/workout/{workoutPlanId}/assign',
    tags: ['Users'],
    summary: 'Get workout plan assignments',
    description: 'Get all workout plan assignment details for a specific plan',
    request: {
        params: workoutPlanIdParamSchema,
        query: userIdQuerySchema,
    },
    responses: {
        200: {
            description: 'List of workout plan assignments',
            content: {
                'application/json': {
                    schema: z.array(
                        z
                            .object({
                                id: z.int(),
                                userId: z.string(),
                                workoutPlanId: z.int(),
                                assignedBy: z.string(),
                                startDate: z.string(),
                                endDate: z.string().optional(),
                                status: z.enum([
                                    'active',
                                    'completed',
                                    'paused',
                                    'cancelled',
                                ]),
                                progress: z.number(),
                                notes: z.string().optional(),
                                createdAt: z.string(),
                                updatedAt: z.string(),
                                workoutPlan: workoutPlanSchema,
                            })
                            .openapi('WorkoutPlanAssignment')
                    ),
                },
            },
        },
        400: {
            description: 'Missing userId parameter for coaches',
        },
        401: {
            description: 'Unauthorized',
        },
        403: {
            description: 'Access denied',
        },
    },
});

registry.registerPath({
    method: 'post',
    path: '/api/users/workout/{workoutPlanId}/assign',
    tags: ['Users'],
    summary: 'Assign workout plan',
    description: 'Assign a workout plan to a trainee (coach only)',
    request: {
        params: workoutPlanIdParamSchema,
        body: {
            description: 'Assignment data',
            content: {
                'application/json': {
                    schema: assignWorkoutPlanSchema,
                },
            },
        },
    },
    responses: {
        201: {
            description: 'Workout plan assigned successfully',
            content: {
                'application/json': {
                    schema: z
                        .object({
                            id: z.int(),
                            userId: z.string(),
                            workoutPlanId: z.int(),
                            assignedBy: z.string(),
                            startDate: z.string(),
                            endDate: z.string().optional(),
                            status: z.enum([
                                'active',
                                'completed',
                                'paused',
                                'cancelled',
                            ]),
                            progress: z.number(),
                            notes: z.string().optional(),
                            createdAt: z.string(),
                            updatedAt: z.string(),
                        })
                        .openapi('WorkoutPlanAssignmentResponse'),
                },
            },
        },
        400: {
            description: 'Invalid input data',
        },
        401: {
            description: 'Unauthorized - Coach role required',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/users/workout/user-plans/{userWorkoutPlanId}/results',
    tags: ['Users'],
    summary: 'Get user workout plan results',
    description: 'Get exercise results for a specific user workout plan',
    request: {
        params: userWorkoutPlanIdParamSchema,
        query: userIdQuerySchema,
    },
    responses: {
        200: {
            description: 'Workout plan exercise results',
            content: {
                'application/json': {
                    schema: z
                        .object({
                            id: z
                                .int()
                                .openapi({ description: 'Workout plan ID' }),
                            name: z
                                .string()
                                .openapi({ description: 'Workout plan name' }),
                            description: z.string().optional().openapi({
                                description: 'Workout plan description',
                            }),
                            difficulty: z
                                .enum(['beginner', 'intermediate', 'advanced'])
                                .optional()
                                .openapi({ description: 'Difficulty level' }),
                            estimatedCalories: z.int().optional().openapi({
                                description: 'Estimated calories per session',
                            }),
                            createdBy: z
                                .string()
                                .openapi({ description: 'Creator user ID' }),
                            isActive: z.boolean().openapi({
                                description: 'Whether plan is active',
                            }),
                            createdAt: z
                                .string()
                                .openapi({ description: 'Creation date' }),
                            updatedAt: z
                                .string()
                                .openapi({ description: 'Last update date' }),
                            workoutPlanDays: z
                                .array(
                                    z.object({
                                        id: z
                                            .int()
                                            .openapi({ description: 'Day ID' }),
                                        workoutPlanId: z.int().openapi({
                                            description: 'Workout plan ID',
                                        }),
                                        day: z.int().openapi({
                                            description: 'Day number',
                                        }),
                                        isRestDay: z.boolean().openapi({
                                            description:
                                                'Whether this is a rest day',
                                        }),
                                        estimatedCalories: z
                                            .int()
                                            .optional()
                                            .openapi({
                                                description:
                                                    'Estimated calories for this day',
                                            }),
                                        duration: z.int().optional().openapi({
                                            description: 'Duration in seconds',
                                        }),
                                        exercises: z
                                            .array(
                                                z.object({
                                                    id: z.int().openapi({
                                                        description:
                                                            'Exercise ID',
                                                    }),
                                                    workoutPlanDayId: z
                                                        .int()
                                                        .openapi({
                                                            description:
                                                                'Workout plan day ID',
                                                        }),
                                                    exerciseTypeId: z
                                                        .int()
                                                        .openapi({
                                                            description:
                                                                'Exercise type ID',
                                                        }),
                                                    order: z
                                                        .int()
                                                        .optional()
                                                        .openapi({
                                                            description:
                                                                'Exercise order',
                                                        }),
                                                    targetReps: z
                                                        .int()
                                                        .optional()
                                                        .openapi({
                                                            description:
                                                                'Target repetitions',
                                                        }),
                                                    targetDuration: z
                                                        .int()
                                                        .optional()
                                                        .openapi({
                                                            description:
                                                                'Target duration in seconds',
                                                        }),
                                                    estimatedCalories: z
                                                        .int()
                                                        .optional()
                                                        .openapi({
                                                            description:
                                                                'Estimated calories for this exercise',
                                                        }),
                                                    notes: z
                                                        .string()
                                                        .optional()
                                                        .openapi({
                                                            description:
                                                                'Exercise notes',
                                                        }),
                                                    createdAt: z
                                                        .string()
                                                        .openapi({
                                                            description:
                                                                'Creation date',
                                                        }),
                                                    updatedAt: z
                                                        .string()
                                                        .openapi({
                                                            description:
                                                                'Last update date',
                                                        }),
                                                    exerciseType:
                                                        exerciseTypeSchema.openapi(
                                                            {
                                                                description:
                                                                    'Exercise type details',
                                                            }
                                                        ),
                                                    exerciseResults: z
                                                        .array(
                                                            z.object({
                                                                id: z
                                                                    .int()
                                                                    .openapi({
                                                                        description:
                                                                            'Exercise result ID',
                                                                    }),
                                                                workoutPlanDayExerciseId:
                                                                    z
                                                                        .int()
                                                                        .openapi(
                                                                            {
                                                                                description:
                                                                                    'Exercise ID',
                                                                            }
                                                                        ),
                                                                userWorkoutPlanId:
                                                                    z
                                                                        .int()
                                                                        .openapi(
                                                                            {
                                                                                description:
                                                                                    'User workout plan ID',
                                                                            }
                                                                        ),
                                                                userId: z
                                                                    .string()
                                                                    .openapi({
                                                                        description:
                                                                            'User ID',
                                                                    }),
                                                                reps: z
                                                                    .int()
                                                                    .optional()
                                                                    .openapi({
                                                                        description:
                                                                            'Actual repetitions completed',
                                                                    }),
                                                                duration: z
                                                                    .int()
                                                                    .optional()
                                                                    .openapi({
                                                                        description:
                                                                            'Actual duration in seconds',
                                                                    }),
                                                                calories: z
                                                                    .int()
                                                                    .optional()
                                                                    .openapi({
                                                                        description:
                                                                            'Actual calories burned',
                                                                    }),
                                                                completedAt: z
                                                                    .string()
                                                                    .openapi({
                                                                        description:
                                                                            'Completion timestamp',
                                                                    }),
                                                                createdAt: z
                                                                    .string()
                                                                    .openapi({
                                                                        description:
                                                                            'Creation date',
                                                                    }),
                                                            })
                                                        )
                                                        .openapi({
                                                            description:
                                                                'Exercise results/logs',
                                                        }),
                                                })
                                            )
                                            .openapi({
                                                description:
                                                    'Exercises for this day',
                                            }),
                                    })
                                )
                                .openapi({ description: 'Workout plan days' }),
                            userWorkoutPlan: z
                                .object({
                                    id: z.int().openapi({
                                        description: 'User workout plan ID',
                                    }),
                                    userId: z
                                        .string()
                                        .openapi({ description: 'User ID' }),
                                    workoutPlanId: z.int().openapi({
                                        description: 'Workout plan ID',
                                    }),
                                    assignedBy: z.string().openapi({
                                        description: 'Assigned by user ID',
                                    }),
                                    startDate: z
                                        .string()
                                        .openapi({ description: 'Start date' }),
                                    endDate: z
                                        .string()
                                        .optional()
                                        .openapi({ description: 'End date' }),
                                    status: z
                                        .enum([
                                            'active',
                                            'completed',
                                            'paused',
                                            'cancelled',
                                        ])
                                        .openapi({
                                            description: 'Assignment status',
                                        }),
                                    progress: z.number().openapi({
                                        description: 'Progress percentage',
                                    }),
                                    notes: z.string().optional().openapi({
                                        description: 'Assignment notes',
                                    }),
                                    createdAt: z.string().openapi({
                                        description: 'Creation date',
                                    }),
                                    updatedAt: z.string().openapi({
                                        description: 'Last update date',
                                    }),
                                })
                                .openapi({
                                    description:
                                        'User workout plan assignment details',
                                }),
                        })
                        .openapi('WorkoutPlanResults'),
                },
            },
        },
        400: {
            description: 'Missing userId parameter for coaches',
        },
        401: {
            description: 'Unauthorized',
        },
        403: {
            description: 'Access denied',
        },
        404: {
            description: 'Workout plan not found or not assigned to user',
        },
    },
});

// Exercise routes
registry.registerPath({
    method: 'get',
    path: '/api/exercises',
    tags: ['Exercises'],
    summary: 'Get all exercise types',
    description: 'Retrieve a list of all available exercise types',
    responses: {
        200: {
            description: 'List of exercise types',
            content: {
                'application/json': {
                    schema: z.array(exerciseTypeSchema),
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/exercises/name/{name}',
    tags: ['Exercises'],
    summary: 'Get exercise type by name',
    description: 'Retrieve a specific exercise type by its name',
    request: {
        params: z
            .object({
                name: z.string().openapi({
                    description: 'Name of the exercise',
                    example: 'Push-ups',
                }),
            })
            .openapi('ExerciseNameParam'),
    },
    responses: {
        200: {
            description: 'Exercise type details',
            content: {
                'application/json': {
                    schema: exerciseTypeSchema,
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
        404: {
            description: 'Exercise type not found',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/exercises/{id}',
    tags: ['Exercises'],
    summary: 'Get exercise type by ID',
    description: 'Retrieve a specific exercise type by its ID',
    request: {
        params: idParamSchema,
    },
    responses: {
        200: {
            description: 'Exercise type details',
            content: {
                'application/json': {
                    schema: exerciseTypeSchema,
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
        404: {
            description: 'Exercise type not found',
        },
    },
});

// Nutrition routes
registry.registerPath({
    method: 'get',
    path: '/api/nutrition',
    tags: ['Nutrition'],
    summary: 'Get all nutrition plans',
    description: 'Get all nutrition plans accessible to the current user',
    responses: {
        200: {
            description: 'List of nutrition plans',
            content: {
                'application/json': {
                    schema: z.array(nutritionPlanSchema),
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
    },
});

registry.registerPath({
    method: 'post',
    path: '/api/nutrition',
    tags: ['Nutrition'],
    summary: 'Create nutrition plan',
    description: 'Create a new nutrition plan',
    request: {
        body: {
            description: 'Nutrition plan data',
            content: {
                'application/json': {
                    schema: createNutritionPlanSchema,
                },
            },
        },
    },
    responses: {
        201: {
            description: 'Nutrition plan created successfully',
            content: {
                'application/json': {
                    schema: nutritionPlanSchema,
                },
            },
        },
        400: {
            description: 'Invalid input data',
        },
        401: {
            description: 'Unauthorized',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/nutrition/{id}',
    tags: ['Nutrition'],
    summary: 'Get nutrition plan by ID',
    description: 'Get a specific nutrition plan with full details',
    request: {
        params: idParamSchema,
    },
    responses: {
        200: {
            description: 'Nutrition plan details',
            content: {
                'application/json': {
                    schema: detailedNutritionPlanSchema,
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
        404: {
            description: 'Nutrition plan not found',
        },
    },
});

// registry.registerPath({
//     method: 'put',
//     path: '/api/nutrition/{id}',
//     tags: ['Nutrition'],
//     summary: 'Update nutrition plan',
//     description: 'Update a nutrition plan (coach only)',
//     request: {
//         params: idParamSchema,
//         body: {
//             description: 'Updated nutrition plan data',
//             content: {
//                 'application/json': {
//                     schema: updateNutritionPlanSchema,
//                 },
//             },
//         },
//     },
//     responses: {
//         200: {
//             description: 'Nutrition plan updated successfully',
//             content: {
//                 'application/json': {
//                     schema: nutritionPlanSchema,
//                 },
//             },
//         },
//         400: {
//             description: 'Invalid input data',
//         },
//         401: {
//             description: 'Unauthorized - Coach role required',
//         },
//         404: {
//             description: 'Nutrition plan not found',
//         },
//     },
// });

registry.registerPath({
    method: 'delete',
    path: '/api/nutrition/{id}',
    tags: ['Nutrition'],
    summary: 'Delete nutrition plan',
    description: 'Delete a nutrition plan (coach only)',
    request: {
        params: idParamSchema,
    },
    responses: {
        200: {
            description: 'Nutrition plan deleted successfully',
            content: {
                'application/json': {
                    schema: successMessageSchema,
                },
            },
        },
        401: {
            description: 'Unauthorized - Coach role required',
        },
        404: {
            description: 'Nutrition plan not found',
        },
    },
});

// registry.registerPath({
//     method: 'get',
//     path: '/api/nutrition/{id}/days',
//     tags: ['Nutrition'],
//     summary: 'Get nutrition plan days',
//     description: 'Get all days for a nutrition plan',
//     request: {
//         params: idParamSchema,
//     },
//     responses: {
//         200: {
//             description: 'List of nutrition plan days',
//             content: {
//                 'application/json': {
//                     schema: z.array(nutritionPlanDaySchema),
//                 },
//             },
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Nutrition plan not found',
//         },
//     },
// });

// registry.registerPath({
//     method: 'post',
//     path: '/api/nutrition/{id}/days',
//     tags: ['Nutrition'],
//     summary: 'Create nutrition plan day',
//     description: 'Create a new day for a nutrition plan',
//     request: {
//         params: idParamSchema,
//         body: {
//             description: 'Nutrition plan day data',
//             content: {
//                 'application/json': {
//                     schema: createNutritionPlanDaySchema,
//                 },
//             },
//         },
//     },
//     responses: {
//         201: {
//             description: 'Nutrition plan day created successfully',
//             content: {
//                 'application/json': {
//                     schema: nutritionPlanDaySchema,
//                 },
//             },
//         },
//         400: {
//             description: 'Invalid input data',
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//     },
// });

// registry.registerPath({
//     method: 'get',
//     path: '/api/nutrition/days/{id}',
//     tags: ['Nutrition'],
//     summary: 'Get nutrition plan day',
//     description: 'Get a specific nutrition plan day by ID',
//     request: {
//         params: idParamSchema,
//     },
//     responses: {
//         200: {
//             description: 'Nutrition plan day details',
//             content: {
//                 'application/json': {
//                     schema: nutritionPlanDaySchema,
//                 },
//             },
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Nutrition plan day not found',
//         },
//     },
// });

// registry.registerPath({
//     method: 'put',
//     path: '/api/nutrition/days/{id}',
//     tags: ['Nutrition'],
//     summary: 'Update nutrition plan day',
//     description: 'Update a nutrition plan day',
//     request: {
//         params: idParamSchema,
//         body: {
//             description: 'Updated nutrition plan day data',
//             content: {
//                 'application/json': {
//                     schema: updateNutritionPlanDaySchema,
//                 },
//             },
//         },
//     },
//     responses: {
//         200: {
//             description: 'Nutrition plan day updated successfully',
//             content: {
//                 'application/json': {
//                     schema: nutritionPlanDaySchema,
//                 },
//             },
//         },
//         400: {
//             description: 'Invalid input data',
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Nutrition plan day not found',
//         },
//     },
// });

// registry.registerPath({
//     method: 'delete',
//     path: '/api/nutrition/days/{id}',
//     tags: ['Nutrition'],
//     summary: 'Delete nutrition plan day',
//     description: 'Delete a nutrition plan day',
//     request: {
//         params: idParamSchema,
//     },
//     responses: {
//         200: {
//             description: 'Nutrition plan day deleted successfully',
//             content: {
//                 'application/json': {
//                     schema: successMessageSchema,
//                 },
//             },
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Nutrition plan day not found',
//         },
//     },
// });

// registry.registerPath({
//     method: 'get',
//     path: '/api/nutrition/days/{id}/meals',
//     tags: ['Nutrition'],
//     summary: 'Get nutrition plan day meals',
//     description: 'Get all meals for a nutrition plan day',
//     request: {
//         params: idParamSchema,
//     },
//     responses: {
//         200: {
//             description: 'List of nutrition plan meals',
//             content: {
//                 'application/json': {
//                     schema: z.array(nutritionPlanMealSchema),
//                 },
//             },
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Nutrition plan day not found',
//         },
//     },
// });

// registry.registerPath({
//     method: 'post',
//     path: '/api/nutrition/days/{id}/meals',
//     tags: ['Nutrition'],
//     summary: 'Create nutrition plan meal',
//     description: 'Create a new meal for a nutrition plan day',
//     request: {
//         params: idParamSchema,
//         body: {
//             description: 'Nutrition plan meal data',
//             content: {
//                 'application/json': {
//                     schema: createNutritionPlanMealSchema,
//                 },
//             },
//         },
//     },
//     responses: {
//         201: {
//             description: 'Nutrition plan meal created successfully',
//             content: {
//                 'application/json': {
//                     schema: nutritionPlanMealSchema,
//                 },
//             },
//         },
//         400: {
//             description: 'Invalid input data',
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//     },
// });

// registry.registerPath({
//     method: 'get',
//     path: '/api/nutrition/meals/{id}',
//     tags: ['Nutrition'],
//     summary: 'Get nutrition plan meal',
//     description: 'Get a specific nutrition plan meal by ID',
//     request: {
//         params: idParamSchema,
//     },
//     responses: {
//         200: {
//             description: 'Nutrition plan meal details',
//             content: {
//                 'application/json': {
//                     schema: nutritionPlanMealSchema,
//                 },
//             },
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Nutrition plan meal not found',
//         },
//     },
// });

// registry.registerPath({
//     method: 'put',
//     path: '/api/nutrition/meals/{id}',
//     tags: ['Nutrition'],
//     summary: 'Update nutrition plan meal',
//     description: 'Update a nutrition plan meal',
//     request: {
//         params: idParamSchema,
//         body: {
//             description: 'Updated nutrition plan meal data',
//             content: {
//                 'application/json': {
//                     schema: updateNutritionPlanMealSchema,
//                 },
//             },
//         },
//     },
//     responses: {
//         200: {
//             description: 'Nutrition plan meal updated successfully',
//             content: {
//                 'application/json': {
//                     schema: nutritionPlanMealSchema,
//                 },
//             },
//         },
//         400: {
//             description: 'Invalid input data',
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Nutrition plan meal not found',
//         },
//     },
// });

// registry.registerPath({
//     method: 'delete',
//     path: '/api/nutrition/meals/{id}',
//     tags: ['Nutrition'],
//     summary: 'Delete nutrition plan meal',
//     description: 'Delete a nutrition plan meal',
//     request: {
//         params: idParamSchema,
//     },
//     responses: {
//         200: {
//             description: 'Nutrition plan meal deleted successfully',
//             content: {
//                 'application/json': {
//                     schema: successMessageSchema,
//                 },
//             },
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Nutrition plan meal not found',
//         },
//     },
// });

// registry.registerPath({
//     method: 'get',
//     path: '/api/nutrition/meals/{id}/foods',
//     tags: ['Nutrition'],
//     summary: 'Get nutrition plan meal foods',
//     description: 'Get all foods for a nutrition plan meal',
//     request: {
//         params: idParamSchema,
//     },
//     responses: {
//         200: {
//             description: 'List of nutrition plan foods',
//             content: {
//                 'application/json': {
//                     schema: z.array(nutritionPlanFoodSchema),
//                 },
//             },
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Nutrition plan meal not found',
//         },
//     },
// });

// registry.registerPath({
//     method: 'post',
//     path: '/api/nutrition/meals/{id}/foods',
//     tags: ['Nutrition'],
//     summary: 'Create nutrition plan food',
//     description: 'Create a new food for a nutrition plan meal',
//     request: {
//         params: idParamSchema,
//         body: {
//             description: 'Nutrition plan food data',
//             content: {
//                 'application/json': {
//                     schema: createNutritionPlanFoodSchema,
//                 },
//             },
//         },
//     },
//     responses: {
//         201: {
//             description: 'Nutrition plan food created successfully',
//             content: {
//                 'application/json': {
//                     schema: nutritionPlanFoodSchema,
//                 },
//             },
//         },
//         400: {
//             description: 'Invalid input data',
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//     },
// });

// registry.registerPath({
//     method: 'get',
//     path: '/api/nutrition/foods/{id}',
//     tags: ['Nutrition'],
//     summary: 'Get nutrition plan food',
//     description: 'Get a specific nutrition plan food by ID',
//     request: {
//         params: idParamSchema,
//     },
//     responses: {
//         200: {
//             description: 'Nutrition plan food details',
//             content: {
//                 'application/json': {
//                     schema: nutritionPlanFoodSchema,
//                 },
//             },
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Nutrition plan food not found',
//         },
//     },
// });

// registry.registerPath({
//     method: 'put',
//     path: '/api/nutrition/foods/{id}',
//     tags: ['Nutrition'],
//     summary: 'Update nutrition plan food',
//     description: 'Update a nutrition plan food',
//     request: {
//         params: idParamSchema,
//         body: {
//             description: 'Updated nutrition plan food data',
//             content: {
//                 'application/json': {
//                     schema: updateNutritionPlanFoodSchema,
//                 },
//             },
//         },
//     },
//     responses: {
//         200: {
//             description: 'Nutrition plan food updated successfully',
//             content: {
//                 'application/json': {
//                     schema: nutritionPlanFoodSchema,
//                 },
//             },
//         },
//         400: {
//             description: 'Invalid input data',
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Nutrition plan food not found',
//         },
//     },
// });

// registry.registerPath({
//     method: 'delete',
//     path: '/api/nutrition/foods/{id}',
//     tags: ['Nutrition'],
//     summary: 'Delete nutrition plan food',
//     description: 'Delete a nutrition plan food',
//     request: {
//         params: idParamSchema,
//     },
//     responses: {
//         200: {
//             description: 'Nutrition plan food deleted successfully',
//             content: {
//                 'application/json': {
//                     schema: successMessageSchema,
//                 },
//             },
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Nutrition plan food not found',
//         },
//     },
// });

// Workouts routes
registry.registerPath({
    method: 'get',
    path: '/api/workouts',
    tags: ['Workouts'],
    summary: 'Get all workout plans',
    description: 'Get all workout plans accessible to the current user',
    responses: {
        200: {
            description: 'List of workout plans',
            content: {
                'application/json': {
                    schema: z.array(workoutPlanSchema),
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
    },
});

registry.registerPath({
    method: 'post',
    path: '/api/workouts',
    tags: ['Workouts'],
    summary: 'Create workout plan',
    description: 'Create a new workout plan',
    request: {
        body: {
            description: 'Workout plan data',
            content: {
                'application/json': {
                    schema: createWorkoutPlanSchema,
                },
            },
        },
    },
    responses: {
        201: {
            description: 'Workout plan created successfully',
            content: {
                'application/json': {
                    schema: workoutPlanSchema,
                },
            },
        },
        400: {
            description: 'Invalid input data',
        },
        401: {
            description: 'Unauthorized',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/workouts/{id}',
    tags: ['Workouts'],
    summary: 'Get workout plan by ID',
    description: 'Get a specific workout plan with full details',
    request: {
        params: idParamSchema,
    },
    responses: {
        200: {
            description: 'Workout plan details',
            content: {
                'application/json': {
                    schema: detailedWorkoutPlanSchema,
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
        404: {
            description: 'Workout plan not found',
        },
    },
});

// registry.registerPath({
//     method: 'put',
//     path: '/api/workouts/{id}',
//     tags: ['Workouts'],
//     summary: 'Update workout plan',
//     description: 'Update a workout plan (coach only)',
//     request: {
//         params: idParamSchema,
//         body: {
//             description: 'Updated workout plan data',
//             content: {
//                 'application/json': {
//                     schema: updateWorkoutPlanSchema,
//                 },
//             },
//         },
//     },
//     responses: {
//         200: {
//             description: 'Workout plan updated successfully',
//             content: {
//                 'application/json': {
//                     schema: workoutPlanSchema,
//                 },
//             },
//         },
//         400: {
//             description: 'Invalid input data',
//         },
//         401: {
//             description: 'Unauthorized - Coach role required',
//         },
//         404: {
//             description: 'Workout plan not found',
//         },
//     },
// });

registry.registerPath({
    method: 'delete',
    path: '/api/workouts/{id}',
    tags: ['Workouts'],
    summary: 'Delete workout plan',
    description: 'Delete a workout plan (coach only)',
    request: {
        params: idParamSchema,
    },
    responses: {
        200: {
            description: 'Workout plan deleted successfully',
            content: {
                'application/json': {
                    schema: successMessageSchema,
                },
            },
        },
        401: {
            description: 'Unauthorized - Coach role required',
        },
        404: {
            description: 'Workout plan not found',
        },
    },
});

// registry.registerPath({
//     method: 'get',
//     path: '/api/workouts/{id}/days',
//     tags: ['Workouts'],
//     summary: 'Get workout plan days',
//     description: 'Get all days for a workout plan',
//     request: {
//         params: idParamSchema,
//     },
//     responses: {
//         200: {
//             description: 'List of workout plan days',
//             content: {
//                 'application/json': {
//                     schema: z.array(workoutPlanDaySchema),
//                 },
//             },
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Workout plan not found',
//         },
//     },
// });

// registry.registerPath({
//     method: 'post',
//     path: '/api/workouts/{id}/days',
//     tags: ['Workouts'],
//     summary: 'Create workout plan day',
//     description: 'Create a new day for a workout plan',
//     request: {
//         params: idParamSchema,
//         body: {
//             description: 'Workout plan day data',
//             content: {
//                 'application/json': {
//                     schema: addDayToWorkoutPlanSchema,
//                 },
//             },
//         },
//     },
//     responses: {
//         201: {
//             description: 'Workout plan day created successfully',
//             content: {
//                 'application/json': {
//                     schema: workoutPlanDaySchema,
//                 },
//             },
//         },
//         400: {
//             description: 'Invalid input data',
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//     },
// });

// registry.registerPath({
//     method: 'get',
//     path: '/api/workouts/days/{id}',
//     tags: ['Workouts'],
//     summary: 'Get workout plan day',
//     description: 'Get a specific workout plan day by ID',
//     request: {
//         params: idParamSchema,
//     },
//     responses: {
//         200: {
//             description: 'Workout plan day details',
//             content: {
//                 'application/json': {
//                     schema: workoutPlanDaySchema,
//                 },
//             },
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Workout plan day not found',
//         },
//     },
// });

// registry.registerPath({
//     method: 'put',
//     path: '/api/workouts/days/{id}',
//     tags: ['Workouts'],
//     summary: 'Update workout plan day',
//     description: 'Update a workout plan day',
//     request: {
//         params: idParamSchema,
//         body: {
//             description: 'Updated workout plan day data',
//             content: {
//                 'application/json': {
//                     schema: updateWorkoutPlanDaySchema,
//                 },
//             },
//         },
//     },
//     responses: {
//         200: {
//             description: 'Workout plan day updated successfully',
//             content: {
//                 'application/json': {
//                     schema: workoutPlanDaySchema,
//                 },
//             },
//         },
//         400: {
//             description: 'Invalid input data',
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Workout plan day not found',
//         },
//     },
// });

// registry.registerPath({
//     method: 'delete',
//     path: '/api/workouts/days/{id}',
//     tags: ['Workouts'],
//     summary: 'Delete workout plan day',
//     description: 'Delete a workout plan day',
//     request: {
//         params: idParamSchema,
//     },
//     responses: {
//         200: {
//             description: 'Workout plan day deleted successfully',
//             content: {
//                 'application/json': {
//                     schema: successMessageSchema,
//                 },
//             },
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Workout plan day not found',
//         },
//     },
// });

// registry.registerPath({
//     method: 'get',
//     path: '/api/workouts/days/{id}/exercises',
//     tags: ['Workouts'],
//     summary: 'Get workout plan day exercises',
//     description: 'Get all exercises for a workout plan day',
//     request: {
//         params: idParamSchema,
//     },
//     responses: {
//         200: {
//             description: 'List of workout plan exercises',
//             content: {
//                 'application/json': {
//                     schema: z.array(workoutPlanDayExerciseSchema),
//                 },
//             },
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Workout plan day not found',
//         },
//     },
// });

// registry.registerPath({
//     method: 'post',
//     path: '/api/workouts/days/{id}/exercises',
//     tags: ['Workouts'],
//     summary: 'Add exercise to workout plan day',
//     description: 'Add a new exercise to a workout plan day',
//     request: {
//         params: idParamSchema,
//         body: {
//             description: 'Exercise data',
//             content: {
//                 'application/json': {
//                     schema: addExerciseToPlanDaySchema,
//                 },
//             },
//         },
//     },
//     responses: {
//         201: {
//             description: 'Exercise added to workout plan day successfully',
//             content: {
//                 'application/json': {
//                     schema: workoutPlanDayExerciseSchema,
//                 },
//             },
//         },
//         400: {
//             description: 'Invalid input data',
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//     },
// });

// registry.registerPath({
//     method: 'get',
//     path: '/api/workouts/exercises/{id}',
//     tags: ['Workouts'],
//     summary: 'Get workout plan exercise',
//     description: 'Get a specific workout plan exercise by ID',
//     request: {
//         params: idParamSchema,
//     },
//     responses: {
//         200: {
//             description: 'Workout plan exercise details',
//             content: {
//                 'application/json': {
//                     schema: workoutPlanDayExerciseSchema,
//                 },
//             },
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Workout plan exercise not found',
//         },
//     },
// });

// registry.registerPath({
//     method: 'put',
//     path: '/api/workouts/exercises/{id}',
//     tags: ['Workouts'],
//     summary: 'Update workout plan exercise',
//     description: 'Update a workout plan exercise',
//     request: {
//         params: idParamSchema,
//         body: {
//             description: 'Updated exercise data',
//             content: {
//                 'application/json': {
//                     schema: updateExerciseInPlanDaySchema,
//                 },
//             },
//         },
//     },
//     responses: {
//         200: {
//             description: 'Workout plan exercise updated successfully',
//             content: {
//                 'application/json': {
//                     schema: workoutPlanDayExerciseSchema,
//                 },
//             },
//         },
//         400: {
//             description: 'Invalid input data',
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Workout plan exercise not found',
//         },
//     },
// });

// registry.registerPath({
//     method: 'delete',
//     path: '/api/workouts/exercises/{id}',
//     tags: ['Workouts'],
//     summary: 'Delete workout plan exercise',
//     description: 'Delete a workout plan exercise',
//     request: {
//         params: idParamSchema,
//     },
//     responses: {
//         200: {
//             description: 'Workout plan exercise deleted successfully',
//             content: {
//                 'application/json': {
//                     schema: successMessageSchema,
//                 },
//             },
//         },
//         401: {
//             description: 'Unauthorized',
//         },
//         404: {
//             description: 'Workout plan exercise not found',
//         },
//     },
// });

// Bulk Update Endpoints

// Bulk update workout plan
registry.registerPath({
    method: 'put',
    path: '/api/workouts/{id}/bulk',
    tags: ['Workouts'],
    summary: 'Bulk update workout plan',
    description:
        'Update a workout plan with all its days, exercises in one request. Items not included will be deleted.',
    request: {
        params: idParamSchema,
        body: {
            description: 'Complete workout plan data',
            content: {
                'application/json': {
                    schema: bulkUpdateWorkoutPlanSchema,
                },
            },
        },
    },
    responses: {
        200: {
            description: 'Workout plan updated successfully',
            content: {
                'application/json': {
                    schema: detailedWorkoutPlanSchema,
                },
            },
        },
        400: {
            description: 'Invalid input data',
        },
        401: {
            description: 'Unauthorized',
        },
        404: {
            description: 'Workout plan not found',
        },
        500: {
            description: 'Internal server error',
        },
    },
});

// Bulk update nutrition plan
registry.registerPath({
    method: 'put',
    path: '/api/nutrition/{id}/bulk',
    tags: ['Nutrition'],
    summary: 'Bulk update nutrition plan',
    description:
        'Update a nutrition plan with all its days, meals, foods in one request. Items not included will be deleted.',
    request: {
        params: idParamSchema,
        body: {
            description: 'Complete nutrition plan data',
            content: {
                'application/json': {
                    schema: bulkUpdateNutritionPlanSchema,
                },
            },
        },
    },
    responses: {
        200: {
            description: 'Nutrition plan updated successfully',
            content: {
                'application/json': {
                    schema: detailedNutritionPlanSchema,
                },
            },
        },
        400: {
            description: 'Invalid input data',
        },
        401: {
            description: 'Unauthorized',
        },
        404: {
            description: 'Nutrition plan not found',
        },
        500: {
            description: 'Internal server error',
        },
    },
});

// Planned Workouts routes
registry.registerPath({
    method: 'get',
    path: '/api/planned-workouts',
    tags: ['Planned Workouts'],
    summary: 'Get user planned workouts',
    description: 'Get all planned workouts for the current user',
    responses: {
        200: {
            description: 'List of planned workouts',
            content: {
                'application/json': {
                    schema: z.array(plannedWorkoutSchema),
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
    },
});

registry.registerPath({
    method: 'post',
    path: '/api/planned-workouts',
    tags: ['Planned Workouts'],
    summary: 'Create planned workout',
    description: 'Create/Schedule a new planned workout',
    request: {
        body: {
            description: 'Planned workout data',
            content: {
                'application/json': {
                    schema: createPlannedWorkoutSchema,
                },
            },
        },
    },
    responses: {
        201: {
            description: 'Planned workout created successfully',
            content: {
                'application/json': {
                    schema: plannedWorkoutSchema,
                },
            },
        },
        400: {
            description: 'Invalid input data',
        },
        401: {
            description: 'Unauthorized',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/planned-workouts/today',
    tags: ['Planned Workouts'],
    summary: "Get today's planned workouts",
    description: 'Get planned workouts scheduled for today',
    responses: {
        200: {
            description: "List of today's planned workouts",
            content: {
                'application/json': {
                    schema: z.array(plannedWorkoutSchema),
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/planned-workouts/weekday/{weekday}',
    tags: ['Planned Workouts'],
    summary: 'Get planned workouts for weekday',
    description: 'Get planned workouts for a specific weekday',
    request: {
        params: z
            .object({
                weekday: z
                    .enum(['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'])
                    .openapi({
                        description: 'Day of the week',
                        example: 'mon',
                    }),
            })
            .openapi('WeekdayParam'),
    },
    responses: {
        200: {
            description: 'List of planned workouts for the weekday',
            content: {
                'application/json': {
                    schema: z.array(plannedWorkoutSchema),
                },
            },
        },
        400: {
            description: 'Invalid weekday parameter',
        },
        401: {
            description: 'Unauthorized',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/planned-workouts/{id}',
    tags: ['Planned Workouts'],
    summary: 'Get planned workout by ID',
    description: 'Get a specific planned workout by ID',
    request: {
        params: idParamSchema,
    },
    responses: {
        200: {
            description: 'Planned workout details',
            content: {
                'application/json': {
                    schema: plannedWorkoutSchema,
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
        403: {
            description: 'Access denied',
        },
        404: {
            description: 'Planned workout not found',
        },
    },
});

registry.registerPath({
    method: 'put',
    path: '/api/planned-workouts/{id}',
    tags: ['Planned Workouts'],
    summary: 'Update planned workout',
    description: 'Update a planned workout',
    request: {
        params: idParamSchema,
        body: {
            description: 'Updated planned workout data',
            content: {
                'application/json': {
                    schema: updatePlannedWorkoutSchema,
                },
            },
        },
    },
    responses: {
        200: {
            description: 'Planned workout updated successfully',
            content: {
                'application/json': {
                    schema: plannedWorkoutSchema,
                },
            },
        },
        400: {
            description: 'Invalid input data',
        },
        401: {
            description: 'Unauthorized',
        },
        403: {
            description: 'Access denied',
        },
        404: {
            description: 'Planned workout not found',
        },
    },
});

registry.registerPath({
    method: 'delete',
    path: '/api/planned-workouts/{id}',
    tags: ['Planned Workouts'],
    summary: 'Delete planned workout',
    description: 'Delete a planned workout',
    request: {
        params: idParamSchema,
    },
    responses: {
        200: {
            description: 'Planned workout deleted successfully',
            content: {
                'application/json': {
                    schema: successMessageSchema,
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
        403: {
            description: 'Access denied',
        },
        404: {
            description: 'Planned workout not found',
        },
    },
});

registry.registerPath({
    method: 'post',
    path: '/api/planned-workouts/{id}/toggle',
    tags: ['Planned Workouts'],
    summary: 'Toggle planned workout status',
    description: 'Toggle the active status of a planned workout',
    request: {
        params: idParamSchema,
        body: {
            description: 'Toggle status data',
            content: {
                'application/json': {
                    schema: z
                        .object({
                            isActive: z.boolean().openapi({
                                description: 'New active status',
                                example: true,
                            }),
                        })
                        .openapi('TogglePlannedWorkout'),
                },
            },
        },
    },
    responses: {
        200: {
            description: 'Planned workout status toggled successfully',
            content: {
                'application/json': {
                    schema: plannedWorkoutSchema,
                },
            },
        },
        400: {
            description: 'Invalid input data',
        },
        401: {
            description: 'Unauthorized',
        },
        403: {
            description: 'Access denied',
        },
        404: {
            description: 'Planned workout not found',
        },
    },
});

// Register security scheme
registry.registerComponent('securitySchemes', 'bearerAuth', {
    type: 'http',
    scheme: 'bearer',
    bearerFormat: 'JWT',
    description: 'JWT token for authentication',
});

export function generateOpenAPIDocument() {
    const generator = new OpenApiGeneratorV31(registry.definitions);

    return generator.generateDocument({
        openapi: '3.1.0',
        info: {
            version: '1.0.0',
            title: 'Fitness API',
            description:
                'A comprehensive fitness tracking and coaching API that allows coaches to manage trainees, create workout and nutrition plans, and track progress.',
            contact: {
                name: 'API Support',
                email: 'support@fitness-api.com',
            },
        },
        servers: [
            {
                url: 'http://localhost:3000',
                description: 'Development server',
            },
        ],
        security: [
            {
                bearerAuth: [],
            },
        ],
    });
}

export { registry };
