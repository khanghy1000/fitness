import { Request, Response, NextFunction } from 'express';
import { ZodSchema, ZodError, ZodIssue } from 'zod';

export const validateBody = (schema: ZodSchema) => {
    return (req: Request, res: Response, next: NextFunction) => {
        try {
            schema.parse(req.body);
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
            schema.parse(req.params);
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
            schema.parse(req.query);
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
