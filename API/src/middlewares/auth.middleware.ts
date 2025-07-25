import { auth } from '@lib/auth.ts';
import { fromNodeHeaders } from 'better-auth/node';
import { NextFunction, Request, Response } from 'express';

async function attachUserSession(
    req: Request,
    res: Response,
    next: NextFunction
) {
    const session = await auth.api.getSession({
        headers: fromNodeHeaders(req.headers),
    });
    req.session = session;
    return next();
}

async function requireCoach(req: Request, res: Response, next: NextFunction) {
    const session = await auth.api.getSession({
        headers: fromNodeHeaders(req.headers),
    });
    req.session = session;

    if (session?.user.role === 'coach') {
        return next();
    }
    return res.status(403).json({ error: 'Forbidden: Coach role required' });
}

async function requireTrainee(req: Request, res: Response, next: NextFunction) {
    const session = await auth.api.getSession({
        headers: fromNodeHeaders(req.headers),
    });
    req.session = session;

    if (session?.user.role === 'trainee') {
        return next();
    }
    return res.status(403).json({ error: 'Forbidden: Trainee role required' });
}

async function requireAuthenticated(
    req: Request,
    res: Response,
    next: NextFunction
) {
    const session = await auth.api.getSession({
        headers: fromNodeHeaders(req.headers),
    });
    req.session = session;

    if (session) {
        return next();
    }
    return res
        .status(401)
        .json({ error: 'Unauthorized: Authentication required' });
}

export {
    attachUserSession,
    requireCoach,
    requireTrainee,
    requireAuthenticated,
};
