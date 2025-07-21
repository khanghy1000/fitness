import { Router } from 'express';
import pingRouter from './ping.route.ts';
import testRouter from './test.route.ts';

const routes = Router();

routes.use('/ping', pingRouter);
routes.use('/test', testRouter);

export { routes };
