import { auth } from '@lib/auth.ts';
import { Router } from 'express';

const router = Router();

router.get('/', (req, res) => {
    res.json({ message: 'pong' });
});

export default router;
