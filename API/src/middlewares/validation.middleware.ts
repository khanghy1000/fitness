import { Request, Response, NextFunction } from 'express';
import { ZodSchema, ZodError, ZodIssue } from 'zod';

export const validateBody = (schema: ZodSchema) => {
    return (req: Request, res: Response, next: NextFunction) => {
        try {
            req.body = schema.parse(req.body);
            next();
        } catch (error) {
            if (error instanceof ZodError) {
                const errors = error.issues.map((err: ZodIssue) => ({
                    field: err.path.join('.'),
                    message: err.message,
                }));
                return res.status(400).json({
                    error: 'Validation error',
                    details: errors,
                });
            }
            next(error);
        }
    };
};

export const validateParams = (schema: ZodSchema) => {
    return (req: Request, res: Response, next: NextFunction) => {
        try {
            const parsedParams = schema.parse(req.params);
            req.params = parsedParams as any;
            next();
        } catch (error) {
            if (error instanceof ZodError) {
                const errors = error.issues.map((err: ZodIssue) => ({
                    field: err.path.join('.'),
                    message: err.message,
                }));
                return res.status(400).json({
                    error: 'Validation error',
                    details: errors,
                });
            }
            next(error);
        }
    };
};

export const validateQuery = (schema: ZodSchema) => {
    return (req: Request, res: Response, next: NextFunction) => {
        try {
            const parsedQuery = schema.parse(req.query);
            req.query = parsedQuery as any;
            next();
        } catch (error) {
            if (error instanceof ZodError) {
                const errors = error.issues.map((err: ZodIssue) => ({
                    field: err.path.join('.'),
                    message: err.message,
                }));
                return res.status(400).json({
                    error: 'Validation error',
                    details: errors,
                });
            }
            next(error);
        }
    };
};
