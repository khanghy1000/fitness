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
    createWorkoutPlanSchema,
    updateWorkoutPlanSchema,
    assignWorkoutPlanSchema,
    addDayToWorkoutPlanSchema,
    updateWorkoutPlanDaySchema,
    addExerciseToPlanDaySchema,
    updateExerciseInPlanDaySchema,
    recordExerciseResultSchema,
} from '../validation/schemas.ts';
import { WorkoutService } from '@services/workout.service.ts';

const router = Router();

// Get specific day by ID
router.get(
    '/days/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;
        const day = await WorkoutService.getWorkoutPlanDayById(id);

        if (!day) {
            return res
                .status(404)
                .json({ error: 'Workout plan day not found' });
        }

        res.json(day);
    }
);

// Update workout plan day
router.put(
    '/days/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    validateBody(updateWorkoutPlanDaySchema),
    async (req, res) => {
        const id = (req.params as any).id as number;
        const updateData = req.body;

        const day = await WorkoutService.updateWorkoutPlanDay(id, updateData);
        if (!day) {
            return res
                .status(404)
                .json({ error: 'Workout plan day not found' });
        }

        res.json(day);
    }
);

// Delete workout plan day
router.delete(
    '/days/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;

        const day = await WorkoutService.deleteWorkoutPlanDay(id);
        if (!day) {
            return res
                .status(404)
                .json({ error: 'Workout plan day not found' });
        }

        res.json({ message: 'Workout plan day deleted successfully' });
    }
);

// Get all exercises for a workout plan day
router.get(
    '/days/:id/exercises',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const workoutPlanDayId = (req.params as any).id as number;
        const exercises =
            await WorkoutService.getWorkoutPlanDayExercises(workoutPlanDayId);
        res.json(exercises);
    }
);

// Get specific exercise by ID
router.get(
    '/exercises/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;
        const exercise = await WorkoutService.getWorkoutPlanDayExerciseById(id);

        if (!exercise) {
            return res
                .status(404)
                .json({ error: 'Workout plan exercise not found' });
        }

        res.json(exercise);
    }
);

// Add exercise to workout plan day
router.post(
    '/days/:id/exercises',
    requireAuthenticated,
    validateParams(idParamSchema),
    validateBody(addExerciseToPlanDaySchema),
    async (req, res) => {
        const workoutPlanDayId = (req.params as any).id as number;
        const exerciseData = req.body;

        const exercise = await WorkoutService.addExerciseToPlanDay(
            workoutPlanDayId,
            exerciseData
        );

        res.status(201).json(exercise);
    }
);

// Update workout plan exercise
router.put(
    '/exercises/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    validateBody(updateExerciseInPlanDaySchema),
    async (req, res) => {
        const id = (req.params as any).id as number;
        const updateData = req.body;

        const exercise = await WorkoutService.updateExerciseInPlanDay(
            id,
            updateData
        );
        if (!exercise) {
            return res
                .status(404)
                .json({ error: 'Workout plan exercise not found' });
        }

        res.json(exercise);
    }
);

// Delete workout plan exercise
router.delete(
    '/exercises/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;

        const exercise = await WorkoutService.deleteExerciseFromPlanDay(id);
        if (!exercise) {
            return res
                .status(404)
                .json({ error: 'Workout plan exercise not found' });
        }

        res.json({ message: 'Workout plan exercise deleted successfully' });
    }
);

// Get user's assigned workout plans
router.get('/assigned/:userId', requireAuthenticated, async (req, res) => {
    const userId = req.params.userId;

    // Check if user is requesting their own data or if they're a coach
    if (req.session!.user.id !== userId && req.session!.user.role !== 'coach') {
        return res.status(403).json({ error: 'Access denied' });
    }

    const plans = await WorkoutService.getUserAssignedWorkoutPlans(userId);
    res.json(plans);
});

// Record exercise result
router.post(
    '/exercise-results',
    requireAuthenticated,
    validateBody(recordExerciseResultSchema),
    async (req, res) => {
        const { workoutPlanDayExerciseId, reps, duration, calories } = req.body;

        const result = await WorkoutService.recordExerciseResult({
            workoutPlanDayExerciseId,
            userId: req.session!.user.id,
            reps,
            duration,
            calories,
        });

        res.status(201).json(result);
    }
);

// Get all workout plans
// Coaches get only their created plans
// Trainees get both created plans and assigned plans
router.get('/', requireAuthenticated, async (req, res) => {
    const user = req.session!.user;
    const plans = await WorkoutService.getAllWorkoutPlans(user.id, user.role);
    res.json(plans);
});

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
            userRole: req.session!.user.role,
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

// Get all days for a workout plan
router.get(
    '/:id/days',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const workoutPlanId = (req.params as any).id as number;
        const days = await WorkoutService.getWorkoutPlanDays(workoutPlanId);
        res.json(days);
    }
);

// Create new day for workout plan
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

export default router;
