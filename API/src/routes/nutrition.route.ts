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
    nutritionPlanIdParamSchema,
    createNutritionPlanSchema,
    updateNutritionPlanSchema,
    createNutritionPlanDaySchema,
    updateNutritionPlanDaySchema,
    createNutritionPlanMealSchema,
    updateNutritionPlanMealSchema,
    createNutritionPlanFoodSchema,
    updateNutritionPlanFoodSchema,
    bulkUpdateNutritionPlanSchema,
} from '../validation/schemas.ts';
import { NutritionService } from '@services/nutrition.service.ts';

const router = Router();

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
// router.get(
//     '/days/:id',
//     requireAuthenticated,
//     validateParams(idParamSchema),
//     async (req, res) => {
//         const id = (req.params as any).id as number;
//         const day = await NutritionService.getNutritionPlanDayById(id);

//         if (!day) {
//             return res
//                 .status(404)
//                 .json({ error: 'Nutrition plan day not found' });
//         }

//         res.json(day);
//     }
// );

// // Update nutrition plan day
// router.put(
//     '/days/:id',
//     requireAuthenticated,
//     validateParams(idParamSchema),
//     validateBody(updateNutritionPlanDaySchema),
//     async (req, res) => {
//         const id = (req.params as any).id as number;
//         const updateData = req.body;

//         const day = await NutritionService.updateNutritionPlanDay(
//             id,
//             updateData
//         );
//         if (!day) {
//             return res
//                 .status(404)
//                 .json({ error: 'Nutrition plan day not found' });
//         }

//         res.json(day);
//     }
// );

// // Delete nutrition plan day
// router.delete(
//     '/days/:id',
//     requireAuthenticated,
//     validateParams(idParamSchema),
//     async (req, res) => {
//         const id = (req.params as any).id as number;

//         const day = await NutritionService.deleteNutritionPlanDay(id);
//         if (!day) {
//             return res
//                 .status(404)
//                 .json({ error: 'Nutrition plan day not found' });
//         }

//         res.json({ message: 'Nutrition plan day deleted successfully' });
//     }
// );

// // Get all meals for a nutrition plan day
// router.get(
//     '/days/:id/meals',
//     requireAuthenticated,
//     validateParams(idParamSchema),
//     async (req, res) => {
//         const nutritionPlanDayId = (req.params as any).id as number;
//         const meals =
//             await NutritionService.getNutritionPlanMeals(nutritionPlanDayId);
//         res.json(meals);
//     }
// );

// // Get specific meal by ID
// router.get(
//     '/meals/:id',
//     requireAuthenticated,
//     validateParams(idParamSchema),
//     async (req, res) => {
//         const id = (req.params as any).id as number;
//         const meal = await NutritionService.getNutritionPlanMealById(id);

//         if (!meal) {
//             return res
//                 .status(404)
//                 .json({ error: 'Nutrition plan meal not found' });
//         }

//         res.json(meal);
//     }
// );

// // Create new meal for nutrition plan day
// router.post(
//     '/days/:id/meals',
//     requireAuthenticated,
//     validateParams(idParamSchema),
//     validateBody(createNutritionPlanMealSchema),
//     async (req, res) => {
//         const nutritionPlanDayId = (req.params as any).id as number;
//         const mealData = { ...req.body, nutritionPlanDayId };

//         const meal = await NutritionService.createNutritionPlanMeal(mealData);
//         res.status(201).json(meal);
//     }
// );

// // Update nutrition plan meal
// router.put(
//     '/meals/:id',
//     requireAuthenticated,
//     validateParams(idParamSchema),
//     validateBody(updateNutritionPlanMealSchema),
//     async (req, res) => {
//         const id = (req.params as any).id as number;
//         const updateData = req.body;

//         const meal = await NutritionService.updateNutritionPlanMeal(
//             id,
//             updateData
//         );
//         if (!meal) {
//             return res
//                 .status(404)
//                 .json({ error: 'Nutrition plan meal not found' });
//         }

//         res.json(meal);
//     }
// );

// // Delete nutrition plan meal
// router.delete(
//     '/meals/:id',
//     requireAuthenticated,
//     validateParams(idParamSchema),
//     async (req, res) => {
//         const id = (req.params as any).id as number;

//         const meal = await NutritionService.deleteNutritionPlanMeal(id);
//         if (!meal) {
//             return res
//                 .status(404)
//                 .json({ error: 'Nutrition plan meal not found' });
//         }

//         res.json({ message: 'Nutrition plan meal deleted successfully' });
//     }
// );

// // Get all foods for a nutrition plan meal
// router.get(
//     '/meals/:id/foods',
//     requireAuthenticated,
//     validateParams(idParamSchema),
//     async (req, res) => {
//         const nutritionPlanMealId = (req.params as any).id as number;
//         const foods =
//             await NutritionService.getNutritionPlanFoods(nutritionPlanMealId);
//         res.json(foods);
//     }
// );

// // Get specific food by ID
// router.get(
//     '/foods/:id',
//     requireAuthenticated,
//     validateParams(idParamSchema),
//     async (req, res) => {
//         const id = (req.params as any).id as number;
//         const food = await NutritionService.getNutritionPlanFoodById(id);

//         if (!food) {
//             return res
//                 .status(404)
//                 .json({ error: 'Nutrition plan food not found' });
//         }

//         res.json(food);
//     }
// );

// // Create new food for nutrition plan meal
// router.post(
//     '/meals/:id/foods',
//     requireAuthenticated,
//     validateParams(idParamSchema),
//     validateBody(createNutritionPlanFoodSchema),
//     async (req, res) => {
//         const nutritionPlanMealId = (req.params as any).id as number;
//         const foodData = { ...req.body, nutritionPlanMealId };

//         const food = await NutritionService.createNutritionPlanFood(foodData);
//         res.status(201).json(food);
//     }
// );

// // Update nutrition plan food
// router.put(
//     '/foods/:id',
//     requireAuthenticated,
//     validateParams(idParamSchema),
//     validateBody(updateNutritionPlanFoodSchema),
//     async (req, res) => {
//         const id = (req.params as any).id as number;
//         const updateData = req.body;

//         const food = await NutritionService.updateNutritionPlanFood(
//             id,
//             updateData
//         );
//         if (!food) {
//             return res
//                 .status(404)
//                 .json({ error: 'Nutrition plan food not found' });
//         }

//         res.json(food);
//     }
// );

// // Delete nutrition plan food
// router.delete(
//     '/foods/:id',
//     requireAuthenticated,
//     validateParams(idParamSchema),
//     async (req, res) => {
//         const id = (req.params as any).id as number;

//         const food = await NutritionService.deleteNutritionPlanFood(id);
//         if (!food) {
//             return res
//                 .status(404)
//                 .json({ error: 'Nutrition plan food not found' });
//         }

//         res.json({ message: 'Nutrition plan food deleted successfully' });
//     }
// );

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

// Update nutrition plan
// router.put(
//     '/:id',
//     requireAuthenticated,
//     validateParams(idParamSchema),
//     validateBody(updateNutritionPlanSchema),
//     async (req, res) => {
//         const id = (req.params as any).id as number;
//         const updateData = req.body;

//         const plan = await NutritionService.updateNutritionPlan(id, updateData);
//         if (!plan) {
//             return res.status(404).json({ error: 'Nutrition plan not found' });
//         }

//         res.json(plan);
//     }
// );

// Delete nutrition plan
router.delete(
    '/:id',
    requireAuthenticated,
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

// Get all days for a nutrition plan
// router.get(
//     '/:id/days',
//     requireAuthenticated,
//     validateParams(idParamSchema),
//     async (req, res) => {
//         const nutritionPlanId = (req.params as any).id as number;
//         const days =
//             await NutritionService.getNutritionPlanDays(nutritionPlanId);
//         res.json(days);
//     }
// );

// Create new day for nutrition plan
// router.post(
//     '/:id/days',
//     requireAuthenticated,
//     validateParams(idParamSchema),
//     validateBody(createNutritionPlanDaySchema),
//     async (req, res) => {
//         const nutritionPlanId = (req.params as any).id as number;
//         const dayData = { ...req.body, nutritionPlanId };

//         const day = await NutritionService.createNutritionPlanDay(dayData);
//         res.status(201).json(day);
//     }
// );

// Bulk update nutrition plan
router.put(
    '/:id/bulk',
    requireAuthenticated,
    validateParams(idParamSchema),
    validateBody(bulkUpdateNutritionPlanSchema),
    async (req, res) => {
        try {
            const id = (req.params as any).id as number;
            const updateData = req.body;

            const plan = await NutritionService.bulkUpdateNutritionPlan(
                id,
                updateData
            );
            if (!plan) {
                return res
                    .status(404)
                    .json({ error: 'Nutrition plan not found' });
            }

            res.json(plan);
        } catch (error) {
            console.error('Bulk update nutrition plan error:', error);
            res.status(500).json({ error: 'Internal server error' });
        }
    }
);

export default router;
