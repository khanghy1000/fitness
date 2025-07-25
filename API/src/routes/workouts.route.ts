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
    dayIdParamSchema,
    myQuerySchema,
    createWorkoutPlanSchema,
    updateWorkoutPlanSchema,
    assignWorkoutPlanSchema,
    addDayToWorkoutPlanSchema,
    addExerciseToPlanDaySchema,
} from '../validation/schemas.ts';
import { WorkoutService } from '@services/workout.service.ts';

const router = Router();

// Get all workout plans
router.get(
    '/',
    requireAuthenticated,
    validateQuery(myQuerySchema),
    async (req, res) => {
        const { my } = req.query as { my?: string };
        let createdBy: string | undefined;

        // If 'my' query param is present, filter by user's own plans
        if (my === 'true') {
            createdBy = req.session!.user.id;
        }

        const plans = await WorkoutService.getAllWorkoutPlans(createdBy);
        res.json(plans);
    }
);

// Get workout plan by ID with full details
router.get(
    '/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;

        const plan = await WorkoutService.getWorkoutPlanById(id);
        if (!plan) {
            return res.status(404).json({ error: 'Workout plan not found' });
        }

        res.json(plan);
    }
);

// Create new workout plan (authenticated users can create)
router.post(
    '/',
    requireAuthenticated,
    validateBody(createWorkoutPlanSchema),
    async (req, res) => {
        const planData = {
            ...req.body,
            createdBy: req.session!.user.id,
        };

        const plan = await WorkoutService.createWorkoutPlan(planData);

        res.status(201).json(plan);
    }
);

// Update workout plan (coach only)
router.put(
    '/:id',
    requireCoach,
    validateParams(idParamSchema),
    validateBody(updateWorkoutPlanSchema),
    async (req, res) => {
        try {
            const id = (req.params as any).id as number;
            const updateData = req.body;

            const plan = await WorkoutService.updateWorkoutPlan(id, updateData);
            if (!plan) {
                return res
                    .status(404)
                    .json({ error: 'Workout plan not found' });
            }

            res.json(plan);
        } catch (error) {
            res.status(500).json({ error: 'Internal server error' });
        }
    }
);

// Delete workout plan (coach only)
router.delete(
    '/:id',
    requireCoach,
    validateParams(idParamSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;

        const plan = await WorkoutService.deleteWorkoutPlan(id);
        if (!plan) {
            return res.status(404).json({ error: 'Workout plan not found' });
        }

        res.json({ message: 'Workout plan deleted successfully' });
    }
);

// Add day to workout plan (plan creator only)
router.post(
    '/:id/days',
    requireAuthenticated,
    validateParams(idParamSchema),
    validateBody(addDayToWorkoutPlanSchema),
    async (req, res) => {
        const workoutPlanId = (req.params as any).id as number;
        const dayData = req.body;

        const planDay = await WorkoutService.addDayToWorkoutPlan(
            workoutPlanId,
            dayData
        );

        res.status(201).json(planDay);
    }
);

// Add exercise to workout plan day (plan creator only)
router.post(
    '/days/:dayId/exercises',
    requireAuthenticated,
    validateParams(dayIdParamSchema),
    validateBody(addExerciseToPlanDaySchema),
    async (req, res) => {
        const workoutPlanDayId = (req.params as any).dayId as number;
        const exerciseData = req.body;

        const exercise = await WorkoutService.addExerciseToPlanDay(
            workoutPlanDayId,
            exerciseData
        );

        res.status(201).json(exercise);
    }
);

// Assign workout plan to trainee (coach only)
router.post(
    '/:id/assign',
    requireCoach,
    validateParams(idParamSchema),
    validateBody(assignWorkoutPlanSchema),
    async (req, res) => {
        const workoutPlanId = (req.params as any).id as number;
        const { userId, startDate, endDate } = req.body;

        const assignment = await WorkoutService.assignWorkoutPlanToUser({
            userId,
            workoutPlanId,
            assignedBy: req.session!.user.id,
            startDate: new Date(startDate),
            endDate: endDate ? new Date(endDate) : undefined,
        });

        res.status(201).json(assignment);
    }
);

export default router;
