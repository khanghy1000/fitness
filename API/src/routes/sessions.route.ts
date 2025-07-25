import { Router } from 'express';
import { requireAuthenticated } from '@middlewares/auth.middleware.ts';
import { ExerciseService } from '@services/exercise.service.ts';

const router = Router();

// // Get exercise results for a specific date
// router.get('/date/:date', requireAuthenticated, async (req, res) => {
//     const { date } = req.params;

//     const results = await ExerciseService.getExerciseResultsForDate(
//         req.session!.user.id,
//         new Date(date)
//     );

//     res.json(results);
// });

export default router;
