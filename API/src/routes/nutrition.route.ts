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
    createNutritionPlanDaySchema,
    updateNutritionPlanDaySchema,
    createNutritionPlanMealSchema,
    updateNutritionPlanMealSchema,
    createNutritionPlanFoodSchema,
    updateNutritionPlanFoodSchema,
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
// Coaches get only their created plans
// Trainees get both created plans and assigned plans
router.get('/', requireAuthenticated, async (req, res) => {
    const user = req.session!.user;
    const plans = await NutritionService.getAllNutritionPlans(
        user.id,
        user.role
    );
    res.json(plans);
});

// Create new nutrition plan (authenticated users can create)
router.post(
    '/',
    requireAuthenticated,
    validateBody(createNutritionPlanSchema),
    async (req, res) => {
        const planData = {
            ...req.body,
            createdBy: req.session!.user.id,
            userRole: req.session!.user.role,
        };

        const plan = await NutritionService.createNutritionPlan(planData);

        res.status(201).json(plan);
    }
);

// Get specific day by ID
router.get(
    '/days/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;
        const day = await NutritionService.getNutritionPlanDayById(id);

        if (!day) {
            return res
                .status(404)
                .json({ error: 'Nutrition plan day not found' });
        }

        res.json(day);
    }
);

// Update nutrition plan day
router.put(
    '/days/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    validateBody(updateNutritionPlanDaySchema),
    async (req, res) => {
        const id = (req.params as any).id as number;
        const updateData = req.body;

        const day = await NutritionService.updateNutritionPlanDay(
            id,
            updateData
        );
        if (!day) {
            return res
                .status(404)
                .json({ error: 'Nutrition plan day not found' });
        }

        res.json(day);
    }
);

// Delete nutrition plan day
router.delete(
    '/days/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;

        const day = await NutritionService.deleteNutritionPlanDay(id);
        if (!day) {
            return res
                .status(404)
                .json({ error: 'Nutrition plan day not found' });
        }

        res.json({ message: 'Nutrition plan day deleted successfully' });
    }
);

// Get all meals for a nutrition plan day
router.get(
    '/days/:id/meals',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const nutritionPlanDayId = (req.params as any).id as number;
        const meals =
            await NutritionService.getNutritionPlanMeals(nutritionPlanDayId);
        res.json(meals);
    }
);

// Get specific meal by ID
router.get(
    '/meals/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;
        const meal = await NutritionService.getNutritionPlanMealById(id);

        if (!meal) {
            return res
                .status(404)
                .json({ error: 'Nutrition plan meal not found' });
        }

        res.json(meal);
    }
);

// Create new meal for nutrition plan day
router.post(
    '/days/:id/meals',
    requireAuthenticated,
    validateParams(idParamSchema),
    validateBody(createNutritionPlanMealSchema),
    async (req, res) => {
        const nutritionPlanDayId = (req.params as any).id as number;
        const mealData = { ...req.body, nutritionPlanDayId };

        const meal = await NutritionService.createNutritionPlanMeal(mealData);
        res.status(201).json(meal);
    }
);

// Update nutrition plan meal
router.put(
    '/meals/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    validateBody(updateNutritionPlanMealSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;
        const updateData = req.body;

        const meal = await NutritionService.updateNutritionPlanMeal(
            id,
            updateData
        );
        if (!meal) {
            return res
                .status(404)
                .json({ error: 'Nutrition plan meal not found' });
        }

        res.json(meal);
    }
);

// Delete nutrition plan meal
router.delete(
    '/meals/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;

        const meal = await NutritionService.deleteNutritionPlanMeal(id);
        if (!meal) {
            return res
                .status(404)
                .json({ error: 'Nutrition plan meal not found' });
        }

        res.json({ message: 'Nutrition plan meal deleted successfully' });
    }
);

// Get all foods for a nutrition plan meal
router.get(
    '/meals/:id/foods',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const nutritionPlanMealId = (req.params as any).id as number;
        const foods =
            await NutritionService.getNutritionPlanFoods(nutritionPlanMealId);
        res.json(foods);
    }
);

// Get specific food by ID
router.get(
    '/foods/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;
        const food = await NutritionService.getNutritionPlanFoodById(id);

        if (!food) {
            return res
                .status(404)
                .json({ error: 'Nutrition plan food not found' });
        }

        res.json(food);
    }
);

// Create new food for nutrition plan meal
router.post(
    '/meals/:id/foods',
    requireAuthenticated,
    validateParams(idParamSchema),
    validateBody(createNutritionPlanFoodSchema),
    async (req, res) => {
        const nutritionPlanMealId = (req.params as any).id as number;
        const foodData = { ...req.body, nutritionPlanMealId };

        const food = await NutritionService.createNutritionPlanFood(foodData);
        res.status(201).json(food);
    }
);

// Update nutrition plan food
router.put(
    '/foods/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    validateBody(updateNutritionPlanFoodSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;
        const updateData = req.body;

        const food = await NutritionService.updateNutritionPlanFood(
            id,
            updateData
        );
        if (!food) {
            return res
                .status(404)
                .json({ error: 'Nutrition plan food not found' });
        }

        res.json(food);
    }
);

// Delete nutrition plan food
router.delete(
    '/foods/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;

        const food = await NutritionService.deleteNutritionPlanFood(id);
        if (!food) {
            return res
                .status(404)
                .json({ error: 'Nutrition plan food not found' });
        }

        res.json({ message: 'Nutrition plan food deleted successfully' });
    }
);

// Get user's assigned nutrition plans with adherence info
router.get('/assigned/:userId', requireAuthenticated, async (req, res) => {
    const userId = req.params.userId;

    // Check if user is requesting their own data or if they're a coach
    if (req.session!.user.id !== userId && req.session!.user.role !== 'coach') {
        return res.status(403).json({ error: 'Access denied' });
    }

    const plans = await NutritionService.getUserAssignedNutritionPlans(userId);
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

// Get nutrition plan by ID with full details
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

// Get all days for a nutrition plan
router.get(
    '/:id/days',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const nutritionPlanId = (req.params as any).id as number;
        const days =
            await NutritionService.getNutritionPlanDays(nutritionPlanId);
        res.json(days);
    }
);

// Create new day for nutrition plan
router.post(
    '/:id/days',
    requireAuthenticated,
    validateParams(idParamSchema),
    validateBody(createNutritionPlanDaySchema),
    async (req, res) => {
        const nutritionPlanId = (req.params as any).id as number;
        const dayData = { ...req.body, nutritionPlanId };

        const day = await NutritionService.createNutritionPlanDay(dayData);
        res.status(201).json(day);
    }
);

export default router;
