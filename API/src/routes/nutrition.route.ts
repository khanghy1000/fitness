import { Router } from 'express';
import {
    requireCoach,
    requireAuthenticated,
} from '@middlewares/auth.middleware.ts';
import {
    validateBody,
    validateParams,
    validateQuery,
} from '@middlewares/validation.middleware.ts';
import {
    idParamSchema,
    createNutritionPlanSchema,
    updateNutritionPlanSchema,
    assignNutritionPlanSchema,
    nutritionAdherenceSchema,
    mealCompletionSchema,
} from '../validation/schemas.ts';
import { NutritionService } from '@services/nutrition.service.ts';

const router = Router();

// Helper function to convert JavaScript getDay() result to weekday enum
const getWeekdayEnum = (
    dayNumber: number
): 'sun' | 'mon' | 'tue' | 'wed' | 'thu' | 'fri' | 'sat' => {
    const weekdays = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'] as const;
    return weekdays[dayNumber];
};

// Get all nutrition plans
router.get('/', requireAuthenticated, async (req, res) => {
    const user = req.session!.user;
    const plans = await NutritionService.getAllNutritionPlans(
        user.id,
        user.role
    );
    res.json(plans);
});

// Get nutrition plan by ID with full details
router.get(
    '/:id/details',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;

        const plan = await NutritionService.getNutritionPlanWithDetails(id);
        if (!plan) {
            return res.status(404).json({ error: 'Nutrition plan not found' });
        }

        res.json(plan);
    }
);

// Get nutrition plan by ID
router.get(
    '/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;

        const plan = await NutritionService.getNutritionPlanById(id);
        if (!plan) {
            return res.status(404).json({ error: 'Nutrition plan not found' });
        }

        res.json(plan);
    }
);

// Create new nutrition plan (authenticated users can create)
router.post(
    '/',
    requireAuthenticated,
    validateBody(createNutritionPlanSchema),
    async (req, res) => {
        const planData = {
            ...req.body,
            createdBy: req.session!.user.id,
        };

        const plan = await NutritionService.createNutritionPlan(planData);

        res.status(201).json(plan);
    }
);

// Update nutrition plan (coach only)
router.put(
    '/:id',
    requireCoach,
    validateParams(idParamSchema),
    validateBody(updateNutritionPlanSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;
        const updateData = req.body;

        const plan = await NutritionService.updateNutritionPlan(id, updateData);
        if (!plan) {
            return res.status(404).json({ error: 'Nutrition plan not found' });
        }

        res.json(plan);
    }
);

// Delete nutrition plan (coach only)
router.delete(
    '/:id',
    requireCoach,
    validateParams(idParamSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;

        const plan = await NutritionService.deleteNutritionPlan(id);
        if (!plan) {
            return res.status(404).json({ error: 'Nutrition plan not found' });
        }

        res.json({ message: 'Nutrition plan deleted successfully' });
    }
);

// Assign nutrition plan to trainee (coach only)
router.post(
    '/:id/assign',
    requireCoach,
    validateParams(idParamSchema),
    validateBody(assignNutritionPlanSchema),
    async (req, res) => {
        const nutritionPlanId = (req.params as any).id as number;
        const { userId, startDate, endDate } = req.body;

        const assignment = await NutritionService.assignNutritionPlanToUser({
            userId,
            nutritionPlanId,
            assignedBy: req.session!.user.id,
            startDate: new Date(startDate),
            endDate: endDate ? new Date(endDate) : undefined,
        });

        res.status(201).json(assignment);
    }
);

// Get user's nutrition plans with adherence info
router.get('/user/:userId', requireAuthenticated, async (req, res) => {
    const userId = req.params.userId;

    // Check if user is requesting their own data or if they're a coach
    if (req.session!.user.id !== userId && req.session!.user.role !== 'coach') {
        return res.status(403).json({ error: 'Access denied' });
    }

    const plans = await NutritionService.getUserNutritionPlans(userId);
    res.json(plans);
});

// Create daily adherence record
router.post(
    '/adherence',
    requireAuthenticated,
    validateBody(nutritionAdherenceSchema),
    async (req, res) => {
        const { userNutritionPlanId, date, weekday, totalMeals } = req.body;

        const adherence = await NutritionService.createDailyAdherence({
            userNutritionPlanId,
            userId: req.session!.user.id,
            date: new Date(date || new Date()),
            weekday: weekday || getWeekdayEnum(new Date().getDay()),
            totalMeals,
        });

        res.status(201).json(adherence);
    }
);

// Complete a meal
router.post(
    '/adherence/:adherenceId/meals/:mealId/complete',
    requireAuthenticated,
    validateBody(mealCompletionSchema),
    async (req, res) => {
        const adherenceId = parseInt(req.params.adherenceId);
        const mealId = parseInt(req.params.mealId);

        const completion = await NutritionService.completeMeal({
            nutritionAdherenceId: adherenceId,
            nutritionPlanMealId: mealId,
            userId: req.session!.user.id,
            ...req.body,
        });

        res.status(201).json(completion);
    }
);

// Get daily adherence
router.get(
    '/adherence/:userNutritionPlanId/:date',
    requireAuthenticated,
    async (req, res) => {
        const userNutritionPlanId = parseInt(req.params.userNutritionPlanId);
        const date = new Date(req.params.date);

        const adherence = await NutritionService.getDailyAdherence(
            userNutritionPlanId,
            date
        );
        res.json(adherence);
    }
);

export default router;
