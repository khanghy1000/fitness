import { Router } from 'express';
import { z } from 'zod';
import {
    requireAuthenticated,
    requireTrainee,
    requireCoach,
} from '@middlewares/auth.middleware.ts';
import {
    validateBody,
    validateParams,
    validateQuery,
} from '@middlewares/validation.middleware.ts';
import {
    idParamSchema,
    createUserGoalSchema,
    updateUserGoalSchema,
    recordUserStatsSchema,
    nutritionAdherenceSchema,
    userSearchQuerySchema,
    nutritionPlanIdParamSchema,
    userNutritionPlanIdParamSchema,
    workoutPlanIdParamSchema,
    userWorkoutPlanIdParamSchema,
    assignNutritionPlanSchema,
    assignWorkoutPlanSchema,
    recordExerciseResultSchema,
    mealCompletionSchema,
    mealIdParamSchema,
    userIdParamSchema,
} from '../validation/schemas.ts';
import { UserService } from '@services/user.service.ts';
import { WorkoutService } from '@services/workout.service.ts';
import { NutritionService } from '@services/nutrition.service.ts';

const router = Router();

// Get user's goals
// router.get('/goals', requireAuthenticated, async (req, res) => {
//     const goals = await UserService.getUserGoals(req.session!.user.id);
//     res.json(goals);
// });

// Create user goal (trainee only)
// router.post(
//     '/goals',
//     requireTrainee,
//     validateBody(createUserGoalSchema),
//     async (req, res) => {
//         const goalData = {
//             ...req.body,
//             userId: req.session!.user.id,
//             targetDate: req.body.targetDate
//                 ? new Date(req.body.targetDate)
//                 : undefined,
//         };

//         const goal = await UserService.createUserGoal(goalData);

//         res.status(201).json(goal);
//     }
// );

// Update user goal
// router.put(
//     '/goals/:id',
//     requireAuthenticated,
//     validateParams(idParamSchema),
//     validateBody(updateUserGoalSchema),
//     async (req, res) => {
//         const id = (req.params as any).id as number;
//         const updateData = {
//             ...req.body,
//             targetDate: req.body.targetDate
//                 ? new Date(req.body.targetDate)
//                 : undefined,
//         };

//         const goal = await UserService.updateUserGoal(id, updateData);
//         if (!goal) {
//             return res.status(404).json({ error: 'Goal not found' });
//         }

//         res.json(goal);
//     }
// );

// Get user's body stats
router.get('/stats', requireAuthenticated, async (req, res) => {
    const stats = await UserService.getUserStats(req.session!.user.id);
    res.json(stats);
});

// Get user's latest body stats
router.get('/stats/latest', requireAuthenticated, async (req, res) => {
    const stats = await UserService.getLatestUserStats(req.session!.user.id);
    res.json(stats);
});

// Record user body stats
router.post(
    '/stats',
    requireAuthenticated,
    validateBody(recordUserStatsSchema),
    async (req, res) => {
        const statsData = {
            ...req.body,
            userId: req.session!.user.id,
            recordedBy: req.session!.user.id,
            recordedAt: new Date(),
        };

        const stats = await UserService.recordUserStats(statsData);

        res.status(201).json(stats);
    }
);

// Get user's assigned workout plans
router.get('/workout-plans', requireAuthenticated, async (req, res) => {
    const plans = await WorkoutService.getUserAssignedWorkoutPlans(
        req.session!.user.id
    );
    res.json(plans);
});

// Get user's assigned nutrition plans
router.get('/nutrition-plans', requireAuthenticated, async (req, res) => {
    const plans = await NutritionService.getUserAssignedNutritionPlans(
        req.session!.user.id
    );
    res.json(plans);
});

// Search for users (for coaches to find trainees)
router.get(
    '/search',
    requireAuthenticated,
    validateQuery(userSearchQuerySchema),
    async (req, res) => {
        const { query, role } = req.query as {
            query: string;
            role?: 'coach' | 'trainee';
        };

        const users = await UserService.searchUsers(query, role);

        res.json(users);
    }
);

// Helper function to convert JavaScript getDay() result to weekday enum
const getWeekdayEnum = (
    dayNumber: number
): 'sun' | 'mon' | 'tue' | 'wed' | 'thu' | 'fri' | 'sat' => {
    const weekdays = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'] as const;
    return weekdays[dayNumber];
};

// Get nutrition plan assignments
router.get(
    '/nutrition/:nutritionPlanId/assign',
    requireAuthenticated,
    validateParams(nutritionPlanIdParamSchema),
    validateQuery(z.object({ userId: z.string().optional() }).optional()),
    async (req, res) => {
        const { nutritionPlanId } = req.params;
        const user = req.session!.user;

        if (user.role === 'trainee') {
            // Get current user's assigned plans
            const plans = await NutritionService.getUserAssignedNutritionPlans(
                user.id
            );
            const matchingPlans = plans.filter(
                (p) => p.nutritionPlan?.id === parseInt(nutritionPlanId)
            );
            res.json(matchingPlans);
        } else if (user.role === 'coach') {
            // Get assigned plans for specified user
            const { userId } = req.query as { userId?: string };
            if (!userId) {
                return res.status(400).json({
                    error: 'userId query parameter is required for coaches',
                });
            }
            const plans =
                await NutritionService.getUserAssignedNutritionPlans(userId);
            const matchingPlans = plans.filter(
                (p) => p.nutritionPlan?.id === parseInt(nutritionPlanId)
            );
            res.json(matchingPlans);
        } else {
            res.status(403).json({ error: 'Access denied' });
        }
    }
);

// Assign nutrition plan to trainee (coach only)
router.post(
    '/nutrition/:nutritionPlanId/assign',
    requireCoach,
    validateParams(nutritionPlanIdParamSchema),
    validateBody(assignNutritionPlanSchema),
    async (req, res) => {
        const { nutritionPlanId } = req.params;
        const { userId, startDate, endDate } = req.body;

        const assignment = await NutritionService.assignNutritionPlanToUser({
            userId,
            nutritionPlanId: parseInt(nutritionPlanId),
            assignedBy: req.session!.user.id,
            startDate: new Date(startDate),
            endDate: endDate ? new Date(endDate) : undefined,
        });

        res.status(201).json(assignment);
    }
);

// Complete a meal
router.post(
    '/nutrition/user-plans/:userNutritionPlanId/meals/:mealId/complete',
    requireAuthenticated,
    validateParams(userNutritionPlanIdParamSchema.merge(mealIdParamSchema)),
    validateBody(mealCompletionSchema),
    async (req, res) => {
        const { userNutritionPlanId, mealId } = req.params;

        const completion = await NutritionService.completeMeal({
            userNutritionPlanId: parseInt(userNutritionPlanId),
            nutritionPlanMealId: parseInt(mealId),
            userId: req.session!.user.id,
            notes: req.body.notes,
        });

        res.status(201).json(completion);
    }
);

// Get adherence history for a user nutrition plan
router.get(
    '/nutrition/user-plans/:userNutritionPlanId/adherence',
    requireAuthenticated,
    validateParams(userNutritionPlanIdParamSchema),
    validateQuery(z.object({ userId: z.string().optional() }).optional()),
    async (req, res) => {
        const { userNutritionPlanId } = req.params;
        const user = req.session!.user;

        if (user.role === 'trainee') {
            // Get current user's adherence history
            const adherenceHistory =
                await NutritionService.getUserAdherenceHistoryByPlan(
                    user.id,
                    parseInt(userNutritionPlanId)
                );
            res.json(adherenceHistory);
        } else if (user.role === 'coach') {
            // Get adherence history for specified user
            const { userId } = req.query as { userId?: string };
            if (!userId) {
                return res.status(400).json({
                    error: 'userId query parameter is required for coaches',
                });
            }
            const adherenceHistory =
                await NutritionService.getUserAdherenceHistoryByPlan(
                    userId,
                    parseInt(userNutritionPlanId)
                );
            res.json(adherenceHistory);
        } else {
            res.status(403).json({ error: 'Access denied' });
        }
    }
);

// Record exercise result
router.post(
    '/workout/exercise-results',
    requireAuthenticated,
    validateBody(recordExerciseResultSchema),
    async (req, res) => {
        const {
            workoutPlanDayExerciseId,
            userWorkoutPlanId,
            reps,
            duration,
            calories,
        } = req.body;

        const result = await WorkoutService.recordExerciseResult({
            workoutPlanDayExerciseId,
            userWorkoutPlanId,
            userId: req.session!.user.id,
            reps,
            duration,
            calories,
        });

        res.status(201).json(result);
    }
);

// Get workout plan assignments
router.get(
    '/workout/:workoutPlanId/assign',
    requireAuthenticated,
    validateParams(workoutPlanIdParamSchema),
    validateQuery(z.object({ userId: z.string().optional() }).optional()),
    async (req, res) => {
        const { workoutPlanId } = req.params;
        const user = req.session!.user;

        if (user.role === 'trainee') {
            // Get current user's assigned plans
            const plans = await WorkoutService.getUserAssignedWorkoutPlans(
                user.id
            );

            const matchingPlans = plans.filter(
                (p) => p.workoutPlan?.id === parseInt(workoutPlanId)
            );
            res.json(matchingPlans);
        } else if (user.role === 'coach') {
            // Get assigned plans for specified user
            const { userId } = req.query as { userId?: string };
            if (!userId) {
                return res.status(400).json({
                    error: 'userId query parameter is required for coaches',
                });
            }
            const plans =
                await WorkoutService.getUserAssignedWorkoutPlans(userId);
            const matchingPlans = plans.filter(
                (p) => p.workoutPlan?.id === parseInt(workoutPlanId)
            );
            res.json(matchingPlans);
        } else {
            res.status(403).json({ error: 'Access denied' });
        }
    }
);

// Assign workout plan to trainee (coach only)
router.post(
    '/workout/:workoutPlanId/assign',
    requireCoach,
    validateParams(workoutPlanIdParamSchema),
    validateBody(assignWorkoutPlanSchema),
    async (req, res) => {
        const { workoutPlanId } = req.params;
        const { userId, startDate, endDate } = req.body;

        const assignment = await WorkoutService.assignWorkoutPlanToUser({
            userId,
            workoutPlanId: parseInt(workoutPlanId),
            assignedBy: req.session!.user.id,
            startDate: new Date(startDate),
            endDate: endDate ? new Date(endDate) : undefined,
        });

        res.status(201).json(assignment);
    }
);

// Get user exercise results for a user workout plan
router.get(
    '/workout/user-plans/:userWorkoutPlanId/results',
    requireAuthenticated,
    validateParams(userWorkoutPlanIdParamSchema),
    validateQuery(z.object({ userId: z.string().optional() }).optional()),
    async (req, res) => {
        const { userWorkoutPlanId } = req.params;
        const user = req.session!.user;

        if (user.role === 'trainee') {
            // Get current user's exercise results
            const results = await WorkoutService.getWorkoutPlanResults(
                parseInt(userWorkoutPlanId),
                user.id
            );

            if (!results) {
                return res.status(404).json({
                    error: 'User workout plan not found or not assigned to user',
                });
            }

            res.json(results);
        } else if (user.role === 'coach') {
            // Get exercise results for specified user
            const { userId } = req.query as { userId?: string };
            if (!userId) {
                return res.status(400).json({
                    error: 'userId query parameter is required for coaches',
                });
            }

            const results = await WorkoutService.getWorkoutPlanResults(
                parseInt(userWorkoutPlanId),
                userId
            );

            if (!results) {
                return res.status(404).json({
                    error: 'User workout plan not found or not assigned to user',
                });
            }

            res.json(results);
        } else {
            res.status(403).json({ error: 'Access denied' });
        }
    }
);

// Get user information by ID
router.get(
    '/:userId',
    requireAuthenticated,
    validateParams(userIdParamSchema),
    async (req, res) => {
        const { userId } = req.params;

        const user = await UserService.getUserById(userId);
        if (!user) {
            return res.status(404).json({ error: 'User not found' });
        }

        res.json(user);
    }
);

export default router;
