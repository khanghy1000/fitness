import { Router } from 'express';
import {
    requireAuthenticated,
    requireTrainee,
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

// Record nutrition adherence
router.post(
    '/nutrition-adherence',
    requireAuthenticated,
    validateBody(nutritionAdherenceSchema),
    async (req, res) => {
        const { userNutritionPlanId, date, adherencePercentage, notes } =
            req.body;

        const adherence = await UserService.recordNutritionAdherence({
            userId: req.session!.user.id,
            userNutritionPlanId,
            date: date ? new Date(date) : new Date(),
            adherencePercentage,
            notes,
        });

        res.status(201).json(adherence);
    }
);

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

export default router;
