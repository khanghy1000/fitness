import { Router } from 'express';
import { requireAuthenticated } from '@middlewares/auth.middleware.ts';
import {
    validateBody,
    validateParams,
    validateQuery,
} from '@middlewares/validation.middleware.ts';
import {
    idParamSchema,
    createPlannedWorkoutSchema,
    updatePlannedWorkoutSchema,
} from '../validation/schemas.ts';
import { PlannedWorkoutService } from '@services/planned-workout.service.ts';

const router = Router();

// Get user's planned workouts
router.get('/', requireAuthenticated, async (req, res) => {
    const plannedWorkouts = await PlannedWorkoutService.getUserPlannedWorkouts(
        req.session!.user.id
    );
    res.json(plannedWorkouts);
});

// Get planned workout by ID
router.get(
    '/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;

        const plannedWorkout =
            await PlannedWorkoutService.getPlannedWorkoutById(id);
        if (!plannedWorkout) {
            return res.status(404).json({ error: 'Planned workout not found' });
        }

        // Check if user owns this planned workout
        if (plannedWorkout.userId !== req.session!.user.id) {
            return res.status(403).json({ error: 'Access denied' });
        }

        res.json(plannedWorkout);
    }
);

// Create/Schedule a planned workout
router.post(
    '/',
    requireAuthenticated,
    validateBody(createPlannedWorkoutSchema),
    async (req, res) => {
        const { userWorkoutPlanId, weekdays, time, isActive } = req.body;

        const plannedWorkout = await PlannedWorkoutService.createPlannedWorkout(
            {
                userId: req.session!.user.id,
                userWorkoutPlanId,
                weekdays,
                time,
                isActive,
            }
        );

        res.status(201).json(plannedWorkout);
    }
);

// Update planned workout
router.put(
    '/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    validateBody(updatePlannedWorkoutSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;

        // First check if user owns this planned workout
        const plannedWorkout =
            await PlannedWorkoutService.getPlannedWorkoutById(id);
        if (!plannedWorkout) {
            return res.status(404).json({ error: 'Planned workout not found' });
        }

        if (plannedWorkout.userId !== req.session!.user.id) {
            return res.status(403).json({ error: 'Access denied' });
        }

        const updateData = req.body;

        const updatedPlannedWorkout =
            await PlannedWorkoutService.updatePlannedWorkout(id, updateData);

        res.json(updatedPlannedWorkout);
    }
);

// Delete planned workout
router.delete(
    '/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;

        // First check if user owns this planned workout
        const plannedWorkout =
            await PlannedWorkoutService.getPlannedWorkoutById(id);
        if (!plannedWorkout) {
            return res.status(404).json({ error: 'Planned workout not found' });
        }

        if (plannedWorkout.userId !== req.session!.user.id) {
            return res.status(403).json({ error: 'Access denied' });
        }

        await PlannedWorkoutService.deletePlannedWorkout(id);
        res.json({ message: 'Planned workout deleted successfully' });
    }
);

// Toggle planned workout active status
router.post(
    '/:id/toggle',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;
        const { isActive } = req.body;

        // First check if user owns this planned workout
        const plannedWorkout =
            await PlannedWorkoutService.getPlannedWorkoutById(id);
        if (!plannedWorkout) {
            return res.status(404).json({ error: 'Planned workout not found' });
        }

        if (plannedWorkout.userId !== req.session!.user.id) {
            return res.status(403).json({ error: 'Access denied' });
        }

        const updatedPlannedWorkout =
            await PlannedWorkoutService.togglePlannedWorkout(id, isActive);
        res.json(updatedPlannedWorkout);
    }
);

// Get planned workouts for a specific weekday
router.get('/weekday/:weekday', requireAuthenticated, async (req, res) => {
    const { weekday } = req.params;

    // Validate weekday parameter
    const validWeekdays = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'];
    if (!validWeekdays.includes(weekday)) {
        return res.status(400).json({
            error: 'Weekday must be one of: sun, mon, tue, wed, thu, fri, sat',
        });
    }

    const plannedWorkouts =
        await PlannedWorkoutService.getUserPlannedWorkoutsForWeekday(
            req.session!.user.id,
            weekday as 'sun' | 'mon' | 'tue' | 'wed' | 'thu' | 'fri' | 'sat'
        );

    res.json(plannedWorkouts);
});

// Get today's planned workouts
router.get('/today', requireAuthenticated, async (req, res) => {
    const todaysWorkouts = await PlannedWorkoutService.getTodaysPlannedWorkouts(
        req.session!.user.id
    );

    res.json(todaysWorkouts);
});

export default router;
