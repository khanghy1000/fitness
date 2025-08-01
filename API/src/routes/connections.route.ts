import { Router } from 'express';
import {
    requireAuthenticated,
    requireCoach,
    requireTrainee,
} from '@middlewares/auth.middleware.ts';
import {
    validateBody,
    validateParams,
} from '@middlewares/validation.middleware.ts';
import {
    connectRequestSchema,
    connectionRequestTypeSchema,
    traineeIdSchema,
    traineeParamSchema,
} from '../validation/schemas.ts';
import { CoachTraineeService } from '@services/coach-trainee.service.ts';
import { WorkoutService } from '@services/workout.service.ts';
import { ExerciseService } from '@services/exercise.service.ts';
import { UserService } from '@services/user.service.ts';

const router = Router();

// Send connection request (trainee to coach)
router.post(
    '/connect',
    requireTrainee,
    validateBody(connectRequestSchema),
    async (req, res) => {
        try {
            const { coachId, notes } = req.body;

            const connection = await CoachTraineeService.sendConnectionRequest(
                coachId,
                req.session!.user.id,
                notes
            );

            res.status(201).json(connection);
        } catch (error) {
            if (
                error instanceof Error &&
                error.message ===
                    'Connection request already exists or is active'
            ) {
                return res.status(400).json({ error: error.message });
            }
            res.status(500).json({ error: 'Internal server error' });
        }
    }
);

// Get connection requests (sent or received)
router.get(
    '/requests/:type',
    requireAuthenticated,
    validateParams(connectionRequestTypeSchema),
    async (req, res) => {
        const { type } = req.params;

        // Only trainees can see sent requests, only coaches can see received requests
        if (type === 'sent' && req.session!.user.role !== 'trainee') {
            return res
                .status(403)
                .json({ error: 'Only trainees can view sent requests' });
        }

        if (type === 'received' && req.session!.user.role !== 'coach') {
            return res
                .status(403)
                .json({ error: 'Only coaches can view received requests' });
        }

        const requests = await CoachTraineeService.getConnectionRequests(
            req.session!.user.id,
            type as 'sent' | 'received'
        );

        res.json(requests);
    }
);

// Accept connection request (coach only)
router.post(
    '/accept',
    requireCoach,
    validateBody(traineeIdSchema),
    async (req, res) => {
        const { traineeId } = req.body;

        const connection = await CoachTraineeService.acceptConnectionRequest(
            req.session!.user.id,
            traineeId
        );

        if (!connection) {
            return res
                .status(404)
                .json({ error: 'Connection request not found' });
        }

        res.json(connection);
    }
);

// Reject connection request (coach only)
router.post(
    '/reject',
    requireCoach,
    validateBody(traineeIdSchema),
    async (req, res) => {
        const { traineeId } = req.body;

        const connection = await CoachTraineeService.rejectConnectionRequest(
            req.session!.user.id,
            traineeId
        );

        if (!connection) {
            return res
                .status(404)
                .json({ error: 'Connection request not found' });
        }

        res.json({ message: 'Connection request rejected successfully' });
    }
);

// Get active connections
router.get('/active', requireAuthenticated, async (req, res) => {
    const userRole = req.session!.user.role;

    if (userRole !== 'coach' && userRole !== 'trainee') {
        return res.status(403).json({ error: 'Access denied' });
    }

    const connections = await CoachTraineeService.getActiveConnections(
        req.session!.user.id,
        userRole
    );

    res.json(connections);
});

// Get all connections
router.get('/all', requireAuthenticated, async (req, res) => {
    const userRole = req.session!.user.role;

    if (userRole !== 'coach' && userRole !== 'trainee') {
        return res.status(403).json({ error: 'Access denied' });
    }

    const connections = await CoachTraineeService.getAllConnections(
        req.session!.user.id,
        userRole
    );

    res.json(connections);
});

// End connection (coach only)
router.post(
    '/disconnect',
    requireCoach,
    validateBody(traineeIdSchema),
    async (req, res) => {
        const { traineeId } = req.body;

        const connection = await CoachTraineeService.endConnection(
            req.session!.user.id,
            traineeId
        );

        if (!connection) {
            return res
                .status(404)
                .json({ error: 'Active connection not found' });
        }

        res.json({ message: 'Connection ended successfully' });
    }
);

export default router;
