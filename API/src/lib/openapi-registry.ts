import {
    OpenAPIRegistry,
    OpenApiGeneratorV3,
    OpenApiGeneratorV31,
} from '@asteasolutions/zod-to-openapi';
import { z } from 'zod';
import {
    // Common schemas
    idParamSchema,
    dayIdParamSchema,
    myQuerySchema,
    paginationQuerySchema,
    successMessageSchema,

    // Connection schemas
    connectRequestSchema,
    connectionRequestTypeSchema,
    traineeIdSchema,
    traineeParamSchema,
    connectionSchema,

    // Exercise schemas
    exerciseTypeSchema,
    recordExerciseResultSchema,

    // Nutrition schemas
    createNutritionPlanSchema,
    updateNutritionPlanSchema,
    assignNutritionPlanSchema,
    mealCompletionSchema,
    nutritionAdherenceSchema,
    nutritionPlanSchema,

    // Workout schemas
    createWorkoutPlanSchema,
    updateWorkoutPlanSchema,
    assignWorkoutPlanSchema,
    addDayToWorkoutPlanSchema,
    addExerciseToPlanDaySchema,
    workoutPlanSchema,

    // Planned workout schemas
    createPlannedWorkoutSchema,
    updatePlannedWorkoutSchema,

    // User schemas
    createUserGoalSchema,
    updateUserGoalSchema,
    recordUserStatsSchema,
    userSearchQuerySchema,
    userSchema,
} from '../validation/schemas.ts';

const registry = new OpenAPIRegistry();

// Register all schemas
registry.register('IdParam', idParamSchema);
registry.register('DayIdParam', dayIdParamSchema);
registry.register('MyQuery', myQuerySchema);
registry.register('PaginationQuery', paginationQuerySchema);
registry.register('SuccessMessage', successMessageSchema);

// Connection schemas
registry.register('ConnectRequest', connectRequestSchema);
registry.register('ConnectionRequestType', connectionRequestTypeSchema);
registry.register('TraineeId', traineeIdSchema);
registry.register('TraineeParam', traineeParamSchema);
registry.register('Connection', connectionSchema);

// Exercise schemas
registry.register('ExerciseType', exerciseTypeSchema);
registry.register('RecordExerciseResult', recordExerciseResultSchema);

// Nutrition schemas
registry.register('CreateNutritionPlan', createNutritionPlanSchema);
registry.register('UpdateNutritionPlan', updateNutritionPlanSchema);
registry.register('AssignNutritionPlan', assignNutritionPlanSchema);
registry.register('MealCompletion', mealCompletionSchema);
registry.register('NutritionAdherence', nutritionAdherenceSchema);
registry.register('NutritionPlan', nutritionPlanSchema);

// Workout schemas
registry.register('CreateWorkoutPlan', createWorkoutPlanSchema);
registry.register('UpdateWorkoutPlan', updateWorkoutPlanSchema);
registry.register('AssignWorkoutPlan', assignWorkoutPlanSchema);
registry.register('AddDayToWorkoutPlan', addDayToWorkoutPlanSchema);
registry.register('AddExerciseToPlanDay', addExerciseToPlanDaySchema);
registry.register('WorkoutPlan', workoutPlanSchema);

// Planned workout schemas
registry.register('CreatePlannedWorkout', createPlannedWorkoutSchema);
registry.register('UpdatePlannedWorkout', updatePlannedWorkoutSchema);

// User schemas
registry.register('CreateUserGoal', createUserGoalSchema);
registry.register('UpdateUserGoal', updateUserGoalSchema);
registry.register('RecordUserStats', recordUserStatsSchema);
registry.register('UserSearchQuery', userSearchQuerySchema);
registry.register('User', userSchema);

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
        404: {
            description: 'Exercise type not found',
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
        404: {
            description: 'Exercise type not found',
        },
    },
});

registry.registerPath({
    method: 'post',
    path: '/api/exercises/exercise-results',
    tags: ['Exercises'],
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
                            id: z
                                .number()
                                .openapi({ description: 'Result ID' }),
                            workoutPlanDayExerciseId: z.number(),
                            userId: z.string(),
                            reps: z.number().optional(),
                            duration: z.number().optional(),
                            calories: z.number().optional(),
                            recordedAt: z.string(),
                        })
                        .openapi('ExerciseResult'),
                },
            },
        },
        400: {
            description: 'Invalid input data',
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/exercises/results',
    tags: ['Exercises'],
    summary: 'Get user exercise results',
    description: 'Retrieve all exercise results for the authenticated user',
    responses: {
        200: {
            description: 'List of user exercise results',
            content: {
                'application/json': {
                    schema: z.array(
                        z
                            .object({
                                id: z
                                    .number()
                                    .openapi({ description: 'Result ID' }),
                                workoutPlanDayExerciseId: z.number(),
                                userId: z.string(),
                                reps: z.number().optional(),
                                duration: z.number().optional(),
                                calories: z.number().optional(),
                                completedAt: z.string(),
                                workoutPlanExercise: z.object({
                                    exerciseType: exerciseTypeSchema,
                                    workoutPlanDay: z.object({
                                        id: z.number(),
                                        day: z.number(),
                                        isRestDay: z.boolean(),
                                    }),
                                }),
                            })
                            .openapi('ExerciseResultWithDetails')
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
    path: '/api/exercises/workout-plan-day-exercise/{id}',
    tags: ['Exercises'],
    summary: 'Get exercise results for workout plan day exercise',
    description:
        'Retrieve exercise results for a specific workout plan day exercise',
    request: {
        params: idParamSchema,
    },
    responses: {
        200: {
            description:
                'Exercise results for the specified workout plan day exercise',
            content: {
                'application/json': {
                    schema: z.array(
                        z
                            .object({
                                id: z
                                    .number()
                                    .openapi({ description: 'Result ID' }),
                                workoutPlanDayExerciseId: z.number(),
                                userId: z.string(),
                                reps: z.number().optional(),
                                duration: z.number().optional(),
                                calories: z.number().optional(),
                                completedAt: z.string(),
                                workoutPlanExercise: z.object({
                                    exerciseType: exerciseTypeSchema,
                                }),
                            })
                            .openapi('ExerciseResultForPlanExercise')
                    ),
                },
            },
        },
        401: {
            description: 'Unauthorized',
        },
    },
});

// Nutrition routes
registry.registerPath({
    method: 'get',
    path: '/api/nutrition',
    tags: ['Nutrition'],
    summary: 'Get all nutrition plans',
    description:
        'Retrieve nutrition plans, optionally filtered by current user',
    request: {
        query: myQuerySchema,
    },
    responses: {
        200: {
            description: 'List of nutrition plans',
            content: {
                'application/json': {
                    schema: z.array(nutritionPlanSchema),
                },
            },
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/nutrition/{id}',
    tags: ['Nutrition'],
    summary: 'Get nutrition plan by ID',
    description: 'Retrieve a specific nutrition plan',
    request: {
        params: idParamSchema,
    },
    responses: {
        200: {
            description: 'Nutrition plan details',
            content: {
                'application/json': {
                    schema: nutritionPlanSchema,
                },
            },
        },
        404: {
            description: 'Nutrition plan not found',
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
    },
});

registry.registerPath({
    method: 'put',
    path: '/api/nutrition/{id}',
    tags: ['Nutrition'],
    summary: 'Update nutrition plan',
    description: 'Update an existing nutrition plan (coach only)',
    request: {
        params: idParamSchema,
        body: {
            description: 'Updated nutrition plan data',
            content: {
                'application/json': {
                    schema: updateNutritionPlanSchema,
                },
            },
        },
    },
    responses: {
        200: {
            description: 'Nutrition plan updated successfully',
            content: {
                'application/json': {
                    schema: nutritionPlanSchema,
                },
            },
        },
        404: {
            description: 'Nutrition plan not found',
        },
    },
});

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
        404: {
            description: 'Nutrition plan not found',
        },
    },
});

// Workout routes
registry.registerPath({
    method: 'get',
    path: '/api/workouts',
    tags: ['Workouts'],
    summary: 'Get all workout plans',
    description: 'Retrieve workout plans, optionally filtered by current user',
    request: {
        query: myQuerySchema,
    },
    responses: {
        200: {
            description: 'List of workout plans',
            content: {
                'application/json': {
                    schema: z.array(workoutPlanSchema),
                },
            },
        },
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/workouts/{id}',
    tags: ['Workouts'],
    summary: 'Get workout plan by ID',
    description: 'Retrieve a specific workout plan with details',
    request: {
        params: idParamSchema,
    },
    responses: {
        200: {
            description: 'Workout plan details',
            content: {
                'application/json': {
                    schema: workoutPlanSchema,
                },
            },
        },
        404: {
            description: 'Workout plan not found',
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
    },
});

registry.registerPath({
    method: 'put',
    path: '/api/workouts/{id}',
    tags: ['Workouts'],
    summary: 'Update workout plan',
    description: 'Update an existing workout plan (coach only)',
    request: {
        params: idParamSchema,
        body: {
            description: 'Updated workout plan data',
            content: {
                'application/json': {
                    schema: updateWorkoutPlanSchema,
                },
            },
        },
    },
    responses: {
        200: {
            description: 'Workout plan updated successfully',
            content: {
                'application/json': {
                    schema: workoutPlanSchema,
                },
            },
        },
        404: {
            description: 'Workout plan not found',
        },
    },
});

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
        404: {
            description: 'Workout plan not found',
        },
    },
});

registry.registerPath({
    method: 'post',
    path: '/api/workouts/{id}/assign',
    tags: ['Workouts'],
    summary: 'Assign workout plan to user',
    description: 'Assign a workout plan to a trainee (coach only)',
    request: {
        params: idParamSchema,
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
                            id: z.number(),
                            userId: z.string(),
                            workoutPlanId: z.number(),
                            assignedBy: z.string(),
                            startDate: z.string(),
                            endDate: z.string().optional(),
                            isActive: z.boolean(),
                            assignedAt: z.string(),
                        })
                        .openapi('WorkoutPlanAssignment'),
                },
            },
        },
    },
});

// Connection routes
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
                    schema: connectionSchema,
                },
            },
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
        403: {
            description: 'Access denied',
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
                    schema: connectionSchema,
                },
            },
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
    },
});

registry.registerPath({
    method: 'get',
    path: '/api/connections/connections',
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
    },
});

// User routes
registry.registerPath({
    method: 'get',
    path: '/api/users/stats',
    tags: ['Users'],
    summary: 'Get user body stats',
    description: 'Get body measurement statistics for the current user',
    responses: {
        200: {
            description: 'User body stats',
            content: {
                'application/json': {
                    schema: z.array(
                        z
                            .object({
                                id: z.number(),
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
                            id: z.number(),
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
