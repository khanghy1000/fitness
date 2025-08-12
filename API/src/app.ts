import express, { ErrorRequestHandler } from 'express';
import cors from 'cors';
import logger from 'morgan';
import cookieParser from 'cookie-parser';
import bodyHandler from 'body-parser';
import { createServer } from 'http';
import { Server } from 'socket.io';
import 'dotenv/config';
import { routes } from './routes/index.ts';
import { toNodeHandler } from 'better-auth/node';
import { auth } from '@lib/auth.ts';
import { attachUserSession } from '@middlewares/auth.middleware.ts';
import { initializeSocketIO } from './socket/index.ts';
import { apiReference } from '@scalar/express-api-reference';
import { extendZodWithOpenApi } from '@asteasolutions/zod-to-openapi';
import { z } from 'zod';
import { generateApiDocs } from 'utils/generate-openapi.ts';

extendZodWithOpenApi(z);
generateApiDocs();

const { urlencoded, json } = bodyHandler;

const app = express();
const server = createServer(app);
const io = new Server(server, {
    cors: {
        origin: '*',
        methods: ['GET', 'POST'],
    },
});

// Initialize Socket.IO handlers
initializeSocketIO(io);

app.use(logger('dev'));
app.use(cors());
app.use(urlencoded({ extended: false }));

app.use(
    '/scalar',
    apiReference({
        sources: [
            { url: '/openapi.json', title: 'Api' },
            { url: '/api/auth/open-api/generate-schema', title: 'Auth' },
        ],
    })
);

// BetterAuth middleware
app.all('/api/auth/{*any}', toNodeHandler(auth));
app.use(attachUserSession);

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
    console.error(err.stack || err.message);
    res.json({
        status: res.statusCode,
        message: err.message,
    });
};

app.use(errorHandler);

server.listen(3000, () => {
    console.log('Server is running on http://localhost:3000');
    console.log('Socket.IO server is ready for real-time messaging');
});