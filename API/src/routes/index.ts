import { Router } from 'express';
import pingRouter from './ping.route.ts';

const routes = Router();

routes.use('/ping', pingRouter);

export { routes };
