import { Router } from 'express';
import pingRouter from './ping.route.ts';
import exercisesRouter from './exercises.route.ts';
import nutritionRouter from './nutrition.route.ts';
import workoutsRouter from './workouts.route.ts';
import usersRouter from './users.route.ts';
import sessionsRouter from './sessions.route.ts';
import connectionsRouter from './connections.route.ts';
// import plannedWorkoutsRouter from './planned-workouts.route.ts';

const routes = Router();

routes.use('/ping', pingRouter);
routes.use('/exercises', exercisesRouter);
routes.use('/nutrition', nutritionRouter);
routes.use('/workouts', workoutsRouter);
routes.use('/users', usersRouter);
routes.use('/sessions', sessionsRouter);
routes.use('/connections', connectionsRouter);
// routes.use('/planned-workouts', plannedWorkoutsRouter);

export { routes };
