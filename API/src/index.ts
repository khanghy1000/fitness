import express, { ErrorRequestHandler } from 'express';
import cors from 'cors';
import logger from 'morgan';
import cookieParser from 'cookie-parser';
import bodyHandler from 'body-parser';
import 'dotenv/config';
import { routes } from './routes/index.ts';
import { toNodeHandler } from 'better-auth/node';
import { auth } from '@lib/auth.ts';

const { urlencoded, json } = bodyHandler;

const app = express();

app.use(logger('dev'));
app.use(cors());
app.use(urlencoded({ extended: false }));

// BetterAuth middleware
app.all('/api/auth/{*any}', toNodeHandler(auth));

app.use(json());
app.use(cookieParser());
app.use(express.static('./src/static'));

app.use('/api', routes);

// catch 404 and forward to error handler
app.use(function (req, res, next) {
    const err = new Error('Not Found') as any;
    err.status = 404;
    next(err);
});

// error handler
const errorHandler: ErrorRequestHandler = (err, req, res, next) => {
    res.status(err.status || 500);
    res.json({
        status: res.statusCode,
        message: err.message,
    });
};

app.use(errorHandler);

app.listen(3000, () => {
    console.log('Server is running on http://localhost:3000');
});
