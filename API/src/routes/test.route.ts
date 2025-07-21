import { auth } from '@lib/auth.ts';
import { fromNodeHeaders } from 'better-auth/node';
import { Router } from 'express';
import {
    requireAuthenticated,
    requireCoach,
} from '@middlewares/auth.middleware.ts';

const router = Router();

router.get('/auth/coach', requireCoach, (req, res) => {
    res.json({ message: 'User authenticated' });
});

router.get('/auth/trainee', requireAuthenticated, (req, res) => {
    res.json({ message: 'User authenticated' });
});

router.get('/auth/current-user', requireAuthenticated, async (req, res) => {
    const session = await auth.api.getSession({
        headers: fromNodeHeaders(req.headers),
    });
    return res.json(session);
});

export default router;
