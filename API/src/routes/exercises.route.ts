import { Router } from 'express';
import { requireAuthenticated } from '@middlewares/auth.middleware.ts';
import {
    validateParams,
    validateBody,
} from '@middlewares/validation.middleware.ts';
import {
    idParamSchema,
    recordExerciseResultSchema,
} from '../validation/schemas.ts';
import { ExerciseService } from '@services/exercise.service.ts';

const router = Router();

// Get all exercise types
router.get('/', requireAuthenticated, async (req, res) => {
    const exerciseTypes = await ExerciseService.getAllExerciseTypes();
    res.json(exerciseTypes);
});

// Get exercise type by name
router.get('/name/:name', requireAuthenticated, async (req, res) => {
    const name = req.params.name;

    const exerciseType = await ExerciseService.getExerciseTypeByName(name);
    if (!exerciseType) {
        return res.status(404).json({ error: 'Exercise type not found' });
    }

    res.json(exerciseType);
});

// Get exercise type by ID
router.get(
    '/:id',
    requireAuthenticated,
    validateParams(idParamSchema),
    async (req, res) => {
        const id = (req.params as any).id as number;

        const exerciseType = await ExerciseService.getExerciseTypeById(id);
        if (!exerciseType) {
            return res.status(404).json({ error: 'Exercise type not found' });
        }

        res.json(exerciseType);
    }
);

export default router;
